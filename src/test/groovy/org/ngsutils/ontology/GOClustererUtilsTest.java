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
        String base = "/home/victor/Escritorio/Genotipado_Alternativo/colocalizacion_20180211";
        workDir = base + "/go_distances/";
        enrichrOut = base + "/output_GSE50588/output_enrichr_100K.txt";        
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
//        double [] pvalues = {0.02, 0.015, 0.01};
//        
//        GOClustererUtils utils = new GOClustererUtils();
//        utils.setWorkDir(GOClustererUtilsTest.workDir);
//        utils.setFeatures(GOClusterer.readTSVGenesGroups(enrichrOut, 1, 9, false));
//        
//        utils.generateDistances(pvalues);
//    }
    
//    @Test
//    public void testGridSearch() {
//        GOClustererUtils utils = new GOClustererUtils();
//        utils.setWorkDir(GOClustererUtilsTest.workDir);
//        utils.setFeatures(GOClusterer.readTSVGenesGroups(GOClustererUtilsTest.enrichrOut, 1, 9, false));
//                
//        double [] pValues = new double [] {0.01};  // {0.015, 0.0125, 0.01};
//        
//        HashMap<String, String []> parameters = new HashMap<String, String []>();
//        parameters.put("-C", new String [] {"3","4","5","6","7"});
//        parameters.put("-lambda", new String [] {"0.05","0.1","0.25","0.5","1","2","5","10","15","25"});
//        parameters.put("-epsilon", new String [] {"1e-4"});
//        
//        utils.gridSearch(pValues, "BP", parameters, 20, false);
//        
//        for(double pval : pValues) {
//            System.out.println("\nBest for pvalue " + pval + ":");
//            utils.printTop(pval, 20);
//        }
//    }
}
