/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths.weka

import org.ngsutils.fuzzy.FMBSimilarity
import org.ngsutils.ontology.GOManager
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
    protected double goFMBDistance( Instance instnc1, Instance instnc2 ){
        //instance attributes: 0=label, 1=index
        String g1 = instnc1.attribute(0).value((int) instnc1.value(0))
        String g2 = instnc2.attribute(0).value((int) instnc2.value(0))
        
        if( g1==g2 ){ return 0.0 }
        
        def a1 = annotationMap[g1]
        def a2 = annotationMap[g2]
        
        return 1.0-similarity.afms(a1,a2)
    }	
}

