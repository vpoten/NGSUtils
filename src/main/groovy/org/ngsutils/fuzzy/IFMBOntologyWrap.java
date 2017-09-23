/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.fuzzy;

import java.util.Collection;

/**
 * Interface for FMBSimilarity ontology wrappers
 * 
 * @author victor
 */
public interface IFMBOntologyWrap {
    
    // get nearest common ancestror of two terms
    public String getNCAncestor(Object term1, Object term2);
    
    // get density of a term
    public Double getDensity(Object term);
    
    // get density of an annotation
    public Double getDensity(Object product, Object term);
    
    // get the evidence reliability of the annotation
    public Double getEvidence(Object product, Object term);
            
    // checks whether term is ancestor of any term in given collection
    public boolean isAncestor(Object term, Collection terms);
}
