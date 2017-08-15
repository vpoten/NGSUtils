/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils

import groovy.json.JsonSlurper

/**
 *
 * @author victor
 */
public class JBrowseDataManager {

    //jbrowse track generation constants
    public static final String JBROWSE_VERSION_HEADER = "//jbrowse_version"
    public static final String JBROWSE_VERSION = "1.9"// TODO update jbrowse and test 

    static private final String REFSEQS_FILE =  "refSeqs.json"
    static private final String TRACKLIST_FILE =  "trackList.json"
    static private final String TRACKDATA_FILE =  "trackData.jsonz"
    static private final String TRACKDATA_ZFILE =  "trackData.jsonz"
    static private final boolean COMPRESS_DATA = false
    
    static protected final String TEMPLATE_GENE = '?id={id}&name={name}&strand={strand}&start={start}&end={end}&seq_id={seq_id}'
    static protected final String TEMPLATE_BED = '?strand={strand}&start={start}&end={end}&seq_id={seq_id}'
    static protected final String CODE_FEAT_SEQID = '{"seq_id": "sub {shift->seq_id;}"}'
    static protected final String CODE_FEAT_SEQID_JSON = "{\"extraData\": ${CODE_FEAT_SEQID}}"

    static String jbrowsePath


    /**
     * @return refseqs file constant
     */
    static String refseqsFile(){
        return REFSEQS_FILE
    }
    
    /**
     *
     * @param jpath : JBrowse bin path
     */
    static def config( String jpath ){
        jbrowsePath = jpath

        if( !jbrowsePath.endsWith(File.separator) )
            jbrowsePath += File.separator
    }

    /**
     * gets the version header for trackInfo files
     */
    static String versionHeader(){
        return "${JBROWSE_VERSION_HEADER} ${JBROWSE_VERSION}"
    }

    static String getVersion(String header){
        return header.substring( header.lastIndexOf(' ')+1 )
    }
    
    static String trackDataFile(){
        return (COMPRESS_DATA) ? TRACKDATA_ZFILE : TRACKDATA_FILE
    }

    /**
     * runs generate-names.pl JBrowse program
     */
    static protected boolean generateNames( String outDir ){
        def params = ["${jbrowsePath}bin/generate-names.pl", '--dir', outDir]
        params = params.findAll{it}.collect{it as String}//cast to String
        
        return Utils.procBuildRun(params, jbrowsePath)
    }

    /**
     * prepare a sequence track using bin/prepare-refseqs.pl
     */
    static boolean prepareSequence( String faFile, String outDir, boolean noseq, String seqDir='' ){
        String optNoseq = (noseq) ? '--noseq' : ''
        String optSeqDir = (seqDir) ? "--seqdir ${seqDir}" : ''
        
        // generate gff of sequences from fasta index
        def strb = new StringBuilder()
        strb << "##gff-version\t3\n"
        
        new File(faFile+'.fai').splitEachLine("\\s"){ toks->
            if( !toks[0].toLowerCase().contains('random') )
                strb << "##sequence-region\t${toks[0]}\t1\t${toks[1]}\n"
        }
        
        def seqsFile = outDir+'sequences.gff'
        new File(seqsFile).text = strb.toString()
        
        if( noseq )
            Utils.createDir(outDir+'seq')
        
        def params = ["${jbrowsePath}bin/prepare-refseqs.pl", '--gff', seqsFile, '--out', outDir, optSeqDir, optNoseq]
        
        params = params.findAll{it}.collect{it as String}//cast to String
        
        if( !Utils.procBuildRun(params, jbrowsePath) )
            return false

        //post process refSeqs; fix sequence names
        postProcessRefSeqs(outDir)
        
        "rm -f ${seqsFile}".execute()

        return true
    }
    
    /**
     * generates outDir/seq/refSeqs.json
     */
    static boolean generateRefSeqsJson(faFile, outDir){
        Utils.createDir(outDir+'seq')
        def sequences = []
        
        new File(faFile+'.fai').splitEachLine("\\s"){ toks->
            if( !toks[0].toLowerCase().contains('random') ){
                sequences << ['start':0, 'name':toks[0], 'length':toks[1], 'end':((toks[1] as Long) -1)]
            }
        }
        
        def writer = new FileWriter(outDir+'seq/'+REFSEQS_FILE)
        def builder = new groovy.json.JsonBuilder()
        builder.call(sequences)
        builder.writeTo(writer)
        writer.close()
        
        postProcessRefSeqs(outDir)
        
        return true
    }

