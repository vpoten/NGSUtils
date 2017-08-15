/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.biojava

import org.biojava.ontology.Term
import org.biojava.bio.program.gff3.*
import org.ngsutils.Utils

/**
 * document handler that converts gff3 to genePred
 *
 * @author victor
 */
class GFF3DocumentHandlerConvert implements GFF3DocumentHandler {

    //gff3 types (lowercase)
    protected static final String TYPE_GENE = "gene"
    protected static final String TYPE_EXON = "exon"
    protected static final String TYPE_CDS = "cds"
    protected static final String TYPE_MRNA = "mrna"
    protected static final String TYPE_5UTR = "five_prime_utr"
    protected static final String TYPE_3UTR = "three_prime_utr"

    protected static final String GFF3_HEADER = '##gff-version 3'
    //gff3 attributes 
    public static final String TERM_ID = 'ID'
    public static final String TERM_NAME = 'Name'
    public static final String TERM_PARENT = 'Parent'
    public static final String TERM_ALIAS = 'Alias'


    //current annotations
    protected def gene //current gene (hierarchy)

    protected def writer//output file writer

   /**
    * Indicates that a new GFF document has been started.
    * This gives you a hook to set up per-document resources.
    *
    * @param locator A URI for the stream being parsed.
    */
    void startDocument(String locator){}

   /**
    * Indicates that the current GFF document has now ended.
    * <p>
    * This gives you the chance to flush results, or do calculations if
    * you wish.
    */
    void endDocument(){
        writeGenePredRecords()//action: write last gene
        writer.close()
    }

   /**
    * A comment line has been encountered.
    * <p>
    * <span class="arg">comment</span> has already had the leading '<code>#</code>'
    * removed, and may have had leading-and-trailing whitespace trimmed.
    *
    * @param comment  the comment <span class="type">String</span>
    */
    void commentLine(String comment){}


   /**
    * A record line has been encountered.
    * <p>
    * It is already preseneted to you into a <span class="type">GFFRecord</span> object.
    *
    * @param record  the <span class="type">GFFRecord</span> containing all the info
    */
    void recordLine(GFF3Record record){
        String type = record.type.name.toLowerCase()

        if( type == TYPE_GENE ){
            writeGenePredRecords()//action
            newGene(record)
        }
        else if( type == TYPE_EXON ){
            newFeat(record, 'exon')
        }
        else if( type == TYPE_CDS || type == TYPE_5UTR || type == TYPE_3UTR ){
            newFeat(record, 'cds')
        }
        else if( type == TYPE_MRNA ){
            newMRNA(record)
        }
    }

    /**
     * set output file
     */
    void setOutFile( String outFile ){
        //creates writer
         writer = new BufferedWriter(new FileWriter(outFile))
    }


    
    /**
     * utility method
     */
    private def getRecordTerm(GFF3Record record, String term, int idx=0){
        Term key = record.source.ontology.getTerm(term)
        return URLDecoder.decode( record.annotation.properties.get(key)[idx] )
    }

    protected def newGene(GFF3Record record){
        String id = getRecordTerm(record,TERM_ID)
        gene = [TERM_ID:id, 'mrna':[:], 'exon':[], 'cds':[], 'record':record ]
    }

    protected def newMRNA(GFF3Record record){
        String id = getRecordTerm(record,TERM_ID)
        gene.mrna.put( id, [TERM_ID:id, 'exon':[], 'cds':[], 'record':record ] )
    }

    protected newFeat(GFF3Record record, String feat){
        String parent = getRecordTerm(record,TERM_PARENT)

        if( gene.TERM_ID==parent ){
            gene.get(feat) << record
            return
        }

        def mrna = gene.mrna.get(parent)

        if( mrna )
            mrna.get(feat) << record
    }

    protected def writeGenePredRecords(){
        if( !gene )
            return
            
        if( gene.mrna )
        //write each mrna
            gene.mrna.each{ writeGenePredRecord(it.value.record) }
        else
        //write gene features
            writeGenePredRecord(null)
    }

