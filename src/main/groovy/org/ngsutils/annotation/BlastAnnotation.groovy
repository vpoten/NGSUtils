/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation

import org.ngsutils.semantic.LinkedLifeData
import org.biojava.bio.BioException
import org.biojava.bio.seq.Sequence
import org.biojava.bio.seq.DNATools
import org.biojava.bio.seq.SequenceTools
import org.biojavax.bio.seq.RichSequence
import org.biojava.bio.seq.StrandedFeature
import org.ngsutils.annotation.biojava.RemoteQBlastServiceNGS
import org.ngsutils.annotation.IsoAnnotation
import org.ngsutils.ontology.GOManager
import org.biojavax.bio.alignment.blast.RemoteQBlastAlignmentProperties
import org.biojavax.bio.alignment.blast.RemoteQBlastOutputProperties
import org.biojavax.bio.alignment.blast.RemoteQBlastOutputFormat

import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity

/**
 * BLAST configuration parameters
 * 
 */
class BlastConfig {
    boolean localBlast = false
    String blastUrl
    String db = 'nr'
    String email = 'ngsengine@gmail.com'
    int numHits = 20
    double evalue = 1e-3
    String program = 'blastx'
    int hspLenCut = 33
    int annotCut = 55
    int goWeight = 5
}

/**
 * class that performs the annotation of transcripts using BLAST
 * search.
 * 
 * @author victor
 */
class BlastAnnotation {
    
    BlastConfig blastConfig = new BlastConfig()
    GOManager goManager = null
    LinkedLifeData lifeData = null
    
    protected static final String CMD_FAIDX = 'samtools faidx'
    protected static final String GO_PREF = 'GO:'
    protected static final String ENZC_PREF = 'EC:'
    protected static final String KEGG_PREF = 'KEGG:'
    
    /**
     * a getter method
     */
    BlastConfig getBlastConfig(){
        return blastConfig
    }
    
    /**
     * obtains the sequence of an isoform 
     */
    static def getSequence(Isoform iso, String fasta){
        String regions = ''
            
        iso.exons.each{ exon->
            regions += "${exon.seqName}:${exon.start}-${exon.end} "
        }

        def p = "${CMD_FAIDX} ${fasta} ${regions}".execute()

        if( p.waitFor()==0 ){
            try {
                //read sequences from FASTA file
                def seqIter = RichSequence.IOTools.readFastaDNA(new BufferedReader(new StringReader(p.text)),null)

                String seqStr = ''
                while( seqIter.hasNext() ){
                    seqStr += seqIter.nextSequence().seqString()
                }

                //return the sequence for the current isoform
                def seq = DNATools.createDNASequence(seqStr, iso.transcriptId)
                return (iso.exons[0].strand==StrandedFeature.NEGATIVE) ? SequenceTools.reverseComplement(seq) : seq
            } catch(BioException e){
                return null
            }
        }
        
        return null
    }
    
    /**
     * obtains the sequence of each isoform and assigns the isoform.sequence field
     */
    static def getSequences(Collection isoforms, String fasta){
        if( !(new File(fasta).exists()) )
            return null
            
        isoforms.each{ iso ->
            iso.sequence = getSequence(iso, fasta)
        }
        
        return isoforms
    }
    
    /**
     * creates a map of {isoform.id : isoform.sequence} pairs
     */
    static def extractSeqMap(Collection isoforms){
        def map = [:]
        
        isoforms.each{ iso ->
            if(iso.sequence)
                map[iso.transcriptId] = iso.sequence
        }
        
        return map
    }
    
    /**
     * do BLAST against NCBI server
     */
    protected def doRemoteBlast(sequence){
        try {
            RemoteQBlastServiceNGS rbw = new RemoteQBlastServiceNGS(blastConfig.blastUrl)
            rbw.numHits = blastConfig.numHits
            rbw.email = blastConfig.email
            
            RemoteQBlastAlignmentProperties rqb = new RemoteQBlastAlignmentProperties()
            //rqb.setBlastProgram( blastConfig.program ) //biojava bug
            rqb.setAlignementOption( 'PROGRAM', blastConfig.program )
            rqb.setBlastDatabase( blastConfig.db )
            
            String rid = rbw.sendAlignmentRequest(sequence, rqb)

            boolean wasBlasted = false

            while (!wasBlasted) {
                Thread.sleep(5000)//wait 5 seconds
                wasBlasted = rbw.isReady(rid, System.currentTimeMillis())
            }

            RemoteQBlastOutputProperties rof = new RemoteQBlastOutputProperties();
            rof.setOutputFormat(RemoteQBlastOutputFormat.XML);
            rof.setAlignmentOutputFormat(RemoteQBlastOutputFormat.PAIRWISE);
            rof.setAlignmentNumber(0);

            InputStream is = rbw.getAlignmentResults(rid, rof)
            
            return parseBlastOutput(is)
        }
        catch(BioException bio){
            return null
        } 
    }
    
