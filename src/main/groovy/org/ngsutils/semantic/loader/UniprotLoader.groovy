/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
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
class UniprotLoader extends BaseLoader {
	
    static final String ID_ENS_GENE = 'Ensembl'
    static final String ID_ENS_TRANS = 'Ensembl_TRS'
    static final String ID_ENS_PROT = 'Ensembl_PRO'
    static final String ID_GENE_ID = 'GeneID'
    
    static final int F_ENS_ISO = 0
    static final int F_ENS_PROT = 1
    
    GeneQueryUtils geneQuery = null
    
    /**
     * 
     */
    def parseIdMapping(file, taxId) {
       if(!geneQuery) {
           geneQuery = new GeneQueryUtils(graph)
       }
       
       //predicates and types used in this method
       URI uniAccPred = graph.URIref(LLD.GENE_UNIPROTACC)
       URI uniSeqPred = graph.URIref(LLD.UNIPROT_SEQ)
       URI splicePred = graph.URIref(LLD.GENE_SPLICING)
       URI rnaPred = graph.URIref(LLD.GENE_RNAACC)
       URI protPred = graph.URIref(LLD.GENE_PROTACC)
       URI protType = graph.URIref(LLD.UNIPROT_TYPE)
       URI simpleSeqType = graph.URIref(LLD.UNIPROT_SEQ_TYPE)
       
        
       def reader = Utils.createReader(file)
       reader.readLine() 
       
       String currentAC = null
       String geneId = null
       String splicingId = null
       def spliceMap = [[:],[:]]
       
       
       def addUniprot = {
           URI geneURI = graph.URIref(LLD.GENEURI_PREF+'/id/'+geneId)
           URI protURI = graph.URIref(LLD.UNIPROT_ID+currentAC)

           //add uniprotAccession to gene
           graph.add(geneURI, uniAccPred, protURI, context)
           //add type to protein
           graph.add(protURI, RDF.TYPE, protType, context)
           
            [F_ENS_ISO,F_ENS_PROT].each{ mode->
                URI featPred = (mode==F_ENS_ISO) ? rnaPred : protPred
                
                spliceMap[mode].each{ splId, listId->
                    URI seqURI = graph.URIref(LLD.UNIPROT_ISOID+splId)
                    
                    //add (sequence variant) splicing to gene
                    graph.add( geneURI, splicePred, seqURI, context)
                    //add uniprot sequence variant to protein
                    graph.add(protURI, uniSeqPred, seqURI, context)
                    // add type to variant
                    graph.add(seqURI, RDF.TYPE, simpleSeqType, context)
                    
                    listId.each{ id->//add each protein or isoform to splice variant
                        graph.add(seqURI, featPred, graph.URIref(LLD.GENEURI_ENSEMBL+'/'+id), context)
                    }
                }
            }
       }//end closure
       
       reader.splitEachLine("\\t"){ toks->
           def acCode = toks[0]
           def idType = toks[1]
           def id = toks[2]
           
           if( currentAC==null ){ currentAC=acCode }
           
           if( currentAC!=acCode ) {
               addUniprot()
               //reset status variables
               currentAC = acCode
               geneId = null
               splicingId = null
               spliceMap = [[:],[:]]
            }
           
            if( idType==ID_GENE_ID ){
                geneId = id
            }
            else if( idType==ID_ENS_GENE ){
                // add splicing variation of protein
                int pos = id.indexOf('[')
                if( pos>0 ){
                    splicingId = id.substring( pos+1, id.indexOf(']'))
                    spliceMap.each{ it[splicingId] = [] }
                }
                else{
                    splicingId = null
                }
            }
            else if( idType==ID_ENS_TRANS && splicingId!=null ){
                // associate splicing with transcript
                spliceMap[F_ENS_ISO][splicingId] << id
            }
            else if( idType==ID_ENS_PROT && splicingId!=null ){
                // associate splicing with protein
                spliceMap[F_ENS_PROT][splicingId] << id
            }
       }
       
        // add the last protein in file
        addUniprot()
        
       reader.close()
    }
    
    /**
     *
     */
    public boolean load(String workDir) {
        for( String taxId : this.taxonomyIds ) {
            def urlSrc = AnnotationDB.uniprotIdMapUrl(taxId)
            def name = getFileName(urlSrc)
            def file = new File("${workDir}${taxId}/${name}")
            
            if( !downloadFile(urlSrc, file) ){
                return false
            }
            
            println "Loading ${name} uniprot info for organism [${taxId}]"
            parseIdMapping(file, taxId)
        }
        
        return true
    }
    
}