    /**
     * writes a record for a transcript:
     * @param mrna : if not null writes mrna record, else writes gene record
     *
     * Fields:
     *  string name;        	"Name of gene (usually transcript_id from GTF)"
     *  string chrom;       	"Chromosome name"
     *  char[1] strand;     	"+ or - for strand"
     *  uint txStart;       	"Transcription start position"
     *  uint txEnd;         	"Transcription end position"
     *  uint cdsStart;      	"Coding region start"
     *  uint cdsEnd;        	"Coding region end"
     *  uint exonCount;     	"Number of exons"
     *  uint[exonCount] exonStarts; "Exon start positions"
     *  uint[exonCount] exonEnds;   "Exon end positions"
     *  uint id;            	"Unique identifier"
     *  string name2;       	"Alternate name (e.g. gene_id from GTF)"
     *  string cdsStartStat; 	"enum('none','unk','incmpl','cmpl')"
     *  string cdsEndStat;   	"enum('none','unk','incmpl','cmpl')"
     *  lstring exonFrames; 	"Exon frame offsets {0,1,2}"
     */
    protected def writeGenePredRecord(GFF3Record mrna){

        //get CDS features from gene or mrna record
        def cdss = (mrna) ? gene.mrna.get(getRecordTerm(mrna, TERM_ID)).cds : gene.cds
       
        def name = (mrna) ? getRecordTerm(mrna, TERM_ID) : getRecordTerm(gene.record, TERM_ID)
        def chrom = gene.record.sequenceID
        def strand = gene.record.strand.token
        def txStart = (mrna) ? mrna.start : gene.record.start
        def txEnd = (mrna) ? mrna.end : gene.record.end
        def cdsStart = cdss.find({it.type.name.toLowerCase()==TYPE_CDS}).start
        def cdsEnd = cdss.reverse().find({it.type.name.toLowerCase()==TYPE_CDS}).end
        def exonCount = cdss.size()
        def exonStarts = cdss.sum{it.start+','}
        def exonEnds =  cdss.sum{it.end+','}
        def id = '0'
        def name2 = getRecordTerm(gene.record, TERM_ID)
        def cdsStartStat = 'cmpl' //enum('none','unk','incmpl','cmpl')
        def cdsEndStat = 'cmpl' //enum('none','unk','incmpl','cmpl')
        def exonFrames = cdss.sum{it.phase+','}

        String record = "${name}\t${chrom}\t${strand}\t${txStart}\t${txEnd}\t${cdsStart}\t${cdsEnd}\t${exonCount}\t${exonStarts}\t${exonEnds}\t${id}\t${name2}\t${cdsStartStat}\t${cdsEndStat}\t${exonFrames}"
        writer.writeLine(record)
    }
    

    /**
     * converts a genePred file to gff3
     */
    static boolean convertGenePredToGff3( String genePredFile, String gff3File ){

        //remove comments, blank lines and extra field from source file
        def reader = Utils.createReader(genePredFile)
        String tmpfile = genePredFile+".temp"

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

        //sort temporal file
        String sortfile = genePredFile+".sorted"
        "sort -k2,2 -k4,4n -k12,12 ${tmpfile} -o ${sortfile}".execute().waitFor()

        //remove temporal file
        "rm -f ${tmpfile}".execute()

        reader = Utils.createReader(sortfile)
        writer = new BufferedWriter(new FileWriter(gff3File))

        // write header
        writer.writeLine(GFF3_HEADER)

        //define current gene name and records
        def geneRecs = null
        String currGene = ''

        reader.eachLine{ line ->
            def gprec = buildGPredRecord(line)

            if( gprec.name2 ){
                if( gprec.name2!=currGene ){
                    writeGff3Gene(writer, geneRecs)
                    currGene = gprec.name2
                    geneRecs = [gprec]
                }
                else{
                    geneRecs << gprec
                }
            }
        }

        //write last gene
        writeGff3Gene(writer, geneRecs)

        //close streams
        reader.close()
        writer.close()

        //remove temporal sorted file
        "rm -f ${sortfile}".execute()

        return true
    }

    
    /**
     * builds a gene pred record map
     */
    static protected buildGPredRecord(String line){

        def tokens = line.split("\t")
        def gprec = [:]

        gprec.name = tokens[0]
        gprec.chrom = tokens[1]
        gprec.strand = tokens[2]
        gprec.txStart = tokens[3]
        gprec.txEnd = tokens[4]
        gprec.cdsStart = tokens[5]
        gprec.cdsEnd = tokens[6]
        gprec.exonCount = tokens[7]
        gprec.exonStarts = (!tokens[8].endsWith(',') ? tokens[8] : tokens[8].substring(0,tokens[8].length()-1)  ).split(",") as List
        gprec.exonEnds = (!tokens[9].endsWith(',') ? tokens[9] : tokens[9].substring(0,tokens[9].length()-1)  ).split(",") as List

        if( tokens.length>10 ){
            gprec.id = tokens[10]
            gprec.name2 = tokens[11]
            gprec.cdsStartStat = tokens[12]
            gprec.cdsEndStat = tokens[13]
            gprec.exonFrames = (!tokens[14].endsWith(',') ? tokens[14] : tokens[14].substring(0,tokens[14].length()-1)  ).split(",") as List
        }
        else{
            gprec.id = '0'
            gprec.name2 = gprec.name+".gene"
            gprec.cdsStartStat = 'unk'
            gprec.cdsEndStat = 'unk'

            int nexons = gprec.exonCount as Integer
            def frames = []
            (1..nexons).each{ frames << '0' }
            gprec.exonFrames = frames
        }

        return gprec
    }


