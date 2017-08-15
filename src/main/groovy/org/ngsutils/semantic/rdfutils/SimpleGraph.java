/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.rdfutils;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.*;
import org.openrdf.model.vocabulary.*;
import org.openrdf.repository.*;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.rio.*;
import org.openrdf.model.*;


import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.io.*;
import org.openrdf.repository.http.HTTPRepository;


/**
 *
 * @author victor
 */
public class SimpleGraph {
	
    Repository therepository= null; 

    //useful -local- constants
    public static RDFFormat NTRIPLES = RDFFormat.NTRIPLES;
    public static RDFFormat N3 = RDFFormat.N3;
    public static RDFFormat RDFXML = RDFFormat.RDFXML;
    public static RDFFormat TRIG = RDFFormat.TRIG;
    public static String RDFTYPE =  RDF.TYPE.toString();

    /**
     *  In memory Sesame repository without type inferencing
     */
    public SimpleGraph(){
        this(false);
    }

    /**
     * In memory Sesame Repository with optional inferencing
     * @param inferencing
     */
    public SimpleGraph(boolean inferencing){
        try {
            if (inferencing){
                therepository = new  SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));

            }else{
                therepository = new SailRepository(new MemoryStore());
            }
            therepository.initialize();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * use another repository
     * 
     * @param repository 
     */
    public SimpleGraph(Repository repository){
        therepository = repository;
    }
    
