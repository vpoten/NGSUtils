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
public class FMBSimilarityTest {
    
    public FMBSimilarityTest() {
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
    public void testFMS() {
        FMBSimilarity instance = new FMBSimilarity();
        MapOntologyWrap ontWrap = new MapOntologyWrap();
        instance.setOntology(ontWrap);
        
        //test 1
        ontWrap.setTestMap1();
        double val = instance.fms(MapOntologyWrap.getTerms1A(), MapOntologyWrap.getTerms1B());
        double expected = 0.5713;
        assertTrue( Math.abs(val-expected)<1e-3 );
        
        val = instance.fms(MapOntologyWrap.getTerms1A(), MapOntologyWrap.getTerms1A());
        expected = 1.0;
        assertTrue( Math.abs(val-expected)<1e-3 );
        
        //test 2
        ontWrap.setTestMap2();
        val = instance.fms(MapOntologyWrap.getTerms2A(), MapOntologyWrap.getTerms2B());
        expected = 0.86;
        assertTrue( Math.abs(val-expected)<1e-3 );
        
        //test 3 (fms and afms)
        ontWrap.setTestMap3();
        val = instance.fms(MapOntologyWrap.getTerms3A(), MapOntologyWrap.getTerms3B());
        expected = 0.0;
        assertTrue( Math.abs(val-expected)<1e-3 );
        
        val = instance.afms(MapOntologyWrap.getTerms3A(), MapOntologyWrap.getTerms3B());
        expected = 0.42;
        assertTrue( Math.abs(val-expected)<1e-3 );
    }
}