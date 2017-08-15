/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.variation;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author victor
 */
public class PopAnalysisTest {
    
    public PopAnalysisTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void eigenAdjustment() {
        String tpedFile = "/home/victor/Escritorio/tests_ngsengine/test_popanalysis.tped";
        PopAnalysis res = PopAnalysis.eigenAdjustment(tpedFile, 3);
        
        assertNotNull(res);
        
        // calculate cumulative explained variance (2 or 3 first components) 
        // for original and population corrected data:
        // (transformed_variance < original_variance)
        
        PopAnalysis res2 = PopAnalysis.eigenWithoutAdjustment(res);
        
        double transVar = 0.0;
        double origVar = 0.0;
        
        for(int i=0; i<3; i++){
            transVar += res2.explainedVar(i);
            origVar += res.explainedVar(i);
        }
        
        assertTrue( transVar < origVar );
    }
}
