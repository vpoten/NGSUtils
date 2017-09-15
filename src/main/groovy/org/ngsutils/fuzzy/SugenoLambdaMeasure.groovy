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
    static final int maxEval = 10000
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
        if(densities.size()==1) {
            lambda = 0
            return
        }
        PolynomialFunction current = polyOne
        densities.each{ current = current.multiply(new PolynomialFunction([1,it] as double[])) }
        current = current.add(polyMinusOne)
        
        //the polynomial equation characterized by 'current' has an unique solution l>-1
        double min, max
        
        try {
            // check if root in ]-1,0[ interval or ]0, +infinite[
            boolean inFirstInter = (Math.signum(current.value(-1)*current.value(-NEAR_ZERO)) < 0.0)
            min = inFirstInter ? -1.0 : NEAR_ZERO
            max = inFirstInter ?-NEAR_ZERO : findUpperLimit(current, current.value(NEAR_ZERO), 1.0)
        } catch (TooManyEvaluationsException ex) {
            lambda = 0
            return
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
            throw new TooManyEvaluationsException(totalEval)
        }
        
        return root
    }
    
    private double findUpperLimit(function, fLower, initVal) {
        double upper = initVal
        double step = 1
        double attempts = 0
        int currentPower = 0
        double funcVal = function.value(upper)
        
        while( Math.signum(fLower*funcVal)>-1.0 ) {
            int power = (int)(attempts * 0.1)
            if (power > currentPower) {
                step *= 2
                currentPower = power
            }
            upper += step
            attempts += 1
            funcVal = function.value(upper)
            
            if(attempts>maxEval) {
                throw new TooManyEvaluationsException(maxEval)
            }
        }
        return upper
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

