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
    static distBaseName = 'go_similarities'
    static MF = "molecular_function"
    static BP = "biological_process"
	
    /**
     *
     */
    static generateDistances(workDir, pValues, features) {
        GOClustererData data = new GOClustererData(workDir, "9606")
        def goNamespaces = ['BP': [BP], 'MF': [MF], 'BP+MF': [BP, MF]]
        
        for(pvalue in pValues) {
            for(key in goNamespaces.keySet()) {
                def namespaces = goNamespaces[key]
                def clusterer = new GOClusterer(data, features, namespaces, pvalue);
                def fileName = "${distBaseName}_${String.format('%.3f', pvalue).substring(2)}_${key}.json"
                clusterer.writeSimilarities(new File(workDir, fileName).path)
            }
        }
    }
}

