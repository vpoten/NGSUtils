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
    
    final double [] silhCoeff
    final double [] intraAvgDist // intra cluster average distance
    final double [] minAvgExtDist // minimun average distance to points in other clusters
    final double overall
    final Double [] clusterOverall
    
    
    public Silhouette(AbstractFuzzyClusterer clusterer) {
        def clustering = clusterer.getClustering()
        
        // calc intra cluster average distance for each point and
        // calc minimun average distance to points in other clusters
        silhCoeff = new double [clusterer.numInstances]
        intraAvgDist = new double [clusterer.numInstances]
        minAvgExtDist = new double [clusterer.numInstances]
        
        for(cluster in clustering) {
            def denom = 1.0/((double)cluster.size()-1)
            def others = clustering.findAll{it != cluster}
            
            for(index in cluster) {
                if( cluster.size()==1 ) {
                    intraAvgDist[index] = Double.NaN
                }
                else {
                    intraAvgDist[index] = denom * cluster.sum{clusterer.distanceByIndex(index, it)}
                }
                
                minAvgExtDist[index] = others.collect{ cluster2 ->
                    if(cluster2.isEmpty()) {return Double.MAX_VALUE}
                    cluster2.sum{clusterer.distanceByIndex(index, it)} / ((double)cluster2.size())
                }.min()
                
                if( cluster.size()==1 ) {
                    silhCoeff[index] = -1.0d  // penalty for single instance cluster
                }
                else {
                    silhCoeff[index] = (minAvgExtDist[index] - intraAvgDist[index]) /
                        Math.max(minAvgExtDist[index], intraAvgDist[index])
                }
            }
        }
        
        // calc overall coefficient (average of all points)
        overall = (silhCoeff as List).sum() / ((double)clusterer.numInstances)
        
        clusterOverall = clustering.collect{ cluster ->
            if(cluster.isEmpty()) {return null}
            cluster.sum{silhCoeff[it]} / ((double)cluster.size())
        } as Double []
    }	
}

