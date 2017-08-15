/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.query

import org.ngsutils.semantic.rdfutils.SimpleGraph
import org.ngsutils.semantic.LinkedLifeData as LLD
import org.openrdf.model.vocabulary.RDF as RDF
import org.openrdf.model.URI
import org.openrdf.model.Resource

/**
 * Gene Ontology query utils
 *
 * @author victor
 */
class GOQueryUtils extends BaseQueryUtils {
	
    protected final URI predGOTerm
    protected final URI predRnaAcc
    protected final URI simpleSeqType
    protected final URI predProtAcc
    protected final URI predUnipAcc
    protected final URI predEnsemblRef
    
    /**
     *
     */
    public GOQueryUtils(SimpleGraph p_graph){
        super(p_graph)
        predGOTerm = graph.URIref(LLD.GENE_GOTERM)
        predRnaAcc = graph.URIref(LLD.GENE_RNAACC)
        simpleSeqType = graph.URIref(LLD.UNIPROT_SEQ_TYPE)
        predProtAcc = graph.URIref(LLD.GENE_PROTACC)
        predUnipAcc = graph.URIref(LLD.GENE_UNIPROTACC)
        predEnsemblRef = graph.URIref(LLD.GENE_ENSEMBLREF)
    }
    
    /**
     *
     */
    public Set getGeneTerms(geneId) {
        def subject = (geneId instanceof URI) ? geneId : graph.URIref(LLD.GENEURI_PREF+'/id/'+geneId)
        
        def statements = graph.tuplePattern(subject, predGOTerm, null)
        
        return statements.collect{ it.object.localName } as Set
    }
    
    /**
     * get go terms for a specific variant (uniprot sequence)
     */
    public Set getSpliceTerms(splicingId) {
        def subject = (splicingId instanceof URI) ? splicingId : graph.URIref(LLD.UNIPROT_ISOID+splicingId)
        
        def statements = graph.tuplePattern(subject, predGOTerm, null)
        
        return statements.collect{ it.object.localName } as Set
    }
    
    /**
     * get go specific terms for a variant of isoform or protein
     */ 
    protected Set getProductTerms(featId, predicate) {
        def object = (featId instanceof URI) ? featId : graph.URIref(LLD.GENEURI_ENSEMBL+'/'+featId)
        def statements = graph.tuplePattern(null, predicate, object)
        
        def seq = statements.find{ stat-> graph.tuplePattern(stat.subject, RDF.TYPE, simpleSeqType) }?.subject
        
        return (seq) ? getSpliceTerms(seq) : null
    }
    
    /**
     * get go specific terms for an ensembl isoform
     */
    public Set getIsoformTerms(isoId) {
        return getProductTerms(isoId, predRnaAcc)
    }
    
    /**
     * get go specific terms for an ensembl protein
     */
    public Set getProteinTerms(protId) {
        return getProductTerms(protId, predProtAcc)
    }
    
    /**
     * get gene terms using an uniprot accesion
     */
    public Set getUniprotTerms(protId) {
        def object = (protId instanceof URI) ? protId : graph.URIref(LLD.UNIPROT_ID+protId)
        def statements = graph.tuplePattern(null, predUnipAcc, object)
        return (statements) ? getGeneTerms(statements[0].subject) : null
    }
    
    /**
     * get gene terms using an ensembl gene id
     */
    public Set getEnsemblTerms(ensemblId) {
        def object = (ensemblId instanceof URI) ? ensemblId : graph.URIref(LLD.GENEURI_ENSEMBL+'/id/'+ensemblId)
        def statements = graph.tuplePattern(null, predEnsemblRef, object)
        return (statements) ? getGeneTerms(statements[0].subject) : null
    }
    
    /**
     *
     */
    public Set getTerms(URI uri){
        String namespace = uri.getNamespace()
        
        if( namespace.startsWith(LLD.GENEURI_PREF+'/id/') ){
            return getGeneTerms(uri)
        }
        else if( namespace.startsWith(LLD.UNIPROT_ISOID) ){
            return getSpliceTerms(uri)
        }
        else if( namespace.startsWith(LLD.UNIPROT_ID) ){
            return getUniprotTerms(uri)
        }
        else if( namespace.startsWith(LLD.GENEURI_ENSEMBL) ){
            def localName = uri.getLocalName()
            
            if( localName.startsWith('ENST') ){
                return getIsoformTerms(uri)
            }
            else if( localName.startsWith('ENSP') ){
                return getProteinTerms(uri)
            }
            else if( localName.startsWith('ENSG') ){
                return getEnsemblTerms(uri)
            }
        }
        
        return null
    }
    
}