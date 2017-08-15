/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils

import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.bio.program.gff3.*
import org.biojava.ontology.*
import org.biojava.ontology.io.OboParser
import org.ngsutils.annotation.biojava.GFFToolsLight
import org.ngsutils.annotation.biojava.GFF3DocumentHandlerConvert
import org.ngsutils.annotation.biojava.GFFParserLight
import org.ngsutils.annotation.biojava.GtfDocumentHandlerConvert

/**
 * creates the annotation DB or bowtie indexes DB
 *
 * @author victor
 */
class AnnotationDB {

    //organisms ids (NCBI taxonomy id), add new organisms to this list
    public static organisms = [ '6239', '9913', '7955', '7227', '83333', '9031', '9606', '10090',
        '39947', '10116', '4932' ]

    
    //repositories URLs:
    //
    //KEGG pathways
    protected static String KEGG_BASE = "ftp://ftp.genome.jp/pub/kegg/genes/organisms/"
    //UCSC Annotations
    protected static String UCSC_ANN_BASE = "ftp://hgdownload.cse.ucsc.edu/goldenPath/%/database/"
    //UCSC Sequences
    protected static String UCSC_SEQ_BASE = "ftp://hgdownload.cse.ucsc.edu/goldenPath/%/bigZips/"
    //GO Annotations
    protected static String GO_BASE = "ftp://ftp.geneontology.org/pub/go/gene-associations/gene_association.%.gz"
    //Bowtie pre-build indexes
    protected static String BOWTIE_BASE = "ftp://ftp.cbcb.umd.edu/pub/data/bowtie_indexes/"
    //Plant genomes sequences
    protected static String PLANTGDB_BASE = "ftp://ftp.plantgdb.org/download/Genomes/"
    // Plant annotation
    protected static String PLANTGDB_ANN = "http://www.plantgdb.org/yrGATE/%/AnnotationExport.pl?amt=all&format=gff3"
    // Entrez-Gene info base
    protected static String ENTREZ_GENE_INFO = 'ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/'
    
    protected static String UNIPROT_IDS_URL =  'ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/by_organism/'
    
    protected static final UNIPROT_ID_MAPP_SUFF = '_idmapping.dat.gz'

    //output files
    public final static String GOA_FILE = "gene_association.goa.gz"

    public final static String REFGENE_FILE = "refGene.txt.gz"
    protected final static String REFGENE_FILE_TMP = "refGene_tmp"
    public final static String REFGENE_GFF3_FILE = "refGene.gff3.gz"
    public final static String REFGENE_GTF_FILE = "refGene.txt.gtf.gz"
    public final static String UCSC_ENSEMBL_2_GENE = "ensemblToGeneName.txt.gz"
    public final static String ENSGENE_FILE = "ensGene.txt.gz"
    public final static String ENSGENE_GTF_FILE = "ensGene.txt.gtf.gz"
    public final static String ENSGENE_GFF3_FILE = "ensGene.gff3.gz"

    public final static String KEGG_FILE = "kegg.txt.gz"
    protected final static String KEGG_FILE_TMP = "kegg.txt"

    //KEGG file suffixes
    protected static KEGG_ENT_SUF = ".ent"
    protected static KEGG_PATH_SUF = "_pathway.list"
    //Bowtie suffixes
    protected static BOWTIE_SUF = ".ebwt.zip"
    protected static BOWTIE_COLOR_SUF = "_c.ebwt.zip"
    
    //
    static def ecoli_form = [url: 'http://microbes.ucsc.edu/cgi-bin/hgTables',
                    format: 'genepred',
                    compressed: '', //compressed extension
                    parameters: [
                    clade: 'bacteria-gammaproteobacteria',
                    org: 'Escherichia coli K12',
                    db: 'eschColi_K12',
                    hgta_group: 'genes',
                    hgta_track: 'refSeq',
                    hgta_table: 'refSeq',
                    hgta_regionType: 'genome',
                    hgta_outputType: 'primaryTable',
                    hgta_compressType: 'none',
                    submit: 'submit',
                    hgta_doTopSubmit: '1'
                    ] ]

    //TODO : translate ensembl gene code to gene name
    static def osativa_form = [url: 'ftp://ftp.plantbiology.msu.edu/pub/data/Eukaryotic_Projects/o_sativa/annotation_dbs/pseudomolecules/version_6.1/all.dir/all.gff3',
                    format: 'gff3',
                    compressed: ''
                ]

