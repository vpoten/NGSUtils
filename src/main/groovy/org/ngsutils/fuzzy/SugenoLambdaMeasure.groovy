/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.fuzzy

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.analysis.solvers.LaguerreSolver
import org.apache.commons.math3.exception.TooManyEvaluationsException


/**
 *
 * @author victor
 */
class SugenoLambdaMeasure {
	
    static final double relativeAccuracy = 1.0e-12
    static final double absoluteAccuracy = 1.0e-9
    static final int maxEval = 1000
    static final double NEAR_ZERO = 1.0e-12
    static final double bracketStep = 1.0
    
    //polynomial constants
    static final def polyOne = new PolynomialFunction([1] as double[])
    static final def polyMinusOne = new PolynomialFunction([-1,-1] as double[])
    
    
    final double lambda
    
    /**
     *
     * @param densities : a list or array of double
     */
    public SugenoLambdaMeasure(densities) {
        PolynomialFunction current = polyOne
        densities.each{ current = current.multiply(new PolynomialFunction([1,it] as double[])) }
        current = current.add(polyMinusOne)
        
        //the polynomial equation characterized by 'current' has an unique solution l>-1
        double min
        double max
        
        if( Math.signum(current.value(-1)*current.value(-NEAR_ZERO))<0.0 ) {
            // root in ]-1,0[ interval
            min = -1.0
            max = -NEAR_ZERO
        }
        else{
            // root in ]0,+infinite] interval
            double upper = bracketStep
            double fLower = current.value(NEAR_ZERO)
            
            while( Math.signum(fLower*current.value(upper))>-1.0 ){
                upper += bracketStep
            }

            min = NEAR_ZERO
            max = upper
        }
        
        lambda = trySolve(current, min, max)
    }
    
    private double trySolve(function, double min, double max) {
        double relAcc = relativeAccuracy
        double absAcc = absoluteAccuracy
        def solver = new LaguerreSolver(relAcc, absAcc)
        Double root = null
        int totalEval = maxEval
        
        while(root==null && absAcc<1e-3){
            try{
                root = solver.solve(maxEval, function, min, max)
            }
            catch (TooManyEvaluationsException ex){
                root = null
                relAcc *= 10.0d
                absAcc *= 10.0d
                solver = new LaguerreSolver(relAcc, absAcc)
                totalEval += maxEval
            }
        }
        
        if(root==null) {
            throw TooManyEvaluationsException(totalEval)
        }
        
        return root
    }
    
    /**
     *
     */
    public double value(double a, double b) {
        return a+b+lambda*a*b
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

