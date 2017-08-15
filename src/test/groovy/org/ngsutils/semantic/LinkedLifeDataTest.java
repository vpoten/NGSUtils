/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic;

import org.ngsutils.semantic.rdfutils.SimpleGraphTest;
import org.ngsutils.semantic.rdfutils.SimpleGraph;
import java.util.List;
import java.util.Map;
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
public class LinkedLifeDataTest {
    
    public LinkedLifeDataTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    
    @Test
    public void testSomething() {
        Map databases = (Map) LinkedLifeData.getDatabases();
        
        assertEquals( databases.size(), 33);
        assertNotNull( databases.get("BioGRID") );
        assertNotNull( databases.get("Gene Ontology") );
        assertNotNull( databases.get("Reactome") );
        assertNotNull( databases.get("DrugBank") );
        assertNotNull( databases.get("NCBI Entrez-Gene") );
    } 
    
    @Test
    public void testGetBySize() {
        Map databases = (Map) LinkedLifeData.getBySize(0,5000000);
        assertEquals( databases.size(), 13);
    }
    
    /**
     * enable this method if a local LLD repository is running
     */
//    @Test
//    public void testLocalQuery() {
//        System.out.println("testLocalQuery");
//        LinkedLifeData lld = new LinkedLifeData();
//        SimpleGraph g = new SimpleGraph( SimpleGraph.HTTPRepository(SimpleGraphTest.LOCAL_SERVER, SimpleGraphTest.LLD_REPOS_ID) );
//        lld.setGraph(g);
//        
//        List results = (List) lld.query(SimpleGraphTest.GOGENE_QUERY);
//        
//        assertTrue(!results.isEmpty());
//    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testWebQuery() {
        System.out.println("testWebQuery");
        LinkedLifeData lld = new LinkedLifeData();
        
        List results = (List) lld.query(SimpleGraphTest.GOGENE_QUERY);
        
        assertTrue(!results.isEmpty());
    }
    
}