    /**
     * get organism data by taxonomy id, add new organisms to this list
     */
    public static organismData(String orgId){
        // TODO : complete organism data
        if( orgId=='9913' )
            return [id:'9913', name:"Bos taurus", sname:'bta', assembly:'bosTau4', kingdom:'animal',
                keggName:'B.taurus', goName:'goa_cow', ucscSeq:'bosTau4.fa.gz', egeneInfo:'Mammalia' ]

        if( orgId=='6239' )
            return [id:'6239', name:"Caenorhabditis elegans", sname:'cel', assembly:'ce6', kingdom:'animal',
                keggName:'C.elegans', goName:'wb', ucscSeq:'chromFa.tar.gz', egeneInfo:'Invertebrates',
                uniprotName:'CAEEL']

        if( orgId=='7955' )
            return [id:'7955', name:"Danio rerio", sname:'dre', assembly:'danRer7', kingdom:'animal',
                keggName:'D.rerio', goName:'zfin', ucscSeq:'danRer7.fa.gz', egeneInfo:'Non-mammalian_vertebrates',
                uniprotName:'DANRE' ]

        if( orgId=='7227' )
            return [id:'7227', name:"Drosophila melanogaster", sname:'dme', assembly:'dm3', kingdom:'animal',
                keggName:'D.melanogaster', goName:'fb', ucscSeq:'chromFa.tar.gz', egeneInfo:'Invertebrates',
                uniprotName:'DROME' ]

        if( orgId=='83333' )
            return [id:'83333', name:"Escherichia coli", sname:'eco', assembly:'e_coli', kingdom:'bacteria',
                keggName:'E.coli', goName:'ecocyc', bowtie:'e_coli', refGene:ecoli_form ,
                uniprotName:'ECOLI']

        if( orgId=='9031' )
            return [id:'9031', name:"Gallus gallus", sname:'gga', assembly:'galGal3', kingdom:'animal',
                keggName:'G.gallus', goName:'goa_chicken', ucscSeq:'chromFa.tar.gz', egeneInfo:'Non-mammalian_vertebrates',
                uniprotName:'CHICK' ]

        if( orgId=='9606' )
            return [id:'9606', name:"Homo sapiens", sname:'hsa', assembly:'hg19', kingdom:'animal',
                keggName:'H.sapiens', goName:'goa_human', bowtie:'hg19', egeneInfo:'Mammalia',
                uniprotName:'HUMAN' ]

        if( orgId=='10090' )
            return [id:'10090', name:"Mus musculus", sname:'mmu', assembly:'mm9', kingdom:'animal',
                keggName:'M.musculus', goName:'mgi', bowtie:'mm9', egeneInfo:'Mammalia',
                uniprotName:'MOUSE' ]

        if( orgId=='39947' )
            return [id:'39947', name:"Oryza sativa", sname:'osa', assembly:'osv61', kingdom:'plant',
                keggName:'O.sativa', goName:'gramene_oryza', plantGDBname:'OsGDB', plantGDBassembly:'OSgenomeV6.1',
                refGene:osativa_form, egeneInfo:'Plants']

        if( orgId=='10116' )
            return [id:'10116', name:"Rattus norvegicus", sname:'rno', assembly:'rn4', kingdom:'animal',
                keggName:'R.norvegicus', goName:'rgd', bowtie:'rn4', egeneInfo:'Mammalia',
                uniprotName:'RAT' ]
            
        if( orgId=='4932' )
            return [id:'4932', name:"Saccharomyces cerevisiae", sname:'sce', assembly:'sacCer2', kingdom:'animal',
                keggName:'S.cerevisiae', goName:'sgd', ucscSeq:'chromFa.tar.gz', egeneInfo:'Fungi' ]

        return null
    }

    
    static public String egeneInfoUrl( String taxId ) {
        def data = organismData(taxId)
        return "${ENTREZ_GENE_INFO}${data.egeneInfo}/${data.name.replace(' ','_')}.gene_info.gz"
    }
    
    static public String goAssocUrl( String taxId ) {
        def data = organismData(taxId)
        return GO_BASE.replace("%", data.goName)
    }

    static public String ensemblToGeneUrl( String taxId ) {
        def data = organismData(taxId)
        return UCSC_ANN_BASE.replace("%", data.assembly)+UCSC_ENSEMBL_2_GENE
    }
    
