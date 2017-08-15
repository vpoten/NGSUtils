/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.stats;

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
public class FisherExactTest {
    
    public FisherExactTest() {
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
    public void testGetCumlativeP() {
        int [] vals = new int [] {1,9,11,3};//a,b,c,d
        
        FisherExact instance = new FisherExact(vals[0]+vals[1]+vals[2]+vals[3]);
        
        double result = instance.getCumlativeP(vals[0],vals[1],vals[2],vals[3]);
        double expected = 0.001379728;
        
        assertTrue( Math.abs(result-expected)<1e-6 );
    }
}