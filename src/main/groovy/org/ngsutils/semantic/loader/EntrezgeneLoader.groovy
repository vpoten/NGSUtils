/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.loader

import org.ngsutils.Utils
import org.ngsutils.AnnotationDB
import org.ngsutils.semantic.LinkedLifeData as LLD
import org.openrdf.model.vocabulary.RDF as RDF
import org.ngsutils.semantic.rdfutils.SimpleGraph
import org.openrdf.model.BNode
import org.openrdf.model.Resource
import org.openrdf.model.URI
import org.ngsutils.semantic.query.GeneQueryUtils

/**
 *
 * @author victor
 */
class EntrezgeneLoader extends BaseLoader {
    
    //constants for gene_info file fields
    static final int TAX_ID = 0
    static final int GENE_ID = 1
    static final int SYMBOL = 2
    static final int SYNONYM = 4
    static final int XREF = 5
    static final int CHR = 6
    static final int LOCATION = 7
    static final int DESC = 8
    static final int TYPE = 9
    
    //constants for xref
    static final String XREF_ENSMBL = 'Ensembl'
    static final String XREF_VEGA = 'Vega'
    static final String XREF_MIM = 'MIM'
    
    static final def XREF_PREFS = [ 
        (XREF_ENSMBL):LLD.GENEURI_XREF_ENSEMBL,
        (XREF_VEGA):LLD.GENEURI_XREF_VEGA,
        (XREF_MIM):LLD.GENEURI_XREF_MIM ]
    
    static final String GENE2ENSMBL_URL = 'ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2ensembl.gz'
    
    //constants for gene2ensembl file fields
    static final int G2E_REF_RNA = 3
    static final int G2E_ENSMB_RNA = 4
    static final int G2E_REF_PROT = 5
    static final int G2E_ENSMB_PROT = 6
    
    GeneQueryUtils geneQuery = null
    
    /**
     * parses and inserts an entrezGene gene_info file into the triplestore
     */
    def parseGeneInfo(file, taxonomyIds = []){
        def reader = Utils.createReader(file)
        reader.readLine()
        
        reader.splitEachLine("\\t"){ toks->
            if( !toks[0].startsWith('#') && toks[TAX_ID] in taxonomyIds ){
                // add gene ID
                URI geneURI = graph.URIref(LLD.GENEURI_PREF+'/id/'+toks[GENE_ID])
                graph.add(geneURI, RDF.TYPE, graph.URIref(LLD.GENE_TYPE), context)
                graph.add(geneURI, graph.URIref(LLD.GENE_ID), graph.Literal(toks[GENE_ID]), context)
                
                // add Symbol
                graph.add(geneURI, graph.URIref(LLD.GENE_SYMBOL), graph.Literal(toks[SYMBOL]), context)
                
                // add taxonomyId
                graph.add(geneURI, graph.URIref(LLD.GENE_EXPRIN), graph.URIref(LLD.GENEURI_TAXON+'/'+toks[TAX_ID]), context)
                
                // add synonym
                splitByPipe(toks[SYNONYM])?.each{ value->
                    graph.add(geneURI, graph.URIref(LLD.GENE_SYNONYM), graph.Literal(value), context)
                }
                
                // add external refs
                splitByPipe(toks[XREF])?.each{ value->
                    addXref(geneURI, value)
                }
                
                // add chr
                graph.add(geneURI, graph.URIref(LLD.GENE_CHR), graph.Literal(toks[CHR]), context)
                
                // add location
                graph.add(geneURI, graph.URIref(LLD.GENE_MAPLOC), graph.Literal(toks[LOCATION]), context)
                
                // add description
                graph.add(geneURI, graph.URIref(LLD.GENE_DESC), graph.Literal(toks[DESC]), context)
                
                // add type
                graph.add(geneURI, graph.URIref(LLD.GENE_GENETYPE), graph.Literal(toks[TYPE]), context)
            }
        }
        
        reader.close()
    }
    