    /**
     * creates and initializes a HTTPRepository
     * 
     * @param sesameServer
     * @param repositoryID
     * @return 
     */
    public static Repository HTTPRepository(String sesameServer, String repositoryID){
        Repository myRepository = null;
        
        try{
            myRepository = new HTTPRepository(sesameServer, repositoryID);
            myRepository.initialize();
        } catch(Exception ex){
            Logger.getLogger(SimpleGraph.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        return myRepository;
    }
    
    /**
     * shut down repository
     */
    public void shutDown(){
        try {
            therepository.shutDown();
        } catch (RepositoryException ex) {
            Logger.getLogger(SimpleGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *  Literal factory
     * 
     * @param s the literal value
     * @param typeuri uri representing the type (generally xsd)
     * @return
     */
    public org.openrdf.model.Literal Literal(String s, URI typeuri){
        try {
            RepositoryConnection con = therepository.getConnection();
            try {
                ValueFactory vf = con.getValueFactory();
                if (typeuri == null) {
                    return vf.createLiteral(s);
                } else {
                    return vf.createLiteral(s, typeuri);
                }
            } finally {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Untyped Literal factory
     * 
     * @param s the literal
     * @return
     */
    public org.openrdf.model.Literal Literal(String s){
        return Literal(s, null);
    }

    /**
     *  URIref factory
     * 
     * @param uri
     * @return
     */
    public URI URIref(String uri){
        try {
            RepositoryConnection con = therepository.getConnection();
            try {
                ValueFactory vf = con.getValueFactory();
                return vf.createURI(uri);
            } finally {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *  BNode factory
     * 
     * @return
     */
    public BNode bnode(){
        try{
            RepositoryConnection con = therepository.getConnection();
            try {
                ValueFactory vf = con.getValueFactory();
                return vf.createBNode();
            } finally {
                con.close();
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     *  dump RDF graph
     * 
     * @param out output stream for the serialization
     * @param outform the RDF serialization format for the dump
     * @return
     */
    public void dumpRDF(OutputStream out, RDFFormat outform){
        try {
            RepositoryConnection con = therepository.getConnection();
            try {
                RDFWriter w = Rio.createWriter(outform, out);
                con.export(w, new Resource[0]);
            } finally {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Convenience URI import for RDF/XML sources
     * 
     * @param urlstring absolute URI of the data source
     */
    public void addURI(String urlstring){
        addURI(urlstring, RDFFormat.RDFXML, null);
    }

    /**
     *  Import data from URI source
     *  Request is made with proper HTTP ACCEPT header
     *  and will follow redirects for proper LOD source negotiation
     * 
     * @param urlstring absolute URI of the data source
     * @param format RDF format to request/parse from data source
     */
    public void addURI(String urlstring, RDFFormat format, Resource context){
        try {
            RepositoryConnection con = therepository.getConnection();
            try {
                URL url = new URL(urlstring);
                URLConnection uricon = (URLConnection) url.openConnection();
                uricon.addRequestProperty("accept", format.getDefaultMIMEType());
                InputStream instream = uricon.getInputStream();

                if( context==null )
                    con.add(instream, urlstring, format, new Resource[0]);
                else
                    con.add(instream, urlstring, format, context);
            } finally {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Import RDF data from a string
     * 
     * @param rdfstring string with RDF data
     * @param format RDF format of the string (used to select parser)
     */
    public void addString(String rdfstring,  RDFFormat format){
        addString(rdfstring, format, null);
    }
    
    public void addString(String rdfstring,  RDFFormat format, Resource context){
        try {
            RepositoryConnection con = therepository.getConnection();
            try {
                StringReader sr = new StringReader(rdfstring);
                
                if( context==null )
                    con.add(sr, "", format, new Resource[0]);
                else
                    con.add(sr, "", format, context);
            } finally {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Import RDF data from a file
     * 
     * @param location of file (/path/file) with RDF data
     * @param format RDF format of the string (used to select parser)
     */
    public void addFile(String filepath,  RDFFormat format){
        addFile(filepath,  format, null);
    }
    
    public void addFile(String filepath,  RDFFormat format, Resource context){
        try {
            RepositoryConnection con = therepository.getConnection();
            try {
                if( context==null )
                    con.add(new File(filepath), "", format, new Resource[0]);
                else
                    con.add(new File(filepath), "", format, context);
            } finally {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Import RDF data from an input stream
     * 
     * @param instr
     * @param format
     * @param context 
     */
    public void addInputStream(InputStream instr,  RDFFormat format, Resource context){
        try {
            RepositoryConnection con = therepository.getConnection();
            try {
                if( context==null )
                    con.add(instr, "", format, new Resource[0]);
                else
                    con.add(instr, "", format, context);
            } finally {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Insert Triple/Statement into graph 
     * 
     * @param s subject uriref
     * @param p predicate uriref
     * @param o value object (URIref or Literal)
     */
    public void add(Resource s, URI p, Value o){
        add(s, p, o, null);
    }
    
    public void add(Resource s, URI p, Value o, Resource context){
        try {
           RepositoryConnection con = therepository.getConnection();
           try {
                ValueFactory myFactory = con.getValueFactory();
                Statement st = myFactory.createStatement(s, p, (Value) o);
                
                if(context==null)
                    con.add(st, new Resource[0]);
                else
                    con.add(st, context);
           }finally {
              con.close();
           }
        }
        catch (Exception e) {
           // handle exception
        }
    }
    
    /**
     * Removes all statements from a specific context in the repository. 
     * 
     * @param context 
     */
    public void clear( Resource context ){
        try {
           RepositoryConnection con = therepository.getConnection();
           try {
               con.clear(context);
           }finally {
               con.close();
           }
        }
        catch (Exception e) {
           // handle exception
        }
    }
    
    /**
     * Insert Triple/Statement into graph if not exists yet
     * 
     * @param s
     * @param p
     * @param o
     * @param context 
     */
    public void addIfNot(Resource s, URI p, Value o, Resource context){
        try {
           RepositoryConnection con = therepository.getConnection();
           try {
                ValueFactory myFactory = con.getValueFactory();
                Statement st = myFactory.createStatement( s, p, (Value) o);
                
                if(context==null && !con.hasStatement(st, true, new Resource[0]) )
                    con.add(st, new Resource[0]);
                else if( context!=null && !con.hasStatement(st, true, context) )
                    con.add(st, context);
           }finally {
              con.close();
           }
        }
        catch (Exception e) {
           // handle exception
        }
    }
    

    /**
     *  Tuple pattern query - find all statements with the pattern, where null is a wild card 
     * 
     * @param s subject (null for wildcard)
     * @param p predicate (null for wildcard)
     * 	@param o object (null for wildcard)
     * @return serialized graph of results
     */
    public List tuplePattern(Resource s, URI p, Value o){
        try{
            RepositoryConnection con = therepository.getConnection();
            try {
                RepositoryResult repres = con.getStatements(s, p, o, true, new Resource[0]);
                ArrayList reslist = new ArrayList();
                while (repres.hasNext()) {
                    reslist.add(repres.next());
                }
                return reslist;
            } finally {
                con.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  Execute a CONSTRUCT/DESCRIBE SPARQL query against the graph 
     * 
     * @param qs CONSTRUCT or DESCRIBE SPARQL query
     * @param format the serialization format for the returned graph
     * @return serialized graph of results
     */
    public String runSPARQL(String qs, RDFFormat format){
        ByteArrayOutputStream stringout = new ByteArrayOutputStream();
        
        if( !runSPARQL(qs, format, stringout) )
            return null;
            
        return stringout.toString();
    }
    
    public boolean runSPARQL(String qs, RDFFormat format, OutputStream out){
        try{
            RepositoryConnection con = therepository.getConnection();
            try {
                GraphQuery query = con.prepareGraphQuery(org.openrdf.query.QueryLanguage.SPARQL, qs);
                RDFWriter w = Rio.createWriter(format, out);
                query.evaluate(w);
            } finally {
                con.close();
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     *  Execute a SELECT SPARQL query against the graph 
     * 
     * @param qs SELECT SPARQL query
     * @return list of solutions, each containing a hashmap of bindings
     */
    public List runSPARQL(String qs){
        try{
            RepositoryConnection con = therepository.getConnection();
            try {
                TupleQuery query = con.prepareTupleQuery(org.openrdf.query.QueryLanguage.SPARQL, qs);
                TupleQueryResult qres = query.evaluate();
                ArrayList reslist = new ArrayList();
                while (qres.hasNext()) {
                    BindingSet b = qres.next();
                    Set names = b.getBindingNames();
                    HashMap hm = new HashMap();
                    for (Object n : names) {
                        hm.put((String) n, b.getValue((String) n));
                    }
                    reslist.add(hm);
                }
                return reslist;
            } finally {
                con.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    
//    public static void main(String[] args) {
//
//        //create a graph with type inferencing
//        SimpleGraph g = new SimpleGraph(true); 
//
//        //load the film schema and the example data
//        g.addFile("film-ontology.owl", SimpleGraph.RDFXML);
//
//        List solutions = g.runSPARQL("SELECT ?who WHERE  { " +
//                                "?who <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://semprog.com/Person> ." +
//                        "}");
//        System.out.println("SPARQL solutions: " + solutions.toString());
//
//    }

}

