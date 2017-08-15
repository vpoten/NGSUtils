/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.rdfutils

import org.openrdf.OpenRDFException
import org.openrdf.repository.Repository
import org.openrdf.repository.RepositoryConnection
import org.openrdf.rio.RDFFormat
import org.openrdf.repository.http.HTTPRepository
import org.openrdf.model.Resource
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer

import java.util.zip.GZIPOutputStream
import org.ngsutils.Utils
import org.ngsutils.semantic.memcached.MemcachedStore


/**
 * exception throwed by RDFLoad
 */
class RDFLoadException extends RuntimeException {
    String message
}

/**
 *
 * @author victor
 */
class RDFLoad {
	
    protected static final String SUFF = '.tmp.gz'
    protected static final int MAX_LINES = 500000

    String sesameServer
    String baseURI
    String repositoryID
    String context
    
    /**
     * returns the RDF format associated with the file extension
     */
    protected def getFormat(file){
        if( file.indexOf('.trig')>=0 )
            return RDFFormat.TRIG
        else if( file.indexOf('.nt')>=0 )
            return RDFFormat.NTRIPLES
        else if( file.indexOf('.n3')>=0 )
            return RDFFormat.N3
        else if( file.indexOf('.ttl')>=0 )
            return RDFFormat.TURTLE
        else
            return RDFFormat.RDFXML
    }
    
    /**
     * checks whether the file is a RDF format
     */
    static boolean isRdf(file){
        return RDFFormat.values().any{ file.indexOf('.'+it.defaultFileExtension)>=0 }
    }
    
    /**
     *
     */
    def addRdf(String file, myRepository=null){
        if( !myRepository ){
            // init repository
            try{
                myRepository = new HTTPRepository(sesameServer, repositoryID)
                myRepository.initialize()
            } catch(e){
                throw new RDFLoadException(message:"Cannot initialize repository with ID=${repositoryID}")
            }
        }
        
        def fact = myRepository.getValueFactory()
        
        addRdf(Utils.createInputStream(file), myRepository, getFormat(file), fact.createURI(context?:"file://${file}"))
    }
    
    /**
     *
     */
    def addRdf(InputStream istr, myRepository, RDFFormat format, Resource contextRes=null){
        // add RDF
        try {
            RepositoryConnection con = myRepository.getConnection();
            con.setAutoCommit(true)
            try {
                if(contextRes)
                    con.add( istr, baseURI, format,  contextRes)
                else
                    con.add( istr, baseURI, format)
            }
            finally {
                con.close()
                ////myRepository.shutDown()
            }
        }
        catch (OpenRDFException e) {
           // handle exception
           throw new RDFLoadException(message: e.message)
        }
        catch (java.io.IOException e) {
           // handle io exception
           throw new RDFLoadException(message: e.message)
        }
    }
    
    
    /**
     *
     */
    def splitAndAdd(String file, PrintStream printer=null, myRepository=null){
        
        String file2 = null
        
        if( getFormat(file)!=RDFFormat.NTRIPLES ){
            //convert to NTRIPLES (one line per statement)
            file2 = file.substring(0, file.indexOf('.'))+'.nt.gz'
            RDF2RDF.convert(file,file2)
            file = file2
        }
        
        printer?.println("Setted ${MAX_LINES} lines per fragment.")
        
        def reader = Utils.createReader(file)
        int cont = 0
        int part = 1
        long processed = 0
        String tmpFile = file+SUFF
        def writer = null

        reader.eachLine{ line ->
            if(writer == null )
                writer = new GZIPOutputStream(new FileOutputStream(tmpFile))

            processed++
            writer.write((line+'\n').getBytes())

            if( cont<MAX_LINES ){
                cont++
            }
            else{
                writer.close()
                printer?.println("Loading part ${part++}. ${new Date()}")
                try{
                    addRdf(tmpFile, myRepository)
                } catch(e){}
                cont = 0
                writer = null
            }
        }

        reader.close()

        if( writer ){
            writer.close()
            printer?.println("Loading part ${part++}. ${new Date()}")
            try{
                addRdf(tmpFile, myRepository)
            } catch(e){}
        }

        //cleaning
        "rm -rf ${tmpFile}".execute().waitFor()
        
        if(file2)
            "rm -rf ${file2}".execute().waitFor()
        
        printer?.println("#Triples loaded: ${processed}")
    }
      
    /**
     * Utility method
     */
    static Repository createMemRepository(boolean memcached, boolean inferencing, args = null ) 
    {
        def store = (memcached) ? new MemcachedStore(args) : new MemoryStore()
        SailRepository therepository = (inferencing) ? 
            new SailRepository(new ForwardChainingRDFSInferencer(store)) : new SailRepository(store)
        
            
        therepository.initialize()
        return therepository
    }
    
    
}

