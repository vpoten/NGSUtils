/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.stats.BiNGO

/**
 * ************************************************************
 * HypergeometricTestCalculate.java
 * -----------------------------------------
 * <p/>
 * Steven Maere & Karel Heymans (c) March 2005
 * <p/>
 * Class that calculates the hypergeometric test results for a given cluster
 * *************************************************************
 */

public class HypergeometricTestCalculate implements CalculateTestTask {

    /*--------------------------------------------------------------
    FIELDS.
    --------------------------------------------------------------*/

    /**
     * hashmap with as values the values of small n ; keys = GO labels.
     */
    Map mapSmallN;
    /**
     * hashmap with as values the values of small x ; keys = GO labels.
     */
    Map mapSmallX;
    /**
     * hashmap containing values for big N.
     */
    Map mapBigN;
    /**
     * hashmap containing values for big X.
     */
    Map mapBigX;
    /**
     * hashmap with the hypergeometric distribution results as values ; keys = GO labels
     */
    private Map hypergeometricTestMap;
    
    private int maxValue


    /*--------------------------------------------------------------
    CONSTRUCTOR.
    --------------------------------------------------------------*/

    /**
     * constructor with as argument the selected cluster and the
     * annotation, ontology and alpha.
     */
    public HypergeometricTestCalculate(DistributionCount dc) {
        dc.calculate();
        this.mapSmallN = dc.getMapSmallN();
        this.mapSmallX = dc.getMapSmallX();
        this.mapBigN = dc.getMapBigN();
        this.mapBigX = dc.getMapBigX();
        this.maxValue = mapSmallX.size();

    }


    /*--------------------------------------------------------------
      METHODS.
    --------------------------------------------------------------*/

    /**
     * method that redirects the calculation of hypergeometric distribution.
     */
    public void calculate() {

        hypergeometricTestMap = new HashMap();

        int currentProgress = 0;
        
        mapSmallX.keySet().each { id->
            def smallXvalue = mapSmallX.get(id)
            def smallNvalue = mapSmallN.get(id)
            def bigXvalue = mapBigX.get(id)
            def bigNvalue = mapBigN.get(id)
            
            def hd = new HypergeometricDistribution(smallXvalue,
                    bigXvalue, smallNvalue, bigNvalue);
            hypergeometricTestMap.put(id, hd.calculateHypergDistr());

            // Calculate Percentage.  This must be a value between 0..100.
            int percentComplete = (int) (((double) currentProgress / maxValue) * 100);

            //  Estimate Time Remaining
            long timeRemaining = maxValue - currentProgress;
            currentProgress++;

        }
        
    }

    
    public Map getTestMap() {
        return hypergeometricTestMap;
    }
    
	
}