    /**
     *
     * @param featFile : features file to prepare
     * @param outDir : ends with '/'
     * @param label : track label
     * @param dataRoot : not used
     */
    static boolean prepareFeatTrack( String featFile, String outDir, String label, String dataRoot, Map options=null ){
        options = addKeyOption(options, label)
        String strOpts = options ? buildOptions(options) : ''

        String typeFeat = ''

        if( [".gff3",".gff"].any{ featFile.toLowerCase().endsWith(it) } )
            typeFeat='--gff'
        else if( featFile.toLowerCase().endsWith(".bed") )
            typeFeat='--bed'
        else
            return false

        //build params list
        def params = ["${jbrowsePath}bin/flatfile-to-json.pl", '--out', outDir, typeFeat, featFile, '--tracklabel', label]
        if( options ){
            options.each{ k, v->
                params << "--${k}"
                if(v) params << v
            }
        }

        params = params.findAll{it}.collect{it as String}//cast to String

        if( !Utils.procBuildRun(params, jbrowsePath) )
            return false

        if( options && options.autocomplete )
            generateNames(outDir)

        return true
    }

    /**
     * prepare a BAM track (alignments)
     */
    static boolean prepareBamTrack(String bamFile, String outDir, String label, Map options=null){
        options = addKeyOption(options, label)
        String strOpts = options ? buildOptions(options) : ''

        def params = ["${jbrowsePath}bin/bam-to-json.pl", '--out', outDir, '--tracklabel', label, '--bam', bamFile, strOpts]
        params = params.findAll{it}.collect{it as String}//cast to String
        
        if( !Utils.procBuildRun(params, jbrowsePath) )
            return false

        return true
    }


    static boolean prepareWigTrack(String wigFile, String outDir, String tileDir, String label, Map options=null){
        options = addKeyOption(options, label)
        String strOpts = options ? buildOptions(options) : ''

        def params = ["${jbrowsePath}bin/wig-to-json.pl", '--out', outDir, '--tile', tileDir, '--tracklabel', label, '--wig', wigFile, strOpts]
        params = params.findAll{it}.collect{it as String}//cast to String
        
        if( !Utils.procBuildRun(params, jbrowsePath) )
            return false

        return true
    }


    
    static protected buildOptions(Map opts){
        String options = ''
        opts.each{ k, v-> options+="--${k} ${v} " }

        if( !options.isEmpty() )
            options = options.substring(0, options.length()-1)
            
        return options
    }

    
    /**
     * post process refSeqs.js, fix sequence names, removes randoms and non-chromosomes
     */
    static protected postProcessRefSeqs(String outDir){

        String refFile = outDir+'seq/'+refseqsFile()

        def reader = new BufferedReader(new InputStreamReader(new FileInputStream(refFile)))

        //parse original file
        String header = ''
            
        def jsondata = new JsonSlurper().parse(reader)
        reader.close()
        
        //process data, fix names
        def processed = []

        //remove random sequences, make them start with 'chr'
        jsondata.each{ val ->
            if( jsondata.size()==1 ){
                //if is an unique chromosome call it 'chr'
                val.name = 'chr'
                processed << val
            }
            else if( !val.name.toLowerCase().contains('random') ){
                //if is not a random treat it
                if( val.name.toLowerCase().startsWith('chr') ){
                    processed << val
                }
                else{
                    int pos = val.name.toLowerCase().indexOf('chr')
                    if( pos>=0 ){
                        val.name = val.name.substring(pos)
                        processed << val
                    }
                }
            }
        }

        processed = processed.sort{it.name}//sort by name

        //write new data to refSeqs file
        def writer = new PrintWriter(refFile)
        
        
        def builder = new groovy.json.JsonBuilder()
        builder.call(processed)
        builder.writeTo(writer)
        writer.close()
    }


