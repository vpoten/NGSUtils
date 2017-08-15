/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths.weka.clusterer


/**
 *
 * @author victor
 */
class KernelFuzzyCMeans extends AbstractKernelFuzzyClusterer {
	
    double lambda = 2.0d //weight for entropy term (user defined)
    
    /**
     *
     */
    protected void setup() {
        super.setup()
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
        
        // get distances to cluster centroid
        idxs.eachWithIndex{center, cl->
            (0..numInstances-1).each{ h->
                Uprior.set(cl, h, distances.get(center,h)) 
            }
        }
        
        // normalize distances and convert to membership values
        (0..numInstances-1).each{ h->
            double sum = (0..numberOfClusters()-1).sum{ Uprior.get(it,h) }
            (0..numberOfClusters()-1).each{ cl-> Uprior.set(cl,h, 1.0d-Uprior.get(cl,h)/sum ) }
        }
    }
    
    /**
     *
     */
    protected void update() {
        updateInternals()
        updateMembershipsEFCM(lambda)
    }
    
}

