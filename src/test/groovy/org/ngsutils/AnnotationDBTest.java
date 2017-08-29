/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils;

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
public class AnnotationDBTest {

    public AnnotationDBTest() {
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
    public void testOrganismData() {
        Map res = (Map) AnnotationDB.organismData("0");
        assertNull(res);

        res = (Map) AnnotationDB.organismData("9606");
        assertTrue( res.get("sname").equals("hsa") );
    }

    @Test
    public void testConvertGffToGenePred(){
        String gffFile="/home/victor/Escritorio/tests_ngsengine/osativa.gff3";
        String outFile="/home/victor/Escritorio/tests_ngsengine/osativa.gp.txt";

        String res = AnnotationDB.convertGffToGenePred( gffFile, outFile, false);

        assertNotNull(res);
    }
}