/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.diff

import org.ngsutils.Utils
import org.ngsutils.semantic.LinkedLifeData as LLD
import org.ngsutils.semantic.NGSResults as NGSR
import org.ngsutils.semantic.rdfutils.SimpleGraph
import org.ngsutils.diff.DiffExpresionFileTool as DEFT
import org.ngsutils.annotation.GeneIndex
import org.ngsutils.annotation.IsoAnnotation

import org.openrdf.model.URI
import org.openrdf.model.Value
import org.openrdf.model.BNode
import org.openrdf.model.vocabulary.XMLSchema as XML
import org.openrdf.model.vocabulary.RDF as RDF

/**
 *
 * @author victor
 */
class DiffExpresionRdfGen {
    
    SimpleGraph graph
    
    private static String featIdKey = 'id/'
    
    /**
     * Generates a graph for a .diff file (without .gz extension), using baseURI 
     * as context. The generated statements are added to this.graph variable.
     * Generated resources have the form baseURI+'/'+featIdKey+'/'+<ID>
     */
    def genGraph(String file, String baseURI){
        
        if( !baseURI.endsWith('/') )
            baseURI+='/'
        
        //create a reader for the .diff file
        def reader = Utils.createReader( Utils.getFileIfCompress(file) )
        def context
        
        if( !graph ){
            graph = new SimpleGraph(true) // creates a graph with rdf inferencing
            context = graph.URIref(baseURI) // generate context
             // load the ngsengine results schema
            def istr = this.getClass().getClassLoader().getSystemResourceAsStream(NGSR.resultsNGSFile)
            graph.addInputStream(istr, SimpleGraph.RDFXML, context)
        }
        else{
            // generate context
            context = graph.URIref(baseURI)
        }
        
        reader.readLine()//skip header
        
        //generates DNAFeature graph
        reader.splitEachLine('\\s'){ tokens->
            if( getField(file, DEFT.F_SIGNIF, tokens)=='yes' ){
                //for significant diff expressed features
                
                //add feature - type triple
                URI featURI = graph.URIref(baseURI+featIdKey+getField(file,DEFT.F_TEST_ID,tokens))
                graph.add(featURI, RDF.TYPE, graph.URIref(getFeatType(file)), context)

                String locus = getField(file,DEFT.F_LOCUS,tokens)
                def mat =  (locus =~ /(\w+):(\d+)-(\d+)/)
                
                if( mat ){
                    //add feature - chrom triple
                    graph.add(featURI, graph.URIref(NGSR.NGS_CHROM), graph.Literal(mat[0][1],XML.STRING), context)

                    //add feature - start triple
                    graph.add(featURI, graph.URIref(NGSR.NGS_START), graph.Literal(mat[0][2],XML.LONG), context)

                    //add feature - end triple
                    graph.add(featURI, graph.URIref(NGSR.NGS_END), graph.Literal(mat[0][3],XML.LONG), context)
                }

                //add feature - geneRef triple
                if( getField(file,DEFT.F_GENE,tokens)!='-' )
                    graph.add( featURI, graph.URIref(NGSR.NGS_GENEREF), graph.Literal(getField(file,DEFT.F_GENE,tokens),XML.STRING), context)

                //add feature - diff expr triples using bnode
                BNode bnode = graph.bnode()
                graph.add( featURI, graph.URIref(getDiffExprType(file)), bnode, context)

                //add bnode type
                graph.add( bnode, RDF.TYPE, graph.URIref(NGSR.TYPE_DIFFEXPR), context)

                //add sample1 to bnode
                graph.add( bnode, graph.URIref(NGSR.NGS_SAMPLE1), graph.Literal(getField(file,DEFT.F_SAMPLE1,tokens),XML.STRING), context)

                //add sample2 to bnode
                graph.add( bnode, graph.URIref(NGSR.NGS_SAMPLE2), graph.Literal(getField(file,DEFT.F_SAMPLE2,tokens),XML.STRING), context)

                //add value1 to bnode
                graph.add( bnode, graph.URIref(NGSR.NGS_VALUE1), graph.Literal(getField(file,DEFT.F_VAL1,tokens),XML.DOUBLE), context)

                //add value2 to bnode
                graph.add( bnode, graph.URIref(NGSR.NGS_VALUE2), graph.Literal(getField(file,DEFT.F_VAL2,tokens),XML.DOUBLE), context)

                //add pvalue to bnode
                graph.add( bnode, graph.URIref(NGSR.NGS_PVALUE), graph.Literal(getField(file,DEFT.F_PVAL,tokens),XML.DOUBLE), context)
            }
        }
        
        reader.close()
        
        // generates references using associated .gtf files
        [DEFT.FEATOVER_FILE_EXT, DEFT.FEATUNDER_FILE_EXT].each{
            GeneIndex gindex = new GeneIndex( Utils.getFileIfCompress(file+it) )
            
            gindex.getIsoforms().each{ iso->
                URI featURI = graph.URIref(baseURI+featIdKey+iso.transcriptId )
                
                //add cuffClassCode to Isoform
                if( iso.exons[0].classCode )
                    graph.addIfNot(featURI, graph.URIref(NGSR.NGS_CUFFCLASS), graph.Literal(iso.exons[0].classCode,XML.STRING), context)
        
                //add nearestRef to Isoform
                if( iso.exons[0].nearestRef )
                    graph.addIfNot(featURI, graph.URIref(NGSR.NGS_NEARREF), graph.Literal(iso.exons[0].nearestRef,XML.STRING), context)
                    
                //add strand
                graph.addIfNot(featURI, graph.URIref(NGSR.NGS_STRAND), graph.Literal(iso.exons[0].strand.token as String,XML.STRING), context)
                
                //add hasPart
                if( iso.primTranscript ){
                    def primTrans = graph.URIref(baseURI+featIdKey+iso.primTranscript.tssId)
                    def gene = graph.URIref(baseURI+featIdKey+iso.gene.geneId)
                    
                    graph.addIfNot( primTrans, graph.URIref(NGSR.NGS_HASPART), featURI, context)
                    graph.addIfNot( gene, graph.URIref(NGSR.NGS_HASPART), primTrans, context)
                }
                else{
                    def gene = graph.URIref(baseURI+featIdKey+iso.gene.geneId)
                    graph.addIfNot( gene, graph.URIref(NGSR.NGS_HASPART), featURI, context)
                }
            }
        }
        
    }
    
