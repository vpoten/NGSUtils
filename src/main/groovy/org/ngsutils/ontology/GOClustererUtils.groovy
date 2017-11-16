/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.ontology

/**
 *
 * @author victor
 */
class GOClustererUtils {
    static final distBaseName = 'go_similarities'
    static final MF = "molecular_function"
    static final BP = "biological_process"
    static final goNamespaces = ['BP': [BP], 'MF': [MF], 'BP+MF': [BP, MF]]
    
    String workDir
    def features
    def searchResults = [:]
    
    /**
     * 
     */ 
    private static distFileName(pvalue, namespaceKey) {
        "${distBaseName}_${String.format('%.3f', pvalue).substring(2)}_${namespaceKey}.json"
    }
    
    /**
     *
     */
    def generateDistances(pValues) {
        GOClustererData data = new GOClustererData(workDir, "9606")
        
        for(pvalue in pValues) {
            for(key in goNamespaces.keySet()) {
                def namespaces = goNamespaces[key]
                def clusterer = new GOClusterer(data, features, namespaces, pvalue);
                def fileName = GOClustererUtils.distFileName(pvalue, key)
                clusterer.writeSimilarities(new File(workDir, fileName).path)
            }
        }
    }
    
    /**
     *
     */
    def gridSearch(pValues, namespaceKey, parameters, numExecs) {
        for(pvalue in pValues) {
            println "\n===== Grid search for pValue ${pvalue} ====="
            def distFile = GOClustererUtils.distFileName(pvalue, namespaceKey)
            def clusterer = new GOClusterer(features, new File(workDir, distFile))
            searchResults[pvalue] = GOClusterer.gridSearch(clusterer, parameters, numExecs, true)
        }
    }
}

