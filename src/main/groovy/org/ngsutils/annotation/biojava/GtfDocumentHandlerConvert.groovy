/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.biojava

import org.biojava.bio.program.gff.*

/**
 * document handler that converts gtf to gff3
 *
 * @author victor
 */
class GtfDocumentHandlerConvert implements GFFDocumentHandler {

    protected static final String TERM_ID = 'ID'
    protected static final String TERM_PARENT = 'Parent'
    protected static final String TERM_DBREF = 'Dbxref'
    protected static final String TERM_ALIAS = 'Alias'
    protected static final String TYPE_EXON = "exon"
    protected static final String TYPE_CDS = "cds"

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
        writeGff3Records()//action
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
    void recordLine(GFFRecord record){
        String type = record.feature.toLowerCase()
        def srec = (SimpleGFFRecordLight)record

        if( !gene || gene.TERM_ID!=srec.geneId ){
            writeGff3Records()//action
            newGene(record)
        }

        if( !gene.mrna.get(srec.transcriptId) ){
            newMRNA(srec)
        }

        if( type == TYPE_EXON ){
            newFeat(srec, 'exon')
        }
        else if( type == TYPE_CDS ){
            newFeat(srec, 'cds')
        }
    }

    protected def newGene(SimpleGFFRecordLight record){
        gene = [TERM_ID:(record.geneId), 'mrna':[:], 'record':[] ]
    }

    protected def newMRNA(SimpleGFFRecordLight record){
        gene.mrna.put( record.transcriptId, [TERM_ID:(record.transcriptId), 'exon':[], 'cds':[]] )
    }

    protected newFeat(SimpleGFFRecordLight record, String feat){
        def mrna = gene.mrna.get(record.transcriptId)

        gene.record << record

        if( mrna )
            mrna.get(feat) << record
    }

    /**
     * set output file
     */
    void setOutFile( String outFile ){
        //creates writer
        writer = new BufferedWriter(new FileWriter(outFile))
        // write header
        writer.writeLine("##gff-version 3")
    }


    protected def writeGff3Records(){
        if( !gene )
            return

        def genemap = [:]
        //write gene record
        genemap.seqid = gene.record[0].seqName
        genemap.source = gene.record[0].source
        genemap.type = 'gene'
        genemap.start = gene.record.collect{it.start as Integer}.min()
        genemap.end = gene.record.collect{it.end as Integer}.max()
        genemap.score = '.'
        genemap.strand = gene.record[0].strand.token
        genemap.phase = '.'
        genemap.attributes = ["${TERM_ID}":gene.record[0].geneId]
        GFF3DocumentHandlerConvert.writeGff3Record(writer, genemap)
        writer.writeLine("")

        //write mrnas
        gene.mrna.each{ k, rna ->
            def mrna = [:]

            def recs = rna.exon ? rna.exon : rna.cds

            mrna.seqid = recs[0].seqName
            mrna.source = recs[0].source
            mrna.type = 'mRNA'
            mrna.start = recs.collect{it.start as Integer}.min()
            mrna.end = recs.collect{it.end as Integer}.max()
            mrna.score = '.'
            mrna.strand = recs[0].strand.token
            mrna.phase = '.'
            mrna.attributes = ["${TERM_ID}":recs[0].transcriptId, "${TERM_PARENT}":recs[0].geneId]
            if( recs[0].nearestRef ){mrna.attributes.put(TERM_DBREF, recs[0].nearestRef ) }
            GFF3DocumentHandlerConvert.writeGff3Record(writer, mrna)
        }

        writer.writeLine("")

        //write CDSs and exons
        gene.mrna.each{ k, rna ->

            if( rna.cds ){
                rna.cds.each{ rec->
                    def cds = [:]

                    cds.seqid = rec.seqName
                    cds.source = rec.source
                    cds.score = (rec.score==GFFRecord.NO_SCORE) ? '.' : rec.score
                    cds.type = 'CDS'
                    cds.start = rec.start
                    cds.end = rec.end
                    cds.phase = (rec.frame==GFFRecord.NO_FRAME) ? '0' : rec.frame
                    cds.strand = rec.strand.token
                    cds.attributes = ["${TERM_PARENT}":rec.transcriptId]
                    GFF3DocumentHandlerConvert.writeGff3Record(writer, cds)
                }
                
                writer.writeLine("")
            }

            if( rna.exon ){
                rna.exon.each{ rec->
                    def exon = [:]

                    exon.seqid = rec.seqName
                    exon.source = rec.source
                    exon.score = (rec.score==GFFRecord.NO_SCORE) ? '.' : rec.score
                    exon.type = 'exon'
                    exon.start = rec.start
                    exon.end = rec.end
                    exon.phase = (rec.frame==GFFRecord.NO_FRAME) ? '.' : rec.frame
                    exon.strand = rec.strand.token
                    exon.attributes = ["${TERM_PARENT}":rec.transcriptId]
                    GFF3DocumentHandlerConvert.writeGff3Record(writer, exon)
                }

                writer.writeLine("")
            }
        }

        writer.writeLine("")
    }

}

