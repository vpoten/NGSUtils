/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.ontology

import org.ngsutils.semantic.LinkedLifeDataFactory
import org.ngsutils.AnnotationDB

/**
 * Source of semantic data and GO annotations for GOClusterer
 * 
 * @author victor
 */
class GOClustererData {
    def graph  // semantic data
    GOManager goManager  // GeneOntology manager
    def taxonomyId
    
    /**
     * 
     */ 
    public GOClustererData(String workDir, String taxId) {
        // load semantic data
        loadSemanticData(workDir, taxId)
    }
    
    /**
     * 
     */ 
    private loadSemanticData(String workDir, String taxId) {
        taxonomyId = taxId
        // load semantic data
        graph = LinkedLifeDataFactory.loadRepository(LinkedLifeDataFactory.LIST_BASIC_GO, [taxonomyId], workDir)
        
        // get GOA file
        def urlSrc = AnnotationDB.goAssocUrl(taxonomyId)
        def name = urlSrc.substring( urlSrc.lastIndexOf('/')+1 )
        def goaFile = "${workDir}${taxonomyId}/${name}"
        
        // create GO manager
        goManager = new GOManager( graph )
        goManager.calculateProbTerms(goaFile)
        goManager.setEcodeFactors( GOEvidenceCodes.ecodeFactorsSet1 )
    }
}
