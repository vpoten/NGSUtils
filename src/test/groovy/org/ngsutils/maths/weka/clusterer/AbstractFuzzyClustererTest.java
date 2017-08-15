/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths.weka.clusterer;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.core.matrix.Matrix;

/**
 *
 * @author victor
 */
public class AbstractFuzzyClustererTest {
    
    public AbstractFuzzyClustererTest() {
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

    @Test
    public void product() {
        Matrix m = Matrix.identity(4, 4);
        m = m.times(2.0);
        
        assertTrue( areEqual(m.get(2,2), 2.0) );
        assertTrue( areEqual(m.get(2,0), 0.0) );
        assertTrue( areEqual(m.trace(), 8.0) );
        
        Matrix b = Matrix.identity(4,4);
        Matrix c = new Matrix(4,4);
        
        AbstractFuzzyClusterer.product(m, b, c);
        assertTrue( AbstractFuzzyClusterer.equals(m, c) );
        assertFalse( AbstractFuzzyClusterer.equals(b, c) );
        
        double[][] vals = {{-2,-4},{-1,1},{-3,5}};
        Matrix A = new Matrix(vals);
        
        double [][]vals2 = {{2,6,-2},{3,4,-1}};
        Matrix B = new Matrix(vals2);
        
        double [][]vals3 = {{-16,-28,8},{1,-2,1},{9,2,1}};
        Matrix C = new Matrix(vals3);
        
        c = new Matrix(3,3);
        AbstractFuzzyClusterer.product(A, B, c);
        assertTrue( AbstractFuzzyClusterer.equals(C, c) );
        assertFalse( AbstractFuzzyClusterer.equals(A,B) );
        
        assertTrue( areEqual(AbstractFuzzyClusterer.rowSum(C,1),0) );
        assertTrue( areEqual(AbstractFuzzyClusterer.rowSum(A,2),2) );
        
        double [][]vals4 = {{2,3},{6,4},{-2,-1}};
        Matrix Bt = new Matrix(vals4);
        
        c = new Matrix(3,3);
        AbstractFuzzyClusterer.equals(B, Bt.transpose());
        AbstractFuzzyClusterer.productByTrans(A, Bt, c);
        assertTrue( AbstractFuzzyClusterer.equals(C, c) );
    }
}
