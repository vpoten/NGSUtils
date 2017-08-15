/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation;

import java.io.File;
import java.util.HashMap;
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
public class IsoAnnotationTest {

    public IsoAnnotationTest() {
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



    /**
     * Test of getOfficialSymbol method, of class IsoAnnotation.
     */
//    @Test
//    public void testGetOfficialSymbol() {
//        System.out.println("getOfficialSymbol");
//
//        IsoAnnotation instance = new IsoAnnotation("NM_000374");
//        String expResult = "UROD";
//        String result = instance.getOfficialSymbol();
//        assertEquals(expResult, result);
//    }



    /**
     * Test of genAnnotFromRefGeneGOA method, of class IsoAnnotation.
     */
    @Test
    public void testGenAnnotFromRefGeneGOA() throws Exception {
        System.out.println("genAnnotFromRefGeneGOA");

        File refGene = new File("/home/victor/work_bio/annotation/9606/refGene.txt.gz");
        File goa = new File("/home/victor/work_bio/annotation/9606/gene_association.goa.gz");
        ///File kegg = new File("/home/victor/work_bio/annotation/9606/kegg.txt.gz");


        HashMap<String,IsoAnnotation> result = IsoAnnotation.genAnnotFromRefGeneGOA(refGene, goa, null);

        IsoAnnotation ins=result.get("NM_000374");

        assertEquals( ins.getOfficialSymbol(), "UROD");
    }

}