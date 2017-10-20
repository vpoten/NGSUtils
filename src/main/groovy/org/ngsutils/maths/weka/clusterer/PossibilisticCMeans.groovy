/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths.weka.clusterer

import weka.core.Utils as WekaUtils

/**
 *
 * @author victor
 */
class PossibilisticCMeans extends FuzzyCMeans {
	
    double gamma = 1.0 //weight for entropy term (user defined)
    double [] etas = null //weights for clusters in membership update equation
    
    /**
     *
     */
    protected void setup() {
        super.setup()
        
        //init U using FCM result
        def initClusterer = new FuzzyCMeans(lambda:lambda)
        initClusterer.setNumClusters( numberOfClusters() )
        initClusterer.distFunc = this.distFunc
        initClusterer.buildClusterer( this.data )
        Uprior.setMatrix(0, Uprior.rowDimension-1, 0, Uprior.columnDimension-1, initClusterer.U)
        
        //copy FCM centroids
        this.centroids = initClusterer.centroids
        
        //init etas
        etas = calcPCMWeights(gamma)
    }
    
    /**
     *
     */
    protected void update() {
        updateCentroids()
        updateMembershipsEPCM(etas)
    }
    
    /**
     * Clusterer options:
     * -gamma <double> = PCM entropy parameter (default 1)
     */
    public void setOptions (String[] options) {
        def opt = WekaUtils.getOption('gamma',options)
        if( opt ){ this.gamma = opt as Double }
        super.setOptions(options)
    }
}

