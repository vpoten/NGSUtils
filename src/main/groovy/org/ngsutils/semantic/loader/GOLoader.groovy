/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.loader

import org.ngsutils.Utils
import org.ngsutils.AnnotationDB
import org.ngsutils.semantic.LinkedLifeData as LLD
import org.ngsutils.semantic.query.GeneQueryUtils
import org.ngsutils.ontology.GOManager as GOM
import org.openrdf.model.vocabulary.RDF as RDF
import org.ngsutils.semantic.rdfutils.SimpleGraph
import org.openrdf.model.BNode
import org.openrdf.model.Resource
import org.openrdf.model.URI
import org.ngsutils.FileIndex

/**
 * helper class used to store gene ontology terms (readed from OBO file)
 * 
 */
class Term {
    String id = null
    String name
    String namespace
    String definition
    boolean obsolete = false
    def synonym = []
    def xref = []
    def isA = []
    
    //constants
    static final String ID = 'id'
    static final String NAME = 'name'
    static final String NAMESPACE = 'namespace'
    static final String DEF = 'def'
    static final String COMMENT = 'comment'
    static final String SYNONYM = 'synonym'
    static final String XREF = 'xref'
    static final String OBSOLETE = 'is_obsolete'
    static final String IS_A = 'is_a'
    
    /**
     *
     */
    def clear() {
        this.id = null
        this.name = null
        this.namespace = null
        this.definition = null
        this.obsolete = false
        this.synonym.clear()
        this.xref.clear()
        this.isA.clear()
    }
    
    /**
     *
     */
    protected String parseTagName(String line){
        return line.substring(0, line.indexOf(':'))
    }
    
    /**
     *
     */
    protected String parseTagValue(String line){
        int start = line.indexOf(':')+1
        int end = line.indexOf('!')
        end = (end<0) ? line.length() : end
        
        return line.substring(start, end).trim()
    }
    
    /**
     *
     */
    def parseLine(String line){
        String tagName = parseTagName(line)
        String tagValue = parseTagValue(line)
        
        switch(tagName) {
            case ID: this.id = tagValue; break;
            case NAME: this.name = tagValue; break;
            case NAMESPACE: this.namespace = tagValue; break;
            case DEF: this.definition = tagValue; break;
            case SYNONYM: this.synonym << tagValue; break;
            case XREF: this.xref << tagValue; break;
            case IS_A: this.isA << tagValue; break;
            case OBSOLETE: this.obsolete = (tagValue=='true'); break;
        }
    }
    
    /**
     *
     */
    def writeTriples(SimpleGraph graph, Resource context){
        if( this.id==null || this.obsolete )
            return
            
        //add GO term 
        URI goURI = graph.URIref(LLD.GOURI_PREF+'/id/'+this.id)
        graph.add(goURI, RDF.TYPE, graph.URIref(LLD.SKOS_PREF+'Concept'), context)
        graph.add(goURI, graph.URIref(LLD.SKOS_PREF+GOM.IN_SCHEME), graph.URIref(LLD.GOURI_PREF), context)
        
        //add name
        graph.add(goURI, graph.URIref(LLD.SKOS_PREF+GOM.LABEL), graph.Literal(this.name), context)
        
        //add def
        graph.add(goURI, graph.URIref(LLD.SKOS_PREF+GOM.DEFINITION), graph.Literal(this.definition), context)
        
        //add namespace
        graph.add(goURI, graph.URIref(LLD.GOURI_PREF+'/'+GOM.NAMESPACE), graph.Literal(this.namespace), context)
        
        //add xref
        this.xref.each{
            graph.add(goURI, graph.URIref(LLD.SKOS_PREF+GOM.NOTATION), graph.Literal(it), context)
        }
        
        //add synonym
        this.synonym.each{
            graph.add(goURI, graph.URIref(LLD.SKOS_PREF+GOM.SYNONYM), graph.Literal(it), context)
        }
        
        //add is_a
        this.isA.each{
            graph.add(goURI, graph.URIref(LLD.SKOS_PREF+GOM.IS_A), graph.URIref(LLD.GOURI_PREF+'/id/'+it), context)
        }
    }
}

