/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.ontology

/**
 * A simple class for annotation, used by FMBSimilarity
 * 
 * @author victor
 */
class OntologyAnnotation {
    def id
    def product // gene or feature (usually gene symbol) or list of feature
    def terms // a collection of terms
}

