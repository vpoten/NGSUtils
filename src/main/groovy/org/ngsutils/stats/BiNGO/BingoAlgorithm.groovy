/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.stats.BiNGO

import org.ngsutils.stats.StatTestParams

/**
 *
 * @author victor
 */
class BingoAlgorithm {
    
    def params
    Map testMap
    Map correctionMap
	
    public CalculateTestTask calculateDistribution() {
        def test = new HypergeometricTestCalculate( new StandardDistributionCount(params.annotation, 
                params.selectedNodes, params.allNodes ) );
        
        test.calculate()
        return test
    }
    
    
    public CalculateCorrectionTask calculateCorrections(Map testMap) {
        def correction = new BenjaminiHochbergFDR(testMap, params.significance );
        
        correction.calculate()
        return correction
    }
    
    
    static BingoAlgorithm performCalculations(params){
        BingoAlgorithm algorithm = new BingoAlgorithm(params:params)
        
        CalculateTestTask test = algorithm.calculateDistribution();
        algorithm.testMap = test.getTestMap();
        
        CalculateCorrectionTask correction = algorithm.calculateCorrections(algorithm.testMap);
        algorithm.correctionMap = correction.getCorrectionMap();
        
        //remove insignificant terms
        def insignificants = []
        
        algorithm.correctionMap.each{ k, v->
            if( v>=params.significance )
                insignificants << k
        }
        
        insignificants.each{ term-> 
            algorithm.correctionMap.remove(term)
            algorithm.testMap.remove(term)
        }
        
        return algorithm
    }
    
}

