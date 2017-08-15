/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths.weka.clusterer

import weka.core.matrix.Matrix
import org.ngsutils.maths.KernelUtils
import weka.core.Utils as WekaUtils

/**
 *
 * @author victor
 */
abstract class AbstractKernelFuzzyClusterer extends AbstractFuzzyClusterer {
	
    protected Matrix distances
    protected Matrix K = null //kernel matrix
    protected Matrix Z = null  //auxiliary matrix
    protected Matrix Z2 = null  //auxiliary matrix
    protected double [] rowSumsInv = null //auxiliary vector
    protected int kernelType = 0 // 0=linear, 1=gaussian
    protected double gausStdev = 1.0 // gaussian kernel stdev
    
    /**
     * 
     */
    public void setLinearKernel() {
        K = KernelUtils.intoMercerKernel(distances)
    }
    
    /**
     * 
     */
    public void setGaussianKernel(double stdev) {
        K = KernelUtils.intoGaussianKernel(distances, stdev)
    }
   
    /**
     * 
     */ 
    protected void setup() {
        if( distances==null ){
            throw new Exception("No distances matrix has been set");
        }
        
        if( K==null ){
            switch(kernelType){
                case 0: setLinearKernel(); break;
                case 1: setGaussianKernel(gausStdev); break;
            }
        }
        
        int numInstances = instances.size()
        U = new Matrix(numberOfClusters(), numInstances)
        Uprior = new Matrix(numberOfClusters(), numInstances)
        Z = new Matrix(numberOfClusters(), numInstances)// aux matrix
        Z2 = new Matrix(numberOfClusters(), numberOfClusters())// aux matrix
        rowSumsInv = new double [numberOfClusters()]
    }
    
    /**
     * 
     */ 
    protected void updateInternals() {
        //update Z matrix: Z = U*K
        product(Uprior, K, Z)
        //update Z2 matrix: Z2 = U*K*t(U)
        productByTrans(Z, Uprior, Z2)
        //update rowSumsInv
        (0..numberOfClusters()-1).each{ rowSumsInv[it] = 1.0d/rowSum(Uprior,it) }
    }
    
    /**
     * @param h : pattern index
     * @param i : cluster index
     */
    protected double sqDistToCentroid(int h, int i) {
        double ai = rowSumsInv[i]
        return (K.get(h,h) - 2.0d*ai*Z.get(i,h) + ai*ai*Z2.get(i,i))
    }
    
    /**
     * Clusterer options:
     * -K <integer> = type of kernel to use 0=linear, 1=gaussian (default 0)
     * -stdev <double> = gaussian kernel stdev (default 1)
     */
    public void setOptions (String[] options) {
        def opt = WekaUtils.getOption('K',options)
        if( opt ){ this.kernelType = opt as Integer }
        
        opt = WekaUtils.getOption('stdev',options)
        if( opt ){ this.gausStdev = opt as Double }
        
        super.setOptions(options)
    }
    
}
