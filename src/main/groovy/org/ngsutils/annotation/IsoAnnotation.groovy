/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation



import org.ngsutils.FileIndex
///import org.efetchGene.EFetchGene
///import org.eutils.ESearch

/**
 * Isoforms annotation class
 * 
 * @author victor
 */
public class IsoAnnotation {

    String refseqCode=null
    List<String> goTerms=null
    List<String> ecTerms=null
    List<String> keggPaths=null
    List<String> accessions=null
    String officialSymbol=null


    /**
     * creates an annotation getting the values from NCBI web services
     *
     * @param refseq
     */
//    public IsoAnnotation( String refseq ) {
//        this.setRefseqCode(refseq);
//
//        //search the gene code in NCBI
//        List<String> codes=ESearch.runESearch("gene", this.getRefseqCode(), "10");
//
//        if( codes==null || codes.isEmpty() )
//            return;
//
//        //search the gene in NCBI
//        EFetchGene eGene=new EFetchGene( codes.get(0) );
//
//        if( eGene.getEntrezGene()==null )
//            return;
//
//        try{
//            this.setOfficialSymbol( eGene.getOfficialSymbol() );
//        } catch( Exception ex ){}
//        try{
//            this.setGoTerms( eGene.getGOTerms() );
//        } catch( Exception ex ){}
//        try{
//            this.setKeggPaths( eGene.getKEGGPathways() );
//        } catch( Exception ex ){}
//        
//    }


    /**
     * builds an annotations table using refGene UCSC file and GO annotation file
     *
     * @param refGene : refGene.txt file (from UCSC)
     * @param goa : gene ontology annotation file
     * @param kegg : kegg annotation file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HashMap<String,IsoAnnotation> genAnnotFromRefGeneGOA(File refGene, File goa, File kegg = null)
            throws FileNotFoundException, IOException {

        HashMap<String,IsoAnnotation> table=new HashMap<String,IsoAnnotation>();

        FileIndex indexRefGene=new FileIndex( refGene, 1, 12, "\t", "#" );
        FileIndex indexGOA=new FileIndex( goa, 2, 4, "\t", "!" );
        FileIndex indexKegg = (kegg==null) ? null : new FileIndex( kegg, 0, 1, "\t", null )

        for( String refCode : indexRefGene.getKeys() ){
            List<String> geneName=indexRefGene.getValue(refCode);
            List<String> goCodes=indexGOA.getValue( geneName.get(0) );
            List<String> keggCodes=indexKegg?.getValue( geneName.get(0) );


            IsoAnnotation ann=new IsoAnnotation();
            ann.setOfficialSymbol( geneName.get(0) );
            ann.setRefseqCode(refCode);
            ann.setGoTerms(goCodes);
            ann.setKeggPaths(keggCodes);

            table.put(refCode, ann);
        }

        return table;
    }
    
}
