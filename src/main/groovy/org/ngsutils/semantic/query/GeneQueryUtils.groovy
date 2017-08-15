/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.query

import org.ngsutils.semantic.rdfutils.SimpleGraph
import org.openrdf.model.BNode
import org.ngsutils.semantic.LinkedLifeData as LLD
import org.openrdf.model.vocabulary.RDF as RDF
import org.openrdf.model.URI
import org.openrdf.model.Resource

/**
 * LLD Entrez gene queries
 * 
 * @author victor
 */
class GeneQueryUtils extends BaseQueryUtils {
    
    protected final URI predGeneSymbol
    protected final URI predRnaAcc
    protected final URI predEnsemblRef
    protected final URI predExprIn
    protected final URI predSplicing
    protected final URI protAcc
    protected final URI predUniprotSeq
    protected final URI predUniprotAcc
    protected final URI predProtAcc
    protected final def predGeneIdByName
    
    /**
     *
     */
    public GeneQueryUtils(SimpleGraph p_graph){
        super(p_graph)
        
        predGeneSymbol = graph.URIref(LLD.GENE_SYMBOL)
        predRnaAcc = graph.URIref(LLD.GENE_RNAACC)
        predEnsemblRef = graph.URIref(LLD.GENE_ENSEMBLREF)
        predExprIn = graph.URIref(LLD.GENE_EXPRIN)
        predSplicing = graph.URIref(LLD.GENE_SPLICING)
        protAcc = graph.URIref(LLD.GENE_PROTACC)
        predUniprotSeq = graph.URIref(LLD.UNIPROT_SEQ)
        predUniprotAcc = graph.URIref(LLD.GENE_UNIPROTACC)
        predProtAcc = graph.URIref(LLD.GENE_PROTACC)
        
        predGeneIdByName = [predGeneSymbol, predRnaAcc, predRnaAcc, predEnsemblRef]
    }
    
    /**
     *
     */
    public String getGeneSymbol(geneId) {
        def subject = (geneId instanceof URI) ? geneId : graph.URIref(LLD.GENEURI_PREF+'/id/'+geneId)
        def statements = graph.tuplePattern(subject, predGeneSymbol, null)
        
        return (statements) ? stringValue(statements[0].object) : null
    }
    
    /**
     *
     */
    public String getEnsemblGene(geneId) {
        def subject = (geneId instanceof URI) ? geneId : graph.URIref(LLD.GENEURI_PREF+'/id/'+geneId)
        def statements = graph.tuplePattern(subject, predEnsemblRef, null)
        
        return (statements) ? localName(stringValue(statements[0].object)) : null
    }
    
    /**
     *
     * @return a list of URI (uniprot splicing variants)
     */
    public List getGeneSplicing(geneId) {
        def subject = (geneId instanceof URI) ? geneId : graph.URIref(LLD.GENEURI_PREF+'/id/'+geneId)
        def statements = graph.tuplePattern(subject, predSplicing, null)
        
        return (statements) ? statements.collect{it.object} : null
    }
    
    /**
     * 
     * @return a List of URI (ensembl transcripts)
     */
    public List getIsoformSplicing(splicingId) {
        def subject = (splicingId instanceof URI) ? splicingId : graph.URIref(LLD.UNIPROT_ISOID+splicingId)
        def statements = graph.tuplePattern(subject, predRnaAcc, null)
        
        return (statements) ? statements.collect{it.object} : null
    }
    
    /**
     * filter statements by taxonomyId
     */
    protected URI filterStatByTaxonomy(statements, URI taxId){
        for(stat in statements) {
            def stat2 = graph.tuplePattern(stat.subject, predExprIn, taxId)

            if( stat2 ){
                return stat2[0].subject
            }
        }

        return null
    }
    
    /**
     * get gene id by symbol, refseq_rna, ensembl_rna or ensembl_gene
     * 
     * @return a gene URI
     */
    public URI getGeneByName(String name, taxIdStr=null ) {
        def objects = [ graph.Literal(name), 
            graph.URIref(LLD.GENEURI_REFSEQ+'/'+name), 
            graph.URIref(LLD.GENEURI_ENSEMBL+'/'+name),
            graph.URIref(LLD.GENEURI_ENSEMBL+'/id/'+name) ]
        
        URI taxId = null
        
        if( taxIdStr ){
            taxId = (taxIdStr instanceof String) ? graph.URIref(LLD.GENEURI_TAXON+'/'+taxIdStr) : taxIdStr
        }
        
        for(int i=0; i<objects.size(); i++){
            def statements = graph.tuplePattern(null, predGeneIdByName[i], objects[i])
            
            if( statements ) {
                return (taxId) ? filterStatByTaxonomy(statements,taxId) : statements[0].subject
            }
        }
        
        return null
    }
    
    /**
     * get gene id by symbol
     * 
     * @return a gene URI
     */
    public URI getGeneBySymbol(String name, taxIdStr=null ) {
        URI taxId = null
        
        if( taxIdStr ){
            taxId = (taxIdStr instanceof String) ? graph.URIref(LLD.GENEURI_TAXON+'/'+taxIdStr) : taxIdStr
        }
        
        def statements = this.graph.tuplePattern(null, predGeneSymbol, graph.Literal(name))
        
        if( statements ) {
            return (taxId) ? filterStatByTaxonomy(statements,taxId) : statements[0].subject
        }
        
        return null
    }
    
    /**
     *
     * @return a gene URI
     */
    public URI getGeneByProtein(prot) {
        def object = (prot instanceof URI) ? prot : graph.URIref(LLD.GENEURI_REFSEQ+'/'+prot)
        
        def statements = graph.tuplePattern(null, protAcc, object)
        
        return (statements) ? statements[0].subject : null
    }
    
    /**
     *
     */
    public URI getGeneByURI(URI uri) {
        String namespace = uri.getNamespace()
        def predicate = null
        
        if( namespace.startsWith(LLD.GENEURI_PREF+'/id/') ){
            return uri
        }
        else if( namespace.startsWith(LLD.UNIPROT_ISOID) ){
            predicate = predSplicing
        }
        else if( namespace.startsWith(LLD.UNIPROT_ID) ){
            predicate = predUniprotAcc
        }
        else if( namespace.startsWith(LLD.GENEURI_ENSEMBL) ){
            def localName = uri.getLocalName()
            
            if( localName.startsWith('ENST') ){
                predicate = predRnaAcc
            }
            else if( localName.startsWith('ENSP') ){
                predicate = predProtAcc
            }
            else if( localName.startsWith('ENSG') ){
                predicate = predEnsemblRef
            }
        }
        
        def statements = graph.tuplePattern(null, predicate, uri)
        return (statements) ? statements[0].subject : null
    }
    
}

