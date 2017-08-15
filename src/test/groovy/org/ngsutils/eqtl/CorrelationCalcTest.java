/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.eqtl;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeSet;
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
public class CorrelationCalcTest {
    
    public CorrelationCalcTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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
    public void calcCorrelations() {
       
        CorrelationCalc instance = new CorrelationCalc();
        
        //create subjects, snps and isoforms ids
        ArrayList subjects = new ArrayList();
        ArrayList snps = new ArrayList();
        ArrayList isoforms = new ArrayList();
        int numSnps = 5;
        
        for(int j=0; j<numSnps; j++) {
            snps.add("snp"+j);
            isoforms.add("iso"+j);
        }
        
        instance.setSubjects(subjects);
        instance.setSnps(snps);
        instance.setIsoforms(isoforms);
        
        //add genotypes and expression values
        double [][] values = {{106, 7}, {86, 0}, {100, 28}, {100, 50}, {99, 28},
            {103, 28}, {97, 20}, {113, 12}, {113, 7}, {110, 17} };
        
        for( int i=0; i<values.length; i++){
            String sbjId = "sbj"+i;
            subjects.add(sbjId);
            HashMap geno = new HashMap();
            HashMap expr = new HashMap();
                
            for(int j=0; j<numSnps; j++) {
                geno.put("snp"+j, values[i][0]);
                expr.put("iso"+j, values[i][1]);
            }
            
            instance.getGenotypes().put(sbjId, geno);
            instance.getExpression().put(sbjId, expr);
        }
        
        String sbjId = "sbj"+(values.length+1);
        subjects.add(sbjId );//add a subject without data
        instance.getGenotypes().put(sbjId, new HashMap());
        instance.getExpression().put(sbjId, new HashMap());
            
        instance.calcCorrelations();
        double expected = 0.18787878;
        
        for(int j=0; j<numSnps; j++) {
            String snp = "snp"+j;
            String iso = "iso"+j;
            Double value = (Double) instance.getCorrValues(snp,iso);
            
            if( j==0 ){
                instance.printResults( System.out );
            }
            
            assertTrue( Math.abs(value-(-expected))<0.05 );
            
            instance.correlation("snp"+j, "iso"+j);
            value = (Double) instance.getCorrValues(snp,iso);
            assertTrue( Math.abs(value-(-expected))<0.05 );
        }
        
        TreeSet<String> pairSet = new TreeSet<String>();
        for(int j=0; j<numSnps; j++) {
            String snp = "snp"+j;
            String iso = "iso"+j;
            pairSet.add(snp+"_"+iso);
        }
        
        // test sparse correlation
        instance.calcSparse(pairSet);
        
        for(int j=0; j<numSnps; j++) {
            String snp = "snp"+j;
            String iso = "iso"+j;
            Double value = (Double) instance.getSparseResults().get(snp+"_"+iso).getCorrelation();
            
            if( j==0 ){
                instance.printResults( System.out );
            }
            
            assertTrue( Math.abs(value-(-expected))<0.05 );
        }
        
    }
    
}
