/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths.weka.clusterer

import weka.core.matrix.Matrix
import weka.core.DistanceFunction
import weka.core.Instance

/**
 *
 * @author victor
 */
class FuzzyCMeans extends AbstractFuzzyClusterer {
	
    double lambda = 2.0d //weight for entropy term (user defined)
    DistanceFunction distFunc = null
    def centroids = null //list of centroids (instance objects)
    
    /**
     *
     */
    protected void update() {
        updateCentroids()
        updateMembershipsEFCM(lambda)
    }
    
    /**
     *
     */
    protected void setup() {
        int numInstances = instances.size()
        U = new Matrix(numberOfClusters(), numInstances)
        Uprior = new Matrix(numberOfClusters(), numInstances)
        
        simpleInitialization()
    }
    
    /**
     * 
     */
    protected void simpleInitialization() {
        int numInstances = instances.size()
        
        // pick randomly C different data points and use them as centroids
        def idxs = [] as Set
        
        while( idxs.size()<numberOfClusters() ){
            idxs << (int)(Math.random()*numInstances)
        }
        
        centroids = idxs.collect{ instances[it].copy() }
        
        // initialize Uprior using normalized distances to centroids
        instances.eachWithIndex{ inst, i->
            double sum = 0.0
            
            centroids.eachWithIndex{ cent, cl->
                double val = distFunc.distance(inst, cent)
                sum += val
                Uprior.set(cl,i,val)
            }
            
            (0..numberOfClusters()-1).each{ cl->
                Uprior.set(cl,i, Uprior.get(cl,i)/sum)
            }
        }
    }
    
    /**
     * 
     */
    protected void updateCentroids() {
        int dim = instances[0].numAttributes()
        
        centroids = (0..numberOfClusters()-1).collect{ cl->
            double [] vector = (1..dim).collect{ 0.0d } as double []
            
            instances.eachWithIndex{ inst, i->
                double u = Uprior.get(cl,i)
                (0..dim-1).each{ vector[it] += u*inst.value(it) }
            }
            
            double denom = 1.0d/rowSum(Uprior, cl)
            (0..dim-1).each{ vector[it] *= denom }
            
            def inst = new Instance(1.0d, vector)
            inst.setDataset( instances[0].dataset() )
            return inst
        }
    }
    
    /**
     *
     */
    protected double sqDistToCentroid(int patt, int clust) {
        double val = distFunc.distance(instances[patt], centroids[clust])
        return val*val
    }
    
    /**
     *
     */
    public double distance(Instance a, Instance b) {
        return distFunc.distance(a, b)
    }
}
