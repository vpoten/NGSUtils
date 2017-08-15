/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.rdfutils;

import java.io.ByteArrayInputStream;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.repository.sail.SailRepository;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import static org.junit.Assert.*;

/**
 *
 * @author victor
 */
public class RDFLoadTest {
    
    public RDFLoadTest() {
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
    public void testGetFormat() {
        RDFLoad inst = new RDFLoad();
        assertEquals(inst.getFormat("file.trig"), RDFFormat.TRIG);
        assertEquals(inst.getFormat("file.nt"), RDFFormat.NTRIPLES);
        assertEquals(inst.getFormat("file.n3"), RDFFormat.N3);
        assertEquals(inst.getFormat("file.ttl"), RDFFormat.TURTLE);
    }
    
    @Test
    public void testAddRdf() throws RepositoryException {
        RDFLoad inst = new RDFLoad();
        inst.setBaseURI("http://example.com/test");
        inst.setContext("file://test_context");
        
        String str = "<http://www.w3.org/2001/08/rdf-test/> <http://purl.org/dc/elements/1.1/creator> \"Dave Beckett\" .\n"+
                    "<http://www.w3.org/2001/08/rdf-test/> <http://purl.org/dc/elements/1.1/creator> \"Jan Grant\" .\n"+
                    "<http://www.w3.org/2001/08/rdf-test/> <http://purl.org/dc/elements/1.1/publisher>  _:a .\n"+
                    "_:a <http://purl.org/dc/elements/1.1/title> \"World Wide Web Consortium\" .\n"+
                    "_:a <http://purl.org/dc/elements/1.1/source> <http://www.w3.org/> .\n";

        SailRepository repos = new SailRepository(new MemoryStore());
        repos.initialize();

        inst.addRdf( new ByteArrayInputStream(str.getBytes()), repos, RDFFormat.NTRIPLES );
        
        repos.shutDown();
    }
    
}
