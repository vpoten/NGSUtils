/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.fuzzy

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.ngsutils.maths.PolynomialRootFinder


/**
 *
 * @author victor
 */
class SugenoLambdaMeasure {
	
    static final double NEAR_ZERO = 1.0e-12
    
    //polynomial constants
    static final def polyOne = new PolynomialFunction([1] as double[])
    static final def polyMinusOne = new PolynomialFunction([-1,-1] as double[])
    
    final Double lambda
    
    /**
     *
     * @param densities : a list or array of double
     */
    public SugenoLambdaMeasure(densities) {
        if(densities.size()==1) {
            lambda = 0
            return
        }
        PolynomialFunction current = polyOne
        densities.each{ current = current.multiply(new PolynomialFunction([1,it] as double[])) }
        current = current.add(polyMinusOne)
        
        def roots = PolynomialRootFinder.findRoots(current.coefficients)

        lambda = (roots as List).find{it.isReal() && it.getReal()>-1.0d && Math.abs(it.getReal())>NEAR_ZERO}?.getReal()
    }
    
    /**
     * Basic calc of Sugeno lambda measure
     */
    public double value(double a, double b) {
        return a + b + lambda * (a * b)
    }
    
    /**
     * @param array : list or array with 2 or more elements
     */
    public double value(array) {
        double measure = value(array[0],array[1])
        
        for(int i=2; i<array.size(); i++) {
            measure = value(measure,array[i])
        }
        
        return measure
    }
    
}