    static public String uniprotIdMapUrl( String taxId ) {
        def data = organismData(taxId)
        return UNIPROT_IDS_URL+data.uniprotName+'_'+taxId+UNIPROT_ID_MAPP_SUFF
    }
    
    static protected boolean genFasta( String bowtie){
        println "Generating fasta sequence for ${bowtie} bowtie index."

        def output = new BufferedOutputStream(new FileOutputStream( bowtie+".fa" ))
        def proc="bowtie-inspect ${bowtie}".execute()
        proc.consumeProcessOutputStream(output)

        if( proc.waitFor()!=0 ){
            System.err.println("Error: generating fasta ${bowtie}.")
            return false
        }

        output.close()

        return true
    }

    static public boolean genFastaIndex( String faFile){
        println "Generating fasta index for ${faFile}."

        if( "samtools faidx ${faFile}".execute().waitFor()!=0 ){
            System.err.println("Error: generating faidx ${faFile}.")
            return false
        }

        return true
    }

    static public boolean genBowtieIndex( String faFile, String version = '1'){
        println "Generating bowtie index for ${faFile}."
        def base = faFile.substring(0, faFile.lastIndexOf('.'))
        def execName = (version=='1') ? 'bowtie-build' : 'bowtie2-build'

        def proc="${execName} ${faFile} ${base}".execute()
        proc.consumeProcessOutput()

        if( proc.waitFor()!=0 ){
            System.err.println("Error: generating bowtie index ${faFile}.")
            return false
        }

//        //generate color index
//        base = faFile.substring(0, faFile.lastIndexOf('.'))+"_c"
//
//        proc="bowtie-build --color ${faFile} ${base}".execute()
//        proc.consumeProcessOutput()
//
//        if( proc.waitFor()!=0 ){
//            System.err.println("Error: generating bowtie color index ${faFile}.")
//            return false
//        }

        return true
    }

    /**
     * uses genePredToGTf from http://hgdownload.cse.ucsc.edu/admin/exe/linux.x86_64/
     */
    static protected boolean genePredToGtf( String refGeneFile ){

        def reader = Utils.createReader(refGeneFile)
        String tmpfile = refGeneFile.substring(0,refGeneFile.lastIndexOf('.'))+".tmp"

        def writer = new BufferedWriter(new FileWriter(tmpfile))


        //writes to tmp file 
        reader.eachLine{ line ->
            if( !line.startsWith('#') && !line.isEmpty() ){
                if( line.split("\t").length==16 )// write all fields except the first one
                    writer.writeLine( line.substring(line.indexOf('\t')+1) )
                else
                    writer.writeLine(line)
            }
        }

        reader.close()
        writer.close()

        String outFile = refGeneFile.substring(0,refGeneFile.lastIndexOf('.'))+".gtf"
        String source = refGeneFile.substring(refGeneFile.lastIndexOf(File.separator)+1, refGeneFile.lastIndexOf('.'))

        if( "genePredToGtf -source=${source} file ${tmpfile} ${outFile}".execute().waitFor()!=0 ){
            System.err.println("Error: generating gtf file ${outFile}.")
            return false
        }

        //remove temporal files and compress output
        "rm ${tmpfile}".execute()
        "gzip ${outFile}".execute().waitFor()

        return true
    }


    /**
     * gets file from url form
     *
     * @param outFile : out file path without compressed extension
     * @param formData
     * @return final output file path (with compressed extension)
     */
    static protected String getFormFile( String outFile, Map formData, boolean compressOut = false){

        String content = ""

        if( formData.parameters ){
            content = "?"
            formData.parameters.each{ k, v-> content += "${k}=${URLEncoder.encode(v)}&"}
            content = content.substring(0, content.length()-1)
        }

        String urlSrc = formData.url+content;

        if( formData.compressed )
            outFile += formData.compressed

        if( !Utils.download( urlSrc, outFile) ){
            System.err.println("Error: cannot download from ${urlSrc}.")
            return null
        }

        if( compressOut && !formData.compressed ){
            "gzip ${outFile}".execute().waitFor()
            outFile+='.gz'
        }

        return outFile
    }

