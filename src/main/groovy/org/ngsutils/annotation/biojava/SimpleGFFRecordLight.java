/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.biojava;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.biojava.bio.program.gff.GFFWriter;
import org.biojava.bio.program.gff.SimpleGFFRecord;
import org.biojava.utils.SmallMap;
import org.ngsutils.annotation.Isoform;

/**
 * gff record for a cufflinks gtf out file
 *
 * @author victor
 */
public class SimpleGFFRecordLight extends SimpleGFFRecord {

    private Isoform isoform = null;
    private String geneId = null;
    private String transcriptId = null;
    private String tssId = null;
    private String nearestRef = null;
    private String classCode = null;
    private String exonNumber = null;
    private String oId = null;
    private String geneName = null;

    //common attributes
    public static final String GENE_ID ="gene_id";
    public static final String TRANSCRIPT_ID ="transcript_id";
    public static final String TSS_ID ="tss_id";
    public static final String NEAREST_REF ="nearest_ref";
    public static final String CLASS_CODE ="class_code";
    public static final String EXON_NUMBER ="exon_number";
    public static final String OID ="oId";
    public static final String GENE_NAME ="gene_name";
    
    /**
     * 
     */
    public SimpleGFFRecordLight() {
        super();
    }
    
    /**
     * copy constructor
     * 
     * @param rec 
     */
    public SimpleGFFRecordLight(SimpleGFFRecordLight rec) {
        super(rec);
        isoform = rec.isoform;
        geneId = rec.geneId;
        transcriptId = rec.transcriptId;
        tssId = rec.tssId;
        nearestRef = rec.nearestRef;
        classCode = rec.classCode;
        exonNumber = rec.exonNumber;
        oId = rec.oId;
        geneName = rec.geneName;
    }

  /**
   * Replace the group-attribute <span class="type">Map</span> with
   * <span class="arg">ga</span>.
   * <p>
   * To efficiently add a key, call <span class="method">getGroupAttributes()</span>
   * and modify the <span class="type">Map</span>.
   *
   * @param ga  the new group-attribute <span class="type">Map</span>
   */
    @Override
    public void setGroupAttributes(Map ga) {

        this.setGeneId( (String) ga.get(GENE_ID));
        this.setTranscriptId( (String) ga.get(TRANSCRIPT_ID) );
        this.setTssId( (String) ga.get(TSS_ID) );
        this.setNearestRef( (String) ga.get(NEAREST_REF) );
        this.setClassCode( (String) ga.get(CLASS_CODE) );
        this.setExonNumber( (String) ga.get(EXON_NUMBER) );
        this.setoId( (String) ga.get(OID) );
        this.setGeneName( (String) ga.get(GENE_NAME) );
    }

    @Override
    public Map getGroupAttributes() {

        Map attMap = new SmallMap();

        if( this.getGeneId()!=null )
            attMap.put( GENE_ID, this.getGeneId() );

        if( this.getTranscriptId()!=null )
            attMap.put( TRANSCRIPT_ID, this.getTranscriptId() );

        if( this.getTssId()!=null )
            attMap.put( TSS_ID, this.getTssId() );

        if( this.getExonNumber()!=null )
            attMap.put( EXON_NUMBER, this.getExonNumber() );

        if( this.getNearestRef()!=null )
            attMap.put( NEAREST_REF, this.getNearestRef() );

        if( this.getClassCode()!=null )
            attMap.put( CLASS_CODE, this.getClassCode() );

        if( this.getoId()!=null )
            attMap.put( OID, this.getoId() );

        if( this.getGeneName()!=null )
            attMap.put( GENE_NAME, this.getGeneName() );

        return attMap;
    }
  
