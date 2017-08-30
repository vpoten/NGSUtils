/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils;

import java.util.ArrayList;
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
public class GOClustererFactoryTest {
    
    public GOClustererFactoryTest() {
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
    public void doGOClusterer() {
        String workDir = "/home/victor/Escritorio/tests_ngsengine/go_clusterer/";
        ArrayList<String> data = new ArrayList<String>();
        
        // add some genes to data
        data.add("SP110");
        data.add("HLA-F");
        data.add("STAT5A");
        data.add("MMP10");
        data.add("HLA-DQB1");
        data.add("HLA-DQA1");
        data.add("SYK");
        data.add("GRHL3");
        data.add("TYMP");
        data.add("CD86");
        data.add("ODF3B");
        data.add("SLC15A3");
        data.add("MAF");
        data.add("ACP5");
        data.add("CEACAM19");
        
        GOClustererFactory.doGOCluster(data, workDir, "9606");
    }
}
