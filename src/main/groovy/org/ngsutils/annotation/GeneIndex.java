/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.program.gff.GFFEntrySet;
import org.biojava.bio.program.gff.GFFRecordFilter;
import org.biojava.utils.ParserException;
import org.ngsutils.Utils;
import org.ngsutils.annotation.biojava.GFFToolsLight;
import org.ngsutils.annotation.biojava.SimpleGFFRecordLight;

/**
 * gene index for cufflinks GTF output file
 *
 * @author victor
 */
public class GeneIndex {

    private GFFEntrySet entrySet=null;

    private HashMap<String,Gene> geneIdTable=null;
    private HashMap<String,PrimaryTranscript> primTransIdTable=null;
    private HashMap<String,Isoform> isoformIdTable=null;

    public GeneIndex(File inFile) {

        try {
            
            BufferedReader reader=Utils.createReader(inFile);
            setEntrySet( GFFToolsLight.readGFF(reader) );

        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneIndex.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserException ex) {
            Logger.getLogger(GeneIndex.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BioException ex) {
            Logger.getLogger(GeneIndex.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeneIndex.class.getName()).log(Level.SEVERE, null, ex);
        }

        indexEntrySet();
    }

    public GeneIndex(File inFile, String chrom ) {

        try {

            BufferedReader reader=Utils.createReader(inFile);
            GFFRecordFilter.SequenceFilter filter=new GFFRecordFilter.SequenceFilter(chrom);
            setEntrySet( GFFToolsLight.readGFF(reader, filter) );
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneIndex.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserException ex) {
            Logger.getLogger(GeneIndex.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BioException ex) {
            Logger.getLogger(GeneIndex.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeneIndex.class.getName()).log(Level.SEVERE, null, ex);
        }

        indexEntrySet();
    }

    /**
     * get the list of sequences in gff file
     *
     * @param inFile
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> getSequences(File inFile)
            throws FileNotFoundException, IOException {

        BufferedReader reader=Utils.createReader(inFile);
        HashMap<String,String> sequences=new HashMap<String,String>();

        String line=null;

        while( (line=reader.readLine())!=null ){
            line=line.trim();

            if( line.startsWith("#") )
                continue;

            String seq=line.substring(0,line.indexOf('\t'));

            if( !sequences.containsKey(seq) )
                sequences.put(seq, seq);

        }

        reader.close();

        List<String> list=new ArrayList<String>(sequences.keySet());
        Collections.sort(list);
        return list;
    }

    /**
     * @return the entrySet
     */
    public GFFEntrySet getEntrySet() {
        return entrySet;
    }

    /**
     * @param entrySet the entrySet to set
     */
    protected void setEntrySet(GFFEntrySet entrySet) {
        this.entrySet = entrySet;
    }


    /**
     *
     */
    private void indexEntrySet(){

        geneIdTable=new HashMap<String,Gene>();
        primTransIdTable=new HashMap<String,PrimaryTranscript>();
        isoformIdTable=new HashMap<String,Isoform>();

        Iterator line=this.getEntrySet().lineIterator();

        while( line.hasNext() ){

            Object next=line.next();

            if( !(next instanceof SimpleGFFRecordLight) )
                continue;

            SimpleGFFRecordLight rec=(SimpleGFFRecordLight)next;

            String geneId=rec.getGeneId();

            Gene gene=null;

            if( geneIdTable.containsKey(geneId) ){
                gene = geneIdTable.get(geneId);
            }
            else{
                gene = new Gene(geneId, rec.getSeqName());
                geneIdTable.put( geneId, gene);
            }

            Isoform iso=gene.addFeature(rec);

            if( !isoformIdTable.containsKey(iso.getTranscriptId()) )
                isoformIdTable.put( iso.getTranscriptId(), iso);
            
            if( iso.getPrimTranscript()!=null ){
                if( !primTransIdTable.containsKey(iso.getPrimTranscript().getTssId()) ){
                    primTransIdTable.put(
                            iso.getPrimTranscript().getTssId(), iso.getPrimTranscript());
                }
            }
            
        }//end while

    }

    /**
     *
     * @param geneId : gene_id 
     * @return
     */
    public Gene getGene( String geneId ){
        return geneIdTable.get(geneId);
    }

    
    /**
     *
     * @param transcriptId
     * @return
     */
    public Isoform getIsoform( String transcriptId ){
        return isoformIdTable.get(transcriptId);
    }

    
    /**
     *
     * @param tssId
     * @return
     */
    public PrimaryTranscript getPrimaryTranscript(String tssId){
        return primTransIdTable.get(tssId);
    }
    
    /**
     * 
     * @return 
     */
    public Collection<Gene> getGenes(){
        return geneIdTable.values();
    }

    /**
     * 
     * @return 
     */
    public Collection<Isoform> getIsoforms(){
        return isoformIdTable.values();
    }

    /**
     * 
     * @return 
     */
    public Collection<PrimaryTranscript> getPrimaryTranscripts(){
        return primTransIdTable.values();
    }

    
    /**
     * annotate the isoforms using the table indexed by refSeq code (NM_*)
     *
     * @param table
     * @param useNCBI : if uses NCBI web services
     * @return
     */
    public int annotateIsoforms( HashMap<String,IsoAnnotation> table, boolean useNCBI ){

        int count=0;
        for( Isoform iso : this.getIsoforms() ){
            if( iso.annotate(table, useNCBI) )
                count++;
        }

        return count;
    }
    
}
