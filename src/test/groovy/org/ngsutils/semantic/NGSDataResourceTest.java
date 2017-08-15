/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic;

import org.ngsutils.semantic.diff.DiffExpresionRdfGenTest;
import java.util.Set;
import org.ngsutils.semantic.rdfutils.SimpleGraphTest;
import org.ngsutils.semantic.rdfutils.SimpleGraph;
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
public class NGSDataResourceTest {
    
    static NGSDataResource instance = new NGSDataResource();
    
    public NGSDataResourceTest() {
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
    
    // TODO uncomment openrdf semantic test
    
//    @Test
//    public void testLoadLocal() {
//        System.out.println("testLoadLocal");
//        LinkedLifeData lld = new LinkedLifeData();
//        SimpleGraph g = new SimpleGraph( SimpleGraph.HTTPRepository(SimpleGraphTest.LOCAL_SERVER, SimpleGraphTest.LLD_REPOS_ID) );
//        lld.setGraph(g);
//        
//        instance.setLifeData(lld);
//        
//        instance.clear();
//        instance.load("9606");
//        
//        assertTrue( instance.getAllGenes().size()>15000 );
//    }
    
//    @Test
//    public void testLoadWeb() {
//        System.out.println("testLoadWeb");
//        LinkedLifeData lld = new LinkedLifeData();
//        
//        instance.setLifeData(lld);
//        
//        instance.clear();
//        instance.load("9606"); //fails, doesn't load all the data
//        
//        assertTrue( instance.getAllGenes().size()>15000 );
//    }
    
//    @Test
//    public void testGetGOTerms() {
//        System.out.println("testGetGOTerms");
//        
//        Set terms = instance.getGOTerms("http://linkedlifedata.com/resource/entrezgene/id/7389");
//        assertEquals(terms.size(), 21);
//        assertTrue( terms.contains("GO:0014070") );
//    }
    
//    @Test
//    public void testGetResultGenes() {
//        SimpleGraph g = new SimpleGraph(true);
//        g.addFile( DiffExpresionRdfGenTest.RESULT_RDF, SimpleGraph.RDFXML);
//        instance.setNgsResGraph(g);
//        
//        Set result = instance.getResultGenes(null);
//        
//        assertTrue( !result.isEmpty() );
//    }
}
