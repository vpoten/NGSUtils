/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.diff;

import org.biojava.bio.program.gff.GFFWriter;
import org.ngsutils.Grep;
import org.ngsutils.Utils;
import org.ngsutils.AnnotationDB;
import org.ngsutils.annotation.Gene;
import org.ngsutils.annotation.GeneIndex;
import org.ngsutils.annotation.IsoAnnotation;
import org.ngsutils.annotation.Isoform;
import org.ngsutils.annotation.PrimaryTranscript;
import org.ngsutils.annotation.biojava.GFFWriterLight;
import org.ngsutils.diff.BedGraphItem.LimitType;
import org.ngsutils.diff.tracking.BaseTracking;
import org.ngsutils.diff.tracking.GeneTracking;
import org.ngsutils.diff.tracking.IsoformTracking;
import org.ngsutils.diff.tracking.PrimaryTranscriptTracking;
import org.ngsutils.semantic.diff.DiffExpresionRdfGen
import org.ngsutils.semantic.rdfutils.SimpleGraph
import org.ngsutils.semantic.LinkedLifeData
import org.ngsutils.semantic.NGSDataResource
import org.ngsutils.semantic.NGSResults as NGSR
import org.ngsutils.stats.StatTestParams
import org.ngsutils.stats.BiNGO.BingoAlgorithm
import org.ngsutils.ontology.GOManager
import org.ngsutils.annotation.BlastAnnotation
///import weka.core.Attribute;
///import weka.core.DenseInstance;
///import weka.core.Instances;
///import weka.core.converters.ArffSaver;

/**
 * tools for processing of cuffdiff output files
 * 
 * @author victor
 */
class DiffExpresionFileTool {


    static public final String OVER_EXPRESS="over";
    static public final String UNDER_EXPRESS="under";
    
    //feature file extension
    public static final String FEAT_FILE_EXT=".gtf";
    public static final String RDF_FILE_EXT = NGSR.RDF_FILE_EXT
    public static final String GOA_FILE_EXT=".goa";//(NOT USED)
    public static final String WIG_FILE_EXT=".wig";//(NOT USED)
    public static final String INFO_FILE_EXT=".info";//(NOT USED)
    public static final String GZIP_FILE_EXT=".gz";
    public static final String FEATOVER_FILE_EXT = '.'+OVER_EXPRESS+FEAT_FILE_EXT;
    public static final String FEATUNDER_FILE_EXT = '.'+UNDER_EXPRESS+FEAT_FILE_EXT;
    

    //file types
    static public final String isoformDiffFile="isoform_exp.diff";
    static public final String geneDiffFile="gene_exp.diff";
    static public final String tssDiffFile="tss_group_exp.diff";
    static public final String cdsDiffFile="cds_exp.diff";
    static public final String cdsOverFile="cds.diff";
    static public final String promOverFile="promoters.diff";
    static public final String splicOverFile="splicing.diff";

    public static final String [] ARRAY_FILETYPES = [
        isoformDiffFile, geneDiffFile, tssDiffFile, cdsDiffFile,
        cdsOverFile, promOverFile, splicOverFile ]

    //diff fields indices
    public static final int F_TEST_ID = 0;
    public static final int F_GENE = 1;
    public static final int F_LOCUS = 2;
    public static final int F_SAMPLE1 = 3;
    public static final int F_SAMPLE2 = 4;
    public static final int F_VAL1 = 6;
    public static final int F_VAL2 = 7;
    public static final int F_PVAL = 10;
    public static final int F_SIGNIF = 11;


    /**
     * returns a new BaseTracking subclass instance, depending on the fileType
     *
     * @param fileType
     * @param locus
     * @param nsamples
     * @return
     */
    protected static BaseTracking trackingFactory( String fileType, String locus, int nsamples ){

        if( fileType.equals(isoformDiffFile) ){
            return new IsoformTracking( nsamples, locus, false );
        }
        else if( fileType.equals(geneDiffFile) || fileType.equals(promOverFile) ){
            return new GeneTracking( nsamples, locus, false );
        }
        else if( fileType.equals(tssDiffFile) || fileType.equals(splicOverFile) ){
            return new PrimaryTranscriptTracking( nsamples, locus, false );
        }
        else if( fileType.equals(cdsDiffFile) ){
            return null;
        }

        return null;
    }


