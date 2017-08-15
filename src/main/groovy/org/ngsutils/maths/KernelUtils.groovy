/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths

import cern.colt.matrix.tdouble.DoubleMatrix2D
import cern.colt.matrix.tdouble.DoubleFactory2D
import weka.core.matrix.Matrix

/**
 * Kernel conversions for distance matrices.
 * 
 * Reference:
 * Filippone, M. (2009) Dealing with non-metric dissimilarities in fuzzy central 
 * clustering algorithms. International Journal of Accounting, 50 (2). pp. 363-384.
 *
 * @author victor
 */
class KernelUtils {
    
    static double tolerance = 1e-9
    
    
    /**
     * 
     */ 
    static private double fixZero(double val){
        (Math.abs(val) < tolerance) ? 0.0d : val
    }
	
    /**
     * 
     */ 
    static private boolean isSymmetric(mat) {
        if( !mat.isSquare() ){
            return false
        }
        
        int n = mat.getColumnDimension()
        
        for(int i=0; i<n; i++){
            for(int j=i; j<n; j++){
                if( Math.abs(mat.get(i,j)-mat.get(j,i))>tolerance ){
                    return false
                }
            }
        }
        
        return true
    }
    
    /**
     * checks whether matrix is symmetric and positive semidefinite.
     */
    static boolean checkMercerKernel(Matrix matrix) {
        if( !isSymmetric(matrix) ){
            return false
        }
        
        def eigen = matrix.eig()
        double [] values = eigen.getRealEigenvalues()
        
        return values.every{ fixZero(it)>=0.0d }
    }
    
    
    /**
     * centralize matrix p and multiply it by a factor
     */
    private static Matrix centralize(Matrix p, double factor = 1.0) {
        int n = p.getColumnDimension()
        double inv = 1.0d/n
        Matrix q = Matrix.identity(n,n)
        
        // Q = I - (1/n)(ones)
        for(int i=0; i<n; i++){
            for(int j=0; j<n; j++){
                q.set(i,j, q.get(i,j)-inv)
            }
        }
        
        
        // Q*P*Q
        Matrix Sc = q.times(p).times(q)
        
        for(int i=0; i<n; i++){
            for(int j=0; j<n; j++){
                Sc.set(i,j, Sc.get(i,j)*factor)
            }
        }
        
        return Sc
    }
    
    
    /**
     * 
     */
    private static double minEigenvalue(Matrix matrix) {
        def eigen = matrix.eig()
        double [] values = eigen.getRealEigenvalues()
        return (values as List).min()
    }
    
    
    /**
     * 
     */
    private static Matrix diagShift(Matrix matrix) {
        int n = matrix.getColumnDimension()
        
        Matrix shifted = matrix.copy()
        
        double l1 = minEigenvalue(matrix)
        
        for(int i=0; i<n; i++){
            shifted.set(i,i, shifted.get(i,i)-l1)
        }
        
        return shifted
    }
    
    
    /**
     * transforms matrix, converting it into an euclidean distances matrix
     *
     * @param matrix : a squared symmetric matrix
     */
    static Matrix intoEuclidean(Matrix matrix) {
        int n = matrix.getColumnDimension()
        Matrix Sc = centralize(matrix, -0.5d)
        
        // get min eigenvalue of Sc
        double l1 = minEigenvalue(Sc)
        
        Matrix R = matrix.copy()
        
        for(int i=0; i<n; i++){
            for(int j=0; j<n; j++){
                if( i!=j ){
                    R.set(i,j, R.get(i,j)-2.0d*l1)
                }
            }
        }
        
        return R
    }
    
    
    /**
     * transforms matrix, converting it into a Mercer kernel.
     * 
     * @param matrix : a squared symmetric matrix
     */
    static Matrix intoMercerKernel(Matrix matrix) {
        Matrix Sc = centralize(matrix, -0.5)
        return (checkMercerKernel(Sc)) ? Sc : diagShift(Sc)
    }
    
    
    /**
     *
     */
    static DoubleMatrix2D toColtMatrix(Matrix matrix){
        DoubleMatrix2D result = DoubleFactory2D.dense.make(
            matrix.getRowDimension(), matrix.getColumnDimension() )
        
        for(int i=0; i<matrix.getRowDimension(); i++){
            for(int j=0; j<matrix.getColumnDimension(); j++){
                result.setQuick(i,j, matrix.get(i,j))
            }
        }
        
        return result
    }
    
    /**
     * @param matrix : a squared symmetric matrix of distances
     * @param stdev : standard deviation of gaussian function
     */
    static Matrix intoGaussianKernel(Matrix mat, double stdev) {
        int n = mat.getColumnDimension()
        Matrix result = Matrix.identity(n,n)
        double denom = -1.0d/(2.0d*stdev*stdev)
        
        for(int i=0; i<n; i++){
            for(int j=i; j<n; j++){
                if( i!=j ){
                    double dist = mat.get(i,j)
                    double val = Math.exp(denom*dist*dist)
                    result.set(i,j, val )
                    result.set(j,i, val )
                }
            }
        }
        
        return result
    }
    
    /**
     * F1 = 1/( (eps+sqDist)^(1/(m-1)) )
     * 
     * @param matrix : a squared symmetric matrix of distances
     * @param eps : param
     * @param m : exponent
     */
    static Matrix intoKernelFunct1(Matrix mat, double eps, double m) {
        int n = mat.getColumnDimension()
        Matrix result = Matrix.identity(n,n)
        double expo = 1.0d/(m-1.0d)
        
        for(int i=0; i<n; i++){
            for(int j=i; j<n; j++){
                double dist = mat.get(i,j)
                double val = 1.0d/Math.pow(eps+dist*dist, expo)
                result.set(i,j, val)
                result.set(j,i, val)
            }
        }
        
        return result
    }
}
