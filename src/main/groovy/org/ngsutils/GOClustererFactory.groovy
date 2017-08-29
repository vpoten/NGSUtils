/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils

import org.ngsutils.semantic.LinkedLifeDataFactory
import org.ngsutils.ontology.GOManager
import org.ngsutils.semantic.NGSDataResource
import org.ngsutils.semantic.query.GeneQueryUtils
import org.ngsutils.ontology.GOClusterer

/**
 *
 * @author victor
 */
class GOClustererFactory {
    
    
    public static void doGOCluster(workDir, taxonomyId='9606', data) {
        // load semantic data
        def graph = LinkedLifeDataFactory.loadRepository(LinkedLifeDataFactory.LIST_BASIC_GO, [taxonomyId], workDir)
        def geneQuery = new GeneQueryUtils(graph)
        
        // prepare annotation
        def goManager = new GOManager(graph)
        def annotation = NGSDataResource.create(graph)
        annotation.load(taxonomyId)
        
        // def data = []  // List<URI>
            
        GOClusterer(graph, workDir, taxonomyId,  data)
    }
}
