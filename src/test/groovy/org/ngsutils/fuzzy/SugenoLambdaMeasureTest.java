/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.fuzzy;

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
public class SugenoLambdaMeasureTest {
    
    public SugenoLambdaMeasureTest() {
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
    public void calcMeasures() {
        SugenoLambdaMeasure instance;
        
        double [] densities = new double [] {0.4, 0.3, 0.2};
        instance = new SugenoLambdaMeasure(densities);
        double expected = 0.3719;
        assertTrue( Math.abs(instance.getLambda()-expected)<1e-3 );
        
        densities = new double [] {0.5, 0.4, 0.3};
        instance = new SugenoLambdaMeasure(densities);
        expected = -0.4515;
        assertTrue( Math.abs(instance.getLambda()-expected)<1e-3 );
        
        densities = new double [] {0.58, 0.44, 0.65};
        instance = new SugenoLambdaMeasure(densities);
        expected = -0.864;
        assertTrue( Math.abs(instance.getLambda()-expected)<1e-3 );
        
        expected = 0.8;
        assertTrue( Math.abs(instance.value(0.58,0.44)-expected)<1e-3 );
        
        expected = 0.903;
        assertTrue( Math.abs(instance.value(0.58,0.65)-expected)<1e-3 );
        
        expected = 1.0;
        double val = instance.value(densities);
        assertTrue( Math.abs(val-expected)<1e-3 );
    }
    
}