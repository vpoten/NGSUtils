/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths.weka.clusterer

import org.ngsutils.Utils
import org.ngsutils.maths.weka.KernelFactory
import weka.clusterers.ClusterEvaluation
import weka.core.EuclideanDistance
import weka.core.Instances
import weka.core.matrix.Matrix

/**
 *
 * @author victor
 */
class CentralClustererUtils {
    
    static final String CLUST_FCM = 'FCM'
    static final String CLUST_PCM = 'PCM'
    static final String CLUST_KFCM = 'KFCM'
    static final String CLUST_KPCM = 'KPCM'
    static final String CLUST_KIPCM = 'KIPCM'
    
    /**
     * 
     */ 
    static def buildClusterer = { name->
        switch(name) {
            case CLUST_FCM: return new FuzzyCMeans();
            case CLUST_PCM: return new PossibilisticCMeans();
            case CLUST_KFCM: return new KernelFuzzyCMeans();
            case CLUST_KPCM: return new KernelPossibilisticCMeans();
            case CLUST_KIPCM: return new KIPossibilisticCMeans();
            default: return null;
        }
    }
    
    /**
     * 
     */ 
    static def isKernelClusterer = { name->
        name in [CLUST_KFCM, CLUST_KPCM, CLUST_KIPCM]
    }
    
    
    /**
     *
     */
    static def runClusterer(arffFile, boolean classPresent, String clustName, options, boolean debug) {
        Instances data = new Instances( Utils.createReader(new File(arffFile)) )
        Instances dataClusterer = null
        
        if( classPresent ){// set class attribute
            data.setClassIndex(data.numAttributes() - 1)
            //build dataset for class to cluster evaluation
            weka.filters.unsupervised.attribute.Remove filter = new weka.filters.unsupervised.attribute.Remove();
            filter.setAttributeIndices("" + (data.classIndex() + 1));
            filter.setInputFormat(data);
            dataClusterer = filter.useFilter(data, filter);
        }
        else{
            dataClusterer = data
        }
        
        AbstractFuzzyClusterer clusterer = buildClusterer(clustName)
        
        if( options ){
            clusterer.setOptions(options as String[])
        }
        
        if( isKernelClusterer(clustName) ){
            clusterer.distances = KernelFactory.calcDistMatrix(dataClusterer, new EuclideanDistance(dataClusterer))
        }
        else{
            clusterer.distFunc = new EuclideanDistance(dataClusterer)
        }
        
        clusterer.buildClusterer(dataClusterer)
        
        //cluster evaluation
        ClusterEvaluation eval = new ClusterEvaluation()
        eval.setClusterer(clusterer)
        eval.evaluateClusterer(data)
        
        println "# of iterations: ${clusterer.iterations}"
        println eval.clusterResultsToString()
        
        if(debug==true) {
            // print instance memberships
            println "\nInstance memberships:"
            (0..data.numInstances()-1).each{
                def inst = data.instance(it)
                def dist = clusterer.distributionForInstance(inst)
                println "${it}: ${dist}"
            }
        }
        
        return clusterer
    }
    
}