    /**
     *
     */
    static protected writeGff3Gene(writer, List geneRecs, String source = 'ngsutils' ){

        if( !geneRecs )
            return

        def gene = [:]
        //write gene record
        gene.seqid = geneRecs[0].chrom
        gene.source = source
        gene.type = 'gene'
        gene.start = geneRecs.collect{it.txStart as Integer}.min()
        gene.end = geneRecs.collect{it.txEnd as Integer}.max()
        gene.score = '.'
        gene.strand = geneRecs[0].strand
        gene.phase = '.'
        gene.attributes = ["${TERM_ID}":geneRecs[0].name2]
        writeGff3Record(writer, gene)
        writer.write("\n")

        //write mrnas
        geneRecs.each{ rec ->
            def mrna = [:]

            mrna.seqid = rec.chrom
            mrna.source = source
            mrna.type = 'mRNA'
            mrna.start = rec.txStart
            mrna.end = rec.txEnd
            mrna.score = '.'
            mrna.strand = rec.strand
            mrna.phase = '.'
            mrna.attributes = ["${TERM_ID}":rec.name, "${TERM_PARENT}":rec.name2]
            writeGff3Record(writer, mrna)
        }

        writer.write("\n")

        //write CDSs and UTRs
        geneRecs.each{ rec ->
            def cds = [:]

            cds.seqid = rec.chrom
            cds.source = source
            cds.score = '.'
            cds.strand = rec.strand
            cds.attributes = ["${TERM_PARENT}":rec.name]
            int nexons = rec.exonCount as Integer
            int cdsStart = rec.cdsStart as Integer
            int cdsEnd = rec.cdsEnd as Integer

            (0..(nexons-1)).each{ idx ->
                int exStart = rec.exonStarts[idx] as Integer
                int exEnd = rec.exonEnds[idx] as Integer

                if( exStart<cdsStart && exEnd>cdsStart ){
                    cds.type = 'five_prime_UTR'
                    cds.start = exStart
                    cds.end = cdsStart-1
                    cds.phase = '.'
                    writeGff3Record(writer, cds)

                    cds.type = 'CDS'
                    cds.start = cdsStart
                    cds.end = rec.exonEnds[idx]
                    cds.phase = '0'
                    writeGff3Record(writer, cds)
                }
                else if( exStart<cdsEnd && exEnd>cdsEnd ){
                    cds.type = 'CDS'
                    cds.start = exStart
                    cds.end = cdsEnd
                    cds.phase = rec.exonFrames[idx]
                    writeGff3Record(writer, cds)

                    cds.type = 'three_prime_UTR'
                    cds.start = cdsEnd+1
                    cds.end = rec.exonEnds[idx]
                    cds.phase = '.'
                    writeGff3Record(writer, cds)
                }
                else{
                    if( (rec.exonFrames[idx] as Integer)<0 )
                        cds.type = (exStart<cdsStart) ? 'five_prime_UTR' : 'three_prime_UTR'
                    else
                        cds.type = 'CDS'

                    cds.start = rec.exonStarts[idx]
                    cds.end = rec.exonEnds[idx]
                    cds.phase = ((rec.exonFrames[idx] as Integer)>=0) ? rec.exonFrames[idx] : '.'
                    writeGff3Record(writer, cds)
                }
            }

            writer.write("\n")
        }

        
    }


    /**
     * writes a gff3 record
     *
     * Map fields:
     * Column 1: "seqid"
     * Column 2: "source"
     * Column 3: "type"
     * Columns 4 & 5: "start" and "end"
     * Column 6: "score"
     * Column 7: "strand"
     * Column 8: "phase" 
     * Column 9: "attributes" (Map)
     */
    static public writeGff3Record(writer, Map fields){

        //build attributes field
        String atts = ''
        fields.attributes.each{ k,v ->  atts += "${k}=${URLEncoder.encode(v)};" }
        atts = atts.substring(0, atts.length()-1)//remove last ';'

        String line =
            "${fields.seqid}\t${fields.source}\t${fields.type}\t${fields.start}\t${fields.end}\t${fields.score}\t${fields.strand}\t${fields.phase}\t${atts}\n"

        writer.write(line)
    }

}

