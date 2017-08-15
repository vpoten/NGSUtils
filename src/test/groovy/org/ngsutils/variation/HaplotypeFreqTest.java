/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.variation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class HaplotypeFreqTest {
    
    public HaplotypeFreqTest() {
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

    static boolean equalDouble(double a, double b){
        return Math.abs(a-b)<1e-3;
    }
    
    @Test
    public void test1() {
        String tpedFile = "/home/victor/Escritorio/tests_ngsengine/1000g_snps_chr1.tped";
        ArrayList<String> snps = new ArrayList<String>();
        String snp1 = "rs6676197";
        String snp2 = "rs11166560";
        String snp3 = "rs7533072";
        String snp4 = "rs4907906";
        
        snps.add(snp1);
        snps.add(snp2);
        snps.add(snp3);
        snps.add(snp4);
        
        Map<String,SNPData> mapSnps = SNPData.createFromTped(tpedFile, snps);
        
        //case 1
        HaplotypeFreq instance = new HaplotypeFreq(mapSnps.get(snp1), mapSnps.get(snp2));
        
        instance.em();
        ///mapSnps.get(snp1).getFreqA1();
        ///mapSnps.get(snp2).getFreqA1();
        Map freqs = instance.haploFreqMap();
     
        assertTrue( equalDouble((Double)freqs.get("GC"), 0.376) );
        
        //case 2
        instance = new HaplotypeFreq(mapSnps.get(snp3), mapSnps.get(snp4));
        instance.em();
        freqs = instance.haploFreqMap();
        assertTrue( equalDouble((Double)freqs.get("AC"), 0.353) );
        assertTrue( equalDouble((Double)freqs.get("GC"), 0.224) );
        
        //case 3
        instance = new HaplotypeFreq(mapSnps.get(snp2), mapSnps.get(snp4));
        instance.em();
        freqs = instance.haploFreqMap();
        assertTrue( equalDouble((Double)freqs.get("TT"), 0.148) );
        assertTrue( equalDouble((Double)freqs.get("CC"), 0.360) );
    }
}
