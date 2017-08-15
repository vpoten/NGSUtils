/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.biojava;

import java.io.BufferedReader;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ngsutils.Utils;
import static org.junit.Assert.*;

/**
 *
 * @author victor
 */
public class GtfDocumentHandlerConvertTest {

    public GtfDocumentHandlerConvertTest() {
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

//    @Test
//    public void testConvertToGff3() throws Exception {
//
//        String gtfFile="/home/victor/Escritorio/cuffcompare_15.combined.gtf.gz";
//        String gff3File="/home/victor/Escritorio/cuffcompare_15.gff3";
//
//        BufferedReader reader=Utils.createReader(gtfFile);
//
//        GtfDocumentHandlerConvert handler = new GtfDocumentHandlerConvert();
//        handler.setOutFile( gff3File );
//
//        GFFParserLight parser = new GFFParserLight();
//        parser.parse(reader,handler);
//                
//        assertTrue(true);
//    }

}