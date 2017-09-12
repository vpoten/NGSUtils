/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths.weka

import org.ngsutils.fuzzy.FMBSimilarity
import org.ngsutils.ontology.GOManager
import weka.core.Attribute
import weka.core.DistanceFunction
import weka.core.Instance
import weka.core.Instances
import weka.core.neighboursearch.PerformanceStats

/**
 * Gene Ontology fuzzy based distance measure; weka wrapper
 * 
 * @author victor
 */
class GOFMBDistance implements DistanceFunction {

    
    protected Instances data = null // the instances used internally.
    protected FMBSimilarity similarity = null
    protected annotationMap //map with key=label, value=OntologyAnnotation
    protected similarityCache = [:] as TreeMap

    /**
     * 
     */ 
    public GOFMBDistance(GOManager gom, annotation){
        similarity = new FMBSimilarity()
        similarity.setOntologyWrap(gom)
        annotationMap = annotation
    }

    public void setInstances(Instances insts) {
        data = insts;
    }

    public Instances getInstances() {
        return this.data;
    }

    public void setAttributeIndices(String string) {
        //nothing to do
    }

    public String getAttributeIndices() {
        //nothing to do
        return null;
    }

    public void setInvertSelection(boolean bln) {
        //nothing to do
    }

    public boolean getInvertSelection() {
        //nothing to do
        return false;
    }

    public double distance(Instance instnc, Instance instnc1) {
        return goFMBDistance( instnc, instnc1 );
    }

    public double distance(Instance instnc, Instance instnc1, PerformanceStats ps) throws Exception {
        return goFMBDistance( instnc, instnc1 );
    }

    public double distance(Instance instnc, Instance instnc1, double d) {
        return goFMBDistance( instnc, instnc1 );
    }

    public double distance(Instance instnc, Instance instnc1, double d, PerformanceStats ps) {
        return goFMBDistance( instnc, instnc1 );
    }

    public void postProcessDistances(double[] doubles) {
        //nothing to do
    }

    public void update(Instance instnc) {
        //nothing to do
    }

    public Enumeration listOptions() {
        //nothing to do
        return null;
    }

    public void setOptions(String[] strings) throws Exception {
        //nothing to do
    }

    public String[] getOptions() {
        //nothing to do
        return null;
    }

    /**
     * calculates the GO FMB semantic distance (1-similarity)
     * 
     * @param instnc1
     * @param instnc2
     * @return
     */
    protected double goFMBDistance( Instance instnc1, Instance instnc2 ) {
        int classIdx = instnc1.classIndex()
        
        // get present genes of each instance
        def instGenes = [instnc1, instnc2].collect{ inst ->
            (0..inst.numAttributes()-1).collect{ idx ->
                def att = inst.attribute(idx)
                if( idx==classIdx ){ return null }
                return inst.stringValue(idx) == "1" ? att.name() : null
            }.findAll{it!=null}
        }
        
        // generate pairs for similarity calculation
        def pairs = []
        instGenes[0].each{g1-> instGenes[1].each{g2-> pairs << new Tuple(g1, g2)}}
        
        if( !pairs ) {
            return 1.0
        }
        
        // calculate mean of similarity
        def total = pairs.sum{p-> geneGOFMBDistance(p[0], p[1])}
        return total / ((double)pairs.size())
    }
    
    /**
     *
     * Internal calc for two individual genes
     */
    protected double geneGOFMBDistance(String g1, String g2) {
        if( g1==g2 ){ return 0.0 }
        
        // first, look in cache to speed calculations
        def value = similarityCache[g1 + '|' + g2]
        value = value ?: similarityCache[g2 + '|' + g1]
        
        if(value != null) { // cache hit!
            return 1.0 - value
        }
        
        def a1 = annotationMap[g1]
        def a2 = annotationMap[g2]
        
        if(!a1.terms || !a2.terms) {
            value = 0.0  // no similarity
        }
        else {
            value = similarity.afms(a1, a2)
        }
        similarityCache[g1 + '|' + g2] = value  // store in cache
        
        return 1.0 - value
    }
}

