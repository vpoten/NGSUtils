/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.fuzzy

import org.ngsutils.ontology.GOManager
import org.ngsutils.ontology.OntologyAnnotation
import org.ngsutils.ontology.FMBGOntologyWrap
import org.apache.commons.math3.exception.TooManyEvaluationsException

/**
 *
 * @author victor
 */
class FMBSimilarity {
	
    IFMBOntologyWrap ontology
    
    /**
     *
     */
    def setOntologyWrap(ontObj){
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
        def suglm = null
        
        try {
            suglm = gdens.collect{ new SugenoLambdaMeasure(it.values()) }
        } catch(TooManyEvaluationsException ex) {
            return 0.0d
        }
        
        return sugenoSum(inters.values() as List, suglm[0], suglm[1])
    }
    
    /**
     *
     */
    protected double sugenoSum(intDens, suglm1, suglm2){
        if( !intDens ){ return 0.0 }
        if( intDens.size()==1 ){ return intDens[0] }
        
        return (suglm1.value(intDens)+suglm2.value(intDens))*0.5
    }
    
}

