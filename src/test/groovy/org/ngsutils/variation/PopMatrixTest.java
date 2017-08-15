/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.variation;

import org.ngsutils.variation.PairValueVector;
import java.util.List;
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
public class PopMatrixTest {
    
    public PopMatrixTest() {
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
    
    private boolean areEqual(double a, double b){
        double ZERO = 1e-9;
        return Math.abs(a-b)<ZERO;
    }
    
    private boolean areEqual2(double a, double b){
        double diff = 0.01;
        return Math.abs(a-b)<diff;
    }
    
    @Test
    public void sum() {
        PopMatrix m = PopMatrix.identity(4);
        
        assertTrue( areEqual(m.rowSum(0), 1.0) );
        assertTrue( areEqual(m.rowSum(3), 1.0) );
        
        assertTrue( areEqual(m.colSum(0), 1.0) );
        assertTrue( areEqual(m.colSum(3), 1.0) );
        
        m.sum(1.0);
        
        assertTrue( areEqual(m.rowSum(0), 5.0) );
        assertTrue( areEqual(m.colSum(3), 5.0) );
        
        m = new PopMatrix(5,4);
        m.fill(1.0);
        
        assertTrue( areEqual(m.rowSum(0), 4.0) );
        assertTrue( areEqual(m.colSum(3), 5.0) );
        
        m = PopMatrix.identity(4);
        PopMatrix m2 = PopMatrix.identity(4);
        m.sum(m2);
        m2.product(2.0);
        
        assertTrue( m.equals(m2) );
    }
    
    @Test
    public void product() {
        PopMatrix m = PopMatrix.identity(4);
        m.product(2.0);
        
        assertTrue( areEqual(m.rowSum(0), 2.0) );
        assertTrue( areEqual(m.colSum(3), 2.0) );
        assertTrue( areEqual(m.get(2,2), 2.0) );
        assertTrue( areEqual(m.get(2,0), 0.0) );
        assertTrue( areEqual(m.trace(), 8.0) );
        
        PopMatrix b = PopMatrix.identity(4);
        PopMatrix c = m.product(b);
        
        assertTrue( m.equals(c) );
        
        PopMatrix c2 = m.productThreads(b, 3);
        
        assertTrue( m.equals(c2) );
    }
    
    @Test
    public void productTrans() {
        PopMatrix m = new PopMatrix(4,4);
        m.set(0, 0, 1.0);
        m.set(0, 1, 1.0);
        m.set(3, 2, 1.0);
        m.set(3, 3, 1.0);
        
        PopMatrix b = m.productTrans();
        
        PopMatrix b2 = m.productTransThreads(3);
        
        assertTrue( b.equals(b2) );
        
        PopMatrix b3 = m.product(m);
        assertFalse( b.equals(b3) );
        assertTrue( m.equals(b3) );
        PopMatrix b4 = m.productThreads(m, 3);
        assertTrue( m.equals(b4) );
    }
    
    @Test
    public void eigen() {
        PopMatrix m = new PopMatrix(2,2);
        m.set(0, 0, 1.9);
        m.set(0, 1, 1.1);
        m.set(1, 0, 1.1);
        m.set(1, 1, 1.1);
        
        List<PairValueVector> list = m.eigenDecomposition();
        
        assertTrue( areEqual2(list.get(0).getValue(), 2.67) );
        assertTrue( areEqual2(list.get(1).getValue(), 0.33) );
        
        double [] vector = list.get(0).getVector();
        
        assertTrue( areEqual2(vector[0], 0.82) );
        assertTrue( areEqual2(vector[1], 0.57) );
        
        // check total variance
        assertTrue( areEqual(list.get(0).getValue()+list.get(1).getValue(), 
                m.get(0,0)+m.get(1,1)) );
        
        
        // check normality
        assertTrue( areEqual(Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]), 1.0) );
        
        vector = list.get(1).getVector();
        assertTrue( areEqual(Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]), 1.0) );
    }
    
}