    /**
     * returns the appropiated feature type given a .diff file
     */
    protected def getFeatType(String file){
        if( file.indexOf(DEFT.isoformDiffFile)>=0 )
            return NGSR.TYPE_ISOFORM
        if( file.indexOf(DEFT.geneDiffFile)>=0 )
            return NGSR.TYPE_GENE
        if( file.indexOf(DEFT.tssDiffFile)>=0 )
            return NGSR.TYPE_PRIMTRANS
        if( file.indexOf(DEFT.cdsDiffFile)>=0 )
            return NGSR.TYPE_GENE
        if( file.indexOf(DEFT.promOverFile)>=0 )
            return NGSR.TYPE_GENE
        if( file.indexOf(DEFT.cdsOverFile)>=0 )
            return NGSR.TYPE_GENE
        if( file.indexOf(DEFT.splicOverFile)>=0 )
            return NGSR.TYPE_PRIMTRANS
    }
    
    /**
     * returns the appropiated diffExpressed subproperty given a .diff file
     */
    protected def getDiffExprType(String file){
        if( !DEFT.isFileOverloadTest(file) )
            return NGSR.PROP_DIFFEXPR
            
        //subproperties: overloaded diff expression
        if( file.indexOf(DEFT.cdsOverFile)>=0 )
            return NGSR.PROP_OVERCDS
        if( file.indexOf(DEFT.splicOverFile)>=0 )
            return NGSR.PROP_OVERISO
        if( file.indexOf(DEFT.promOverFile)>=0 )
            return NGSR.PROP_OVERPRIM
    }
    