    /**
     * returns true if the fileType measures oveloaded expresion
     * (instead of differential expression)
     *
     * @param fileType
     * @return
     */
    private static boolean isFileOverloadTest( String fileType ){

        if( [cdsOverFile, promOverFile, splicOverFile].any{fileType.indexOf(it)>=0} ){
            return true;
        }

        return false;
    }

    
    /**
     * returns field value
     * 
     * @param field
     * @param file
     * @param tokens
     */
    public static String getField(int field, String file, tokens){
        
        if( field==F_SIGNIF && !isFileOverloadTest(file) && !(tokens[field] in ['yes','no']) )
            field += 1
            
        return tokens[field]
    }

    
    /**
     * reads a cuffdiff output file and store the data into the HashMap
     *
     * @param table : table to store the data
     * @param outDir : cuffdir output directory
     * @param nsamples : number of samples (>=2)
     * @param fileType : cuffdif out file type (isoformDiffFile,...)
     * @param onlySignif : read only entities with significative statistical test
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void readDiffExpFiles( HashMap<String,BaseTracking> table, String outDir,
            int nsamples, String fileType, boolean onlySignif )
            throws FileNotFoundException, IOException, URISyntaxException {

        if( !outDir.endsWith(File.separator) )
            outDir+=File.separator;

        boolean diffExprFile=true;

        if( isFileOverloadTest(fileType) )
            diffExprFile=false;

        for(int i=0; i<nsamples-1; i++){
            String fileName = i+"_"+(i+1)+"_"+fileType;

            BufferedReader r =  Utils.createReader(outDir+fileName);

            String l_line="";
            r.readLine();//read header

            while( (l_line=r.readLine())!=null ){
                String [] tokens=l_line.split("\t");

                if( onlySignif )
                    if( !tokens[F_SIGNIF].toLowerCase().equals("yes") )
                        continue;

                BaseTracking track=null;

                if( table.containsKey(tokens[F_TEST_ID]) ){
                    track=table.get(tokens[F_TEST_ID]);
                }
                else{
                    track=trackingFactory( fileType, tokens[F_LOCUS], nsamples );
                    table.put( tokens[F_TEST_ID], track);
                }

                boolean significant=false;

                if( tokens[F_SIGNIF].toLowerCase().equals("yes") )
                    significant=true;

                if( diffExprFile ){
                    track.getSamples()[i] = Double.parseDouble(tokens[F_VAL1]);
                    track.getSamples()[i+1] = Double.parseDouble(tokens[F_VAL2]);
                    track.getDiffExpres()[i]=significant;
                }
                else{
                    track.getOverload()[i]=significant;
                }

            }

            r.close();
        }
    }


    /**
     * 
     * @param outDir
     * @param nsamples
     * @param onlySignif
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HashMap<String,BaseTracking> readIsoformDiffExpFiles( String dir,
            int nsamples, boolean onlySignif )
            throws FileNotFoundException, IOException, URISyntaxException {

        HashMap<String,BaseTracking> table=new HashMap<String,BaseTracking>();
        
        String fileType = DiffExpresionFileTool.isoformDiffFile;
        DiffExpresionFileTool.readDiffExpFiles(table, dir, nsamples, fileType, onlySignif);
        
        return table;
    }


    /**
     *
     * @param outDir
     * @param nsamples
     * @param onlySignif
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HashMap<String,BaseTracking> readGeneDiffExpFiles( String dir,
            int nsamples, boolean onlySignif )
            throws FileNotFoundException, IOException, URISyntaxException {

        HashMap<String,BaseTracking> table=new HashMap<String,BaseTracking>();

        String fileType = DiffExpresionFileTool.geneDiffFile;
        DiffExpresionFileTool.readDiffExpFiles(table, dir, nsamples, fileType, onlySignif);

        fileType = DiffExpresionFileTool.promOverFile;
        DiffExpresionFileTool.readDiffExpFiles(table, dir, nsamples, fileType, onlySignif);

        return table;
    }


    /**
     *
     * @param outDir
     * @param nsamples
     * @param onlySignif
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HashMap<String,BaseTracking> readPrimTransDiffExpFiles( String dir,
            int nsamples, boolean onlySignif )
            throws FileNotFoundException, IOException, URISyntaxException {

        HashMap<String,BaseTracking> table=new HashMap<String,BaseTracking>();

        String fileType = DiffExpresionFileTool.tssDiffFile;
        DiffExpresionFileTool.readDiffExpFiles(table, dir, nsamples, fileType, onlySignif);

        fileType = DiffExpresionFileTool.splicOverFile;
        DiffExpresionFileTool.readDiffExpFiles(table, dir, nsamples, fileType, onlySignif);

        return table;
    }

    
//    /**
//     * generates an arff output based on the samples values of the BaseTracking
//     * instances
//     *
//     * @param name : relation name (arff field)
//     * @param table
//     * @param out
//     * @throws IOException
//     */
//    public static void generateArff(String name, HashMap<String,BaseTracking> table, OutputStream out)
//            throws IOException {
//
//        Instances dataSet=null;
//
//        for( BaseTracking track : table.values() ){
//
//            if( dataSet==null ){
//            //in the first iteration create the dataset
//
//                //set up attributes
//                ArrayList<Attribute> atts = new ArrayList<Attribute>();
//
//                for( int i=0; i<track.getNSamples(); i++){
//                    //create numeric attributes
//                    atts.add(new Attribute("att"+i));
//                }
//
//                dataSet=new Instances( name, atts, 0);
//            }
//
//
//            double [] vals = new double[dataSet.numAttributes()];
//
//            System.arraycopy(track.getSamples(), 0, vals, 0, track.getNSamples());
//
//            dataSet.add( new DenseInstance(1.0,vals) );
//
//        }
//
//        if( dataSet==null )
//            return;
//
//        ArffSaver saver = new ArffSaver();
//        saver.setInstances(dataSet);
//        saver.setDestination(out);
//        saver.writeBatch();
//    }


