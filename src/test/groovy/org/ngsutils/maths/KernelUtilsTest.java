/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths;


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
public class KernelUtilsTest {
    
    public KernelUtilsTest() {
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
    public void testCheckMercerKernel() {
        double [][] points = new double [8][];
        points[0] = new double [] {0,0,0};
        points[1] = new double [] {1,0,0};
        points[2] = new double [] {0,1,0};
        points[3] = new double [] {0,0,1};
        points[4] = new double [] {0,1,1};
        points[5] = new double [] {1,1,0};
        points[6] = new double [] {1,0,1};
        points[7] = new double [] {1,1,1};
        
        Matrix distances = new Matrix(points.length, points.length);
        
        for(int i=0; i<points.length; i++){
            for(int j=0; j<points.length; j++){
                double dist = 0.0;
                for(int k=0; k<3; k++){ 
                    double val = points[i][k]-points[j][k];
                    dist+= val*val; 
                }
                
                distances.set(i, j, Math.sqrt(dist));
            }
        }
        
        //distances is an squared euclidean distance matrix
        assertFalse( KernelUtils.checkMercerKernel(distances) );
        
        Matrix K = KernelUtils.intoMercerKernel(distances);
        assertTrue( KernelUtils.checkMercerKernel(K) );
        
        Matrix K2 = KernelUtils.intoGaussianKernel(distances, 0.5);
        assertTrue( KernelUtils.checkMercerKernel(K2) );
    }
    
}
