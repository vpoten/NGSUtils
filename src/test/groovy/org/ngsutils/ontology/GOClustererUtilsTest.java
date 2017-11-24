/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.ontology;

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
public class GOClustererUtilsTest {
    static String workDir;
    static String enrichrOut;
    
    public GOClustererUtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        workDir = "/home/victor/Escritorio/Genotipado_Alternativo/colocalizacion/go_distances/";
        enrichrOut = "/home/victor/Escritorio/Genotipado_Alternativo/colocalizacion/output_GSE50588/output_enrichr_100K.txt";        
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

//    @Test
//    public void testGenerateSimilarities() {
//        String enrichrOut =
//                "/home/victor/Escritorio/Genotipado_Alternativo/colocalizacion/output_GSE50588/output_enrichr_100K.txt";
//        double [] pvalues = {0.02, 0.015, 0.01};
//        
//        GOClustererUtils utils = new GOClustererUtils();
//        utils.setWorkDir(GOClustererUtilsTest.workDir);
//        utils.setFeatures(GOClusterer.readTSVGenesGroups(enrichrOut, 1, 9, false));
//        
//        utils.generateDistances(pvalues);
//    }
    
    @Test
    public void testGridSearch() {
        GOClustererUtils utils = new GOClustererUtils();
        utils.setWorkDir(GOClustererUtilsTest.workDir);
        utils.setFeatures(GOClusterer.readTSVGenesGroups(GOClustererUtilsTest.enrichrOut, 1, 9, false));
                
        double [] pValues = new double [] {0.015, 0.0125, 0.01};
        
        HashMap<String, String []> parameters = new HashMap<String, String []>();
        parameters.put("-C", new String [] {"3","5","7","10"}); // {"3","4","5","6","7","8","9","10"});
        parameters.put("-lambda", new String [] {"0.5","1","2","5","10","15"});
        parameters.put("-epsilon", new String [] {"1e-3"});
        
        utils.gridSearch(pValues, "BP", parameters, 5);
        
        for(double pval : pValues) {
            System.out.println("\nBest for pvalue " + pval + ":");
            utils.printTop(pval, 5);
        }
    }
}