    /**
     * gets feature from geneIndex by id, test gene, isoform and prim. transcript
     */
    protected static def getFeature( String id, GeneIndex geneIndex){
        def feat = null

        feat = geneIndex.getGene(id)

        if( feat!=null )
            return feat

        feat = geneIndex.getIsoform(id)

        if( feat!=null )
            return feat

        feat = geneIndex.getPrimaryTranscript(id)

        return feat
    }


    /**
     * generates a feature file joining the given diff file and the feature file
     *
     * @param source : diff file
     * @param out : output stream to write
     * @param geneIndex
     * @param expressed : filter OVER/UNDER_EXPRESS or null
     * @throws IOException
     */
    protected static void generateFeatFile( String source, OutputStream out,
            GeneIndex geneIndex, String expressed = null )
            throws IOException {

        //get differentialy expressed items
        def lines = []
        Grep.grepSimple( new File(source), "\tyes", lines, -1, "\t");

        
        GFFWriter writer = new GFFWriterLight( new PrintWriter(out) );
        writer.startDocument("");

        lines.each{ line ->
            def tokens = line.split('\t')
            boolean skip = false

            if( expressed ){
                double val1=Double.parseDouble( tokens[F_VAL1] );
                double val2=Double.parseDouble( tokens[F_VAL2] );
                def curr = ( val2>val1 ) ? OVER_EXPRESS : UNDER_EXPRESS

                if( curr!=expressed )
                    skip=true
            }

            if( !skip ){
                def feat = getFeature(tokens[F_TEST_ID], geneIndex)

                if( feat ){
                    //print features lines
                    if( feat instanceof Gene ){
                        feat.record(writer);
                    }
                    else if( feat instanceof Isoform ){
                        feat.record(writer);
                    }
                    else if( feat instanceof PrimaryTranscript ){
                        feat.record(writer);
                    }
                }
            }
        }

        writer.endDocument();
        
    }

//    /**
//     * generates an extended info file relative to a diff. expresion file with
//     * the fields (NOT USED):
//     * id, isoforms ids, references, tss ids, locus, fpkm1, fpkm2, diff_expr?, p-value
//     * 
//     * @param source
//     * @param out
//     * @param geneIndex
//     * @throws FileNotFoundException
//     * @throws IOException
//     */
//    protected static void generateDiffInfoFile( String source, OutputStream out,
//            GeneIndex geneIndex ) throws FileNotFoundException, IOException, URISyntaxException {
//
//        //read diff file
//        BufferedReader r =  Utils.createReader(source);
//        r.readLine();//read header
//
//        r.splitEachLine("\t"){ tokens ->
//            def feat = getFeature(tokens[F_TEST_ID], geneIndex)
//
//            if( feat ){
//
//                String reference="";
//                String tssIds="";
//                String isoIds="";
//
//                List<String> refs=null;
//                Collection<Isoform> isoforms=null;
//
//                //print extended info lines
//                if( feat instanceof Gene ){
//                    refs=feat.getReferences();
//                    isoforms=feat.getIsoforms().values();
//                    Collection<PrimaryTranscript> vals=feat.getPrimTranscript().values();
//
//                    //get tss ids
//                    vals.each{ tssIds += it.getTssId()+',' }
//                    if( tssIds ){ tssIds = tssIds.substring(0,tssIds.length()-1) }
//                }
//                else if( feat instanceof Isoform ){
//                    if( feat.getReference()!=null )
//                        reference=feat.getReference();
//                    if( feat.getPrimTranscript()!=null )
//                        tssIds=feat.getPrimTranscript().getTssId();
//                    isoIds=feat.getTranscriptId();
//                }
//                else if( feat instanceof PrimaryTranscript ){
//                    tssIds=feat.getTssId();
//                    refs=feat.getReferences();
//                    isoforms=feat.getIsoforms().values();
//                }
//
//                if( refs!=null ){
//                    //if is an entity with more than one reference
//                    refs.each{ reference += it+',' }
//                    if( reference ){ reference = reference.substring(0,reference.length()-1) }
//                }
//
//                if( isoforms!=null ){
//                    isoforms.each{ isoIds += it.getTranscriptId()+',' }
//                    if( isoIds ){ isoIds = isoIds.substring(0,isoIds.length()-1) }
//                }
//
//                String line = tokens[F_TEST_ID]+'\t'+isoIds+'\t'+reference+'\t'+tssIds+'\t'+tokens[F_LOCUS]+'\t'+
//                        tokens[F_VAL1]+'\t'+tokens[F_VAL2]+'\t'+tokens[F_SIGNIF]+'\t'+tokens[F_PVAL]+'\n';
//
//                out.write(line.getBytes());
//            }
//        }
//
//        r.close();
//    }



//    /**
//     * (NOT USED)
//     * @param source : diff file
//     * @param geneIndex
//     * @param out : OutputStream to write
//     * @throws IOException
//     */
//    protected static void generateGOAFile(String source, GeneIndex geneIndex, OutputStream out)
//            throws IOException, URISyntaxException {
//
//        //read diff file
//        BufferedReader r =  Utils.createReader(source);
//        r.readLine();//read header
//
//        r.splitEachLine("\t"){ tokens ->
//
//            double val1=Double.parseDouble( tokens[F_VAL1] );
//            double val2=Double.parseDouble( tokens[F_VAL2] );
//
//            def feat = getFeature(tokens[F_TEST_ID], geneIndex)
//
//            if( feat ){
//                
//                String name=null;
//                int geneLen=0;
//                String line=null;
//                List<String> goTerms=null;
//                List<String> keggTerms=null;
//
//                //print features lines
//                if( feat instanceof Gene ){
//                    name=feat.getGeneId();
//                    geneLen=feat.getLength();
//                    goTerms=feat.getGOTerms();
//                    keggTerms=feat.getKeggTerms();
//                }
//                else if( feat instanceof Isoform ){
//                    name=feat.getTranscriptId();
//                    geneLen=feat.getLength();
//                    goTerms=feat.getGOTerms();
//                    keggTerms=feat.getKeggTerms();
//                }
//                else if( feat instanceof PrimaryTranscript ){
//                    name=feat.getTssId();
//                    geneLen=feat.getLength();
//                    goTerms=feat.getGOTerms();
//                    keggTerms=feat.getKeggTerms();
//                }
//
//                String expres = '-' //under or over expressed
//                if( tokens[F_SIGNIF]=='yes' ){
//                    expres = ( val2>val1 ) ? OVER_EXPRESS : UNDER_EXPRESS
//                }
//
//                //write line : <name> <length> <is_de> <over/under> [<go:term1>,<go:term2>...] [<kegg1>,<kegg2>...]
//                line=name+'\t'+geneLen+'\t'+tokens[F_SIGNIF]+'\t'+expres+'\t';
//
//                if( goTerms!=null ){
//                    goTerms.each{ line += it+',' }
//                    line = line.substring(0,line.length()-1)
//                }
//
//                line+='\t';
//
//                if( keggTerms!=null ){
//                    keggTerms.each{ line += it+',' }
//                    line = line.substring(0,line.length()-1)
//                }
//
//                line+='\n';
//
//                out.write(line.getBytes());
//            }
//        }
//
//        r.close();
//    }



//    /**
//     * generates a BedGraph file for a diff expression file (NOT USED)
//     *
//     * @param source : diff file
//     * @param geneIndex
//     * @param out
//     * @param wHeader : if true then write header
//     * @throws FileNotFoundException
//     * @throws IOException
//     */
//    protected static void generateWIGFile(String source, GeneIndex geneIndex, OutputStream out, boolean wHeader)
//            throws FileNotFoundException, IOException, URISyntaxException {
//
//        BedGraphItem<Float> bedItem=new BedGraphItem<Float>();
//
//        ArrayList<BedGraphItem<Float>.BedGraphLimit<Float>> list =
//                new ArrayList<BedGraphItem<Float>.BedGraphLimit<Float>>(10000);
//
//        //read diff file
//        BufferedReader r =  Utils.createReader(source);
//
//        String l_line="";
//        r.readLine();//read header
//
//        while( (l_line=r.readLine())!=null ){
//            String [] tokens=l_line.split("\t");
//
//            def feat = getFeature(tokens[F_TEST_ID], geneIndex)
//
//            if( !feat )
//                continue;
//
//            if( !tokens[F_SIGNIF].toLowerCase().equals("yes") )
//                continue;//only diff expressed
//
//            int pos=tokens[F_LOCUS].indexOf(':');
//            int pos2=tokens[F_LOCUS].indexOf('-');
//            String chr=tokens[F_LOCUS].substring(0,pos);
//            int start=Integer.parseInt( tokens[F_LOCUS].substring(pos+1, pos2) );
//            int end=Integer.parseInt( tokens[F_LOCUS].substring(pos2+1) );
//
//            float diff=Float.valueOf(tokens[F_VAL2])-Float.valueOf(tokens[F_VAL1]);
//
//            //adds to list the limits of segments
//            bedItem.setChrom(chr);
//            bedItem.setChromStart(start);
//            bedItem.setChromEnd(end);
//            bedItem.setDataValue(diff);
//
//            list.add( bedItem.createBGLimit(
//                    LimitType.up, bedItem.getChrom(), bedItem.getChromStart(), bedItem.getDataValue())
//                    );
//            list.add( bedItem.createBGLimit(
//                    LimitType.down, bedItem.getChrom(), bedItem.getChromEnd(), bedItem.getDataValue())
//                    );
//
//        }
//
//        r.close();
//
//        //sort the limits
//        Collections.sort(list, bedItem.createBGLimitComp() );
//
//        int lim=list.size();
//
//         //write WIG file
//        if( wHeader ){
//            String file=source.substring( source.lastIndexOf(File.separator)+1 );
//            String header="track type=bedGraph name=\""+file+" FPKM diff.\" alwaysZero=on\n";
//            out.write(header.getBytes());
//        }
//
//        if( lim==0 )
//            return;
//
//        def begin=list.get(0);
//        def end=null;
//
//        float dataValue=begin.getValue();
//
//        //collect the segments and sum the value of overlapped ones
//        for( int i=1; i<lim; i++){
//
//            end=list.get(i);
//            boolean emitSegment = false;
//
//            if( begin.getCoord()!=end.getCoord() &&
//                    begin.getChr().equals(end.getChr()) ){
//                bedItem.setChromStart( begin.getCoord() );
//                bedItem.setChromEnd( end.getCoord() );
//                bedItem.setChrom( end.getChr() );
//                emitSegment=true;
//            }
//            else if( !begin.getChr().equals(end.getChr()) ){
//                dataValue=0;
//            }
//
//            if( emitSegment ){
//                bedItem.setDataValue( dataValue );
//                String textLine=bedItem.toString()+'\n';
//                out.write(textLine.getBytes());
//            }
//
//            if( end.getType()==LimitType.up ){
//                dataValue+=end.getValue();
//            }
//            else{
//                dataValue-=end.getValue();
//            }
//
//            begin=end;
//        }
//
//    }



