/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.stats

/**
 * based on (PAL) pal.statistic.FisherExact
 * 
 * @author victor
 */
class FisherExact {
    private double[] f;
    int maxSize;


   /**
    * constructor for FisherExact table
    *
    * @param maxSize is the maximum sum that will be encountered by the table (a+b+c+d)
    */
    public FisherExact(int maxSize) {
        this.maxSize=maxSize;
        double cf=1.0;
        f=new double[maxSize+1];
        f[0]=0.0;
        for(int i=1; i<=this.maxSize; i++) {
            f[i]=f[i-1]+Math.log(i);
        }
    }

   /**
    * calculates the P-value for this specific state
    *
    * @param a,b,c,d are the four cells in a 2x2 matrix
    * @return the P-value
    */
    public final double getP(int a, int b, int c, int d) {
        int n=a+b+c+d;
        if(n>maxSize) {
            return Double.NaN;
        }
        double p;
        p=(f[a+b]+f[c+d]+f[a+c]+f[b+d])-(f[a]+f[b]+f[c]+f[d]+f[n]);
        return Math.exp(p);
    }

   /**
    * calculates the one tail P-value for the Fisher Exact test
    * This
    *
    * @param a,b,c,d are the four cells in a 2x2 matrix
    * @return the P-value
    */
    public final double getCumlativeP(int a, int b, int c, int d) {
        int min,i;
        int n=a+b+c+d;
        if(n>maxSize) {
            return Double.NaN;
        }
        double p=0;
        p+=getP(a, b, c, d);
        if((a*d)>=(b*c)) {
            min=(c<b)?c:b;
            for(i=0; i<min; i++) {
                p+=getP(++a, --b, --c, ++d);
            }
        }
        else {
            min=(a<d)?a:d;
            for(i=0; i<min; i++) {
                p+=getP(--a, ++b, ++c, --d);
            }
        }
        return p;
    }
    
}

