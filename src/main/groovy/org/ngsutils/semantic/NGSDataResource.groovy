/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic

import org.ngsutils.semantic.rdfutils.SimpleGraph
import org.ngsutils.semantic.LinkedLifeData as LLD
import org.ngsutils.semantic.NGSResults as NGSR
import org.openrdf.model.URI

/**
 *
 * @author victor
 */
class NGSDataResource {
	
    LinkedLifeData lifeData
    SimpleGraph ngsResGraph = null
    Set selectedGenes = null
    
    //map with keys=gene_uri and values=set of GO terms
    def annotation = [:]
    
    
    /**
     * factory class method
     */
    static NGSDataResource create( serverUrl, reposId, resultRdfFile=null ){
        NGSDataResource annot = new NGSDataResource();
        LinkedLifeData lld = new LinkedLifeData();
        SimpleGraph g = new SimpleGraph( SimpleGraph.HTTPRepository(serverUrl, reposId) );
        lld.setGraph(g);       
        annot.setLifeData(lld);
        
        if( resultRdfFile ){
            g = new SimpleGraph(true);
            g.addFile( resultRdfFile, SimpleGraph.RDFXML);
            annot.setNgsResGraph(g);
        }
        
        return annot
    }
    
    /**
     * factory class method
     */
    static NGSDataResource create(SimpleGraph g){
        NGSDataResource annot = new NGSDataResource();
        LinkedLifeData lld = new LinkedLifeData();
        lld.setGraph(g);       
        annot.setLifeData(lld);
        
        return annot
    }
    
    /**
     *
     */
    def getLldGraph(){
        return lifeData.graph
    }
    
    /**
     * load the organism whole GO annotation
     */
    def load(taxId){
        lifeData.getAllGenesAndGO(taxId)?.each{ map->
            if( !annotation[map['gene']] )
                annotation[map['gene']] = [] as Set
                
            annotation[map['gene']] << map['go'].substring( map['go'].indexOf(LLD.GO_PREF) )
        }
    }
    
    /**
     * clear loaded data
     */
    def clear(){
        annotation = [:]
    }
    
    /**
     *
     */
    Set getAllGenes(filter=null){
        return annotation.keySet()+getResultGenes(filter)
    }
    
    /**
     *
     * @param filter : null, 'over', 'under'
     */
    Set getResultGenes(filter=null) {
        if( !ngsResGraph ){
            return (selectedGenes!=null) ? selectedGenes : [] as Set
        }
            
        def map = ['over':'FILTER (?val1 < ?val2)', 'under':'FILTER (?val1 > ?val2)']
        def filterStr = filter ? map[filter] : ''
        
        String sparql = 
"""PREFIX rdf: <${LLD.RDF_PREF}>
SELECT ?feat
WHERE {
    ?feat rdf:type <${NGSR.TYPE_DNAFEAT}> .
    ?feat ?diffPred ?node .
    ?node rdf:type <${NGSR.TYPE_DIFFEXPR}> .
    ?node <${NGSR.NGS_VALUE1}> ?val1 .
    ?node <${NGSR.NGS_VALUE2}> ?val2 .
    ${filterStr}
}"""
        
        def list = ngsResGraph.runSPARQL(sparql)
        return list.collect{ it['feat'].stringValue() } as Set
    }
    
    /**
     *
     */
    Set getGOTerms( nodeURI ) {
        
        if( nodeURI.startsWith(LLD.GENEURI_PREF) ) {
            //if node is an entrezgene id
            return annotation[nodeURI]
        }
        else if( ngsResGraph!=null ) { 
            //else is a diff result id
            def statements = ngsResGraph.tuplePattern( ngsResGraph.URIref(nodeURI), ngsResGraph.URIref(LLD.GENE_GOTERM), null )
            
            return statements.collect{ 
                def str = it.object.stringValue(); 
                str.substring( str.indexOf(LLD.GO_PREF) )
            } as Set
        }
        
        return rnull
    }
    
}

