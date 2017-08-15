/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.stats

import org.ngsutils.semantic.NGSDataResource

/**
 *
 * @author victor
 */
class StatTestParams {

    double significance = 0.05
    String taxonomyId
    String namespace
    String overOrUnder //select 'over' or 'under' expressed genes set
    NGSDataResource annotation
    
    def loadAnnotation(){
        annotation.load(taxonomyId)
    }
    
    Set getSelectedNodes(){
        return annotation.getResultGenes(overOrUnder)
    }
    
    Set getAllNodes(){
        return annotation.getAllGenes(overOrUnder)
    }
    
}

