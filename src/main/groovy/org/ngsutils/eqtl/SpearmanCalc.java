/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.eqtl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.ranking.*;
import org.apache.commons.math3.stat.correlation.*;
import org.apache.commons.math3.linear.*;    
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.util.FastMath;

/**
 * Wraps apache commons Spearmans correlation
 * 
 * @author victor
 */
class SpearmanCalc {
    
    double [] rankX = null;
    double [] rankY = null;
    IntDoublePair[] rankPairs = null;
    int maxObs = 0;
    private Double correlation = null;
    private Double pval = null;
    
    private final NaNStrategy nanStrategy = NaNStrategy.MAXIMAL;
    private final TiesStrategy tiesStrategy = TiesStrategy.AVERAGE;
    private final RandomData randomData;
    
    /**
     * 
     * @param maxObs 
     */
    public SpearmanCalc(int maxObs) {
        this.maxObs = maxObs;
        this.rankX = new double [maxObs];
        this.rankY = new double [maxObs];
        
        this.rankPairs = new IntDoublePair[maxObs];
        for (int i = 0; i < rankPairs.length; i++) {
            rankPairs[i] = new IntDoublePair();
        }
        
        randomData = new RandomDataImpl();
    }
            
    public double getCorrelation(){
        return correlation;
    }
    
    public double getPValue(){
        return pval;
    }
    
    /**
     * 
     * @param xArray
     * @param yArray
     * @param numObs 
     */
    public void correlation(final double[] xArray, final double[] yArray, int numObs) {
        
        if (xArray.length < 2 || numObs < 2 ) {
            throw new MathIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION,
                                                   xArray.length, 2);
        } 
        
        //do rank
        rank(xArray, this.rankX, numObs);
        rank(yArray, this.rankY, numObs);
        
        //compute correlation
        SimpleRegression regression = new SimpleRegression();
        
        for(int i=0; i<numObs; i++) {
            regression.addData(this.rankX[i], this.rankY[i]);
        }

        this.correlation = regression.getR();
        