    /**
     * returns the field value given a .diff file, a field index (DEFT.F_*) and 
     * the tokens list
     */
    protected def getField(String file, int field, tokens){
        return DEFT.getField(field, file, tokens)
    }
    
    /**
     * add to the graph the annotation of each isoform
     * 
     * @param isoforms: collection of annotated isoforms
     */
    def genAnnotation(Collection isoforms, String baseURI){
        
        if( !baseURI.endsWith('/') )
            baseURI+='/'
            
        def context = graph.URIref(baseURI) // generate context
        
        isoforms.each{ iso->
            URI featURI = graph.URIref(baseURI+featIdKey+iso.transcriptId)
            //add sequence
            if( iso.sequence )
                graph.add(featURI, graph.URIref(NGSR.NGS_SEQUENCE), graph.Literal( iso.sequence.seqString(),XML.STRING), context)
            
            //add GO terms
            iso?.isoAnnotation?.goTerms.each{
                graph.add(featURI, graph.URIref(LLD.GENE_GOTERM), graph.URIref( LLD.GOURI_PREF+'/'+it), context)
            }
            
            //add accessions
            iso?.isoAnnotation?.accessions.each{
                graph.add(featURI, graph.URIref(NGSR.NGS_ACCESSION), graph.Literal(it,XML.STRING), context)
            }
            
            //add exons
            String exonstr = iso.exons.sum{ "${it.start}-${it.end}," }
            graph.add(featURI, graph.URIref(NGSR.NGS_EXONS), graph.Literal(exonstr,XML.STRING), context)
        }
    }
    
    /**
     * 
     * generates annotations using associated over/under.gtf files, for testing purposes only.
     * 
     * @param file: a String with .diff file path
     */
    def genNaiveAnnotation(String file, lifeData, String baseURI, taxonomyId=null ){
        
        def isoforms = []
            
        [DEFT.FEATOVER_FILE_EXT, DEFT.FEATUNDER_FILE_EXT].each{
            GeneIndex gindex = new GeneIndex( Utils.getFileIfCompress(file+it) )
            
            gindex.getIsoforms().each{ iso->
                if( iso.exons[0].geneName ){
                    //add geneName as accession
                    iso.isoAnnotation = new IsoAnnotation()
                    iso.isoAnnotation.accessions = [iso.exons[0].geneName]
                 
                    //add associated GO terms
                    iso.isoAnnotation.goTerms = 
                        lifeData.getGOTermsBySymbol( iso.exons[0].geneName, taxonomyId).collect{ 
                            String str = it['go'];
                            str.substring( str.indexOf(LLD.GO_PREF) ) 
                        }
                }
            }
            
            isoforms += gindex.getIsoforms()
        }
        
        genAnnotation(isoforms, baseURI)
    }
    
    
    /**
     * generates triples of overrepresented terms
     * 
     * @param testMap : map (key=GO terms) with test p-values
     * @param corrMap : map (key=GO terms) with corrected p-values
     * @param baseURI
     * @param overProperty : NGSResults.PROP_OVEREPOVER or PROP_OVEREPUNDER
     */
    def genOverepTerms( testMap, corrMap, String baseURI, overProperty ){
        if( !baseURI.endsWith('/') )
            baseURI+='/'
            
        def context = graph.URIref(baseURI) // generate context
        
        def overPropURI = graph.URIref(overProperty)
        
        testMap.keySet().each{ term->
            
            BNode bnode = graph.bnode()
            graph.add( graph.URIref(LLD.GOURI_PREF+'/'+term), overPropURI, bnode, context)
            
            //add test pvalue
            graph.add( bnode, graph.URIref(NGSR.NGS_TESTPVALUE), 
                graph.Literal(testMap[term] as String, XML.DOUBLE), context)
                
            //add corr pvalue
            graph.add( bnode, graph.URIref(NGSR.NGS_CORRPVALUE), 
                graph.Literal(corrMap[term] as String, XML.DOUBLE), context)
            
        }
    }
    
}

