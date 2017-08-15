/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.fuzzy

import org.ngsutils.ontology.OntologyAnnotation

/**
 * Test class for IFMBOntologyWrap, used by FMBSimilarityTest
 * 
 * @author victor
 */
class MapOntologyWrap implements IFMBOntologyWrap {
	
    def densMap
    def ancMap
    
    // test data 1
    static testMap1 = ['4722':0.6134, '4725':0.8778, '16787':0.3274, 
        '6470':0.5139, '7517':0.5713, '8151':0.2026, '8372':0.3545,
        '8138':0.7093, '5201':0.8778, '7155':0.7093, '7397':0.3274,
        '5581':0.2222, '5588':0.2474 ]
    
    static OntologyAnnotation terms1A = 
        new OntologyAnnotation(terms:['4722', '4725', '16787', '6470', '7517', '8151', '8372', '8138'] as Set)
    static OntologyAnnotation terms1B =
        new OntologyAnnotation(terms:['5201', '7155', '7397', '5581', '5588', '7517'] as Set)
    
    //test data 2
    static testMap2 = ['4721':0.52, '6470':0.57, '8270':0.54, 
        '16787':0.33 ]
    
    static OntologyAnnotation terms2A = 
        new OntologyAnnotation(terms:['4721', '6470', '8270'] as Set)
    static OntologyAnnotation terms2B =
        new OntologyAnnotation(terms:['4721', '6470', '16787'] as Set)
    
    //test data 3
    static testMap3 = ['T1':0.42, 'T2':0.57, 'T3':0.54, 
        'T4':0.33 ]
    
    static OntologyAnnotation terms3A = 
        new OntologyAnnotation(terms:['T1', 'T2'] as Set)
    static OntologyAnnotation terms3B = 
        new OntologyAnnotation(terms:['T3', 'T4'] as Set)
    
    
    
    def setTestMap1(){
        densMap = testMap1
    }
    
    def setTestMap2(){
        densMap = testMap2
    }
    
    def setTestMap3(){
        densMap = testMap3
        ancMap = [(buildKey('T1','T3')):'T1']
    }
    
    
    protected buildKey(String t1, String t2) {
        [t1,t2].sort().sum{it+':'}
    }

    public String getNCAncestor(Object term1, Object term2) {
        return ancMap[buildKey(term1,term2)]
    }
    
    public Double getDensity(Object term) {
        return densMap[term]
    }

    public Double getDensity(Object product, Object term) {
        return densMap[term]
    }
    
    public Double getEvidence(Object product, Object term) {
        return 1.0
    }
}

