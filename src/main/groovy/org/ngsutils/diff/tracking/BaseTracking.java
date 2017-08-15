/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.diff.tracking;

import org.ngsutils.annotation.GeneIndex;


/**
 *
 * @author victor
 */
public abstract class BaseTracking {

    private double [] samples;
    private double [] samplesLow=null;
    private double [] samplesHigh=null;
    
    private Object reference=null;//annotation object reference

    private boolean [] diffExpres;
    private boolean [] overload;
    

    private String locus;

    /**
     *
     * @param nsamples : value >=2
     * @param locus
     * @param storeConfidence : if stores confidence intervals for samples
     */
    public BaseTracking(int nsamples, String p_locus, boolean storeConfidence ) {
        samples=new double [nsamples];
        diffExpres=new boolean [nsamples-1];
        overload=new boolean [nsamples-1];
        locus=p_locus;

        if( storeConfidence ){
            samplesLow=new double [nsamples];
            samplesHigh=new double [nsamples];
        }

        for(int i=0;i<nsamples;i++){
            samples[i]=0.0;

            if( storeConfidence ){
                samplesLow[i]=0.0;
                samplesHigh[i]=0.0;
            }
        }

        for(int i=0;i<nsamples-1;i++){
            diffExpres[i]=false;
            overload[i]=false;
        }
    }


    /**
     * 
     * @param arr
     */
    public void setSamples(double [] arr){
        System.arraycopy(arr, 0, this.getSamples(), 0, arr.length);
    }

    /**
     * @return the reference
     */
    public Object getReference() {
        return reference;
    }

    /**
     * @param reference the reference to set
     */
    public void setReference(Object reference) {
        this.reference = reference;
    }

    /**
     * @return the samples
     */
    public double[] getSamples() {
        return samples;
    }

    /**
     * @return the samplesLow
     */
    public double[] getSamplesLow() {
        return samplesLow;
    }

    /**
     * @return the samplesHigh
     */
    public double[] getSamplesHigh() {
        return samplesHigh;
    }

    /**
     * @return the diffExpres
     */
    public boolean[] getDiffExpres() {
        return diffExpres;
    }

    /**
     * @return the overload
     */
    public boolean[] getOverload() {
        return overload;
    }

    /**
     * @return the locus
     */
    public String getLocus() {
        return locus;
    }

    /**
     * @param locus the locus to set
     */
    public void setLocus(String locus) {
        this.locus = locus;
    }

    /**
     * 
     * @return
     */
    public int getNSamples(){
        return this.samples.length;
    }

    /**
     * set the annotation object reference
     * @param refId
     * @param track
     * @param index
     */
    static public void setReference(String refId, BaseTracking track, GeneIndex index){

        if( track instanceof GeneTracking ){
            track.setReference( index.getGene(refId) );
        }
        else if( track instanceof IsoformTracking ){
            track.setReference( index.getIsoform(refId) );
        }
        else if( track instanceof PrimaryTranscriptTracking ){
            track.setReference( index.getPrimaryTranscript(refId) );
        }

    }

}
