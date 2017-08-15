/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.annotation.genome;

import java.util.List;
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
public class FeatureIndexTest {
    
    public FeatureIndexTest() {
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
    public void testCreate() {
        String workDir = "/home/victor/Escritorio/transcriptome_60/feature_enrich/";
        FeatureIndex instance = AnnotationFactory.createIndex(workDir, "9606", AnnotationFactory.getANN_UCSC_H3K4());
        
        assertNotNull(instance);
        
        List results = instance.getFeatsByPos("chr1:800800-800973");
        assertTrue( results.size()==1 );
        
        results = instance.getFeatsByPos("chr1:800751-800973");
        assertTrue( results.size()==1 );
        
        results = instance.getFeatsByName("H3K4Me1");
        assertTrue( results.size()>=109612 );
        
        results = instance.getFeatsByPos("chr1:824900-825000");
        assertTrue( results.size()==2 );
        assertNotSame(((Feature)results.get(0)).getName(), ((Feature)results.get(1)).getName());
        
        results = instance.getFeatsByPos("chr1:235600-235657");
        assertTrue( results.isEmpty() );
        
        results = instance.getFeatsByPos("chr1:713400-713500");
        assertTrue( results.isEmpty() );
    }
}