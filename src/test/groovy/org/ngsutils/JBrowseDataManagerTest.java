/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils;

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
public class JBrowseDataManagerTest {

    public JBrowseDataManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        JBrowseDataManager.config("/home/victor/work_bio/scripts/jbrowse/bin");
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

    @Test
    public void testGetVersion() {
        String vers = "1";
        String res = JBrowseDataManager.getVersion(JBrowseDataManager.JBROWSE_VERSION_HEADER+" "+vers);
        assertEquals( res, vers);
    }

    @Test
    public void testBuildOptions() {
        HashMap<String,String> map = new  HashMap<String,String>();
        String opt=(String) JBrowseDataManager.buildOptions(map);
        assertTrue(opt.isEmpty());
    }
    
   
//    @Test
//    public void testPrepareWigTrack() {
//
//        String file = "/home/victor/work_bio/wig_example/FP4output_triangle_standard_chr1_filtered_sample.wig";
//        String label = "FP4output_triangle_standard_chr1_filtered_sample.wig";
//        String outDir = "/home/victor/work_bio/wig_example/data/";
//
//        boolean res = JBrowseDataManager.prepareWigTrack(file, outDir, "tiles", label);
//
//        assertTrue(res);
//    }

}