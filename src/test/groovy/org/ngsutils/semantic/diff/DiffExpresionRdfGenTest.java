/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.diff;

import org.ngsutils.stats.BiNGO.BingoAlgorithm;
import org.ngsutils.stats.StatTestParams;
import org.ngsutils.semantic.rdfutils.SimpleGraphTest;
import org.ngsutils.semantic.NGSDataResource;
import org.ngsutils.semantic.NGSResults;
import org.ngsutils.semantic.rdfutils.SimpleGraph;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
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
public class DiffExpresionRdfGenTest {
    
    public static final String RESULT_RDF = "/home/victor/Escritorio/tests_ngsengine/cuffdiff_74/0_1_isoform.xml";
    static private String DIFF_FILE_TEST = "/home/victor/Escritorio/tests_ngsengine/cuffdiff_74/0_1_isoform_exp.diff";
    
    static DiffExpresionRdfGen rdfGen = new DiffExpresionRdfGen();
    
    
    public DiffExpresionRdfGenTest() {
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
    public void testGenerateGraph() throws FileNotFoundException {
        
        
        rdfGen.genGraph( DIFF_FILE_TEST, "http://example.com/job/74");
        
        assertNotNull( rdfGen.getGraph() );
        
        rdfGen.getGraph().dumpRDF( 
                new FileOutputStream(new File( RESULT_RDF )), 
                SimpleGraph.RDFXML);
    }
    
    // TODO uncomment openrdf semantic test
    
//    @Test
//    public void testGenNaiveAnnotation() throws FileNotFoundException {
//        // load over/under.gtf and annotate using gene_name and associated GO terms
//        
//        NGSDataResource annot = NGSDataResource.create( 
//                SimpleGraphTest.LOCAL_SERVER, 
//                SimpleGraphTest.LLD_REPOS_ID);
//                
//        rdfGen.genNaiveAnnotation( DIFF_FILE_TEST, annot.getLifeData(), 
//                "http://example.com/job/74", "9606");
//        
//        rdfGen.getGraph().dumpRDF( 
//                new FileOutputStream(new File( RESULT_RDF )), 
//                SimpleGraph.RDFXML);
//    }
    
//    @Test
//    public void testGenOverepTerms() throws FileNotFoundException {
//        //prepare annotation
//        NGSDataResource annot = NGSDataResource.create( 
//                SimpleGraphTest.LOCAL_SERVER, 
//                SimpleGraphTest.LLD_REPOS_ID, 
//                RESULT_RDF );
// 
//        //prepare stats params
//        StatTestParams statParams = new StatTestParams();
//        statParams.setAnnotation(annot);
//        statParams.setTaxonomyId("9606");
//        statParams.loadAnnotation();
//                
//        statParams.setOverOrUnder("over");
//        BingoAlgorithm res = BingoAlgorithm.performCalculations(statParams);
//        rdfGen.genOverepTerms( res.getTestMap(), res.getCorrectionMap(), 
//                "http://example.com/job/74", NGSResults.PROP_OVEREPOVER );
//        
//        statParams.setOverOrUnder("under");
//        res = BingoAlgorithm.performCalculations(statParams);
//        rdfGen.genOverepTerms( res.getTestMap(), res.getCorrectionMap(), 
//                "http://example.com/job/74", NGSResults.PROP_OVEREPUNDER );
//        
//        
//        rdfGen.getGraph().dumpRDF( 
//                new FileOutputStream(new File( RESULT_RDF )), 
//                SimpleGraph.RDFXML);
//    }
    
}
