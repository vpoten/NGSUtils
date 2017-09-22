/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths.weka

import org.ngsutils.maths.KernelUtils
import weka.core.DistanceFunction
import weka.core.Instances
import weka.core.matrix.Matrix


/**
 *
 * @author victor
 */
class KernelFactory {
    
    /**
     * 
     */ 
    static Matrix calcDistMatrix(Instances data, DistanceFunction distFunc){
        int n = data.numInstances()
        Matrix mat = new Matrix(n,n)
        
        for(int i=0; i<n; i++) {
            for(int j=i; j<n; j++) {
                double val = distFunc.distance(data.instance(i),data.instance(j))
                mat.set(i,j,val)
                mat.set(j,i,val)
            }
        }
        
        return mat
    }
    
    /**
     *
     */
    static Matrix linear(Instances data, DistanceFunction distFunc) {
        Matrix mat = calcDistMatrix(data, distFunc)
        
        Matrix K = KernelUtils.intoMercerKernel(mat)
        return K
    }
    
    /**
     *
     */
    static Matrix gaussian(Instances data, DistanceFunction distFunc, double stdev) {
        Matrix mat = calcDistMatrix(data, distFunc)
        
        Matrix K = KernelUtils.intoGaussianKernel(mat, stdev)
        return K
    }
	
}

