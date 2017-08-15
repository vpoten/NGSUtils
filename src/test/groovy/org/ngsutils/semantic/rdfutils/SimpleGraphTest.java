/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.rdfutils;

import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
/**
 *
 * @author victor
 */
public class SimpleGraphTest {
    
    public static final String LOCAL_SERVER = "http://localhost:8080/openrdf-sesame";
    public static final String LLD_REPOS_ID = "test";
    
    public static final String GOGENE_QUERY = "PREFIX psys: <http://proton.semanticweb.org/2006/05/protons#> "+
                            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
                            "PREFIX gene: <http://linkedlifedata.com/resource/entrezgene/> "+
                            "SELECT ?gene ?go "+
                            "WHERE { "+
                                "?gene rdf:type gene:Gene; "+
                                "gene:geneSymbol \"TP53\" . "+
                                "?gene gene:goTerm ?go . "+
                            "}";
    
            
    public SimpleGraphTest() {
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
    public void testSomething() throws IOException {
        //a test of graph operations
		
        SimpleGraph g = new SimpleGraph();

        //load a RDF foaf example from resources
        InputStream istr = this.getClass().getClassLoader().getSystemResourceAsStream("foaf_example.xml");
        g.addInputStream( istr, RDFFormat.RDFXML, null);
        istr.close();

        //manually add a triple/statement with a URIref object
        URI s1 = g.URIref("http://semprog.com/people/toby");
        URI p1 = g.URIref(SimpleGraph.RDFTYPE);
        URI o1 = g.URIref("http://xmlns.com/foaf/0.1/person");
        g.add(s1, p1, o1);

        //manually add with an object literal
        URI s2 = g.URIref("http://semprog.com/people/toby");
        URI p2 = g.URIref("http://xmlns.com/foaf/0.1/nick");
        Value o2 = g.Literal("kiwitobes");
        g.add(s2, p2, o2);

        //parse a string of RDF and add to the graph
        String rdfstring = "<http://semprog.com/people/jamie> <http://xmlns.com/foaf/0.1/nick> \"jt\" .";
        g.addString(rdfstring, SimpleGraph.NTRIPLES);

        //System.out.println("\n==TUPLE QUERY==\n");
        List rlist = g.tuplePattern(null, g.URIref("http://xmlns.com/foaf/0.1/nick"), null);
        assertFalse(rlist.isEmpty());
        //System.out.print(rlist.toString());
        

        //dump the graph in the specified format
        //System.out.println("\n==GRAPH DUMP==\n");
        //g.dumpRDF(System.out, SimpleGraph.NTRIPLES);

        //run a SPARQL query - get back solution bindings
        //System.out.println("\n==SPARQL SELECT==\n");
        List solutions = g.runSPARQL("SELECT ?who ?nick " +
                        "WHERE { " +
                                "?x <http://xmlns.com/foaf/0.1/knows> ?y . " +
                                "?x <http://xmlns.com/foaf/0.1/nick> ?who ." +
                                "?y <http://xmlns.com/foaf/0.1/nick> ?nick ."   +
                        "}");
        
        assertFalse(solutions.isEmpty());
        //System.out.println("SPARQL solutions: " + solutions.toString());

        //run a CONSTUCT SPARQL query 
        //System.out.println("\n==SPARQL CONSTRUCT==\n");
        String newgraphxml = g.runSPARQL("CONSTRUCT { ?x <http://semprog.com/simple#friend> ?nick . } " +
                        "WHERE { " +
                                "?x <http://xmlns.com/foaf/0.1/knows> ?y . " +
                                "?x <http://xmlns.com/foaf/0.1/nick> ?who ." +
                                "?y <http://xmlns.com/foaf/0.1/nick> ?nick ."   +
                        "}", SimpleGraph.RDFXML);
        assertFalse(newgraphxml.isEmpty());
        //System.out.println("SPARQL solutions: \n" + newgraphxml);
		
	//run a CONSTUCT SPARQL query 
	//System.out.println("\n==SPARQL DESCRIBE==\n");
	String describexml = g.runSPARQL("DESCRIBE ?x  " +
			"WHERE { " +
				"?x <http://xmlns.com/foaf/0.1/knows> ?y . " +
				"?x <http://xmlns.com/foaf/0.1/nick> ?who ." +
				"?y <http://xmlns.com/foaf/0.1/nick> ?nick ."   +
			"}", SimpleGraph.N3);
        assertFalse(describexml.isEmpty());
	//System.out.println("SPARQL solutions: \n" + describexml);
    }
    
    // TODO uncomment openrdf semantic test
    /**
     * enable this method if a local LLD repository is running
     */
//    @Test
//    public void testHttpRepository() {
//        System.out.println("testHttpRepository");
//        
//        SimpleGraph g = new SimpleGraph( SimpleGraph.HTTPRepository(LOCAL_SERVER, LLD_REPOS_ID) );
//        
//        List solutions = g.runSPARQL( GOGENE_QUERY );
//        
//        assertTrue(!solutions.isEmpty());
//        
//        g.shutDown();
//    }
    
}
