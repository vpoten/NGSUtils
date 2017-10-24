/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.ontology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ngsutils.fuzzy.FMBSimilarity;
import org.ngsutils.semantic.LinkedLifeDataFactory;
import org.ngsutils.semantic.rdfutils.SimpleGraph;

/**
 *
 * @author victor
 */
public class GOManagerTest {
    
    public static String obo_file="/home/victor/work_bio/annotation/go-basic.obo";
    static String trig_file="/home/victor/work_bio/annotation/geneontology.tar.gz";
    public static String workdir="/home/victor/work_bio/annotation/";
    public static String probFile="/home/victor/work_bio/annotation/9606/gene_association.goa.gz";
    public static GOManager instance = null;
    
    public GOManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        instance = new GOManager( trig_file );
        
//        ArrayList<String> taxList = new ArrayList<String>();
//        taxList.add("9606");
//        SimpleGraph graph = LinkedLifeDataFactory.loadRepository(
//                ((List)LinkedLifeDataFactory.getLIST_BASIC_GO()), taxList, workdir
//                );
//        instance = new GOManager( graph );
        
        instance.calculateProbTerms(probFile);
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
    public void testSomeMethod() throws Exception {
        System.out.println("getAncestors");

        Set ances=(Set) instance.getAncestors("GO:0008150");
        assertTrue( ances.isEmpty());


        ances=(Set) instance.getAncestors("GO:0000001");
        assertTrue( ances.size()>0 );

        ances=instance.getCommonAncestors( new String [] {"GO:0000001", "GO:0008150"});
        assertTrue( ances.size()==1 );

        ances=instance.getCommonAncestors( new String [] {"GO:0000001", "GO:0048308"});
        assertTrue( ances.size()>1 );

        instance.simLin("GO:0005515", "GO:0005515");
    }
   
    @Test
    public void testGetCommonAncestors() throws Exception {
        System.out.println("getCommonAncestors");

        Set<String> terms=new HashSet<String>();
        terms.add("GO:0008150");
        terms.add("GO:0000001");
        terms.add("GO:0048308");

        Set ances=instance.getCommonAncestors( terms );

        assertTrue( ances.size()==1 );
    }
    
    @Test
    public void testGetDirectAncestors() throws Exception {
        System.out.println("getDirectAncestors");

        Set ances=instance.getDirectAncestors("GO:0008150");
        assertTrue( ances.isEmpty() );
        
        ances=instance.getDirectAncestors("GO:0000001");
        assertTrue( ances.size()>=2 );
        assertTrue( ances.contains(instance.goTermUri("GO:0048308")) );
        
        ances=instance.getDirectAncestors("GO:0048308");
        assertTrue( ances.size()==1 );
        assertTrue( ances.contains(instance.goTermUri("GO:0006996")) );
        
    }
    
    @Test
    public void testGetNCAncestors() throws Exception {
        System.out.println("getNCAncestor");

        Object ances=instance.getNCAncestor("GO:0008150","GO:0005575");
        assertNull( ances );
        
        ances = instance.getNCAncestor("GO:0000011","GO:0032543");
        assertNotNull(ances);
        assertTrue( ances.toString().endsWith("GO:0006996") );
        
        ances = instance.getNCAncestor("GO:0000011","GO:0007033");
        assertNotNull(ances);
        assertTrue( ances.toString().endsWith("GO:0007033") );
        
        ances = instance.getNCAncestor("GO:0007033","GO:0000011");
        assertNotNull(ances);
        assertTrue( ances.toString().endsWith("GO:0007033") );
        
    }
            
    
    @Test
    public void testGetOffspring() throws Exception {
        System.out.println("getOffspring");
        
        Set set=instance.getOffspring("GO:0006996");
        assertTrue( set.size()>20 );
        assertTrue( set.contains(instance.goTermUri("GO:0048308")) );
        assertTrue( set.contains(instance.goTermUri("GO:0007029")) );
        assertTrue( set.contains(instance.goTermUri("GO:0006997")) );
        
        set=instance.getAllOffspring("GO:0006996");
        assertTrue( set.size()>40 );
        
        set=instance.getOffspring("GO:0051469");
        assertTrue( set.isEmpty() );
    }

    @Test
    public void testOrderTermsByIC() throws Exception {
        System.out.println("orderTermsByIC");

        Set terms=new HashSet();
        terms.add( "GO:0008150" );
        terms.add( "GO:0000001" );
        terms.add( "GO:0048308" );

        double i1=instance.getIcontent( "GO:0008150" );
        double i2=instance.getIcontent( "GO:0000001" );
        
        i1=instance.getIcontent( "GO:0006681" );
        i2=instance.getIcontent( "GO:0046037" );

        List<String> ances=instance.orderTermsByIC( terms );

        assertTrue( ances.get(0).contains("GO:0048308") );
    }
    
    
    @Test
    public void testGetNotations(){
        System.out.println("getNotations");
        
        List result = (List)instance.getNotations("GO:0005515");
        assertTrue(result.size()==1);
        
        result = (List)instance.getNotations("GO:0018776");
        assertTrue(result.size()==2);
        
        result = (List)instance.getNotations("GO:0018738");
        assertTrue(result.size()==5);
    }        
            
    
    @Test
    public void testGetNamespace(){
        System.out.println("getNamespace");
        
        String result = (String) instance.getNamespace("GO:0005515");
        assertEquals(result, GOManager.MF );
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testWebQuery(){
        GOManager inst = new GOManager();
        
        // TODO LinkedLifeData has been changed
        String result = (String) inst.getNamespace("GO:0005515");
        assertEquals(result, instance.MF );
        
        Set ances=(Set)inst.getCommonAncestors( new String [] {"GO:0000001", "GO:0008150"});
        assertTrue( ances.size()==1 );
    }
    
    @Test
    public void testSetEvidenceCodes(){
        System.out.println("setEvidenceCodes");
        
        List<String> result = instance.getECode("RAC2","GO:0003924");
        assertEquals(result.get(0), "IEA");
        
        result = instance.getECode("EIF4G1","GO:0005515");
        assertEquals(result.get(0), "IPI");
    }
    
    @Test
    public void testGetAncestorLevels() throws Exception {
        System.out.println("getAncestorLevels");
        
        List levels = instance.getAncestorLevels("GO:0000001");
        assertEquals(levels.size(), 6);
        
        levels = instance.getAncestorLevels("GO:0008150");
        assertTrue(levels.isEmpty());
    }
    
//    @Test
//    public void testFMBSimilarity(){
//        System.out.println("FMBSimilarity");
//        
//        instance.setEcodeFactors(GOEvidenceCodes.getEcodeFactorsSet1());
//        FMBSimilarity fmbs = new FMBSimilarity();
//        fmbs.setOntologyWrap(instance);
//    }

}