    /**
     * converts a gff3 file to genePred
     *
     * @param gffFile file input
     * @param outFile uncompressed path to output file
     * @return final output file path (with compressed extension)
     */
    static public String convertGffToGenePred( String gffFile, String outFile, boolean compressOut = false){
        BufferedReader reader=Utils.createReader(gffFile)

        Ontology ont

        try {
            //load sofa ontology
            OboParser oboparser = new OboParser();
            BufferedReader oboFile = Utils.createReader("sofa.obo");
            ont = oboparser.parseOBO(oboFile, "SOFA", "");
        } catch( Exception ex ){
            System.err.println("Cannot load SOFA ontology.")
            Logger.getLogger(AnnotationDB.class.getName()).log(Level.SEVERE, null, ex);
            return null
        }
        
        GFF3DocumentHandler handler = new GFF3DocumentHandlerConvert();
        handler.setOutFile( outFile )

        GFF3Parser parser = new GFF3Parser()
        parser.parse(reader, handler, ont )

        if( compressOut ){
            "gzip ${outFile}".execute().waitFor()
            outFile+='.gz'
        }

        return outFile
    }


    /**
     * converts a gtf file to gff3
     *
     * @param gtfFile file input
     * @param outFile uncompressed path to output file
     * @return final output file path (with compressed extension)
     */
    static public String convertGtfToGff( String gtfFile, String outFile, boolean compressOut = false){
        BufferedReader reader=Utils.createReader(gtfFile)

        GtfDocumentHandlerConvert handler = new GtfDocumentHandlerConvert()
        handler.setOutFile( outFile )

        GFFParserLight parser = new GFFParserLight()
        parser.parse(reader,handler)

        if( compressOut ){
            "gzip ${outFile}".execute().waitFor()
            outFile+='.gz'
        }

        return outFile
    }

    
    /**
     *
     * @param fileGenePred : genePred file
     */
    static protected convertGenePredToGff( String fileGenePred, String gff3FileName){
        String gff3File = fileGenePred.substring(0, fileGenePred.lastIndexOf(File.separator)+1) +
            gff3FileName.substring(0, gff3FileName.lastIndexOf(".gz") )

        if( !GFF3DocumentHandlerConvert.convertGenePredToGff3( fileGenePred, gff3File ) ){
            System.err.println("Cannot convert to gff3.")
            return null
        }

        //compress output
        "gzip -f ${gff3File}".execute().waitFor()
        gff3File+='.gz'

        return gff3File
    }
    
    
    /**
     * filters gff3 file in order to get transcribed features only (exclude pseudogenes).
     * Uses ensembl rest API.
     * 
     * @param file
     */
    static protected filterGff3ByTranscribed(file){
        if( file instanceof String ){
            file = new File(file)
        }
        
        def reader = Utils.createReader(file)
        def tmpFile = file.absolutePath+'.tmp'
        def writer = new PrintWriter(tmpFile)
        
        def discarded = [] as TreeSet
        def accepted = [] as TreeSet
        
        reader.eachLine{ line->
            if( line.isEmpty() || line.startsWith('#') ){
                writer.println(line)
            }
            else{
                def toks = line.split("\t")
                def atts = toks[8].split(";") as List
                def id = atts.find{ it.startsWith('ID=') }?.substring(3)
                
                if( !id ){
                    id = atts.find{ it.startsWith('Parent=') }?.substring(7)
                }
                
                if( id in accepted ){
                    writer.println(line)
                }
                else if( !(id in discarded) ){
                    def feat = null

                    try{
                        feat = EnsemblUtils.getEnsemblFeature(id)
                    } catch(e){ }
                    
                    if( feat && EnsemblUtils.isTranscribed(feat) ){
                        writer.println(line)
                        accepted << id
                    }
                    else{
                        discarded << id 
                    }
                }
            }
        }
        
        reader.close()
        writer.close()
        
        def unzippedFile = file.absolutePath
        int pos = file.absolutePath.indexOf('.gz')
        
        if( pos>0 ){
            unzippedFile = file.absolutePath.substring(0,pos)
        }
        
        "mv -f ${tmpFile} ${unzippedFile}".execute().waitFor()
        "gzip -f ${unzippedFile}".execute().waitFor()
    }

    
    /**
     * get annotation files for a given organism
     *
     * @return a List of subpaths
     */
    static public List getAnnotations(String orgId){
        if( !organismData(orgId) )
            return null

        return [REFGENE_GFF3_FILE, REFGENE_GTF_FILE, ENSGENE_GTF_FILE, ENSGENE_GFF3_FILE].collect{orgId+File.separator+it}
    }
    
