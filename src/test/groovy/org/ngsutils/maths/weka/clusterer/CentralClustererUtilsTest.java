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
public class CentralClustererUtilsTest {
    
    public CentralClustererUtilsTest() {
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
    public void runClusterer() {
        String file = "/home/victor/software/weka-3-6-9/data/iris.2D.arff";
        //String file = "/home/victor/software/weka-3-6-9/data/iris.arff";
        
        String [] options = new String [] {"-C","3","-lambda","10","-K","1","-stdev","0.5"};
        //String [] options = new String [] {"-C","3","-lambda","10","-gamma","0.1","-K","1"};
        
        CentralClustererUtils.runClusterer(file, true, CentralClustererUtils.getCLUST_KFCM(), options, true);
    }
}
