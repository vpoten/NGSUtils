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
class KernelPossibilisticCMeans extends KernelFuzzyCMeans {
	
    double gamma = 1.0 //weight for entropy term (user defined)
    double [] etas = null //weights for clusters in membership update equation
    
    /**
     *
     */
    protected void setup() {
        super.setup()
        
        //init U using KFCM result
        def initClusterer = new KernelFuzzyCMeans(lambda:lambda, distances:distances)
        initClusterer.setNumClusters( numberOfClusters() )
        initClusterer.K = this.K
        initClusterer.buildClusterer( this.data )
        Uprior.setMatrix(0, Uprior.rowDimension-1, 0, Uprior.columnDimension-1, initClusterer.U)
        
        //init etas
        etas = calcPCMWeights(gamma)
    }
    
    /**
     *
     */
    protected void update() {
        updateInternals()
        updateMembershipsEPCM(etas)
    }
    
    /**
     * Clusterer options:
     * -gamma <double> = PCM entropy parameter (default 1)
     */
    public void setOptions (String[] options) {
        opt = WekaUtils.getOption('gamma',options)
        if( opt ){ this.gamma = opt as Double }
        super.setOptions(options)
    }
}

