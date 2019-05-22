/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.maths.weka.clusterer;

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
public class SilhouetteTest {
    
    public SilhouetteTest() {
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
//    public void testSilhouette() {
//        String file = "/home/victor/software/weka-3-6-9/data/iris.2D.arff";
//        
//        // create a 'good' cluster
//        String [] options = new String [] {"-C","3","-lambda","10","-gamma","0.01","-epsilon","0.001"};
//        AbstractFuzzyClusterer clusterer =
//                (AbstractFuzzyClusterer) CentralClustererUtils.runClusterer(file, true, CentralClustererUtils.getCLUST_KPCM(), options, false);
//        assertNotNull(clusterer);
//        
//        Silhouette scc1 = new Silhouette(clusterer);
//                
//        // create a 'bad' cluster
//        options = new String [] {"-C","3","-lambda","8","-gamma","0.01","-epsilon","0.001"};
//        clusterer =
//                (AbstractFuzzyClusterer) CentralClustererUtils.runClusterer(file, true, CentralClustererUtils.getCLUST_KPCM(), options, false);
//        
//        Silhouette scc2 = new Silhouette(clusterer);
//        
//        // scc1 should be better than scc2
//        assertTrue(scc1.getOverall() > scc2.getOverall());
//        
//        // create another 'bad' cluster
//        options = new String [] {"-C","3","-lambda","2","-gamma","0.01","-epsilon","0.001"};
//        clusterer =
//                (AbstractFuzzyClusterer) CentralClustererUtils.runClusterer(file, true, CentralClustererUtils.getCLUST_KPCM(), options, false);
//        
//        Silhouette scc3 = new Silhouette(clusterer);
//        
//        // scc1 should be better than scc3
//        assertTrue(scc1.getOverall() > scc3.getOverall());
//    }
}