    /**
     * creates the sequence and refGene annotation tracks for the given organisms
     *
     * @param root : base dir
     * @param idxDir : bowtie indexes dir
     * @param annotDir : annotation dir
     * @param urlTemplateBase : url template base for clicked features
     * @param p_organisms : list of organisms to annotate
     */
    static def createAnnotationTracks( String root, String idxDir, String annotDir, String urlTemplateBase, List p_organisms=null ){

        [root, idxDir, annotDir].each{ str->
            if( !str.endsWith(File.separator) )
                str += File.separator
        }

        if( !Utils.createDir(root) )
            return false

        if( !p_organisms )
            p_organisms = AnnotationDB.organisms

        for( String orgId : p_organisms ){
            def orgData = AnnotationDB.organismData(orgId)

            if( !orgData ){
                System.err.println("Error: Organism with id ${orgId} not found.")
                continue
            }

            String outDir = orgUrlBase(root, orgData)

            if( !Utils.createDir(outDir) )
                return false

            String faFile = idxDir+orgData.assembly+".fa"

            if( new File(faFile).exists() ){
                println "Preparing ${faFile} sequence for ${orgData.name}."
                ///prepareSequence( faFile, outDir, true)
                generateRefSeqsJson(faFile, outDir)
            }
            else{
                System.err.println("Error: Organism ${orgId}, ${faFile} sequence not found.")
                continue
            }

            //prepare features tracks
            def featureFiles = [ 
                [file:AnnotationDB.REFGENE_GFF3_FILE, label:'refSeq_mRNA', options:transcriptOptions(urlTemplateBase)],
                [file:AnnotationDB.REFGENE_GFF3_FILE, label:'refSeq_genes', options:geneOptions(urlTemplateBase)],
                [file:AnnotationDB.ENSGENE_GFF3_FILE, label:'ensembl_mRNA', options:transcriptOptions(urlTemplateBase)],
                [file:AnnotationDB.ENSGENE_GFF3_FILE, label:'ensembl_genes', options:geneOptions(urlTemplateBase)]
            ]

            //generate tracks
            featureFiles.each{ entry ->
                String featFile = annotDir+orgId+File.separator+entry.file

                if( new File(featFile).exists() ){
                    // copy to outdir and uncompress feat file
                    "cp ${featFile} ${root}".execute().waitFor()
                    featFile = root+entry.file
                    "gunzip ${featFile}".execute().waitFor()
                    featFile = featFile.substring(0, featFile.lastIndexOf(".gz"))

                    println "Preparing ${entry.label} track for ${orgData.name}."
                    prepareFeatTrack( featFile, outDir, entry.label, '', entry.options )
                    "rm ${featFile}".execute().waitFor()//delete temp file
                }
            }

        }
    }

    /**
     * builds organism URL base
     */
    static def orgUrlBase( String urlBase, Map orgData, String version=null ){

        if( !version )
            version = JBROWSE_VERSION

        return "${urlBase}${version}/${orgData.assembly}/"
    }

    /**
     * add the key option if is not setted
     *
     * @return the options Map
     */
    static protected addKeyOption(Map opts, String label){
        if( opts==null )
            opts = [:]

        if( !opts.key )
            opts.key = label

        return opts
    }

    /**
     * returns options for transcript feature track
     */
    static def transcriptOptions(String urlTemplateBase=null){
        def options = [:]

        if( urlTemplateBase ){
            options.urlTemplate = urlTemplateBase+TEMPLATE_GENE
            options.clientConfig = CODE_FEAT_SEQID_JSON
        }

        options.getLabel = ''
        options.getSubs = ''
        options.type = 'mRNA'
        options.cssclass = 'transcript'
        options.arrowheadClass = 'transcript-arrowhead'
        options.subfeatureClasses='{"CDS":"transcript-CDS", "exon":"transcript-exon", "five_prime_UTR":"transcript-five_prime_UTR", "three_prime_UTR":"transcript-three_prime_UTR"}'
        if( COMPRESS_DATA )
            options.compress = ''
        options.autocomplete = 'all'
        return options
    }

    /**
     * returns options for gene feature track
     */
    static def geneOptions(String urlTemplateBase=null){
        def options = [:]
        
        if( urlTemplateBase ){
            options.urlTemplate = urlTemplateBase+TEMPLATE_GENE
            options.extraData = CODE_FEAT_SEQID
        }

        options.getLabel = ''
        options.getSubs = ''
        options.type = 'gene'
        options.cssclass = 'feature5'
        if( COMPRESS_DATA )
            options.compress = ''
        options.autocomplete = 'all'
        return options
    }

    /**
     * returns options for bed feature track
     */
    static def bedOptions(String urlTemplateBase=null){
        def options = [:]

        if( urlTemplateBase ){
            options.urlTemplate = urlTemplateBase+TEMPLATE_BED
        }

        if( COMPRESS_DATA )
            options.compress = ''
        
        return options
    }


    /**
     * generates trackInfo element for a track
     *
     * @return a Map
     */
    static public def genTrackInfo(String label, String file){

        String trackUrl = "tracks/{refseq}/${label}/${trackDataFile()}"

        if( file.endsWith('.wig') )//urls for image tracks are different
            trackUrl = "tracks/{refseq}/${label}.json"

        [ 'url' : trackUrl,
        'type' : (!file.endsWith('.wig')) ? 'FeatureTrack' : 'ImageTrack',
        'label' : label,
        'key' : label ]
    }

    /**
     * generates a json trackInfo file for a list of elements
     *
     * @return a String
     */
    static public def genTrackInfoJson(List trackInfoData){
        def writer = new StringWriter()
        writer.write("trackInfo = \n")
        def builder = new groovy.json.JsonBuilder()
        builder.call(trackInfoData)
        builder.writeTo(writer)
        writer.close()
        
        return writer.getBuffer().toString()
    }

}

