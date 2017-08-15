/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths.weka;

import java.util.Enumeration;
import org.ngsutils.ontology.GOPairDistances;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.PerformanceStats;

/**
 * weka distance function implementation for GO similarity
 *
 * @deprecated use GOFMBDistance instead.
 * @author victor
 */
@Deprecated
public class GODistanceFunction  implements DistanceFunction {

    protected GOPairDistances goDistances;

    /** the instances used internally. */
    protected Instances m_Data = null;

    public GODistanceFunction(GOPairDistances goDistances) {
        this.goDistances = goDistances;
    }

    /**
     * @return the goDistances
     */
    public GOPairDistances getGoDistances() {
        return goDistances;
    }


    public void setInstances(Instances insts) {
        m_Data = insts;
    }

    public Instances getInstances() {
        return this.m_Data;
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
        return goDistance( instnc, instnc1 );
    }

    public double distance(Instance instnc, Instance instnc1, PerformanceStats ps) throws Exception {
        return goDistance( instnc, instnc1 );
    }

    public double distance(Instance instnc, Instance instnc1, double d) {
        return goDistance( instnc, instnc1 );
    }

    public double distance(Instance instnc, Instance instnc1, double d, PerformanceStats ps) {
        return goDistance( instnc, instnc1 );
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
     * calculates the GO semantic distance (1-sim)
     * 
     * @param instnc1
     * @param instnc2
     * @return
     */
    protected double goDistance( Instance instnc1, Instance instnc2 ){
        //instance attributes: 0=label, 1=index
        int g1= (int) instnc1.value(1);
        int g2= (int) instnc2.value(1);
        return 1.0-this.goDistances.getSimilarity(g1, g2);
    }
    
}