    /**
     * parses and insert gene2ensembl file (rna and protein accesion for refseq and ensembl)
     */
    def parseGene2Ensembl(file, taxonomyIds = []) {
       def reader = Utils.createReader(file)
       reader.readLine() 
       
       URI rnaPred = graph.URIref(LLD.GENE_RNAACC)
       URI protPred = graph.URIref(LLD.GENE_PROTACC)
       
       reader.splitEachLine("\\t"){ toks->
            if( !toks[0].startsWith('#') && toks[TAX_ID] in taxonomyIds ){
                URI geneURI = graph.URIref(LLD.GENEURI_PREF+'/id/'+toks[GENE_ID])
                
                URI refRna = graph.URIref(LLD.GENEURI_REFSEQ+'/'+toks[G2E_REF_RNA])
                URI ensRna = graph.URIref(LLD.GENEURI_ENSEMBL+'/'+toks[G2E_ENSMB_RNA])
                URI refProt = graph.URIref(LLD.GENEURI_REFSEQ+'/'+toks[G2E_REF_PROT])
                URI ensProt = graph.URIref(LLD.GENEURI_ENSEMBL+'/'+toks[G2E_ENSMB_PROT])
                
                //add refseq rna
                graph.add(geneURI, rnaPred, refRna, context)
                //add ensembl rna
                graph.add(geneURI, rnaPred, ensRna, context)
                //add refseq protein
                graph.add(geneURI, protPred,  refProt, context)
                //add ensembl protein
                graph.add(geneURI, protPred, ensProt, context)
            }
       }
       
       reader.close()
    }
    
    /**
     * parses and insert UCSC ensemblToGeneName
     * file format: <ensembl_trans> <gene_name>
     */
    def parseEnsemblToGeneName(file, taxId) {
        if(!geneQuery) {
            geneQuery = new GeneQueryUtils(graph)
        }
        
        def reader = Utils.createReader(file)
        reader.readLine() 
        
        URI rnaPred = graph.URIref(LLD.GENE_RNAACC)
       
        reader.splitEachLine("\\t"){ toks->
            URI geneURI = geneQuery.getGeneByName(toks[1], taxId)
            
            if( geneURI!=null ){
                //add ensembl rna
                graph.add(geneURI, rnaPred, graph.URIref(LLD.GENEURI_ENSEMBL+'/'+toks[0]), context)
            }
        }
       
        reader.close()
    }
    
    /**
     *
     */
    private def splitByPipe(String str){
        if( !str || str=='-' )
            return null
        
        return str.split("\\|")
    }
    
    /**
     *
     */
    private def addXref(geneURI, value){
        def ref = value.substring( value.indexOf(':')+1 )
        def xrefpref = XREF_PREFS[value.substring(0, value.indexOf(':'))]
        
        if( xrefpref==null )
            return
        
        graph.add(geneURI, graph.URIref(LLD.GENE_DBXREF), graph.URIref(xrefpref+'/'+ref), context)
        
        if( value.startsWith(XREF_ENSMBL) ){
            graph.add(geneURI, graph.URIref(LLD.GENE_ENSEMBLREF), graph.URIref(LLD.GENEURI_ENSEMBL+'/id/'+ref), context)
        }
    }
    
    
    /**
     *
     */
    public boolean load(String workDir) {
        
        for( String taxId : this.taxonomyIds ) {
            def urls = [
                (AnnotationDB.egeneInfoUrl(taxId)):{file-> parseGeneInfo(file, [taxId])}, 
                (AnnotationDB.ensemblToGeneUrl(taxId)):{file-> parseEnsemblToGeneName(file,taxId)}
            ]
            
            urls.each{ urlSrc, clos->
                def name = getFileName(urlSrc)
            
                def file = new File("${workDir}${taxId}/${name}")
            
                if( !downloadFile(urlSrc, file) ){
                    return false
                }
            
                println "Loading ${name} info for organism [${taxId}]"
                clos(file)
            }
        }
        
        def name = getFileName(GENE2ENSMBL_URL) 
        def file = new File("${workDir}/${name}")
            
        if( !downloadFile(GENE2ENSMBL_URL, file) ){
            return false
        }
            
        println "Loading Entrez-gene gene2ensembl"
        parseGene2Ensembl(file, this.taxonomyIds)
            
        return true
    }
    
}