    /**
     * generates all the RDF results files related to each diff file in 
     * cuffdiff output dir.
     * Also generates the GTF files for over/under expressed features.
     *
     * @param outDir : cuffdiff output directory
     * @param nsamples : number of samples
     * @param pathToFasta : path to genome fasta
     * @param fileFeat : feature file used by cuffdiff (path to)
     * @param taxonomyId
     * @param serverUrl
     * @param repositoryId
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void generateFeatAndGOAFiles( String outDir, int nsamples,
            String pathToFasta, String fileFeat, String taxonomyId, 
            String serverUrl, String repositoryId, String blastUrl,
            String blastDb, boolean naiveAnnot = false )
            throws FileNotFoundException, IOException, URISyntaxException {

        if( !outDir.endsWith(File.separator) )
            outDir+=File.separator;

        /* not used already 
        if( !anotBase.endsWith(File.separator) )
            anotBase+=File.separator;

        //create annotation
        File refGene = new File(anotBase+AnnotationDB.REFGENE_FILE);
        File goa = new File(anotBase+AnnotationDB.GOA_FILE);
        File kegg = new File(anotBase+AnnotationDB.KEGG_FILE);
        HashMap<String,IsoAnnotation> tableAnnot = null;

        if( refGene.exists() && goa.exists() && kegg.exists() )
            tableAnnot = IsoAnnotation.genAnnotFromRefGeneGOA(refGene, goa, kegg);
        */

