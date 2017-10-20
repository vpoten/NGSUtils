/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths.weka.clusterer

import weka.clusterers.AbstractClusterer
import weka.clusterers.NumberOfClustersRequestable
import weka.core.Instance
import weka.core.Instances
import weka.core.matrix.Matrix
import weka.core.Utils as WekaUtils

/**
 *
 * @author victor
 */
abstract class AbstractFuzzyClusterer extends AbstractClusterer implements NumberOfClustersRequestable {
	
    int numOfClusters = 2
    double epsilon = 0.01d // convergence criterion
    
    protected Matrix U = null
    protected Matrix Uprior = null
    
    protected def instances = null // list of Instance objects
    protected Instances data // the dataset
    
    long iterations
    
    long maxIterations = Long.MAX_VALUE
    
    
    // abstract methods
    abstract protected void update();
    abstract protected void setup();
    abstract protected double sqDistToCentroid(int patt, int clust);
    abstract public double distance(Instance a, Instance b);
    abstract public double distanceByIndex(int a, int b);


    /**
     * 
     */
    public int numberOfClusters(){
        return numOfClusters
    }
    
    
    /**
     *
     */
    void setNumClusters(int n) {
        if (n <= 0) {
            throw new Exception("Number of clusters must be > 0");
        }
        this.numOfClusters = n;
    }
    
    /**
     * 
     */
    public int getNumInstances() {
        return instances.size()
    }
    
    /**
     * 
     */
    public double[] distributionForInstance(Instance instance) throws Exception {
        Integer instIdx = findInstance(instance)
        
        if( instIdx==null ){
            return null
        }
        
        def res = (0..numberOfClusters()-1).collect{ U.get(it,instIdx) } as double []
        double isum = 1.0d/((0..res.length-1).sum{ res[it] })
        (0..res.length-1).each{ res[it] *= isum }
        return res
    }
    
    /**
     * 
     * @return: array of array (instance indexes for each cluster)
     */ 
    public def getClustering() {
        return (0..numberOfClusters()-1).collect{ clIdx->
            (0..instances.size()-1).findAll{ idx->
                clIdx == (0..numberOfClusters()-1).max{U.get(it,idx)}
            }
        }
    }
    
