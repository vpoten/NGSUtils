/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic

import org.ngsutils.Utils
import org.ngsutils.semantic.LinkedLifeData as LLD
import org.ngsutils.semantic.rdfutils.SimpleGraph
import org.ngsutils.semantic.loader.*
import org.openrdf.model.Resource


/**
 *
 * @author victor
 */
class LinkedLifeDataFactory {

    // common database list
    static final def LIST_UNIPROT_GO = [LLD.DB_ENTREZ_GENE, LLD.DB_UNIPROT, LLD.DB_GENE_ONTOLOGY]
    static final def LIST_BASIC_GO = [LLD.DB_ENTREZ_GENE, LLD.DB_GENE_ONTOLOGY]
    static final def LIST_EGENE = [LLD.DB_ENTREZ_GENE]
    
    /**
     *
     */
    static SimpleGraph loadRepository(List databases, List taxonomyIds, String workDir, Resource context = null){
        SimpleGraph graph = new SimpleGraph()
        
        for( String taxId : taxonomyIds ) {
            // prepare work dir
            if( !Utils.createDir("${workDir}${taxId}") )
                return null
        }
        
        def loaders = createLoaders(databases)
        
        for(BaseLoader loader : loaders) {
            loader.graph = graph
            loader.context = context
            loader.taxonomyIds = taxonomyIds
            
            if( !loader.load(workDir) ){
                return null
            }
        }
                
        return graph
    }
    
    /**
     *
     */
    static private createLoaders(List databases) {
        databases.collect{
            if( it==LLD.DB_ENTREZ_GENE ){
                return new EntrezgeneLoader()
            }
            else if( it==LLD.DB_GENE_ONTOLOGY ){
                return new GOLoader()
            }
            else if( it==LLD.DB_UNIPROT ){
                return new UniprotLoader()
            }
            
            return null
        }
    }
    
}

