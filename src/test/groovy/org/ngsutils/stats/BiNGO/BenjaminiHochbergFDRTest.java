/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.stats.BiNGO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
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
public class BenjaminiHochbergFDRTest {
    
    public BenjaminiHochbergFDRTest() {
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
    public void calculate() throws IOException {
        //load data
        InputStream istr = ClassLoader.getSystemResourceAsStream("hedenfalk.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(istr) );
        
        String line;
        ArrayList<double []> values = new ArrayList<double []>();
        
        while( (line=reader.readLine())!=null ){
            String [] toks = line.split("\\s");
            values.add( new double [] {Double.parseDouble(toks[0]), Double.parseDouble(toks[1])} );
        }
        
        reader.close();
        
        HashMap<Integer,Double> golabelstopvalues = new HashMap<Integer,Double>();
        
        for(int i=0; i<values.size(); i++){
            golabelstopvalues.put(i, values.get(i)[0]);
        }
        
        BenjaminiHochbergFDR instance = new BenjaminiHochbergFDR(golabelstopvalues, 0.05);
        instance.calculate();
        
        Map<Integer,Double> corrMap = instance.getCorrectionMap();
        for(int i=0; i<values.size(); i++){
            int a = (int) (Math.random()*values.size());
            int b = (int) (Math.random()*values.size());
            
            assertTrue( (values.get(a)[0]>=values.get(b)[0]) ? 
                    corrMap.get(a)>=corrMap.get(b) : corrMap.get(a)<=corrMap.get(b)
                    );
            
            assertEquals(corrMap.get(i), instance.getAdjustedPValue(i) );
        }
    }
    
}