    /**
     * do BLAST against a  wwwblast server
     */
    protected def doLocalBlast(sequence){
        // POST data to server
        
        def httpclient = new DefaultHttpClient()
        
        try {
            def formparams = []
            formparams << new BasicNameValuePair('PROGRAM', blastConfig.program)
            formparams << new BasicNameValuePair('DATABASE', blastConfig.db)
            formparams << new BasicNameValuePair('QUERY', sequence.seqString())
            formparams << new BasicNameValuePair('ALIGNMENTS', '0')
            formparams << new BasicNameValuePair('XML_OUTPUT', 'yes')
            formparams << new BasicNameValuePair('EXPECT', blastConfig.evalue as String)
            def entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            
            HttpPost httppost = new HttpPost(blastConfig.blastUrl)
            httppost.setEntity(entity)

            def response = httpclient.execute(httppost)
            
            return parseBlastOutput(response.entity.content)
        }
        catch(e){
            return null
        }
        finally {
            httpclient.getConnectionManager().shutdown()
        }
    }
    
    /**
     * BLAST a sequence and returns a list with hits
     */
    def doBlast(sequence){
        if( sequence instanceof String ){
            //if is a single fasta sequence convert to Sequence
            def seqIter = RichSequence.IOTools.readFastaDNA(new BufferedReader(new StringReader(sequence)),null)
            sequence = seqIter.nextSequence()
        }
     
        return blastConfig.localBlast ? doLocalBlast(sequence): doRemoteBlast(sequence)
    }
    
    /**
     * BLAST the map of sequences and returns a map with the hitList of each
     * sequence.
     * 
     * @param seqMap: map with key=seq_id and value=sequence
     * @return a Map with key=seq_id and value=hitList of hitMap
     */
    def doBlast(Map seqMap){
        def result = [:]
        seqMap.each{ 
            def hitList = doBlast(it.value)
            
            if( hitList )
                result[(it.key)] = hitList
        }
        return result
    }
    
    
    /**
     * calculates annotation scores for the mapped GO terms and selects the lowest 
     * term per branch that lies over a user defined threshold (blastConfig.annotCut)
     * 
     * @return a Set<String> of selected terms
     */
    def annotate(hitList){
        def goTermsSet = hitList.collect{it.goTerms}.flatten() as Set
        goTermsSet.remove(null)
        
        //calculates annotation scores 
        def scores = [:]
        goTermsSet.each{ goTerm-> 
            scores[(goTerm)] = annotationScore(goTerm, hitList, goTermsSet)
        }
        
        def selected = [] as Set
        
        //selects the lowest term per branch that lies over a user defined threshold
        
        //get leaves (#GO==0)
        def leaves = scores.findAll{ it.value['#GO']==0 }.collect{it.key}
        def internals = scores.findAll{ it.value['#GO']>0 }.collect{it.key}
        
        //process branches
        leaves.each{ l->
            def branch = [l]  
            
            branch += goManager.getAncestors(l).
                collect{ it.substring(it.indexOf(GO_PREF)) }.
                findAll{ anc-> internals.any{anc==it} }
            
            branch = branch.sort{ scores[it]['#GO'] }
            def node = branch.find{ scores[it]['AS']>=blastConfig.annotCut }
            
            if(node)
                selected << node
        }
        
        return selected
    }
    
    /**
     * calculates annotation score for a candidate GO term
     * 
     * @return a Map with keys {DT,AT,AS,#GO}
     */
    protected def annotationScore(goTerm, hitList, goTermsSet){
        
        int maxSimilarity = hitList.findAll{ goTerm in it.goTerms }.
                collect{ it.hsps.collect{hsp-> hsp.similarity}.max() }.
                max()
            
        int directTerm = maxSimilarity*ecWeight(goTerm)
        
        int narrowTerms = goTermsSet.sum{ 
            (it!=goTerm && goManager.getAncestors(it).any{anc-> anc.contains(goTerm)}) ? 1 : 0 
        }
        
        int abstractTerm = blastConfig.goWeight*(narrowTerms-1)
        
        return [ 'DT':directTerm, 'AT':abstractTerm, 'AS':directTerm+abstractTerm, '#GO':narrowTerms ]
    }
    
