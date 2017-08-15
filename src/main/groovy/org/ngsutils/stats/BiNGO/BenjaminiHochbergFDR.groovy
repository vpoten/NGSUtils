/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.stats.BiNGO

/**
 * ************************************************************************
 * BenjaminiHochbergFDR.java:  Steven Maere & Karel Heymans (c) March 2005
 * ------------------------
 * <p/>
 * Class implementing the Benjamini and Hochberg FDR correction algorithm.
 * <p/>
 * ************************************************************************
 */


public class BenjaminiHochbergFDR implements CalculateCorrectionTask {

    /*--------------------------------------------------------------
    FIELDS.
    --------------------------------------------------------------*/

    private Object [] hash
    
    double [] adjustedPvalues
    protected Map correctionMap = null
    protected String [] ordenedGOLabels = null
    protected double[] ordenedPvalues = null

    /**
     * the significance level.
     */
    private double alpha
    
    /**
     * the number of tests.
     */
    private int m


    /*--------------------------------------------------------------
    CONSTRUCTOR.
    --------------------------------------------------------------*/

    /**
     * Constructor.
     *
     * @param golabelstopvalues Hashmap of Strings with the labels and their pvalues.
     * @param alpha             double with the desired significance level.
     */

    public BenjaminiHochbergFDR(HashMap golabelstopvalues, double alpha) {
        //Get all the go labels and their corresponding pvalues from the map
        def hash = new Object [golabelstopvalues.size()]
        golabelstopvalues.eachWithIndex { entry, i-> hash[i] = entry }
        
        this.hash = hash
        this.alpha = alpha
        this.m = golabelstopvalues.size()
        this.adjustedPvalues = new double [this.m]
    }

    
    /*--------------------------------------------------------------
    METHODS.
    --------------------------------------------------------------*/

    /**
     * method that calculates the Benjamini and Hochberg correction of
     * the false discovery rate
     * NOTE : convert array indexes [0..m-1] to ranks [1..m].
     * orden raw p-values low .. high
     * test p<(i/m)*alpha from high to low (for i=m..1)
     * i* (istar) first i such that the inequality is correct.
     * reject hypothesis for i=1..i* : labels 1..i* are overrepresented
     * <p/>
     * adjusted p-value for i-th ranked p-value p_i^adj = min(k=i..m)[min(1,m/k p_k)]
     */
    public void calculate() {
        // ordening the pvalues.
        Arrays.sort(hash, [compare:{a,b-> a.value<=>b.value}] as Comparator)
        
        // calculating adjusted p-values.
        double min = 1.0
        
        for (int i = m; i > 0; i--) {
            double mkprk = (m * hash[i - 1].value)/(double)i
            if (mkprk < min) {
                min = mkprk;
            }
            adjustedPvalues[i - 1] = min
        }
    }

    /**
     * creates and return the ordenedPvalues
     */
    public double[] getOrdenedPvalues(){
        if( ordenedPvalues!=null )
            return ordenedPvalues
            
        ordenedPvalues = new double [hash.length]
        for (int i = 0; i < hash.length; i++) {
            ordenedPvalues[i] = hash[i].value
        }
        
        return ordenedPvalues
    }

    /**
     * creates and return the ordenedGOLabels
     */
    public String[] getOrdenedGOLabels(){
        if( ordenedGOLabels!=null )
            return ordenedGOLabels
            
        ordenedGOLabels = new String [hash.length]
        for (int i = 0; i < hash.length; i++) {
            ordenedGOLabels[i] = hash[i].key
        }
        
        return ordenedGOLabels
    }
    
    /**
     * creates and return the correctionMap
     */
    public Map getCorrectionMap(){
        if( correctionMap!=null )
            return correctionMap
            
        correctionMap = [:] as TreeMap
        for (int i = 0; i < hash.length; i++) {
            correctionMap[hash[i].key] = adjustedPvalues[i]
        }
        
        return correctionMap
    }
    
    /**
     *
     */
    public Double getAdjustedPValue(label){
        for (int i = 0; i < hash.length; i++) {
            if( hash[i].key==label )
                return adjustedPvalues[i]
        }
        
        return null;
    }
    
}

