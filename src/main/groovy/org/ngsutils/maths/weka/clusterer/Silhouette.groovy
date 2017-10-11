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
    
    double [] silhCoeff
    double [] intraAvgDist // intra cluster average distance
    double [] minAvgExtDist // minimun average distance to points in other clusters
    double overall
    double [] clusterOverall
    
    
    public Silhouette(AbstractFuzzyClusterer clusterer) {
        def clustering = clusterer.getClustering()
        
        // calc intra cluster average distance for each point and
        // calc minimun average distance to points in other clusters
        silhCoeff = new double [clusterer.numInstances]
        intraAvgDist = new double [clusterer.numInstances]
        minAvgExtDist = new double [clusterer.numInstances]
        
        for(cluster in clustering) {
            def denom = 1.0/((double)cluster.size())
            def others = clustering.findAll{it != cluster}
            
            for(index in cluster) {
                intraAvgDist[index] = denom * cluster.sum{clusterer.distanceByIndex(index, it)}
                
                minAvgExtDist[index] = others.collect{ cluster2 ->
                    if(cluster2.isEmpty()) {return 0.0d}
                    cluster2.sum{clusterer.distanceByIndex(index, it)} / ((double)cluster2.size())
                }.min()
                
                silhCoeff[index] = (minAvgExtDist[index] - intraAvgDist[index]) /
                    Math.max(minAvgExtDist[index], intraAvgDist[index])
            }
        }
        
        // calc overall coefficient (average of all points)
        overall = (silhCoeff as List).sum() / ((double)clusterer.numInstances)
        
        clusterOverall = clustering.collect{ cluster ->
            cluster.sum{silhCoeff[it]} / ((double)cluster.size())
        } as double []
    }	
}

