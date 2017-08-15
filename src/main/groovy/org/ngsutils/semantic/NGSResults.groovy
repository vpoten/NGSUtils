/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic

/**
 * NGS Results ontology constants
 * 
 * @author victor
 */
class NGSResults {
	
    //prefix for ngsengine results statements
    public static final String URI_PREF ='http://www.ngsengine.com'
    public static final String resultsNGS = URI_PREF+'/results#'
    public static final String resultsNGSFile = 'ngsengine-results.owl'
 
    public static final String RDF_FILE_EXT = '.rdf.xml'
    
    //data properties
    public static final String NGS_CHROM = resultsNGS+'chromosome'
    public static final String NGS_START = resultsNGS+'start'
    public static final String NGS_END = resultsNGS+'end'
    public static final String NGS_STRAND = resultsNGS+'strand'
    public static final String NGS_GENEREF = resultsNGS+'geneRef'
    public static final String NGS_NEARREF = resultsNGS+'nearestRef'
    public static final String NGS_SAMPLE1 = resultsNGS+'sample1'
    public static final String NGS_SAMPLE2 = resultsNGS+'sample2'
    public static final String NGS_VALUE1 = resultsNGS+'value1'
    public static final String NGS_VALUE2 = resultsNGS+'value2'
    public static final String NGS_PVALUE = resultsNGS+'pValue'
    public static final String NGS_CUFFCLASS = resultsNGS+'cuffClassCode'
    public static final String NGS_ACCESSION = resultsNGS+'accession'
    public static final String NGS_EXONS = resultsNGS+'exons'
    public static final String NGS_SEQUENCE = resultsNGS+'sequence'
    public static final String NGS_HASPART = resultsNGS+'hasPart'
    public static final String NGS_TESTPVALUE = resultsNGS+'testPValue'
    public static final String NGS_CORRPVALUE = resultsNGS+'corrPValue'

    //types
    public static final String TYPE_ISOFORM = resultsNGS+'Isoform'
    public static final String TYPE_GENE = resultsNGS+'Gene'
    public static final String TYPE_PRIMTRANS = resultsNGS+'PrimaryTranscript'
    public static final String TYPE_DNAFEAT = resultsNGS+'DNAFeature'
    public static final String TYPE_DIFFEXPR = resultsNGS+'DiffExpression'
    
    //object properties
    public static final String PROP_DIFFEXPR = resultsNGS+'diffExpressed'
    public static final String PROP_OVERCDS = resultsNGS+'overCDS'
    public static final String PROP_OVERISO = resultsNGS+'overIsoform'
    public static final String PROP_OVERPRIM = resultsNGS+'overPrimTranscript'
    public static final String PROP_OVEREPUNDER = resultsNGS+'overepInUnderSet'
    public static final String PROP_OVEREPOVER = resultsNGS+'overepInOverSet'
    
    
    static String genDiffResURI(jobId){
        return URI_PREF+'/job/diff/'+jobId
    }
    
    static String genJobContextURI(jobId){
        return URI_PREF+'/job/'+jobId
    }
    
}


