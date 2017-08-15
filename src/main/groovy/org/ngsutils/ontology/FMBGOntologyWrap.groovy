/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.ontology

import org.ngsutils.fuzzy.IFMBOntologyWrap

/**
 * Gene Ontology wrapper for FMBSimilarity
 * 
 * @author victor
 */
class FMBGOntologyWrap implements IFMBOntologyWrap {
    
    GOManager goManager = null
	
    /**
     * 
     */
    public String getNCAncestor(Object term1, Object term2) {
        def res = goManager.getNCAncestor(term1, term2)
        (res!=null) ? goManager.goTermStr(res) : null
    }
    
    /**
     *
     */
    public Double getDensity(Object term) {
        goManager.getIcontent(term)
    }
    
    /**
     *
     */
    public Double getDensity(Object product, Object term) {
        goManager.getImportance(product, term)
    }
    
    /**
     *
     */
    public Double getEvidence(Object product, Object term){
        goManager.getEvidence(product, term)
    }
}
