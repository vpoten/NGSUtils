/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.eqtl


import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.ngsutils.stats.BiNGO.BenjaminiHochbergFDR
import org.ngsutils.Utils

/**
 *
 */
class CorrelationRun implements Runnable {
    CorrelationCalc correlation
    def snps //SNPs ids to correlate in this thread
    def sparseSet = null
    
    /**
     *
     */
    public void run() {
        if( sparseSet!=null ){
            runSparse()
        }
        else{
            runStandard()
        }
    }
    
    /**
     *
     */
    public void runStandard() {
        double[] xArray = new double [correlation.subjects.size()]
        double[] yArray = new double [correlation.subjects.size()]
        SpearmanCalc spearman = new SpearmanCalc(xArray.length)
        
        for( String snpId : snps ) {
            int snpIdx = correlation.snpIdxMap[snpId]
            
            for( String isoformId : correlation.isoforms ) {
                
                int i = 0 
                for( String subj : correlation.subjects ) {
                    if( (correlation.genotypes[subj][snpId]!=null) && (correlation.expression[subj][isoformId]!=null) ) {
                        xArray[i] = correlation.genotypes[subj][snpId]
                        yArray[i] = correlation.expression[subj][isoformId]
                        i++
                    }
                }
                
                int numObs = i
                
                try{
                    // compute Spearman correlation
                    spearman.correlation(xArray, yArray, numObs)
                    // store results
                    correlation.storeValues( snpIdx, correlation.isoformIdxMap[isoformId], spearman.correlation, spearman.PValue, numObs)
                } catch(ex){
                    System.err.println(ex.message)
                }
            }
            
            correlation.reportDoneJobs(correlation.isoforms.size() as Long)
        }
        
    }
    
    /**
     *
     */
    public void runSparse() {
        double[] xArray = new double [correlation.subjects.size()]
        double[] yArray = new double [correlation.subjects.size()]
        SpearmanCalc spearman = new SpearmanCalc(xArray.length)
        def results = [:] as TreeMap
        
        for( String key : sparseSet) {
            def snpId = CorrelationCalc.getKeySnp(key)
            def isoformId = CorrelationCalc.getKeyIso(key)
                
            int i = 0 
            for( String subj : correlation.subjects ) {
                if( (correlation.genotypes[subj][snpId]!=null) && (correlation.expression[subj][isoformId]!=null) ) {
                    xArray[i] = correlation.genotypes[subj][snpId]
                    yArray[i] = correlation.expression[subj][isoformId]
                    i++
                }
            }

            int numObs = i

            try{
                // compute Spearman correlation
                spearman.correlation(xArray, yArray, numObs)
                // store results
                results[key] = new SparseResult(correlation:spearman.correlation, pval:spearman.PValue, numObs:numObs)
            } catch(ex){
                System.err.println(ex.message)
            }
            
        }
        
        correlation.reportDoneJobs(sparseSet.size() as Long)
        correlation.storeSparseValues(results)
    }
    
}

/**
 * Spearman correlation calculator
 * 
 * @author victor
 */
class CorrelationCalc {
    def subjects //subjects ids
    def snps //SNPs ids
    def isoforms //Isoforms ids
    Map genotypes = [:] as TreeMap //map with key=subject value=map of pairs {SNP_id, value}
    Map expression = [:] as TreeMap //map with key=subject value=map of pairs {Isoform_id, value}
    
    //result values: matrix[snpIdx][isoIdx]
    double[][] corrValues = null //correlation
    double[][] pValues = null
    Double[][] corrPValues = null//corrected p-values
    int[][] numObs = null //number of observations used in calculation
    
    int threads = 2
    double significance = 0.05
    
    private long totalJobs = 0;
    private long doneJobs = 0;
    private double reported = 0.0;
    
    def snpIdxMap = [:] as TreeMap
    def isoformIdxMap = [:] as TreeMap
    
    def sparseSet = null //sparse set of correlations to calc
    Map<String,SparseResult> sparseResults = null
    
    /**
     *
     */
    def calcCorrelations() {
        buildMatrices()
        
        //create runnables
        def list = (1..threads).collect{ new CorrelationRun(correlation:this, snps:[]) }
        
        //add snps to correlate to each runnable
        snps.eachWithIndex{ snpId, i-> list[i%threads].snps << snpId }
        totalJobs = (isoforms.size() as Long)*(snps.size() as Long)
        
        Utils.runClosures(list, threads)
        
        //perform FDR correction
        correction()
    }
    
    /**
     * builds key (snp,isoform) for sparse correlation calculation
     */
    static String buildKey(snpId, isoId){
        "${snpId}_${isoId}"
    }
    
    static String getKeySnp(String key){
        key.substring(0, key.indexOf('_'))
    }
    
    static String getKeyIso(String key){
        key.substring(key.indexOf('_')+1)
    }
    
    /**
     *
     */
    def calcSparse(pairsSet) {
        sparseSet = pairsSet
        sparseResults = [:] as TreeMap
        
        //create runnables
        def list = (1..threads).collect{ new CorrelationRun(correlation:this, sparseSet:[]) }
        sparseSet.eachWithIndex{ key, i-> list[i%threads].sparseSet << key }
        totalJobs = sparseSet.size() as Long
        doneJobs = 0
        reported = 0.0
        
        Utils.runClosures(list, threads)
        
        correctionSparse()//perform FDR correction
    }
    
