/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.annotation;

import org.ngsutils.semantic.LinkedLifeData;
import java.util.ArrayList;
import java.util.List;
import org.ngsutils.ontology.GOManager;
import java.util.Set;
import java.util.HashMap;
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
public class BlastAnnotationTest {
    
    private static String pathGtf1 =
            "/home/victor/Escritorio/tests_ngsengine/cuffdiff_74/0_1_isoform_exp.diff.over.gtf.gz";
    private static String pathHg19 = "/home/victor/bowtie_indexes/hg19.fa";
    
    private static String unknowSeq = ">sequence2\n"+
"acacagatccacaagctcctgacaggatggcttcccttcgactcttcctc\n"+
"ctttgcctcgctggactggtatttgtgtctgaagctggccccgcgggtgc\n"+
"tggagaatccaaatgtcctctgatggtcaaagtcctggatgctgtccgag\n"+
"gcagccctgctgtagacgtggctgtaaaagtgttcaaaaagacctctgag\n"+
"ggatcctgggagccctttgcctctgggaagaccgcggagtctggagagct\n"+
"gcacgggctcaccacagatgagaagtttgtagaaggagtgtacagagtag\n"+
"aactggacaccaaatcgtactggaagacacttggcatttccccgttccat\n"+
"gaattcgcggatgtggttttcacagccaacgactctggccatcgccacta\n"+
"caccatcgcagccctgctcagcccatactcctacagcaccacggctgtcg\n"+
"tcagcaacccccagaattgagagactcagcccaggaggaccaggatcttg\n"+
"ccaaagcagtagcatcccatttgtaccaaaacagtgttcttgctctataa\n"+
"accgtgttagcagctcaggaagatgccgtgaagcattcttattaaaccac\n"+
"ctgctatttcattcaaactgtgtttcttttttatttcctcatttttctcc\n"+
"cctgctcctaaaacccaaaattttttaaagaattctagaaggtatgcgat\n"+
"caaactttttaaagaaagaaaatactttttgactcatggtttaaaggcat\n"+
"cctttccatcttggggaggtcatgggtgctcctggcaacttgcttgagga\n"+
"agataggtcagaaagcagagtggaccaaccgttcaatgttttacaagcaa\n"+
"aacatacactaacatggtctgtagctattaaaagcacacaatctgaaggg\n"+
"ctgtagatgcacagtagtgttttcccagagcatgttcaaaagccctgggt\n"+
"tcaatcacaatactgaaaagtaggccaaaaaacattctgaaaatgaaata\n"+
"tttgggtttttttttataacctttagtgactaaataaagccaaatctagg\n"+
"ct";
    
    public static final String BLAST_LOCAL = "http://localhost/blast/blast.cgi";
    public static final String BLAST_DB = "refseq_protein";
    
    static String trig_file="/home/victor/work_bio/annotation/geneontology.tar.gz";
    static GOManager goManager=null;
    static List hitList=null;
    
    
    public BlastAnnotationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        goManager = new GOManager( trig_file );
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
    
    
//    @Test
//    public void testGetSequences() {
//        GeneIndex geneidx = new GeneIndex(new File(pathGtf1));
//        
//        assertNotNull( BlastAnnotation.getSequences(geneidx.getIsoforms(), pathHg19) );
//        
//        assertNotNull( geneidx.getIsoforms().iterator().next().getSequence() );
//    }
    
    
//    @Test
//    public void testDoBlast() {
//        BlastAnnotation blast = new BlastAnnotation();
//        blast.setLifeData( new LinkedLifeData() );
//        
//        hitList = (List) blast.doBlast(unknowSeq);
//        assertNotNull( hitList );
//    }
    
    
    @Test(expected = UnsupportedOperationException.class)
    public void testMapGOTerm() {
        HashMap map = new HashMap();
        map.put("id", "gi|7305599|ref|NP_038725.1|");
        map.put("accession", "NP_038725");
        
        BlastAnnotation blast = new BlastAnnotation();
        // add LLData to blast
        blast.setLifeData( new LinkedLifeData() );
        Set termSet = (Set) blast.mapGOTerm(map);
        
        assertNotNull(termSet);
        assertTrue(!termSet.isEmpty());
    }
    
    
//    @Test
//    public void testAnnotate() {
//        BlastAnnotation blast = new BlastAnnotation();
//        blast.setGoManager(goManager);
//        Set res = (Set) blast.annotate(hitList);
//        
//        assertNotNull(res);
//        assertTrue(!res.isEmpty());
//    }
    
//    @Test
//    public void testAnnotateIsoforms(){
//        System.out.println("annotateIsoforms");
//        
//        BlastAnnotation blast = new BlastAnnotation();
//        blast.getBlastConfig().setBlastUrl( BLAST_LOCAL );
//        blast.getBlastConfig().setDb( BLAST_DB );
//        blast.getBlastConfig().setLocalBlast(true);
//        blast.setGoManager(goManager);
//        blast.setLifeData( new LinkedLifeData() );
//        
//        GeneIndex geneidx = new GeneIndex(new File(pathGtf1));
//        
//        List<Isoform> list = new ArrayList<Isoform>();
//        list.addAll(geneidx.getIsoforms());
//        list = list.subList(1, 2);
//        
//        blast.annotateIsoforms(list, pathHg18);
//        
//        assertNotNull( list.get(0).getIsoAnnotation() );
//        assertTrue( !list.get(0).getIsoAnnotation().getGoTerms().isEmpty() );
//        assertTrue( list.get(0).getIsoAnnotation().getGoTerms().get(0).startsWith("GO:") );
//    }
    
}
