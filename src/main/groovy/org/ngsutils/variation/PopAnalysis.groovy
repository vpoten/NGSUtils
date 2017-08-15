/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.variation

import org.ngsutils.variation.PairValueVector
import org.ngsutils.Utils


/**
 * Population structure and Eigenanalysis procedures.
 * 
 * @author victor
 */
class PopAnalysis {
    
    static final double MISSING = Double.NaN
    
    List subjects
    int numSnps
    PopMatrix C //genotypes matrix, rows indexed by individuals, and columns indexed by markers
    PopMatrix M //normalized matrix, rows indexed by individuals, and columns indexed by markers
    PopMatrix X //covarianze matrix
    List<PairValueVector> axesVariation
    int numAxes = 10 //num axes of variation used to adjust
    int numTreads = 2
    
    /**
     * 
     */ 
    public PopAnalysis() {
        
    }
    
    /**
     * 
     */
    public PopAnalysis(List subjects, int nsnps, int nthreads) {
        this.subjects = subjects
        this.numSnps = nsnps
        this.numTreads = nthreads
        this.C = new PopMatrix(subjects.size(), numSnps)
        this.M = new PopMatrix(subjects.size(), numSnps)
    }
    
    /**
     * copy constructor
     */ 
    public PopAnalysis( PopAnalysis orig ) {
        this.subjects = orig.subjects
        this.numSnps = orig.numSnps
        this.numTreads = orig.numTreads
        this.C = new PopMatrix(orig.C)
        this.M = new PopMatrix(subjects.size(), numSnps)
    }
    
    /**
     *
     */
    public static PopAnalysis eigenAdjustment(tpedFile, int nthreads) {
        PopAnalysis instance = new PopAnalysis()
        instance.numTreads = nthreads
        instance.parseTped(tpedFile)
        instance.eigenAdjustment()
        return instance
    }
    
    /**
     * 
     */ 
    public eigenAdjustment() {
        normalize()
        covarianze()
        eigenDecomp()
        adjustGenotypes()
    }
    
    /**
     * for testing purposes
     */
    public static PopAnalysis eigenWithoutAdjustment(PopAnalysis orig) {
        PopAnalysis instance = new PopAnalysis(orig)
        instance.M.copy(orig.C) //genotypes matrix is already normalized
        instance.covarianze()
        instance.eigenDecomp()
        return instance
    }
    
    /**
     * parses a Tped file and fills matrix C of genotypes, coded as {0,1,2}
     */
    protected def parseTped(file) {
        file = new File(file)
        
        // read subject id from .tfam file
        subjects = SNPData.subjectList(file)
        numSnps = Utils.countLines(file.absolutePath)
        
        this.C = new PopMatrix(subjects.size(), numSnps)
        this.M = new PopMatrix(subjects.size(), numSnps)
     
        def reader = Utils.createReader(file)
        int count = 0
        
        reader.eachLine{ line->
            //chr snpId morgans position genotypes 
            def toks = line.split("\\s",5)
            SNPData snp = new SNPData(id:toks[1], chr:toks[0], 
                position:toks[3] as Integer, genotypes:toks[4])
            
            (0..subjects.size()-1).each{ 
                Double val = snp.encode(snp.getSubjectAlleles(it))
                C.set(it, count, ((val==null) ? MISSING : val))
            }
            
            count++
        }
        
        reader.close()
    }
    
    /**
     * 
     */ 
    public PopMatrix getGenotypes() {
       return this.C 
    }
    
    /**
     * 
     */ 
    public double getGenotype(int subj, int snp) {
       return this.C.get(subj, snp)
    }
    
    /**
     * 
     */ 
    public void setGenotype(int subj, int snp, Double val) {
       this.C.set(subj, snp, (val==null) ? MISSING : val)
    }
    
    /**
     *
     */
    protected boolean isMissing(double val){
        return Double.isNaN(val)
    }
	
    /**
     * M = normalize(C)
     */
    protected def normalize() {
        (0..numSnps-1).each{ snp->
            double samples = 0.0
            double sum = 0.0
            
            (0..C.nrow-1).each{ 
                double val = C.get(it,snp)
                if( !isMissing(val) ) {
                    samples += 1.0
                    sum += val
                }
            }
            
            double mean = (samples==0.0) ? 0.0 : sum/samples
            double prob = (1+sum)/(2+2*samples)
            double fact = 1.0/Math.sqrt(prob*(1.0-prob))
            
            (0..C.nrow-1).each{
                double val = C.get(it,snp)
                if( !isMissing(val) ) {
                    M.set(it, snp, (val-mean)*fact)
                }
                else {
                    M.set(it, snp, 0.0)
                }
            }
        }
    }
    
    /**
     * 
     */
    protected def meanAdjust(PopMatrix mat, int snp) {
        double samples = 0.0
        double sum = 0.0

        (0..mat.nrow-1).each{ 
            double val = mat.get(it,snp)
            if( !isMissing(val) ) {
                samples += 1.0
                sum += val
            }
        }

        double mean = (samples==0.0) ? 0.0 : sum/samples

        (0..mat.nrow-1).each{
            double val = mat.get(it,snp)
            if( !isMissing(val) ) {
                mat.set(it, snp, val-mean)
            }
        }
    }
    
    /**
     * X = covarianze(M) = (1/n)*(M*t(M))
     */
    protected def covarianze() {
        X = M.productTransThreads(numTreads)
        X.product( 1.0/((double)numSnps) )
    }
    
    /**
     *
     */
    protected def eigenDecomp(){
        axesVariation = X.eigenDecomposition()
    }
    
    /**
     *
     */
    protected def eigenvectors() {
        axesVariation.collect{ it.vector }
    }
    
    /**
     * 
     * @return the percentage of explained variance associated to a PCA component
     */
    double explainedVar(int axisIdx) {
        double total = axesVariation.sum{ it.value }
        return ((axesVariation[axisIdx].value)/total)
    }
    
    /**
     *
     */
    protected double ancestry(double [] vector, int subj) {
        vector[subj]
    }
    
    /**
     * Adjustment of genotypes using axes of variation
     */
    protected def adjustGenotypes() {
        def vectors = eigenvectors()[(0..numAxes-1)]
        
        // calc. square sum of ancestors for each axes of variation
        def denom = vectors.collect{ vector->
            double sqSumAnc = (0..subjects.size()-1).sum{ 
                double a = ancestry(vector,it)
                return a*a 
            }
            
            return 1.0/sqSumAnc
        }
            
        (0..numSnps-1).each{ snp->//for each SNP
            
            meanAdjust(C, snp)
            
            // for each axes of variation
            vectors.eachWithIndex{ vector, k->
                
                // regression coefficient for ancestry predicting genotype across 
                // individuals j with valid genotypes at SNP i
                double gamma = (0..subjects.size()-1).sum{
                    double val = C.get(it,snp)
                    (!isMissing(val)) ? ancestry(vector,it)*val : 0.0
                }
                
                gamma *= denom[k]
                
                (0..subjects.size()-1).each{
                    double val = C.get(it,snp)
                    if( !isMissing(val) ) {
                        C.set(it, snp, val-gamma*ancestry(vector,it))
                    }
                }
            }
        }
        
    }

    /**
     *
     */
    def writeEigen(file) {
        def writer = new File(file).newWriter()
        
        axesVariation.each{ pair->
            writer.println(pair.value) //eigenvalue
            pair.vector.each{ writer.print(it+' ') } //eigenvector
            writer.println()
        }
        
        writer.close()
    }
      
     
    
}

