/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.ontology;

import java.util.ArrayList;
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
public class GOClustererTest {
    
    public GOClustererTest() {
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

//    @Test
//    public void doGOClusterer() {
//        String workDir = "/home/victor/Escritorio/tests_ngsengine/go_clusterer/";
//        ArrayList<String> data = new ArrayList<String>();
//        ArrayList<String> namespaces = new ArrayList<String>();
//        
//        // add some genes to data
//        data.add("SP110");
//        data.add("HLA-F");
//        data.add("STAT5A");
//        data.add("MMP10");
//        data.add("HLA-DQB1");
//        data.add("HLA-DQA1");
//        data.add("SYK");
//        data.add("GRHL3");
//        data.add("TYMP");
//        data.add("CD86");
//        data.add("ODF3B");
//        data.add("SLC15A3");
//        data.add("MAF");
//        data.add("ACP5");
//        data.add("CEACAM19");
//        
//        namespaces.add("molecular_function");
//        namespaces.add("biological_process");
//        
//        //String [] options = new String [] {"-C","3","-lambda","1","-gamma","2","-K","0","-stdev","1"};
//        GOClusterer clusterer = new GOClusterer(workDir, "9606", data, namespaces);
//        assertNotNull(clusterer.getDistances());
//        //clusterer.runClusterer(null);
//
//        HashMap<String, String []> parameters = new HashMap<String, String []>();
//        parameters.put("-C", new String [] {"3","4","5"});
//        parameters.put("-lambda", new String [] {"1","2","5"});
//        parameters.put("-epsilon", new String [] {"1e-3"});
//        GOClusterer.gridSearch(clusterer, parameters, 3, true);
//    }
    
    @Test
    public void doGOClusterer2() {
        String workDir = "/home/victor/Escritorio/tests_ngsengine/go_clusterer/";
        ArrayList<String> namespaces = new ArrayList<String>();
        
        namespaces.add("molecular_function");
        namespaces.add("biological_process");
        
        String enrichrOut =
                "/home/victor/Escritorio/Genotipado_Alternativo/colocalizacion/output_GSE50588/output_enrichr_100K.txt";
        GOClusterer clusterer = new GOClusterer(workDir, "9606",
                GOClusterer.readTSVGenesGroups(enrichrOut, 1, 9, false), namespaces);
        assertNotNull(clusterer.getDistances());
        clusterer.runClusterer(null);

//        HashMap<String, String []> parameters = new HashMap<String, String []>();
//        parameters.put("-C", new String [] {"3","4","5"});
//        parameters.put("-lambda", new String [] {"1","2","5"});
//        parameters.put("-epsilon", new String [] {"1e-3"});
//        GOClusterer.gridSearch(clusterer, parameters, 3, true);
    }
}