        //compute p-value
        TDistribution tDistribution = new TDistribution(numObs - 2);
        double r = this.correlation;
        double t = FastMath.abs(r * FastMath.sqrt((numObs - 2)/(1 - r * r)));
        this.pval = 2 * tDistribution.cumulativeProbability(-t);
    }
    
    /**
     * 
     * @param dataMatrix
     * @return a double [] with correlation and p-value
     */
    static double[] perform(RealMatrix dataMatrix){
        // compute Spearman correlation
        RankingAlgorithm rankingAlgorithm = new NaturalRanking(NaNStrategy.REMOVED,TiesStrategy.AVERAGE);
        RealMatrix rankMatrix = rankTransform(dataMatrix, rankingAlgorithm);
        
        PearsonsCorrelation baseCorr = new PearsonsCorrelation(rankMatrix);
        RealMatrix corrMat = baseCorr.getCorrelationMatrix();
        RealMatrix pvalMat = baseCorr.getCorrelationPValues();
        
        return new double [] {corrMat.getEntry(0, 1), pvalMat.getEntry(0, 1)};
    }
    
    /**
     * 
     * @param matrix
     * @param rankingAlgorithm
     * @return 
     */
    private static RealMatrix rankTransform(RealMatrix matrix, RankingAlgorithm rankingAlgorithm) {
        Array2DRowRealMatrix newMatrix = null;
        
        for (int i = 0; i < matrix.getColumnDimension(); i++) {
            double [] column = rankingAlgorithm.rank(matrix.getColumn(i));
            
            if( newMatrix==null ){
                newMatrix = new Array2DRowRealMatrix(column.length, matrix.getColumnDimension());
            }
            
            newMatrix.setColumn(i, column);
        }
        
        return newMatrix;
    }
    
    /**
     * 
     * @param data
     * @param out
     * @param numObs 
     */
    private void rank(final double[] data, double[] out, int numObs){
                    
        for (int i = 0; i < rankPairs.length; i++) {
            rankPairs[i].set(Double.NaN,-1);
        }
        
        // Array recording initial positions of data to be ranked
        for (int i = 0; i < numObs; i++) {
            rankPairs[i].set(data[i], i);
        }

        // Recode, remove or record positions of NaNs
        ///List<Integer> nanPositions = null;
        
        switch (nanStrategy) {
            case MAXIMAL: // Replace NaNs with +INFs
                recodeNaNs(rankPairs, Double.POSITIVE_INFINITY, numObs);
                break;
            case MINIMAL: // Replace NaNs with -INFs
                recodeNaNs(rankPairs, Double.NEGATIVE_INFINITY, numObs);
                break;
            /*
            case REMOVED: // Drop NaNs from data
                rankPairs = removeNaNs(rankPairs);
                break;
            case FIXED:   // Record positions of NaNs
                nanPositions = getNanPositions(rankPairs);
                break;
                */
            default: // this should not happen unless NaNStrategy enum is changed
                throw new MathInternalError();
        }

        // Sort the IntDoublePairs
        Arrays.sort(rankPairs, 0, numObs);

        // Walk the sorted array, filling output array using sorted positions,
        // resolving ties as we go
        
        int pos = 1;  // position in sorted array
        out[rankPairs[0].getPosition()] = pos;
        List<Integer> tiesTrace = new ArrayList<Integer>();
        tiesTrace.add(rankPairs[0].getPosition());
        for (int i = 1; i < numObs; i++) {
            if (Double.compare(rankPairs[i].getValue(), rankPairs[i - 1].getValue()) > 0) {
                // tie sequence has ended (or had length 1)
                pos = i + 1;
                if (tiesTrace.size() > 1) {  // if seq is nontrivial, resolve
                    resolveTie(out, tiesTrace);
                }
                tiesTrace = new ArrayList<Integer>();
                tiesTrace.add(rankPairs[i].getPosition());
            } else {
                // tie sequence continues
                tiesTrace.add(rankPairs[i].getPosition());
            }
            out[rankPairs[i].getPosition()] = pos;
        }
        if (tiesTrace.size() > 1) {  // handle tie sequence at end
            resolveTie(out, tiesTrace);
        }
        /*
        if (nanStrategy == NaNStrategy.FIXED) {
            restoreNaNs(out, nanPositions);
        }
        */
    }
    
    /**
     * Recodes NaN values to the given value.
     *
     * @param ranks array to recode
     * @param value the value to replace NaNs with
     */
    private void recodeNaNs(IntDoublePair[] ranks, double value, int numObs) {
        for (int i = 0; i < numObs; i++) {
            if (Double.isNaN(ranks[i].getValue())) {
                ranks[i].set(value, ranks[i].getPosition());
            }
        }
    }
    
    /**
     * Resolve a sequence of ties, using the configured {@link TiesStrategy}.
     * The input <code>ranks</code> array is expected to take the same value
     * for all indices in <code>tiesTrace</code>.  The common value is recoded
     * according to the tiesStrategy. For example, if ranks = <5,8,2,6,2,7,1,2>,
     * tiesTrace = <2,4,7> and tiesStrategy is MINIMUM, ranks will be unchanged.
     * The same array and trace with tiesStrategy AVERAGE will come out
     * <5,8,3,6,3,7,1,3>.
     *
     * @param ranks array of ranks
     * @param tiesTrace list of indices where <code>ranks</code> is constant
     * -- that is, for any i and j in TiesTrace, <code> ranks[i] == ranks[j]
     * </code>
     */
    private void resolveTie(double[] ranks, List<Integer> tiesTrace) {

        // constant value of ranks over tiesTrace
        final double c = ranks[tiesTrace.get(0)];

        // length of sequence of tied ranks
        final int length = tiesTrace.size();

        switch (tiesStrategy) {
            case  AVERAGE:  // Replace ranks with average
                fill(ranks, tiesTrace, (2 * c + length - 1) / 2d);
                break;
            case MAXIMUM:   // Replace ranks with maximum values
                fill(ranks, tiesTrace, c + length - 1);
                break;
            case MINIMUM:   // Replace ties with minimum
                fill(ranks, tiesTrace, c);
                break;
            case RANDOM:    // Fill with random integral values in [c, c + length - 1]
                Iterator<Integer> iterator = tiesTrace.iterator();
                long f = FastMath.round(c);
                while (iterator.hasNext()) {
                    ranks[iterator.next()] =
                        randomData.nextLong(f, f + length - 1);
                }
                break;
            case SEQUENTIAL:  // Fill sequentially from c to c + length - 1
                // walk and fill
                iterator = tiesTrace.iterator();
                f = FastMath.round(c);
                int i = 0;
                while (iterator.hasNext()) {
                    ranks[iterator.next()] = f + i++;
                }
                break;
            default: // this should not happen unless TiesStrategy enum is changed
                throw new MathInternalError();
        }
    }
    
    /**
     * Sets<code>data[i] = value</code> for each i in <code>tiesTrace.</code>
     *
     * @param data array to modify
     * @param tiesTrace list of index values to set
     * @param value value to set
     */
    private void fill(double[] data, List<Integer> tiesTrace, double value) {
        Iterator<Integer> iterator = tiesTrace.iterator();
        while (iterator.hasNext()) {
            data[iterator.next()] = value;
        }
    }
    
    /**
     * Represents the position of a double value in an ordering.
     * Comparable interface is implemented so Arrays.sort can be used
     * to sort an array of IntDoublePairs by value.  Note that the
     * implicitly defined natural ordering is NOT consistent with equals.
     */
    private static class IntDoublePair implements Comparable<IntDoublePair>  {

        /** Value of the pair */
        private double value;

        /** Original position of the pair */
        private int position;

        /**
         * 
         */
        public IntDoublePair() {
            this.value = Double.NaN;
            this.position = -1;
        }
        
        /**
         * Construct an IntDoublePair with the given value and position.
         * @param value the value of the pair
         * @param position the original position
         */
        public IntDoublePair(double value, int position) {
            this.value = value;
            this.position = position;
        }
        
        /**
         * 
         * @param value
         * @param position 
         */
        public void set(double value, int position) {
            this.value = value;
            this.position = position;
        }

        /**
         * Compare this IntDoublePair to another pair.
         * Only the <strong>values</strong> are compared.
         *
         * @param other the other pair to compare this to
         * @return result of <code>Double.compare(value, other.value)</code>
         */
        public int compareTo(IntDoublePair other) {
            return Double.compare(value, other.value);
        }

        /**
         * Returns the value of the pair.
         * @return value
         */
        public double getValue() {
            return value;
        }

        /**
         * Returns the original position of the pair.
         * @return position
         */
        public int getPosition() {
            return position;
        }
    }
    
}
