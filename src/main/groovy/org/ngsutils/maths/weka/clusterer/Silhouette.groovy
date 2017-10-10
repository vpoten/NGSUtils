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
class Silhouette {
    
    double [] overallCoeff
    double [] intraAvgDist
    
    
    public Silhouette(AbstractFuzzyClusterer clusterer) {
        def clustering = clusterer.getClustering()
        
        // calc intra cluster average distance for each point
        intraAvgDist = new double [clusterer.numOfClusters]
        
        
        for(cluster in clustering) {
            def denom = 1.0/((double)cluster.size())
            for(index in cluster) {
                intraAvgDist[index] = denom * cluster.sum{clusterer.distanceByIndex(index, it)}
            }
        }
        
        // calc the minimun distance to points in other clusters
        def minExtDist = new double [clusterer.numOfClusters]
        // TODO
        
        
        // calc overall coefficient (average of all points)
    }	
}