    /**
     * 
     */
    private Integer findInstance(Instance instance){
        Integer idx = null
        int atts = instances[0].numAttributes()
        
        for(int i=0; i<instances.size(); i++) {
            def inst2 = instances[i]
            if( (0..atts-1).every{ instance.value(it)==inst2.value(it) } ) {
                idx = i
                break
            }
        }
        
        return idx
    }
    
    
    /**
     *
     */
    public void buildClusterer(Instances data) {
        this.data = data
        instances = (0..data.numInstances()-1).collect{ data.instance(it) }
        
        setup()
        double delta = Double.MAX_VALUE
        iterations = 0
        
        while( delta>epsilon && iterations<maxIterations ) {
            update()
            delta = pnorm1(U, Uprior)
            
            //Uprior <- U
            Uprior.setMatrix(0, U.rowDimension-1, 0, U.columnDimension-1, U)
            iterations++
        }
    }
    
    
    /**
     * 1-norm between U and Uprior matrices
     */
    static double pnorm1(Matrix A, Matrix B) {
        double sum = 0.0d
        
        for(int i=0; i<A.rowDimension; i++) {
            for(int j=0; j<A.columnDimension; j++) {
                sum += Math.abs( A.get(i,j)-B.get(i,j) )
            }
        }
        
        return sum
    }
    
    
    /**
     * result = left*right
     */
    static void product(Matrix left, Matrix right, Matrix result) {
        if (right.getRowDimension() != left.getColumnDimension()) {
            throw new Exception("Matrix inner dimensions must agree.");
        }
        
        int nrow = left.getRowDimension()
        int ncol = right.getColumnDimension()
        int leftNcol = left.getColumnDimension()
        
        for(int i=0; i<nrow; i++){
            for(int j=0; j<ncol; j++){
                double sum = 0.0d;
                for(int k=0; k<leftNcol; k++){
                    sum += left.get(i,k)*right.get(k,j)
                }
                result.set(i,j,sum);
            }
        }
    }
    
    
    /**
     * result = left*transpose(right)
     */
    static void productByTrans(Matrix left, Matrix right, Matrix result) {
        if (right.getColumnDimension() != left.getColumnDimension()) {
            throw new Exception("Matrix inner dimensions must agree.");
        }
        
        int nrow = left.getRowDimension()
        int ncol = right.getRowDimension()
        int leftNcol = left.getColumnDimension()
        
        for(int i=0; i<nrow; i++){
            for(int j=0; j<ncol; j++){
                double sum = 0.0d;
                for(int k=0; k<leftNcol; k++){
                    sum += left.get(i,k)*right.get(j,k)
                }
                result.set(i,j,sum);
            }
        }
    }
    
    
    /**
     *
     */
    static double rowSum(Matrix m, int row){
        return (0..m.getColumnDimension()-1).sum{ m.get(row,it) }
    }
    
    
    /**
     *
     */
    static boolean equals(Matrix A, Matrix B) {
        ( A.rowDimension==B.rowDimension && 
            A.columnDimension==B.columnDimension && 
            Arrays.equals(A.getColumnPackedCopy(),B.getColumnPackedCopy()) )
    }
    
    
    /**
     * update memberships for entropy based FCM (Fuzzy C Means)
     */
    protected void updateMembershipsEFCM(double lambda) {
        int numInstances = U.columnDimension
        
        for(int h=0; h<numInstances; h++){
            double denom = 0.0d
            
            for(int i=0; i<numberOfClusters(); i++){
                denom += Math.exp(-lambda*sqDistToCentroid(h,i))
            }
            
            denom = 1.0d/denom
            
            for(int i=0; i<numberOfClusters(); i++){
                U.set(i, h, Math.exp(-lambda*sqDistToCentroid(h,i))*denom )
            }
        }
    }
    
    
    /**
     * update memberships for entropy based PCM (Possibilistic C Means)
     */
    protected void updateMembershipsEPCM(double [] etas) {
        int numInstances = U.columnDimension
        
        for(int i=0; i<numberOfClusters(); i++){
            for(int h=0; h<numInstances; h++){
                U.set(i, h, Math.exp(etas[i]*sqDistToCentroid(h,i)) )
            }
        }
    }
    
    /**
     *
     */
    protected double [] calcPCMWeights(double gamma) {
        int numInstances = Uprior.columnDimension
        double [] w = new double [numberOfClusters()]
        
        for(int i=0; i<numberOfClusters(); i++){
            w[i] = 0.0d
            
            for(int h=0; h<numInstances; h++){
                w[i] += Uprior.get(i,h)*sqDistToCentroid(h,i)
            }
            
            w[i] = -1.0d/(gamma*w[i]/rowSum(Uprior,i))
        }
        
        return w
    }
    
    /**
     * Clusterer options:
     * -lambda <double> = FCM entropy parameter (default 2)
     * -gamma <double> = PCM entropy parameter (default 1), for PCM clusterer only
     * -I <integer> = max. iterations
     * -epsilon <double> = convergence criterion (default 0.01)
     * -C <integer> = number of clusters 
     */
    public void setOptions (String[] options) {
        def opt = WekaUtils.getOption('lambda',options)
        if( opt ){ this.lambda = opt as Double }
        
        opt = WekaUtils.getOption('I',options)
        if( opt ){ this.maxIterations = opt as Long }
        
        opt = WekaUtils.getOption('epsilon',options)
        if( opt ){ this.epsilon = opt as Double }
        
        opt = WekaUtils.getOption('C',options)
        if( opt ){ setNumClusters(opt as Integer) }
    }
}