    /**
     * evidence code weight for a GO term
     */
    protected def ecWeight(goTerm){
        return 1
    }

    /**
     * Returns a list of maps with keys: {id, accession, len, goTerms, hsps:{evalue,similarity...} }.
     * Previously filters the results using blastConfig.evalue and 
     * blastConfig.hspLenCut
     * 
     * @param blastOutput : an InputStream
     * 
     */
    protected def parseBlastOutput(blastOutput){
         def hits = []
         
        //parse XML output
        def parser = new XmlSlurper()
        parser.setFeature('http://apache.org/xml/features/nonvalidating/load-external-dtd', false);
        parser.setFeature('http://xml.org/sax/features/namespaces', false) 
        def records = parser.parse(blastOutput)
        blastOutput.close()
        
        records.BlastOutput_iterations.Iteration.Iteration_hits?.Hit.each{ hit->
            def hitMap = [:]
            
            hitMap.id = hit.Hit_id.text()
            hitMap.accession = hit.Hit_accession.text()
            hitMap.len = hit.Hit_len.toInteger()
            hitMap.hsps = []
            
            hit?.Hit_hsps.Hsp.each{ hsp->
                def hspMap = [:]
                
                hspMap.evalue = hsp.Hsp_evalue.toDouble()
                hspMap.queryFrom = hsp.'Hsp_query-from'.toInteger()
                hspMap.queryTo = hsp.'Hsp_query-to'.toInteger()
                hspMap.hitFrom = hsp.'Hsp_hit-from'.toInteger()
                hspMap.hitTo = hsp.'Hsp_hit-to'.toInteger()
                hspMap.identity = hsp.Hsp_identity.toInteger()
                hspMap.positive = hsp.Hsp_positive.toInteger()
                hspMap.alignLen = hsp.'Hsp_align-len'.toInteger()
                //calc similarity
                hspMap.similarity = (int)(hspMap.positive*100 / (double)hspMap.alignLen)
                //calc hit coverage
                hspMap.hitCover = (int)(hspMap.alignLen*100 / (double)hitMap.len)
                
                if( hspMap.evalue<=blastConfig.evalue && hspMap.hitCover>=blastConfig.hspLenCut )
                    hitMap.hsps << hspMap
            }
            
            if( hitMap.hsps )
                hits << hitMap
        }
        
        //limit hitlist size
        if( hits.size() > blastConfig.numHits )
            hits = hits[0..blastConfig.numHits-1]
        
        //set GO terms to each hitMap
        hits.each{ hitMap->
            def termSet = mapGOTerm(hitMap)

            if( termSet )
                hitMap.goTerms = termSet
        }
            
        
        return hits
    }
    
    /**
     * returns a set of GO terms mapped to the accession id of the hitMap
     * 
     * @return a Set<Strings> ('GO:*')
     */
    protected def mapGOTerm(Map hitMap){
        //NCBI FASTA sequence ID
        //id format : gi|<id>|<type>|<accession>|<desc.>
        def tokens = hitMap.id.split('\\|')
        
        if( tokens[2]!='ref' )
            return null
        
        def terms = lifeData.queryProtGOTerms(tokens[3])
        def set = [] as Set
        
        terms.each{ row->
            row.each{ k, v->
                set.add( v.substring(v.indexOf(GO_PREF)) )
            }
        }
        
        return set
    }
    
    
    /**
     * Annotates a collection of isoformss, setting the sequence and 
     * isoAnnotation fields
     */
    def annotateIsoforms(Collection isoforms, String fasta){
        isoforms = getSequences(isoforms, fasta)
        
        def seqMap = extractSeqMap(isoforms)
        
        def hitListMap = doBlast(seqMap)
        
        hitListMap.each{ key, hitList -> 
            def selected = annotate(hitList)
            def iso = isoforms.find{ it.transcriptId==key }
            
            if( !iso.isoAnnotation )
                iso.isoAnnotation = new IsoAnnotation(goTerms:[],ecTerms:[],keggPaths:[],accessions:[])
                
            iso.isoAnnotation.goTerms += selected.findAll{it.startsWith(GO_PREF)}
            
            //add existing enzyme codes/kegg reaction
            iso.isoAnnotation.goTerms.each{ term->
                def notations = goManager.getNotations(term)
                
                iso.isoAnnotation.ecTerms += notations.findAll{it.startsWith(ENZC_PREF)}
                iso.isoAnnotation.keggPaths += notations.findAll{it.startsWith(KEGG_PREF)}
            }
            
            //add accession ids
            iso.isoAnnotation.accessions = hitList.collect{ it.id }
        }
    }
    
    
}

