/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.stats.BiNGO;

import org.ngsutils.semantic.rdfutils.SimpleGraphTest;
import org.ngsutils.semantic.rdfutils.SimpleGraph;
import org.ngsutils.semantic.LinkedLifeData;
import org.ngsutils.semantic.NGSDataResource;
import org.ngsutils.stats.StatTestParams;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ngsutils.semantic.diff.DiffExpresionRdfGenTest;
import static org.junit.Assert.*;

/**
 *
 * @author victor
 */
public class BingoAlgorithmTest {
    
    public BingoAlgorithmTest() {
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
//    public void testPerformCalculations() {
//        
//        //prepare annotation
//        NGSDataResource annot = NGSDataResource.create( 
//                SimpleGraphTest.LOCAL_SERVER, 
//                SimpleGraphTest.LLD_REPOS_ID, 
//                DiffExpresionRdfGenTest.RESULT_RDF );
// 
//        //prepare stats params
//        StatTestParams statParams = new StatTestParams();
//        statParams.setAnnotation(annot);
//        statParams.setTaxonomyId("9606");
//        statParams.setOverOrUnder("over");
//        statParams.loadAnnotation();
//                
//        BingoAlgorithm res = BingoAlgorithm.performCalculations(statParams);
//        
//        assertNotNull(res);
//        assertTrue( !res.getCorrectionMap().isEmpty() );
//    }
    
}
