/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.biojava.bio.program.gff.GFFEntrySet;
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
public class GeneIndexTest {

    private String pathGtf =
            "/home/victor/rnaseq_files/cbm01_01/cuffcompare_refseq/cuffcompare_15.combined.gtf";

    private String pathGtf2 =
            "/home/victor/oral_carcinoma/cuffcompare_5.combined.gtf.gz";

    public GeneIndexTest() {
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
     * Test of getEntrySet method, of class GeneIndex.
     */
//    @Test
//    public void testGetEntrySet() {
//        System.out.println("getEntrySet");
//
//        File inFile=new File(pathGtf);
//
//        GeneIndex instance = new GeneIndex(inFile, "chr1");
//
//        GFFEntrySet result = instance.getEntrySet();
//
//        assertTrue( result!=null );
//
//        Gene gen=instance.getGene("XLOC_000002");
//
//        assertTrue( gen.getIsoforms().size()==4 );
//        assertTrue( gen.getPrimTranscript().size()==2 );
//
//        assertTrue( gen.getIsoforms().get("TCONS_00000004").getExons().size()==3 );
//        assertTrue( gen.getIsoforms().get("TCONS_00000003").getExons().size()==4 );
//
//        gen=instance.getGene("XLOC_272348");
//        assertTrue(gen==null);//gene from chr22
//        ///assertTrue( gen.getIsoforms().size()==1 );
//        ///assertTrue( gen.getPrimTranscript().size()==0 );
//
//        File inFile=new File(pathGtf2);
//
//        GeneIndex instance = new GeneIndex(inFile, "chr1");
//
//        GFFEntrySet result = instance.getEntrySet();
//
//        assertTrue( result!=null );
//    }

////    @Test
////    public void testAnnotateIsoformsNCBI() {
////        System.out.println("annotateIsoformsNCBI");
////
////        File inFile=new File(pathGtf);
////        GeneIndex instance = new GeneIndex(inFile);
////        HashMap<String,IsoAnnotation> table = new HashMap<String,IsoAnnotation>();
////
////        int count=instance.annotateIsoforms(table, true);
////
////        assertTrue( count>0 );
////    }

//    @Test
//    public void testAnnotateIsoforms() throws Exception {
//        System.out.println("annotateIsoforms");
//
//        File inFile=new File(pathGtf);
//        GeneIndex instance = new GeneIndex(inFile,"chr1");
//
//        File refGene = new File("/home/victor/work_bio/annotation/9606/refGene.txt");
//        File goa = new File("/home/victor/work_bio/annotation/9606/gene_association.goa");
//        File kegg = new File("/home/victor/work_bio/annotation/9606/kegg.txt");
//
//
//        HashMap<String,IsoAnnotation> table = IsoAnnotation.genAnnotFromRefGeneGOA(refGene, goa, kegg);
//
//        int count=instance.annotateIsoforms(table,false);
//
//        assertTrue( count>0 );
//    }

//    @Test
//    public void testGetSequences() throws Exception {
//        System.out.println("getSequences");
//
//        File inFile=new File(pathGtf);
//
//        List<String> seqs= GeneIndex.getSequences(inFile);
//
//
//        assertTrue( seqs.size()==24 || seqs.size()==25 );
//    }

}