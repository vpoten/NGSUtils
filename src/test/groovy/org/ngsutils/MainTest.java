/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils;

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
public class MainTest {

    public MainTest() {
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

//    /**
//     * Test of main method, of class Main.
//     */
//    @Test
//    public void testMain() {
//        System.out.println("main:goCluster");
//        String[] args = new String [7];
//
//        //OPT_GO_CLUSTER <MF|CC|BP> <cutval> <obo_file> <goa file> <diffgo_file> --out=<out dir>
//        args[0]="goCluster";
//        args[1]="BP";
//        args[2]="over";
//        args[3]="/home/victor/work_bio/annotation/gene_ontology_ext.obo";
//        args[4]="/home/victor/work_bio/annotation/9606/gene_association.goa";
//        args[5]="/home/victor/Escritorio/0_1_gene_exp.diff.goa.gz";
//        //args[5]="/home/victor/Escritorio/0_1_splicing.diff.goa.gz";
//        args[6]="--out=/home/victor/Escritorio";
//
//        Main.main(args);
//
//        assertTrue( true );
//    }

//    @Test
//    public void testMainDiffTool() {
//        System.out.println("main:diffTool");
//        String[] args = new String [5];
//
//        //OPT_DIFF_TOOL <diffOutDir> <nsamples> <anotBaseDir> <fileFeat>
//        args[0]="diffTool";
//        args[1]="/home/victor/Escritorio/tests_ngsengine/cuffdiff_refseqcomp/";
//        args[2]="2";
//        args[3]="/home/victor/work_bio/annotation/9606/";
//        args[4]="/home/victor/Escritorio/tests_ngsengine/cuffdiff_refseqcomp/cuffcompare.gtf.gz";
//
//        Main.main(args);
//
//        assertTrue( true );
//    }

}