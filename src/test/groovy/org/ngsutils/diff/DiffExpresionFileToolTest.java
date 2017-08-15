/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.diff;

import org.ngsutils.annotation.GeneIndex;
import org.ngsutils.annotation.IsoAnnotation;
import java.io.File;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ngsutils.Utils;
import static org.junit.Assert.*;
import org.ngsutils.annotation.BlastAnnotationTest;
import org.ngsutils.diff.tracking.BaseTracking;
import org.ngsutils.semantic.rdfutils.SimpleGraphTest;


/**
 *
 * @author victor
 */
public class DiffExpresionFileToolTest {

   private String dir = "/home/victor/Escritorio/tests_ngsengine/cuffdiff_b_74/";
   private String bowtie_idx = "/home/victor/bowtie_indexes/";

    public DiffExpresionFileToolTest() {
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
    public void testExtractSamplesDiffFiles() throws Exception {
        System.out.println("extractSamplesDiffFiles");

        int nsamples=2;

        DiffExpresionFileTool.extractSamplesDiffFiles(dir, nsamples);
        assertTrue(true);
    }

    /**
     * Test of readDiffExpFiles method, of class DiffExpresionFileTool.
     */
    @Test
    public void testReadDiffExpFiles() throws Exception {
        System.out.println("readDiffExpFiles");

        HashMap<String, BaseTracking> table = new HashMap<String, BaseTracking>();
        int nsamples = 2;
        String fileType = DiffExpresionFileTool.isoformDiffFile;
        boolean onlySignif = true;
        DiffExpresionFileTool.readDiffExpFiles(table, dir, nsamples, fileType, onlySignif);

        assertTrue( table.size()>0 );
    }
    
    /**
     * long time test, uncomment if needed
     * @throws Exception 
     */
//    @Test
//    public void testGenerateFeatAndGOAFiles() throws Exception {
//        System.out.println("generateFeatAndGOAFiles");
//        
//        int nsamples = 2;
//        boolean naive = false;
//        
//        //remove existing feat files
//        for( String file : DiffExpresionFileTool.ARRAY_FILETYPES ){
//            Utils.execCommand("rm "+dir+"0_1_"+file+DiffExpresionFileTool.FEATOVER_FILE_EXT);
//            Utils.execCommand("rm "+dir+"0_1_"+file+DiffExpresionFileTool.FEATUNDER_FILE_EXT);
//        }
//        
//        DiffExpresionFileTool.generateFeatAndGOAFiles( dir, nsamples,
//            bowtie_idx+"hg18.fa", dir+"cuffcompare.gtf.gz", "9606", 
//            SimpleGraphTest.LOCAL_SERVER, SimpleGraphTest.LLD_REPOS_ID, 
//            BlastAnnotationTest.BLAST_LOCAL, BlastAnnotationTest.BLAST_DB, naive );
//        
//    }

//    @Test
//    public void testReadDiffExpFiles2() throws Exception {
//        System.out.println("readDiffExpFiles2");
//
//        HashMap<String, BaseTracking> table = new HashMap<String, BaseTracking>();
//        int nsamples = 2;
//        String fileType = DiffExpresionFileTool.geneDiffFile;
//        boolean onlySignif = true;
//        DiffExpresionFileTool.readDiffExpFiles(table, dir, nsamples, fileType, onlySignif);
//
//        assertTrue( table.size()==22708 );
//
//        fileType = DiffExpresionFileTool.promOverFile;
//        DiffExpresionFileTool.readDiffExpFiles(table, dir, nsamples, fileType, onlySignif);
//
//        assertTrue( table.get("XLOC_184030").getOverload()[0] );
//    }
//
//    @Test
//    public void testReadDiffExpFiles3() throws Exception {
//        System.out.println("readDiffExpFiles3");
//
//        HashMap<String, BaseTracking> table = new HashMap<String, BaseTracking>();
//        int nsamples = 2;
//        String fileType = DiffExpresionFileTool.tssDiffFile;
//        boolean onlySignif = true;
//        DiffExpresionFileTool.readDiffExpFiles(table, dir, nsamples, fileType, onlySignif);
//
//        assertTrue( table.size()==5816 );
//
//        fileType = DiffExpresionFileTool.splicOverFile;
//        DiffExpresionFileTool.readDiffExpFiles(table, dir, nsamples, fileType, onlySignif);
//
//        assertTrue( table.get("TSS43052").getOverload()[0] );
//    }

//    @Test
//    public void testGenerateArff() throws Exception {
//        System.out.println("generateArff");
//
//        HashMap<String, BaseTracking> table = new HashMap<String, BaseTracking>();
//
//        int nsamples = 2;
//        String fileType = DiffExpresionFileTool.tssDiffFile;
//        boolean onlySignif = true;
//        DiffExpresionFileTool.readDiffExpFiles(table, dir, nsamples, fileType, onlySignif);
//
//        fileType = DiffExpresionFileTool.splicOverFile;
//        DiffExpresionFileTool.readDiffExpFiles(table, dir, nsamples, fileType, onlySignif);
//
//        DiffExpresionFileTool.generateArff( "rel-name",table, System.out);
//
//        assertTrue(true);
//
//    }

//    @Test
//    public void testGenerateFeatFile() throws Exception {
//        System.out.println("generateFeatFile");
//
//        String source=dir+"0_1_"+DiffExpresionFileTool.geneDiffFile;
//        String feat="/home/victor/rnaseq_files/cbm01_01/cuffcompare_refseq/cuffcompare_15.combined.gtf";
//
//        DiffExpresionFileTool.generateFeatFile( source, feat, System.out);
//
//        assertTrue(true);
//    }

//    @Test
//    public void testGenerateGOAFile() throws Exception {
//        System.out.println("generateGOAFile");
//
//        String source=dir+"0_1_"+DiffExpresionFileTool.geneDiffFile;
//        String fileFeat=source+".gtf";
//
//        File refGene = new File("/home/victor/work_bio/annotation/9606/refGene.txt");
//        File goa = new File("/home/victor/work_bio/annotation/9606/gene_association.goa");
//        File kegg = new File("/home/victor/work_bio/annotation/9606/kegg.txt");
//        HashMap<String,IsoAnnotation> tableAnnot = IsoAnnotation.genAnnotFromRefGeneGOA(refGene, goa, kegg);
//
//        GeneIndex geneIndex = new GeneIndex( new File(fileFeat) );
//        geneIndex.annotateIsoforms(tableAnnot, false);
//
//        DiffExpresionFileTool.generateGOAFile( source, geneIndex, System.out);
//
//        assertTrue(true);
//    }

    
}