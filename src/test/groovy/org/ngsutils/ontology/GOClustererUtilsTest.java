/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.ontology;

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
public class GOClustererUtilsTest {
    
    public GOClustererUtilsTest() {
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
    public void testGenerateSimilarities() {
        String workDir = "/home/victor/Escritorio/Genotipado_Alternativo/colocalizacion/go_distances/";
        String enrichrOut =
                "/home/victor/Escritorio/Genotipado_Alternativo/colocalizacion/output_GSE50588/output_enrichr_100K.txt";
        double [] pvalues = {0.02, 0.015, 0.01};
        
        GOClustererUtils utils = new GOClustererUtils();
        utils.setWorkDir(workDir);
        utils.setFeatures(GOClusterer.readTSVGenesGroups(enrichrOut, 1, 9, false));
        
        utils.generateDistances(pvalues);
    }
}
