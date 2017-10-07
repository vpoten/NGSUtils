/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.fuzzy

import org.ngsutils.ontology.GOManager
import org.ngsutils.ontology.OntologyAnnotation
import org.ngsutils.ontology.FMBGOntologyWrap

/**
 *
 * @author victor
 */
class FMBSimilarity {
	
    IFMBOntologyWrap ontology
    def logObjectList
    
    /**
     *
     */
    def setOntologyWrap(ontObj) {
        if( ontObj instanceof GOManager ) {
            ontology = new FMBGOntologyWrap(goManager:ontObj)
        }
    }
    
    /**
     * Fuzzy Based Measure of Similarity (FMS)
     * 
     * @param a1 : an OntologyAnnotation
     * @param a2 : an OntologyAnnotation
     */
    double fms(OntologyAnnotation a1, OntologyAnnotation a2) {
        // build densities map for each set
        def gdens = [a1,a2].collect{ a-> 
            def map = [:]
            a.terms.each{ t-> map[t] = ontology.getDensity(a.product,t) }
            map
        }
        
        // intersection set
        def inters = gdens[0].intersect(gdens[1])
        
        // calculate sugeno measures
        def suglm = gdens.collect{ new SugenoLambdaMeasure(it.values()) }
        
        return sugenoSum(inters.values() as List, suglm[0], suglm[1])
    }
    
    /**
     * Augmented Fuzzy Based Measure of Similarity (AFMS)
     * 
     * @param a1 : an OntologyAnnotation
     * @param a2 : an OntologyAnnotation
     */
    double afms(OntologyAnnotation a1, OntologyAnnotation a2) {
        // 1 - get the map of nearest common ancestors (NCA) of every pair
        def nca = [:]
        a1.terms.each{ t1->
            double ev1 = ontology.getEvidence(a1.product,t1)
            
            a2.terms.each{ t2->
                def res = ontology.getNCAncestor(t1,t2)
                if(res) {
                    double ev2 = ontology.getEvidence(a2.product,t2)
                    double dens = ontology.getDensity(res)*Math.min(ev1,ev2)
                    if( dens>0.0 ){ nca[res] = dens }
                }
            }
        }
        
        // remove redundant ancestors
        def ncaTerms = nca.keySet()
        def ncaRedundant = ncaTerms.findAll{ontology.isAncestor(it, ncaTerms)}
        ncaRedundant.each{nca.remove(it)}
        
        // 2 - build augmented sets
        def gdens = [a1,a2].collect{ a-> 
            def map = [:]
            a.terms.each{ t-> map[t] = ontology.getDensity(a.product,t) }
            map
        }
        
        // intersection set
        def inters = gdens[0].intersect(gdens[1])
        inters += nca
        
        // 3 - calculate sugeno measures
        (0..1).each{ gdens[it]+=nca }
        def suglm = gdens.collect{ new SugenoLambdaMeasure(it.values()) }
        
        double similarity = sugenoSum(inters.values() as List, suglm[0], suglm[1])
        appendToLog(a1.product, a2.product, gdens, nca, similarity)
        
        return similarity
    }
    
    /**
     *
     */
    protected double sugenoSum(intDens, suglm1, suglm2) {
        if( !intDens ){ return 0.0 }
        if( intDens.size()==1 ){ return intDens[0] }
        
        return (Math.min(suglm1.value(intDens),1.0) + Math.min(suglm2.value(intDens),1.0)) * 0.5
    }
    
    /**
     *
     */
    protected void appendToLog(feat1, feat2, annotations, nca, similarity) {
        if( logObjectList==null ) {
            return
        }
        
        def makeFeat = { name, terms ->
            ['name': name, 'annotations': terms.keySet().collect{['id': it, 'ic': terms[it]]}]
        }
        
        def inters = annotations[0].intersect(annotations[1])
        
        def simObject = [
            'product1': makeFeat(feat1, annotations[0]),
            'product2': makeFeat(feat2, annotations[1]),
            'nearestCommonAncestors': nca.keySet().collect{['id': it, 'ic': nca[it]]},
            'intersection': inters.keySet().collect{['id': it, 'ic': inters[it]]},
            'similarity': similarity
        ]
        
        logObjectList << simObject
    }
}
