/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils

import org.biojava.nbio.core.sequence.DNASequence
import org.biojava.nbio.core.sequence.io.FastaWriterHelper
import org.biojava.nbio.core.sequence.AccessionID
import groovy.json.*

/**
 *
 * @author victor
 */
class EnsemblUtils {
    
    static final CDS_CODE = 'cds:KNOWN_protein_coding'
    static final CDNA_PSEUDO_CODE = 'cdna:KNOWN_processed_pseudogene'
    static final CDNA_CDS_CODE = 'cdna:KNOWN_protein_coding'
    static final DNA_SEQ_CODE = 'dna:chromosome'
    static final String TRANS_URL = 
        "http://www.ensembl.org/Homo_sapiens/Export/Output/Transcript?db=core;flank3_display=0;flank5_display=0;g={gene};output=fasta;strand=feature;t={transcript};param=cdna;param=coding;genomic=unmasked;_format=Text";
    
    // transcripted biotypes
    static final def BIOTYPE_TRANS = ['IG_C_gene', 'IG_D_gene', 'IG_gene', 'IG_J_gene', 'IG_LV_gene',
        'IG_M_gene', 'IG_V_gene', 'IG_Z_gene', 'nonsense_mediated_decay', 'nontranslating_CDS', 'non_stop_decay',
        'polymorphic', 'protein_coding', 'TR_C_gene', 'TR_D_gene', 'TR_gene','TR_J_gene', 
        'TR_V_gene', 'miRNA', 'antisense', 'antisense_RNA'] as TreeSet
    
    static final String REST_SERVER = 'http://beta.rest.ensembl.org/'
    static final String REST_EXT = 
        'feature/id/{id}?feature=gene;feature=transcript;species=homo_sapiens;content-type=application/json'
    
    /**
     * composes a transcript sequence file name (fasta)
     * 
     */
    static String transcriptFileName(String transcriptId) {
        return "${transcriptId}.fasta"
    }
    
    /**
     * read fasta sequences from input stream
     * 
     * @return a Map<String,DNASequence>
     */
    protected static def readFasta(reader){
        def seqs = [:] as TreeMap
        
        def seqId = ''
        def str = null
        
        def createSeq = {
            def dnaSeq = new DNASequence(str.toString())
            dnaSeq.accession = new AccessionID(seqId)
            return dnaSeq
        }
        
        reader.eachLine{ line->
            if( !line ){
                //nothing to do
            }
            else if( line.startsWith('>') ){
                if( seqId ){
                    seqs[seqId] = createSeq()
                }
                
                seqId = line.substring(1)
                str = new StringBuilder()
            }
            else
                str << line.trim()
        }
        
        if( !seqs.containsKey(seqId) )//add the last sequence
            seqs[seqId] = createSeq()
            
        reader.close()
        return seqs
    }
    
    /**
     * downloads a transcript sequence from ensembl server
     * 
     */
    static boolean downTranscriptSeq(String geneId, String transcriptId, File outdir) {
        
        def urlSrc = (TRANS_URL.replace('{gene}',geneId).replace('{transcript}',transcriptId)).toURL()
        File fileDst = new File("${outdir.absolutePath}/${transcriptFileName(transcriptId)}")
        
        if( fileDst.exists() && fileDst.size()>0L ) {
            println "EnsemblUtils: ${transcriptFileName(transcriptId)} already exists"
            return true //file already exists
        }
        
        try{ 
            def seqs = readFasta( new BufferedReader(new InputStreamReader(urlSrc.openStream())) )
            
            //get CDS sequence
            def seq = seqs.find{ it.key.contains(CDS_CODE) }
            
            if( !seq ){
                //if CDS not present finds alternatives
                seq = seqs.find{ it.key.contains(CDNA_PSEUDO_CODE) }
                if( !seq ){
                    seq = seqs.find{ it.key.contains(CDNA_CDS_CODE) }
                    if( !seq ){
                        seq = seqs.find{ it.key.contains(DNA_SEQ_CODE) }
                    }
                }
            }
            
            FastaWriterHelper.writeSequence(fileDst, seq.value)
            
        } catch(e) {
            System.err.println("Error downloading sequence for ${geneId}:${transcriptId}")
            return false
        }
        
        return true
    }
    
    /**
     * @return a Map with feature fields, or null if not found.
     * Fields: ID, source, logic_name, external_name, feature_type, description, 
     * end, biotype, seq_region_name, strand, start
     */
    static def getEnsemblFeature(id) {
        def ext = REST_EXT.replace('{id}',id)
        def url = (REST_SERVER+ext).toURL()

        def reader = new InputStreamReader( url.openStream() )
        def slurper = new JsonSlurper()
        def result = slurper.parse(reader)

        return result?.find{ it['ID']==id }
    }
    
    /**
     *
     */
    static boolean isTranscribed(feature) {
        return (feature['biotype'] in BIOTYPE_TRANS)
    }
    
}

