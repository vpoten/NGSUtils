/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.biojava.bio.program.gff.GFFWriter;

/**
 *
 * @author victor
 */
public class PrimaryTranscript {
    private String tssId;
    private Gene gene;
    private int length=0;

    private HashMap<String,Isoform> isoforms=new HashMap<String,Isoform>();

    public PrimaryTranscript(String tssId, Gene gene) {
        this.tssId = tssId;
        this.gene = gene;
    }


    /**
     * @return the tssId
     */
    public String getTssId() {
        return tssId;
    }

    /**
     * @param tssId the tssId to set
     */
    public void setTssId(String tssId) {
        this.tssId = tssId;
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
     * @return the prim transcript length (average of isoforms lenght)
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