    /**
     *
     */
    protected buildMatrices(){
        corrValues = new double [snps.size()][isoforms.size()]
        pValues = new double [snps.size()][isoforms.size()]
        corrPValues = new Double [snps.size()][isoforms.size()]
        numObs = new int [snps.size()][isoforms.size()]
        
        snps.eachWithIndex{ id, i-> snpIdxMap[id] = i }
        isoforms.eachWithIndex{ id, i-> isoformIdxMap[id] = i }
    }
    
    /**
     *
     */
    protected storeValues(int snpIdx, int isoIdx, double corr, double pval, int nobs) {
        corrValues[snpIdx][isoIdx] = corr
        pValues[snpIdx][isoIdx] = pval
        numObs[snpIdx][isoIdx] = nobs
    }
    
    /**
     *
     */
    synchronized protected storeSparseValues(results) {
        sparseResults.putAll(results)
    }
    
    /**
     *
     */
    public double getCorrValues(snp, iso) {
        return corrValues[snpIdxMap[snp]][isoformIdxMap[iso]]
    }
    
    /**
     *
     */
    public int getNumObs(snp, iso) {
        return numObs[snpIdxMap[snp]][isoformIdxMap[iso]]
    }
	
    /**
     *
     */
    public Double getCorrPValues(snp, iso) {
        return corrPValues[snpIdxMap[snp]][isoformIdxMap[iso]]
    }
    
    /**
     *
     */
    synchronized protected reportDoneJobs(long count) {
        doneJobs += count;
        double rate = ((double) doneJobs / (double) totalJobs);

        if( rate>=(reported+0.1) ){
            println "CorrelationCalc: ${rate*100}% completed at ${new Date()}"
            reported = rate
        }
    }
    
    /**
     * performs Spearman correlation of a pair SNP - Isoform
     */
    def correlation( snpId, isoformId) {
        if(corrValues==null) {
            buildMatrices()
        }
        
        def subset = subjects.findAll{ (genotypes[it][snpId]!=null)&&(expression[it][isoformId]!=null) }
        def array = new double [subset.size()][2]
        
        subset.eachWithIndex{ subj, i->
            array[i][0] = genotypes[subj][snpId]
            array[i][1] = expression[subj][isoformId]
        }
        
        try{
            // compute Spearman correlation
            def result = SpearmanCalc.perform(new Array2DRowRealMatrix(array))
            // store results
            storeValues( snpIdxMap[snpId], isoformIdxMap[isoformId], result[0], result[1], subset.size() )
        } catch(ex){
            System.err.println(ex.message)
        }
    }
    
    
    /**
     * performs FDR correction of p-values
     */
    protected def correction() {
        def pValuesMap = [:]
        def buildLabel = { a, b-> Utils.hash("${a}:${b}") }
        
        (0..snps.size()-1).each{ i->
            (0..isoforms.size()-1).each{ j->
                pValuesMap[buildLabel(i,j)] = pValues[i][j]
            }
        }
        
        def correction = new BenjaminiHochbergFDR(pValuesMap, significance )
        correction.calculate()
        def correctionMap = correction.correctionMap
        
        (0..snps.size()-1).each{ i->
            (0..isoforms.size()-1).each{ j->
                corrPValues[i][j] = correctionMap[buildLabel(i,j)]
            }
        }
    }
    
    /**
     * performs FDR correction of p-values
     */
    protected def correctionSparse() {
        def pValuesMap = [:]
        def buildLabel = { k-> Utils.hash(k) }
        
        sparseResults.each{ key, val->
            pValuesMap[buildLabel(key)] = val.pval
        }
        
        
        def correction = new BenjaminiHochbergFDR(pValuesMap, significance )
        correction.calculate()
        def correctionMap = correction.correctionMap
        
        sparseResults.each{ key, val->
            val.corrPval = correctionMap[buildLabel(key)]
        }
    }
    
    
    /**
     *
     */
    def printResults(writer) {
        //print header
        writer.println("snp\tisoform\tcorrelation\tpvalue\tlog_pvalue\tcorr_pvalue\tlog_corr_pvalue\tnum_obs")
        
        def log10 = { val-> (val==null) ? null : -Math.log10(val) }
        
        if( sparseResults ){
            sparseResults.each{ key, val->
                def snpId = CorrelationCalc.getKeySnp(key)
                def isoformId = CorrelationCalc.getKeyIso(key)
                writer.print("${snpId}\t${isoformId}\t${val.correlation}\t")
                writer.print("${val.pval}\t${log10(val.pval)}\t")
                writer.print("${val.corrPval}\t${log10(val.corrPval)}\t")
                writer.println("${val.numObs}")
            }
        }
        else{
            snps.eachWithIndex{ snpId, i->
                isoforms.eachWithIndex{ isoformId, j->
                    writer.print("${snpId}\t${isoformId}\t${corrValues[i][j]}\t")
                    writer.print("${pValues[i][j]}\t${log10(pValues[i][j])}\t")
                    writer.print("${corrPValues[i][j]}\t${log10(corrPValues[i][j])}\t")
                    writer.println("${numObs[i][j]}")
                }
            }
        }
        
        writer.close()
    }
    
}