  /**
   * Create a <span class="type">String</span> representation of
   * <span class="arg">attMap</span>.
   *
   * <span class="arg">attMap</span> is assumed to contain
   * <span class="type">String</span> keys and
   * <span class="type">String</span> values.
   *
   * @param attMap  the <span class="type">Map</span> of attributes and value String
   * @return  a GFF attribute/value <span class="type">String</span>
   */
  public static String stringifyAttributes(Map attMap) {
    String sBuff = "";
    Iterator ki = attMap.keySet().iterator();
    while (ki.hasNext()) {
      String key = (String) ki.next();
      sBuff+=key;
      String value = (String) attMap.get(key);
      
      if (isText(value)) {
          sBuff+=" \"" + value + "\"";
      } else {
          sBuff+=" " + value;
      }
      
      if (ki.hasNext()) {
        sBuff+=" ;";
      }
    }
    return sBuff;
  }

  /**
   * Returns true if a string is "textual". The GFF Spec says that
   * "textual" values must be quoted. This implementation just tests
   * if the string contains letters or whitespace.
   *
   * @param value a <code>String</code> value.
   * @return true if value is "textual".
   */
  private static boolean isText(String value) {
    
      try{
        Double.parseDouble(value);
      }
      catch( NumberFormatException ex ){
        return true;
      }

      return false;
  }

    /**
     * @return the isoform
     */
    public Isoform getIsoform() {
        return isoform;
    }

    /**
     * @param isoform the isoform to set
     */
    public void setIsoform(Isoform isoform) {
        this.isoform = isoform;
    }

    /**
     *
     * @param attId
     * @return
     */
    public Object getAttribute(String attId){

        if( this.getGroupAttributes()==null )
            return null;
        
        Object attrib=this.getGroupAttributes().get(attId);

        if( attrib==null )
            return null;

        if( attrib instanceof List )
            ((List)attrib).get(0);

        return attrib;
    }

    /**
     *
     * @param writer
     */
    public void record( GFFWriter writer ){
        writer.recordLine( this );
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
     * @return the nearestRef
     */
    public String getNearestRef() {
        return nearestRef;
    }

    /**
     * @param nearestRef the nearestRef to set
     */
    public void setNearestRef(String nearestRef) {
        this.nearestRef = nearestRef;
    }

    /**
     * @return the classCode
     */
    public String getClassCode() {
        return classCode;
    }

    /**
     * @param classCode the classCode to set
     */
    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    /**
     * @return the exonNumber
     */
    public String getExonNumber() {
        return exonNumber;
    }

    /**
     * @param exonNumber the exonNumber to set
     */
    public void setExonNumber(String exonNumber) {
        this.exonNumber = exonNumber;
    }

    /**
     * @return the oId
     */
    public String getoId() {
        return oId;
    }

    /**
     * @param oId the oId to set
     */
    public void setoId(String oId) {
        this.oId = oId;
    }

    /**
     * set attributes using the gff record line
     * 
     * @param rest
     */
    void setGroupAttributes(String attValList) {
        String [] sTok = attValList.split(";");

	for(int i=0; i<sTok.length; i++) {
	    String attVal = sTok[i].trim();
	    String attName;
            String value="";

	    int spaceIndx = attVal.indexOf(" ");

	    if(spaceIndx == -1) {
		attName = attVal;
	    }
            else {
		attName = attVal.substring(0, spaceIndx);
		attVal = attVal.substring(spaceIndx).trim();

                if( attVal.indexOf('\"')>=0 )
                    value = attVal.substring( attVal.indexOf('\"')+1, attVal.lastIndexOf('\"') );
                else
                    value = attVal;
	    }

	    if( attName.equals(GENE_ID) )
                this.setGeneId(value);

            else if( attName.equals(TRANSCRIPT_ID) )
                this.setTranscriptId(value);

            else if( attName.equals(TSS_ID) )
                this.setTssId(value);

            else if( attName.equals(NEAREST_REF) )
                this.setNearestRef(value);

            else if( attName.equals(CLASS_CODE) )
                this.setClassCode(value);

            else if( attName.equals(EXON_NUMBER) )
                this.setExonNumber(value);

            else if( attName.equals(OID) )
                this.setoId(value);

            else if( attName.equals(GENE_NAME) )
                this.setGeneName(value);
	}
    }

    /**
     * @return the geneName
     */
    public String getGeneName() {
        return geneName;
    }

    /**
     * @param geneName the geneName to set
     */
    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }


}
