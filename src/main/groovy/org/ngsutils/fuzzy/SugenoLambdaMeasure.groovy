/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.fuzzy

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.analysis.solvers.LaguerreSolver 
import org.ngsutils.maths.PolynomialRootFinder


/**
 *
 * @author victor
 */
class SugenoLambdaMeasure {
	
    static final double NEAR_ZERO = 1.0e-9
    static final int MAX_EVAL = 1000000
    
    //polynomial constants
    static final def polyOne = new PolynomialFunction([1] as double[])
    static final def polyMinusOne = new PolynomialFunction([-1,-1] as double[])
    
    final Double lambda = null
    
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

        double zero = NEAR_ZERO
        def rootFilter = {val-> val.getReal()>-1.0d && Math.abs(val.getImaginary())<0.1}
        // filter and sort by imaginary part
        def sortedByImag = (roots as List).findAll(rootFilter).sort{ Math.abs(it.getImaginary()) }
        def sortedByReal = (roots as List).findAll(rootFilter).sort{ it.getReal() }
        
        laguerreSolver(current)
        
        while(lambda==null) {
            lambda = (roots as List).find{it.isReal() && it.getReal()>-1.0d && Math.abs(it.getReal())>zero}?.getReal()
            zero *= 0.1d
        }
    }
    
    protected laguerreSolver(PolynomialFunction f) {
        def solver = new LaguerreSolver()
        def roots = null
        
        try {
            roots = solver.solveAllComplex(f.coefficients, 0.0d, SugenoLambdaMeasure.MAX_EVAL)
        }
        catch(e) {
            return null
        }
        
        def rootFilter = {val-> val.getReal()>-1.0d && Math.abs(val.getImaginary())<0.1}
        // filter and sort by imaginary part
        def sortedByImag = (roots as List).findAll(rootFilter).sort{ Math.abs(it.getImaginary()) }
        def sortedByReal = (roots as List).findAll(rootFilter).sort{ it.getReal() }
        
        return roots
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
       def subsets = (0..array.size()-1).step(2).collect{
           ((it+1)<array.size()) ? value(array[it], array[it+1]) : array[it]
       }
       return subsets.inject(0){acc, val-> value(acc, val)}
    }
    
}