        List<String> sequences=GeneIndex.getSequences(new File(fileFeat));
        int cont=0;

        for( String seq : sequences ){
            // builds and annotates index
            GeneIndex geneIndex = new GeneIndex( new File(fileFeat), seq );

            /* not used already 
            if( tableAnnot!=null )
                geneIndex.annotateIsoforms(tableAnnot, false);
            */

            for(int i=0; i<nsamples-1; i++){
                for(int j=i+1; j<nsamples; j++){
                    for( String file : ARRAY_FILETYPES ){

                        String fileBase = i+"_"+j+"_"+file;

                        if( !new File(outDir+fileBase).exists() ){
                            //the file is gzipped
                            fileBase += GZIP_FILE_EXT;
                        }

                        FileOutputStream out = null;

                        //generate feat file for over and under expressed
                        out = new FileOutputStream( new File(outDir+fileBase+FEATOVER_FILE_EXT), true);
                        generateFeatFile( outDir+fileBase, out, geneIndex, OVER_EXPRESS );
                        out.close();

                        out = new FileOutputStream( new File(outDir+fileBase+FEATUNDER_FILE_EXT), true);
                        generateFeatFile( outDir+fileBase, out, geneIndex, UNDER_EXPRESS );
                        out.close();
                        
                        /* not used already
                        if( tableAnnot!=null ){
                            //generate goa file (NOT USED)
                            out = new FileOutputStream( new File(outDir+fileBase+GOA_FILE_EXT), true);
                            generateGOAFile( outDir+fileBase, geneIndex, out );
                            out.close();
                        }

                        //generate info file (NOT USED)
                        out = new FileOutputStream( new File(outDir+fileBase+INFO_FILE_EXT), true);
                        generateDiffInfoFile( outDir+fileBase, out, geneIndex );
                        out.close();

                        //generate wig file (NOT USED)
                        out = new FileOutputStream( new File(outDir+fileBase+WIG_FILE_EXT), true);
                        generateWIGFile( outDir+fileBase, geneIndex, out, cont==0 );
                        out.close();
                        */
                    }
                }
            }

            cont++;
        }//end for sequences
        
