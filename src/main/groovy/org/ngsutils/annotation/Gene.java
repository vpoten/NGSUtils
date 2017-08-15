/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.biojava.bio.program.gff.GFFWriter;
import org.ngsutils.annotation.biojava.SimpleGFFRecordLight;

/**
 *
 * @author victor
 */
public class Gene {
    private String geneId;
    private String seqName;
    private int length=0;

    private HashMap<String,PrimaryTranscript> primTranscript=new HashMap<String,PrimaryTranscript>();
    private HashMap<String,Isoform> isoforms=new HashMap<String,Isoform>();

    
    public Gene(String geneId, String seqName) {
        this.geneId = geneId;
        this.seqName = seqName;
    }


    /**
     * @return the geneId
     */
    public String getGeneId() {
        return geneId;
    }

    /**
     * @param geneId the geneId to set
     */
    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    /**
     * @return the seqName
     */
    public String getSeqName() {
        return seqName;
    }

    /**
     * @param seqName the seqName to set
     */
    public void setSeqName(String seqName) {
        this.seqName = seqName;
    }

    /**
     * @return the primTranscript
     */
    public HashMap<String, PrimaryTranscript> getPrimTranscript() {
        return primTranscript;
    }

    /**
     * @param primTranscript the primTranscript to set
     */
    public void setPrimTranscript(HashMap<String, PrimaryTranscript> primTranscript) {
        this.primTranscript = primTranscript;
    }

    /**
     * @return the isoforms
     */
    public HashMap<String, Isoform> getIsoforms() {
        return isoforms;
    }

    /**
     * @param isoforms the isoforms to set
     */
    public void setIsoforms(HashMap<String, Isoform> isoforms) {
        this.isoforms = isoforms;
    }


    /**
     * add the feature to gene
     *
     * @param feat
     * @return : the isoform where the feature is inserted
     */
    public Isoform addFeature(SimpleGFFRecordLight feat){

        //get transcript id
        String transId=feat.getTranscriptId();

        //get tssId
        String tssId=feat.getTssId();
        
        Isoform transcript=null;
        PrimaryTranscript primTrans=null;
        
        
        //create isoform if not exists
        if( this.getIsoforms().containsKey(transId) ){
            transcript=this.getIsoforms().get(transId);
        }
        else{
            transcript=new Isoform( transId, this);
            this.getIsoforms().put(transId, transcript);
        }

        feat.setIsoform(transcript);
        transcript.getExons().add(feat);
        
        
        if( tssId!=null ){
            //create primary transcript if not exists
            if( this.getPrimTranscript().containsKey(tssId) ){
                primTrans=this.getPrimTranscript().get(tssId);
            }
            else{
                primTrans=new PrimaryTranscript( tssId, this);
                this.getPrimTranscript().put(tssId, primTrans);
            }

            //if the primary transcript doesnt contains the transcript insert it
            if( !primTrans.getIsoforms().containsKey(transId) ){
                primTrans.getIsoforms().put(transId, transcript);
                transcript.setPrimTranscript(primTrans);
            }
        }

        return transcript;
    }

    /**
     * 
     * @param writer
     */
    public void record( GFFWriter writer ){
        for( Isoform iso : this.getIsoforms().values() ){
            iso.record(writer);
        }
    }

    /**
     * gets the union of all isoforms go-terms
     *
     * @return
     */
    public List<String> getGOTerms() {

        HashMap<String,String> terms=new HashMap<String,String>();

        for( Isoform iso : this.getIsoforms().values() ){
            List<String> list=iso.getGOTerms();

            if( list==null || list.isEmpty() )
                continue;

            for( String go : list )
                terms.put(go, go);
        }

        return new ArrayList( terms.values() );
    }

    /**
     *
     * @return the gene length (average of isoforms lenght)
     */
    public int getLength() {
        if( this.length==0 ){
            for( Isoform iso : this.getIsoforms().values() ){
                this.length += iso.getLength();
            }

            this.length /= this.getIsoforms().values().size();
        }

        return this.length;
    }

    /**
     *
     * @return
     */
    public List<String> getKeggTerms() {
        HashMap<String,String> terms=new HashMap<String,String>();

        for( Isoform iso : this.getIsoforms().values() ){
            List<String> list=iso.getKeggTerms();

            if( list==null || list.isEmpty() )
                continue;

            for( String go : list )
                terms.put(go, go);
        }

        return new ArrayList( terms.values() );
    }

    /**
     * 
     * @return
     */
    public List<String> getReferences(){
        HashMap<String,String> refs=new HashMap<String,String>();

        for( Isoform iso : this.getIsoforms().values() ){
            String ref=iso.getReference();

            if( ref!=null)
                refs.put(ref, ref);
        }

        return new ArrayList( refs.values() );
    }

}
