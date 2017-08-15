/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.diff;

import java.util.ArrayList;
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
public class IsoformExpDataTest {
    
    public IsoformExpDataTest() {
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
    public void isInRegion() {
        assertTrue( IsoformExpData.isInRegion("chr1", 1000, 4000, "chr1:1200-2000") );
        assertFalse( IsoformExpData.isInRegion("chr1", 1000, 4000, "chr2:1200-2000") );
        assertTrue( IsoformExpData.isInRegion("chr1", 1000, 4000, "chr1:1200-4100") );
        assertTrue( IsoformExpData.isInRegion("chr1", 1000, 4000, "chr1:900-3000") );
        assertFalse( IsoformExpData.isInRegion("chr1", 1000, 4000, "chr1:4100-5000") );
        assertFalse( IsoformExpData.isInRegion("chr1", 1000, 4000, "chr1:700-900") );
    }
    
    @Test
    public void parse() {
        String line = "CUFF.1.1\t-\t-\tABCD\tABCD\t-\tchr1:1000-1500\t350\t3.5\t5.0\t1.0\t11\tOK";
        String [] toks = line.split("\\s");
        
        IsoformExpData instance = IsoformExpData.parse(toks);
        
        assertEquals( instance.getId(), "CUFF.1.1" );
        assertEquals( instance.getGeneId(), "ABCD" );
        assertTrue( Math.abs(instance.getFpkm()-5.0f)<1e-12 );
        assertTrue( Math.abs(instance.getCoverage()-3.5f)<1e-12 );
        assertEquals( instance.getChr(), "chr1");
        assertEquals( instance.getStart(), 1000);
        assertEquals( instance.getEnd(), 1500);
        assertEquals( instance.getLength(), 350);
        assertEquals( instance.getChrNum(), "1");
    }
    
}