        String jobId = outDir.substring(outDir.lastIndexOf('_')+1)
        String baseURI = NGSR.genDiffResURI(jobId)
        
        NGSDataResource annotation = null
        StatTestParams statParams = null
        
        if( [serverUrl, blastUrl].every{try{new URL(it)}catch(e){return false}; return true} ){
            //if the urls are correct then prepare annotation
            annotation = NGSDataResource.create( serverUrl, repositoryId )
            //prepare stats params
            statParams = new StatTestParams( annotation:annotation, taxonomyId:taxonomyId )
            statParams.loadAnnotation()
        }
        
        
        // annotate over and under expressed using BLAST and GO
        for(int i=0; i<nsamples-1; i++){
            for(int j=i+1; j<nsamples; j++){

                String fileBase = i+"_"+j+"_"+isoformDiffFile

                if( !new File(outDir+fileBase).exists() )
                    fileBase += GZIP_FILE_EXT //the file is gzipped
                    
                // generate graph for .diff file
                DiffExpresionRdfGen rdfgen = new DiffExpresionRdfGen()
                rdfgen.genGraph( outDir+fileBase, baseURI)

                if( annotation ){
                    if( naiveAnnot ){
                        rdfgen.genNaiveAnnotation(outDir+fileBase, annotation.lifeData, 
                            baseURI, taxonomyId )
                    }
                    else{
                        //load under and over expressed isoforms
                        def isoforms = [] 

                        [ FEATOVER_FILE_EXT, FEATUNDER_FILE_EXT ].each{
                            GeneIndex gindex = new GeneIndex( Utils.getFileIfCompress(outDir+fileBase+it) )
                            isoforms += gindex.getIsoforms()
                        }

                        //annotate isoforms using BLAST
                        BlastAnnotation blast = new BlastAnnotation();
                        blast.blastConfig.blastUrl = blastUrl
                        blast.blastConfig.db = blastDb
                        blast.blastConfig.localBlast = true
                        blast.setGoManager( new GOManager(annotation.lldGraph) );
                        blast.setLifeData( annotation.lifeData );

                        blast.annotateIsoforms(isoforms, pathToFasta);
                        rdfgen.genAnnotation( isoforms, baseURI)
                    }

                    //calculate overrepresented terms using BiNGO
                    annotation.ngsResGraph = rdfgen.graph

                    ['over':NGSR.PROP_OVEREPOVER, 'under':NGSR.PROP_OVEREPUNDER].each{ k, v->
                        statParams.overOrUnder = k
                        def binRes = BingoAlgorithm.performCalculations(statParams)
                        rdfgen.genOverepTerms( binRes.testMap, binRes.correctionMap, 
                            baseURI, v )
                    }
                }

                //write results graph to disk
                rdfgen.getGraph().dumpRDF( 
                    new FileOutputStream(new File(outDir+fileBase+RDF_FILE_EXT)), 
                    SimpleGraph.RDFXML);

            }
        }
    }

    /**
     * extract different samples in .diff cuffdiff files to diferent
     * x_y_ .diff prefixed files. The generated files are gzipped
     *
     * @param outDir : cuffdiff output directory
     * @param nsamples : number of samples
     */
    public static void extractSamplesDiffFiles( String outDir, int nsamples)
            throws IOException {

        for( String file : ARRAY_FILETYPES ){

            File fsrc=new File(outDir+file);
            if( !fsrc.exists() ){
                //the file is gzipped
                fsrc=new File(outDir+file+GZIP_FILE_EXT);
            }

            BufferedReader r=Utils.createReader(fsrc);

            //read header
            String header=r.readLine()+'\n';

            //generate all files
            for(int i=0; i<nsamples-1; i++){
                for(int j=i+1; j<nsamples; j++){
                    String fileBase = i+"_"+j+"_"+file;
                    File f=new File(outDir+fileBase);
                    FileOutputStream out = new FileOutputStream(f);
                    //write header
                    out.write(header.getBytes());
                    out.close();
                }
            }

            String l_line="";
            FileOutputStream out=null;
            String curr_prefix="";

            //read file and write lines to the appropiate output file
            while( (l_line=r.readLine())!=null ){
                String [] tokens=l_line.split("\t");

                //get fields
                int sample1=Integer.parseInt(tokens[F_SAMPLE1].substring(1));
                int sample2=Integer.parseInt(tokens[F_SAMPLE2].substring(1));

                String prefix=(sample1-1)+"_"+(sample2-1)+"_";

                if( !prefix.equals(curr_prefix) ){
                    curr_prefix=prefix;
                    File f=new File(outDir+prefix+file);

                    if( out!=null )
                        out.close();

                    out = new FileOutputStream(f,true);
                }

                l_line+='\n';
                out.write( l_line.getBytes() );
            }

            if( out!=null )
                out.close();

            r.close();

        }
    }


}
