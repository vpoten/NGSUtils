/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.variation

import org.ngsutils.Utils
import weka.core.matrix.EigenvalueDecomposition
import weka.core.matrix.Matrix

/**
 * exception throwed by PopMatrix
 */
class PopMatrixException extends RuntimeException {
    String message
}


/**
 * parallel matrix product
 */
class MatProductRun implements Runnable {
    PopMatrix A
    PopMatrix B
    PopMatrix result
    boolean transpose = false //if B has to be treated as transpose
    int start //first row to compute
    int step //row step
    
    /**
     *
     */
    public void run() {
        int ncol = (transpose) ? B.nrow : B.ncol
        
        for(int i=start; i<A.nrow; i+=step) {
            for(int j=0; j<ncol; j++) {
                double sum = 0.0
                
                for(int k=0; k<A.ncol; k++) {
                    sum += A.get(i,k)*((transpose) ? B.get(j,k) : B.get(k,j))
                }
                
                result.set(i,j,sum)
            }
        }
    }
}


/**
 *
 */
class PairValueVector implements Comparable<PairValueVector> {
    double value
    double [] vector
    
    /**
     * sort in descending order
     */
    public int compareTo(PairValueVector other){
        return other.value<=>this.value
    }
}


/**
 *
 * @author victor
 */
class PopMatrix {
    double [] mat
    int nrow
    int ncol
    
    /**
     *
     */
    public PopMatrix(int pnrow, int pncol) {
        mat = new double[pnrow*pncol]
        nrow = pnrow
        ncol = pncol
        fill(0.0)
    }
    
    /**
     *
     */
    public PopMatrix( PopMatrix orig ) {
        nrow = orig.nrow
        ncol = orig.ncol
        mat = Arrays.copyOfRange(orig.mat, 0, orig.mat.length)
    }
    
    /**
     *
     */
    public void copy( PopMatrix orig ) {
        assert (orig.nrow==nrow && orig.ncol==ncol)
        System.arraycopy(orig.mat, 0, mat, 0, orig.mat.length)
    }
    
    /**
     *
     */
    static PopMatrix identity(int n){
        def m = new PopMatrix(n, n)
        (0..n-1).each{ m.set(it,it,1.0) }
        return m
    }
    
    /**
     *
     */
    private void checkRow(int i){
        if( i<0 || i>=nrow) {
            throw new PopMatrixException(message:"Wrong row index: ${i}, nrow=${nrow}");
        }
    }
    
    /**
     *
     */
    private void checkCol(int j){
        if( j<0 || j>=ncol) {
            throw new PopMatrixException(message:"Wrong column index: ${j}, ncol=${ncol}");
        }
    }
    
    /**
     *
     */
    private void checkMatrixDimensions(PopMatrix B) {
        if (B.ncol != ncol || B.nrow != nrow) {
            throw new PopMatrixException(message:"Matrix dimensions must agree.");
        }
    }
    
    /**
     *
     */
    private void checkSquare(){
        if( nrow!=ncol ) {
            throw new PopMatrixException(message:"Matrix is not square");
        }
    }
    
    /**
     *
     */
    boolean equals(PopMatrix B) {
        (this.nrow==B.nrow && this.ncol==B.ncol && Arrays.equals(this.mat,B.mat))
    }
    
    /**
     *
     */
    double get(int i, int j) {
        return mat[i*ncol+j]
    }
    
    /**
     *
     */
    void set(int i, int j, double val) {
        mat[i*ncol+j] = val
    }
    
    /**
     *
     */
    void fill(double val = 0.0) {
        Arrays.fill(mat, val)
    }
    
    /**
     *
     */
    void product(double val) {
        (0..mat.length-1).each{ mat[it]*=val }
    }
    
    /**
     *
     */
    void sum(double val) {
        (0..mat.length-1).each{ mat[it]+=val }
    }
    
    /**
     *
     */
    double rowSum(int i) {
        checkRow(i)
        (0..ncol-1).sum{ get(i,it) }
    }
    
    /**
     *
     */
    double colSum(int j) {
        checkCol(j)
        (0..nrow-1).sum{ get(it,j) }
    }
    
    /**
     *
     */
    double trace() {
        checkSquare()
        (0..nrow-1).sum{ get(it,it) }
    }
    
    /**
     *
     */
    PopMatrix clone() {
        return new PopMatrix(this)
    }
    
    /**
     *
     */
    void sum(PopMatrix right) {
        checkMatrixDimensions(right)
        (0..mat.length-1).each{ this.mat[it] += right.mat[it] }
    }
    
    /**
     *
     */
    PopMatrix product(PopMatrix right) {
        if (right.nrow != this.ncol) {
            throw new PopMatrixException(message:"Matrix inner dimensions must agree.");
        }
      
        PopMatrix res = new PopMatrix(this.nrow, right.ncol)
      
        for(int i=0; i<this.nrow; i++){
            for(int j=0; j<right.ncol; j++){
                double sum = 0.0
                
                for(int k=0; k<this.ncol; k++){
                    sum += this.get(i,k)*right.get(k,j)
                }
                
                res.set(i,j,sum)
            }
        }
        
        return res
    }
    
    /**
     * M*t(M)
     */
    PopMatrix productTrans() {
        PopMatrix res = new PopMatrix(nrow, nrow)
        
        for(int i=0; i<nrow; i++){
            for(int j=0; j<nrow; j++){
                double sum = 0.0
                
                for(int k=0; k<ncol; k++){
                    sum += get(i,k)*get(j,k)
                }
                
                res.set(i,j,sum)
            }
        }
        
        return res
    }
    
    /**
     * M*t(M)
     */
    PopMatrix productTransThreads(int nthreads) {
        PopMatrix res = new PopMatrix(nrow, nrow)
        return _productThreads(this, res, true, nthreads)
    }
    
    /**
     *
     */
    PopMatrix productThreads(PopMatrix right, int nthreads) {
        if (right.nrow != this.ncol) {
            throw new PopMatrixException(message:"Matrix inner dimensions must agree.");
        }
      
        PopMatrix res = new PopMatrix(this.nrow, right.ncol)
        return _productThreads(right, res, false, nthreads)
    }
    
    /**
     *
     */
    private PopMatrix _productThreads(PopMatrix right, PopMatrix res, boolean transpose, int nthreads) {
        //create runnables
        def list = (0..nthreads-1).collect{ 
            new MatProductRun(A:this, B:right, result:res, transpose:transpose, start:it, step:nthreads)
        }

        Utils.runClosures(list, nthreads)
        
        return res
    }
    
    
    /**
     * convert to jama Matrix
     */
    Matrix toMatrix(){
        Matrix matrix = new Matrix(nrow, ncol)
        
        for (int row=0; row < nrow; row++) {
            for (int column=0; column < ncol; column++) {
                matrix.set(row, column, get(row,column));
            }
        }
        
        return matrix
    }
    
    /**
     * calc EigenvalueDecomposition and returns the list of eigenvalue/vector 
     * sorted by eigenvalue in descending order
     */
    List<PairValueVector> eigenDecomposition(){
        EigenvalueDecomposition eigen = toMatrix().eig()
        double [] values = eigen.getRealEigenvalues()
        Matrix vectors = eigen.getV()// matrix columns are the eigenvectors
        int n = values.size() as Integer
        
        def list = (0..n-1).collect{ i->
            new PairValueVector(value:values[i], vector: ((0..n-1).collect{vectors.get(it,i)} as double []))
        }
        
        // sort by eigenvalue (desc)
        Collections.sort(list)
        
        return list
    }
    
}