    /**
     * creates the annotation database
     *
     * @param root : base dir
     * @param p_organisms : list of organisms to annotate
     */
    static def createAnnotation( String root, List p_organisms=null ){

        if( !root.endsWith(File.separator) )
            root += File.separator

        if( !Utils.createDir(root) )
            return false

        if( !p_organisms )
            p_organisms = organisms

        for( String orgId : p_organisms ){

            String orgDir = root+orgId+File.separator
            def orgData = organismData(orgId)

            if( !orgData ){
                System.err.println("Error: Organism with id ${orgId} not found.")
                continue
            }

            //create organism dir
            if( !(new File(orgDir).mkdir()) ){
                //error creating dir
                System.err.println("Error: Cannot create ${orgDir} for ${orgData.name}.")
                continue
            }
            
            println "Created ${orgDir} directory for ${orgData.name}."

            String fileDst = null
            String urlSrc = null


            //------ Download GO annotation
            fileDst = orgDir+GOA_FILE
            urlSrc = GO_BASE.replace("%", orgData.goName)

            if( !Utils.download(urlSrc, fileDst) )
                System.err.println("Error: Cannot download GO annotation (${urlSrc}) for ${orgData.name}.")


            if( orgData.refGene ){
                //if the reference is not a standard ftp

                fileDst = orgDir+REFGENE_FILE_TMP
                fileDst = getFormFile( fileDst, orgData.refGene, true)

                if( fileDst ){
                    String fileGenePred
                    if( orgData.refGene.format=='genepred' ){
                        fileGenePred = orgDir+REFGENE_FILE
                        "mv ${fileDst} ${fileGenePred}".execute().waitFor()
                        convertGenePredToGff(fileGenePred, REFGENE_GFF3_FILE)
                    }
                    else if( orgData.refGene.format=='gff3' ){
                        fileGenePred = orgDir+REFGENE_FILE
                        fileGenePred = fileGenePred.substring(0,fileGenePred.lastIndexOf('.'))//remove .gz extension
                        fileGenePred = convertGffToGenePred( fileDst, fileGenePred , true)
                        //rename tmp file to gff3
                        "mv ${fileDst} ${orgDir+REFGENE_GFF3_FILE}".execute().waitFor()
                    }

                    genePredToGtf( fileGenePred )
                }
            }
            else{
                //------ Download refGene from UCSC
                fileDst = orgDir+REFGENE_FILE
                urlSrc = UCSC_ANN_BASE.replace("%", orgData.assembly)+REFGENE_FILE

                if( !Utils.download(urlSrc, fileDst) )
                    System.err.println("Error: Cannot download refGene annotation (${urlSrc}) for ${orgData.name}.")
                else{
                    genePredToGtf( fileDst )
                    convertGenePredToGff(fileDst, REFGENE_GFF3_FILE)
                }


                //------ Download ensGene from UCSC
                fileDst = orgDir+ENSGENE_FILE
                urlSrc = UCSC_ANN_BASE.replace("%", orgData.assembly)+ENSGENE_FILE

                if( !Utils.download(urlSrc, fileDst) )
                    System.err.println("Error: Cannot download ensGene annotation (${urlSrc}) for ${orgData.name}.")
                else{
                    genePredToGtf( fileDst )
                    convertGenePredToGff(fileDst, ENSGENE_GFF3_FILE)
                    filterGff3ByTranscribed(orgDir+ENSGENE_GFF3_FILE)
                }
            }

            boolean downloadKegg = true
            //------ Download KEGG data
            if( downloadKegg ){
                def error = false
                fileDst = orgDir+orgData.keggName+KEGG_ENT_SUF
                urlSrc = KEGG_BASE+orgData.sname+File.separator+orgData.keggName+KEGG_ENT_SUF

                if( !Utils.download(urlSrc, fileDst) ){
                    System.err.println("Error: Cannot download KEGG entries (${urlSrc}) for ${orgData.name}.")
                    error = true
                }

                if( !error ){
                    fileDst = orgDir+orgData.sname+KEGG_PATH_SUF
                    urlSrc = KEGG_BASE+orgData.sname+File.separator+orgData.sname+KEGG_PATH_SUF

                    if( !Utils.download(urlSrc, fileDst) ){
                        System.err.println("Error: Cannot download KEGG paths (${urlSrc}) for ${orgData.name}.")
                        error = true
                    }

                    if( !error ){
                    //join both files
                        println "Joining KEGG files for ${orgData.name}."

                        try {
                            Main.joinKeggGeneFiles(
                                new File(orgDir+orgData.keggName+KEGG_ENT_SUF),
                                new File(orgDir+orgData.sname+KEGG_PATH_SUF),
                                new File(orgDir+KEGG_FILE_TMP), orgData.sname
                            )
                        } catch( e ){
                            System.err.println("Error: Cannot join kegg files for ${orgData.name}.")
                        }

                        //compress join kegg output
                        "gzip ${orgDir+KEGG_FILE_TMP}".execute().waitFor()

                        //remove temporal files
                        println "Deleting temporal files for ${orgData.name}."
                        "rm ${orgDir+orgData.keggName+KEGG_ENT_SUF}".execute()
                        "rm ${orgDir+orgData.sname+KEGG_PATH_SUF}".execute()

                    }
                }
            }//end if kegg data

            println ''
        }//end for each organisms

        return true
    }


    
    /**
     * creates bowtie genomic indexes
     *
     * @param root : base dir
     * @param p_organisms : list of organisms for to get index
     */
    static def createIndexes( String root, List p_organisms=null ){

        if( !root.endsWith(File.separator) )
            root += File.separator

        if( !Utils.createDir(root) )
            return false

        if( !p_organisms )
            p_organisms = organisms

        for( String orgId : p_organisms ){

            def orgData = organismData(orgId)

            if( !orgData ){
                System.err.println("Error: Organism with id ${orgId} not found.")
                continue
            }

            String fileDst = null
            String urlSrc = null

            if( orgData.bowtie ){
                //---- download the index from bowtie ftp
                fileDst = root+orgData.bowtie+BOWTIE_SUF
                urlSrc = BOWTIE_BASE+orgData.bowtie+BOWTIE_SUF

                if( !Utils.download(urlSrc, fileDst) ){
                    System.err.println("Error: Cannot download bowtie index (${urlSrc}) for ${orgData.name}.")
                }
                else{
                    "unzip -n -d ${root} ${fileDst}".execute().waitFor()
                    "rm ${fileDst}".execute()
                    //build sequence and sequence index
                    genFasta(root+orgData.bowtie)
                    genFastaIndex(root+orgData.bowtie+".fa")
                }

                //---- download the color index from bowtie ftp
                fileDst = root+orgData.bowtie+BOWTIE_COLOR_SUF
                urlSrc = BOWTIE_BASE+orgData.bowtie+BOWTIE_COLOR_SUF

                if( !Utils.download(urlSrc, fileDst) ){
                    System.err.println("Error: Cannot download bowtie index (${urlSrc}) for ${orgData.name}.")
                }
                else{
                    "unzip -n -d ${root} ${fileDst}".execute().waitFor()
                    "rm ${fileDst}".execute()
                }

            }
            else if( orgData.ucscSeq || orgData.plantGDBname ){

                if(orgData.ucscSeq){
                    //get the fasta sequence from UCSC
                    fileDst = root+orgData.ucscSeq
                    urlSrc = UCSC_SEQ_BASE.replace("%", orgData.assembly)+orgData.ucscSeq
                }
                else {
                    //get the fasta sequence from plantGDB
                    fileDst = root+orgData.plantGDBassembly
                    urlSrc = PLANTGDB_BASE+orgData.plantGDBname+File.separator+orgData.plantGDBassembly
                }

                if( !Utils.download(urlSrc, fileDst) ){
                    System.err.println("Error: Cannot download sequence (${urlSrc}) for ${orgData.name}.")
                    continue
                }


                //uncompress sequence if needed
                if( fileDst.endsWith('.tar.gz') ){
                    //untar to single file
                    def fileOut = fileDst.substring(0, fileDst.lastIndexOf('.tar.gz'))
                    Utils.untarToFile( fileDst, fileOut )
                    "rm ${fileDst}".execute()//remove temporal file
                    fileDst = fileOut
                }
                else if( fileDst.endsWith('.gz') ){
                    "gunzip ${fileDst}".execute().waitFor()
                    fileDst = fileDst.substring(0, fileDst.lastIndexOf('.'))
                }

                //rename fasta to <data.assembly>.fa
                String faFile = root+orgData.assembly+".fa"
                "mv ${fileDst} ${faFile}".execute().waitFor()

                //build sequence index and bowtie index
                //the base name of the index must be equals to the assembly name
                genFastaIndex(faFile)
                genBowtieIndex(faFile)

            }
            
            println ''
        }//end for each organisms

        return true
    }

}

