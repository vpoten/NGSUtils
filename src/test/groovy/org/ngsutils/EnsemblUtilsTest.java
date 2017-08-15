/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils;

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
public class EnsemblUtilsTest {
    
    public EnsemblUtilsTest() {
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
    public void downTranscriptSeq() {
        assertTrue( EnsemblUtils.downTranscriptSeq("ENSG00000107959","ENST00000451104", new File("/home/victor/Escritorio")) );
    }
    
}