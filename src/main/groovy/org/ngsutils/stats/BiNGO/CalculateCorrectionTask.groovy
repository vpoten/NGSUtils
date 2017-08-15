/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.stats.BiNGO

/**
 * Classes that perform multiple testing corrections can implement this interface
 * 
 */

public interface CalculateCorrectionTask {

    //implement for corrections
    public double[] getOrdenedPvalues();

    public double[] getAdjustedPvalues();

    public String[] getOrdenedGOLabels();

    public Map getCorrectionMap();

}

