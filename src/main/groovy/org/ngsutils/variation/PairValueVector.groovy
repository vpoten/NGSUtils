/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.variation

/**
 *
 * @author victor
 */
class PairValueVector implements Comparable<PairValueVector> {
    double value
    double [] vector
    
    /**
     * sort in descending order
     */
    public int compareTo(PairValueVector other){
        return other.value<=>this.value
    }
}

