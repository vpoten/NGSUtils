/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.loader;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ngsutils.ontology.GOManager;
import org.ngsutils.ontology.GOManagerTest;
import org.ngsutils.semantic.rdfutils.SimpleGraph;
        
/**
 *
 * @author victor
 */
public class GOLoaderTest {
    
    
    public GOLoaderTest() {
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
    public void parseOBO() throws Exception {
        GOLoader instance = new GOLoader();
        instance.setGraph(new SimpleGraph());
        
        instance.parseOBO( GOManagerTest.obo_file );
        
        GOManager gom = new GOManager(instance.getGraph());
        
        GOManagerTest gomTest = new GOManagerTest();
        GOManagerTest.instance = gom;
        
        gomTest.testSomeMethod();
        gomTest.testGetCommonAncestors();
        gomTest.testGetNamespace();
    }
}