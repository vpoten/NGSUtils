/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.loader;

import java.util.ArrayList;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ngsutils.ontology.GOManagerTest;
import org.ngsutils.semantic.query.GOQueryUtils;
import org.ngsutils.semantic.rdfutils.SimpleGraph;
import org.ngsutils.semantic.query.GeneQueryUtils;

/**
 *
 * @author victor
 */
public class EntrezgeneLoaderTest {
    
    public static String geneinfo_file="/home/victor/work_bio/annotation/Homo_sapiens.gene_info.gz";
    public static String gene2ensembl="/home/victor/work_bio/annotation/gene2ensembl.gz";
    public static String ensemblToGeneName = "/home/victor/work_bio/annotation/ensemblToGeneName.txt.gz";
    public static String uniprotIdMap="/home/victor/work_bio/annotation/9606/HUMAN_9606_idmapping.dat.gz";
    
    public EntrezgeneLoaderTest() {
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
    public void parseGeneInfo() {
        EntrezgeneLoader instance = new EntrezgeneLoader();
        instance.setGraph(new SimpleGraph());
        ArrayList<String> taxIds = new ArrayList<String>();
        taxIds.add("9606");
        
        instance.parseGeneInfo(geneinfo_file, taxIds);
        
        instance.parseGene2Ensembl(gene2ensembl, taxIds);
        instance.parseEnsemblToGeneName(ensemblToGeneName, taxIds.get(0));
        
        //test some queries
        GeneQueryUtils query = new GeneQueryUtils(instance.getGraph());
        
        assertEquals( query.getGeneSymbol("7389"), "UROD");
        assertEquals( query.getEnsemblGene("7389"), "ENSG00000126088");
        
        assertNotNull( query.getGeneByName("UROD") );
        assertNotNull( query.getGeneByName("NM_000374.4") );
        assertNotNull( query.getGeneBySymbol("UROD") );
        assertNotNull( query.getGeneByProtein("NP_004427.1") );
        
        assertNotNull( query.getGeneByName("ENST00000540988") );//for ensemblToGeneName
        
        assertNotNull( query.getGeneByName("AMOT") );
        
        // test uniprot data
        UniprotLoader protLoader = new UniprotLoader();
        protLoader.setGraph(instance.getGraph());
        
        protLoader.parseIdMapping(uniprotIdMap, "9606");
        
        assertEquals( query.getGeneSplicing(query.getGeneByName("AMOT")).size(), 2);
        assertEquals( query.getIsoformSplicing("Q4VCS5-1").size(), 2);
        assertEquals( query.getIsoformSplicing("Q4VCS5-2").size(), 1);
        
        // test gene ontology association
        GOLoader goLoader = new GOLoader();
        goLoader.setGraph(instance.getGraph());
        
        goLoader.parseGOA(GOManagerTest.probFile, "9606");
        
        //test some queries
        GOQueryUtils goquery = new GOQueryUtils(instance.getGraph());
        Set result = goquery.getGeneTerms("7389");
        
        assertTrue( result.contains("GO:0004853") );
        assertTrue( result.contains("GO:0006778") );
        
        Set result2 = goquery.getGeneTerms(query.getGeneByName("AMOT"));
        result = goquery.getSpliceTerms("Q4VCS5-1");
        Set result3 = goquery.getIsoformTerms("ENST00000524145");
        Set result4 = goquery.getProteinTerms("ENSP00000429013");
        
        assertEquals(result, result3);
        assertEquals(result4, result3);
        
        assertFalse( result2.contains("GO:0051496") );
        
        assertTrue( result.contains("GO:0043536") );
        assertTrue( result.contains("GO:0051496") );
    }
}