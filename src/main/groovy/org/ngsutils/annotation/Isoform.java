/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.biojava.bio.program.gff.GFFWriter;
import org.biojava.bio.seq.Sequence;
import org.ngsutils.annotation.biojava.SimpleGFFRecordLight;

/**
 *
 * @author victor
 */
public class Isoform {

    private String transcriptId;
    private Gene gene;
    private PrimaryTranscript primTranscript=null;

    private int length=0;

    private List<SimpleGFFRecordLight> exons=new ArrayList<SimpleGFFRecordLight>();

    //annotation variables
    private String refseqCode=null;
    private IsoAnnotation isoAnnotation=null;
    
    //sequence
    private Sequence sequence = null;
    
    
    private static final int LIMIT_TOLERANCE = 5;


    public Isoform(String transcriptId, Gene gene) {
        this.transcriptId = transcriptId;
        this.gene = gene;
    }

    

    /**
     * @return the transcriptId
     */
    public String getTranscriptId() {
        return transcriptId;
    }

    /**
     * @param transcriptId the transcriptId to set
     */
    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
        
        for( SimpleGFFRecordLight ex : this.getExons() )
            ex.setTranscriptId(transcriptId);
    }

    /**
     * @return the exons
     */
    public List<SimpleGFFRecordLight> getExons() {
        return exons;
    }

    /**
     * @param exons the exons to set
     */
    public void setExons(List<SimpleGFFRecordLight> exons) {
        this.exons = exons;
    }

    /**
     * @return the gene
     */
    public Gene getGene() {
        return gene;
    }

    /**
     * @param gene the gene to set
     */
    public void setGene(Gene gene) {
        this.gene = gene;
    }

    /**
     * @return the primTranscript
     */
    public PrimaryTranscript getPrimTranscript() {
        return primTranscript;
    }

    /**
     * @param primTranscript the primTranscript to set
     */
    public void setPrimTranscript(PrimaryTranscript primTranscript) {
        this.primTranscript = primTranscript;
    }

    /**
     *
     * @param writer
     */
    public void record( GFFWriter writer ){
        for( SimpleGFFRecordLight ex : this.getExons() ){
            ex.record(writer);
        }
    }

    
    /**
     * @return the refseqCode
     */
    public String getRefseqCode() {
        return refseqCode;
    }

    /**
     * @param refseqCode the refseqCode to set
     */
    public void setRefseqCode(String refseqCode) {
        this.refseqCode = refseqCode;
    }

     /**
     * @return the isoAnnotation
     */
    public IsoAnnotation getIsoAnnotation() {
        return isoAnnotation;
    }

    /**
     * @param isoAnnotation the isoAnnotation to set
     */
    public void setIsoAnnotation(IsoAnnotation isoAnnotation) {
        this.isoAnnotation = isoAnnotation;
    }

    /**
     * 
     * @return
     */
    public List<String> getGOTerms(){
        if( getIsoAnnotation()!=null )
            return getIsoAnnotation().getGoTerms();

        return null;
    }

    /**
     * annotates the isoform using the annotation table (indexed by refSeq code
     * (NM_*,NR_*) )
     *
     * @param table : table of IsoAnnotation indexed by refseq isoform code, the table
     *  is populated with new records if an annotation is not found and useNCBI is true
     * @param useNCBI : if uses NCBI web services
     * @return
     */
    public boolean annotate( HashMap<String,IsoAnnotation> table, boolean useNCBI ){

        //first get the transcript id
        if( this.getTranscriptId().startsWith("NM_") ||
                this.getTranscriptId().startsWith("NR_") ){
            this.setRefseqCode( this.getTranscriptId() );
        }
        else{
            this.setRefseqCode( (String) this.getExons().get(0).getNearestRef() );
        }

        if( this.getRefseqCode()==null ){
            return false;
        }

        //get the annotation
        IsoAnnotation ref = table.get( this.getRefseqCode() );

        if( ref==null ){
            //if not exists
            if( useNCBI ){
                //if not exists get it from NCBI
                ///ref=new IsoAnnotation( this.getRefseqCode() );
                ///table.put( this.getRefseqCode(), ref);
                return false;
            }
            else{
                return false;
            }
        }

        this.setIsoAnnotation(ref);

        return true;
    }

    /**
     *
     * @return the isoform length (sum of each exon length)
     */
    public int getLength() {
        if( this.length==0 ){
            for( SimpleGFFRecordLight ex : this.getExons() ){
                this.length +=
                        ex.getEnd()-ex.getStart();
            }
        }

        return this.length;
    }

    /**
     * 
     * @return
     */
    public List<String> getKeggTerms() {
        if( getIsoAnnotation()!=null )
            return getIsoAnnotation().getKeggPaths();

        return null;
    }
    
    /**
     *
     * @return
     */
    public String getReference(){
        if( this.getRefseqCode()!=null )
            return this.getRefseqCode();

        String ref=(String) this.getExons().get(0).getNearestRef();

        return ref;

    }

    /**
     * @return the sequence
     */
    public Sequence getSequence() {
        return sequence;
    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }
    
    /**
     * 
     * @param iso2
     * @return true if this isoform is equivalent to the given iso2 object
     */
    public boolean isEqualIso(Isoform iso2){
        if( this.getExons().size()!=iso2.getExons().size() )
            return false;
            
        for(int i=0; i<this.getExons().size(); i++){
            SimpleGFFRecordLight exon1 = this.getExons().get(i);
            SimpleGFFRecordLight exon2 = iso2.getExons().get(i);
            
            if( !exon1.getSeqName().equals(exon2.getSeqName()) )
                return false;
            if( Math.abs(exon1.getStart() - exon2.getStart())>LIMIT_TOLERANCE )
                return false;
            if( Math.abs(exon1.getEnd() - exon2.getEnd())>LIMIT_TOLERANCE )
                return false;
        }
        
        return true;
    }
    
    /**
     * 
     * @return 
     */
    public int getStart() {
        return this.getExons().get(0).getStart();
    }
    
    /**
     * 
     * @return 
     */
    public int getEnd() {
        return this.getExons().get(this.getExons().size()-1).getEnd();
    }
    
}
