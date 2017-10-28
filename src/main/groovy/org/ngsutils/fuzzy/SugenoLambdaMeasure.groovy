/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.fuzzy

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.analysis.solvers.LaguerreSolver
import org.apache.commons.math3.exception.TooManyEvaluationsException
import org.ngsutils.maths.PolynomialRootFinder


/**
 *
 * @author victor
 */
class SugenoLambdaMeasure {
	
    static final double NEAR_ZERO = 1e-9
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

        lambda = (roots as List).find{it.isReal() && it.getReal()>-1.0d && Math.abs(it.getReal())>NEAR_ZERO}?.getReal()
        
        if(lambda==null) {
            lambda = laguerreSolver(current)
        }
    }
    
    protected laguerreSolver(PolynomialFunction f) {
        def solver = new LaguerreSolver()
        Double root = null
        
        def hasDiffSigns = { a, b->
            (f.value(a) * f.value(b)) < 0.0d
        }
        
        try {
            if( hasDiffSigns(-1.0d, -NEAR_ZERO) ) {
                root = solver.solve(SugenoLambdaMeasure.MAX_EVAL, f, -1.0d, -NEAR_ZERO)
            }
            else {
                double upper = 1.0d
                int evals = 0
                
                while(!hasDiffSigns(NEAR_ZERO, upper) && evals < SugenoLambdaMeasure.MAX_EVAL) {
                    upper += 100.0d
                    evals++
                }
                
                if( evals==SugenoLambdaMeasure.MAX_EVAL ){
                    return null
                }
                
                root = solver.solve(SugenoLambdaMeasure.MAX_EVAL, f, NEAR_ZERO, upper)
            }
        }
        catch(TooManyEvaluationsException e) {
            root = null
        }
        
        return root
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