/**
 * Class which loads a Gene Ontology database (OBO file) into a semantic database
 * 
 * @author victor
 */
class GOLoader extends BaseLoader {
    
    //constants
    static final String TERM = '[Term]'
    static final String TYPE = '[Typedef]'
    static final String INSTANCE = '[Instance]'
    static final STANZAS = [TERM, TYPE, INSTANCE]
    
    static final String GO_BASIC_URL = 'ftp://ftp.geneontology.org/pub/go/ontology/go-basic.obo'
    
    static final String UNIPROT_ID = 'UniProtKB:'
    
    GeneQueryUtils geneQuery = null
    
    /**
     * parses and inserts the Gene Ontology into the triplestore
     */
    def parseOBO(file) {
        def reader = new File(file).newReader()
        
        boolean inTerm = false
        Term term = new Term()
        
        reader.eachLine{ line->
            if( line.isEmpty() ){
                //nothing to do
            }
            else if( STANZAS.any{ line.startsWith(it)} ){
                if( inTerm ){
                    //write current term
                    term.writeTriples(this.graph, this.context)
                    term.clear()
                }
                
                inTerm = line.startsWith(TERM)
            }
            else if( inTerm ){
                term.parseLine(line)
            }
        }
        
        reader.close()
    }
    
    /**
     * parses and inserts Gene -> GO relations into the triplestore
     */
    def parseGOA(file, taxonomyId) {
        if( !geneQuery ) {
           geneQuery = new GeneQueryUtils(graph)
        }
       
        def taxonomyURI = graph.URIref(LLD.GENEURI_TAXON+'/'+taxonomyId)
        def goPred = graph.URIref(LLD.GENE_GOTERM)
        
        def reader = Utils.createReader(new File(file))
        def currGene = null
        URI geneURI = null
        
        reader.eachLine{ line->
            if( !line.startsWith('!') ){
                def toks = line.split("\t",-1)
                def gene = toks[2]
                def goTerm = toks[4]
                def objectType = toks[11]
                def prodFormId = toks[16]
                
                if( currGene==null || gene!=currGene ){
                    currGene = gene
                    geneURI = geneQuery.getGeneBySymbol(gene, taxonomyURI)
                }
                
                if( geneURI!=null ){
                    URI goURI = graph.URIref(LLD.GOURI_PREF+'/id/'+goTerm)

                    if( objectType=='protein' && prodFormId.startsWith(UNIPROT_ID) ){
                        def splicing = prodFormId.substring(UNIPROT_ID.length())
                        // add go annotation to splicing variation
                        graph.add(graph.URIref(LLD.UNIPROT_ISOID+splicing), goPred, goURI, context)
                    }
                    else{
                        // add go annotation to gene
                        graph.add(geneURI, goPred, goURI, context)
                    }
                }
            }
        }
        
        reader.close()
    }
    
    /**
     *
     */
    public boolean load(String workDir) {
        
        String name = getFileName(GO_BASIC_URL)
        File file = new File("${workDir}${name}")
        
        if( !downloadFile(GO_BASIC_URL, file) ){
            return false
        }
        
        println "Loading Gene Ontology"
        parseOBO(file.absolutePath)
        
        for( String taxId : this.taxonomyIds ) {
            def urlSrc = AnnotationDB.goAssocUrl(taxId)
            name = getFileName(urlSrc)
            
            file = new File("${workDir}${taxId}/${name}")
            
            if( !downloadFile(urlSrc, file) ){
                return false
            }
            
            println "Loading Gene ontology association for organism [${taxId}]"
            parseGOA(file.absolutePath, taxId)
        }
        
        return true
    }
    
}

