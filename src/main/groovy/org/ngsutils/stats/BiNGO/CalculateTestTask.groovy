/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.stats.BiNGO

/**
 * Classes that perform statistical tests can implement this interface
 * @author victor
 */
public interface CalculateTestTask {
    //implement for statistical tests
    public Map getTestMap();

    public Map getMapSmallX();

    public Map getMapSmallN();

    public Map getMapBigX();

    public Map getMapBigN();
}

