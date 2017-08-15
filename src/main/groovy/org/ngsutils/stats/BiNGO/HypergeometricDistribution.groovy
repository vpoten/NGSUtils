/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.stats.BiNGO

import cern.jet.stat.tdouble.Gamma

/**
 * *****************************************************************
 * HypergeometricDistribution.java    Steven Maere & Karel Heymans (c) March 2005
 * -------------------------------
 * <p/>
 * Class that calculates the Hypergeometric probability P(x or more |X,N,n) for given x, X, n, N.
 * ******************************************************************
 */

public class HypergeometricDistribution {

    /*--------------------------------------------------------------
    FIELDS.
    --------------------------------------------------------------*/

    // x out of X genes in cluster A belong to GO category B which
    // is shared by n out of N genes in the reference set.
    /**
     * number of successes in sample.
     */
    private static int x;
    /**
     * sample size.
     */
    private static int bigX;
    /**
     * number of successes in population.
     */
    private static int n;
    /**
     * population size.
     */
    private static int bigN;
    
	

    /*--------------------------------------------------------------
    CONSTRUCTOR.
    --------------------------------------------------------------*/

    /**
     * constructor with as arguments strings containing numbers.
     *
     * @param x    number of genes with GO category B in cluster A.
     * @param bigX number of genes in cluster A.
     * @param n    number of genes with GO category B in the whole genome.
     * @param bigN number of genes in whole genome.
     */

    public HypergeometricDistribution(int x, int bigX, int n, int bigN) {
        this.x = x;
        this.bigX = bigX;
        this.n = n;
        this.bigN = bigN;	
    }

    /*--------------------------------------------------------------
    METHODS.
    --------------------------------------------------------------*/

    /**
     * method that conducts the calculations.
     * P(x or more |X,N,n) = 1 - sum{[C(n,i)*C(N-n, X-i)] / C(N,X)}
     * for i=0 ... x-1
     *
     * @return String with result of calculations.
     */
    public double calculateHypergDistr() {
        if(bigN >= 2){
            double sum = 0;
            //mode of distribution, integer division (returns integer <= double result)!
            int mode = (bigX+1)*(n+1)/(bigN+2) ;
            if(x >= mode){
                int i = x ;
                while ((bigN - n >= bigX - i) && (i <= Math.min(bigX, n))) {	
                    double pdfi = Math.exp(Gamma.logGamma(n+1)-Gamma.logGamma(i+1)-Gamma.logGamma(n-i+1) + Gamma.logGamma(bigN-n+1)-Gamma.logGamma(bigX-i+1)-Gamma.logGamma(bigN-n-bigX+i+1)- Gamma.logGamma(bigN+1)+Gamma.logGamma(bigX+1)+Gamma.logGamma(bigN-bigX+1)) ;
                    sum = sum+pdfi;
                    i++;
                }	
            }	
            else{
                int i = x - 1;
                while ((bigN - n >= bigX - i) && (i >= 0)) {
                    double pdfi = Math.exp(Gamma.logGamma(n+1)-Gamma.logGamma(i+1)-Gamma.logGamma(n-i+1) + Gamma.logGamma(bigN-n+1)-Gamma.logGamma(bigX-i+1)-Gamma.logGamma(bigN-n-bigX+i+1)- Gamma.logGamma(bigN+1)+Gamma.logGamma(bigX+1)+Gamma.logGamma(bigN-bigX+1)) ;
                    sum = sum+pdfi;
                    i--;
                }	
                sum = 1-sum;
            }
            return sum
        }
        else{
            return 1.0
        }
    }
    
}

