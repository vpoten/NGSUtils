/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils;


import java.util.logging.Level;
import java.util.logging.Logger;
import org.ngsutils.diff.DiffExpresionFileTool;
import org.ngsutils.semantic.rdfutils.RDFLoad
import java.util.zip.GZIPOutputStream
import org.ngsutils.semantic.LinkedLifeData as LLD
import org.ngsutils.Utils

/**
 *
 * @author victor
 */
public class Main {

    protected static final String COMM_ADDRDF = 'addrdf'
    protected static final String COMM_ADDLLD = 'addlld'
    protected static final String COMM_ADDRDFMEM = 'addRdfMem'
    
    protected static final String LOCAL_SERVER = "http://localhost:8080/openrdf-sesame"
    protected static final int RDF_MAX_SIZE = 10000000 //10 MB
    
            
    private static final String OPT_KEGG_GENE = "keggGene";
    private static final String OPT_GO_PROB = "goProb";
    private static final String OPT_GO_CLUSTER = "goCluster";
    private static final String OPT_SPLIT_FQ = "splitFastq";
    private static final String OPT_DIFF_TOOL = "diffTool";
    private static final String OPT_ANNOT_DB = "annotDB";
    private static final String OPT_BOWTIE_DB = "bowtieDB";
    private static final String OPT_JBROW_TRACK = "jbTracks";
    private static final String OPT_CLEAN_FASTQ = "cleanFastq";


    private static def OPTIONS = [ OPT_KEGG_GENE, OPT_GO_PROB, OPT_GO_CLUSTER, OPT_SPLIT_FQ,
        OPT_DIFF_TOOL, OPT_ANNOT_DB, OPT_BOWTIE_DB, OPT_JBROW_TRACK, COMM_ADDRDF, COMM_ADDLLD,
        COMM_ADDRDFMEM, OPT_CLEAN_FASTQ ]

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if( args.length==0 || !(args[0] in OPTIONS) ) {
            
            if( args.length>0 )
                System.err.println( args[0]+" operation, not valid.");

            System.err.println( "Valid operations : "+OPTIONS );
            printLLDUsages();
            System.exit(-1);
        }

        println "Start time: ${new Date()}\n"
        def res = null
        
        //usage: keggGene <organism> <file Ent> <file List> <out file>
        //example: keggGene hsa H.sapiens.ent hsa_pathway.list hsa_out.txt

        if( args[0] == OPT_KEGG_GENE ){
            if( args.length!=5 ){
                System.err.println("usage: "+OPT_KEGG_GENE+" <org> <file Ent> <file List> <out file>");
                System.exit(-1);
            }

            try {
                joinKeggGeneFiles(new File(args[2]), new File(args[3]), new File(args[4]), args[1]);
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            } 
        }
        else if( args[0] == OPT_GO_PROB ){
            //example: goProb gene_association.goa go_prob.txt
            if( args.length!=3 ){
                System.err.println("usage: "+OPT_GO_PROB+" <goa file> <out file>");
                System.exit(-1);
            }
            try {
                calcGOProbabilities(new File(args[1]), new File(args[2]));
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            } 
        }
        else if( args[0] == OPT_GO_CLUSTER ){

//            if( args.length!=7 || !args[6].startsWith("--out=") ){
//                System.err.println("usage: "+OPT_GO_CLUSTER+" <MF|CC|BP> <over|under> <obo_file> <goa file> <diffgo_file> --out=<out dir>");
//                System.exit(-1);
//            }
//
//            String outDir=args[6].substring( args[6].indexOf('=')+1 );
//            if( !outDir.endsWith(File.separator) )
//                outDir+=File.separator;
//            
//            String outFile=outDir+args[5].substring(args[5].lastIndexOf(File.separator)+1)+".clust";
//
//            boolean useOver=true;
//
//            if( args[2].equals("under") )
//                useOver=false;
//
//            try {
//                GOClustererFactory.doGOCluster(args[3], args[4], args[5], new PrintStream(outFile), args[1], useOver);
//            } catch (Exception ex) {
//                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//                System.exit(-1);
//            }
            
        } else if( args[0] == OPT_SPLIT_FQ ){

             boolean badParams=true;

             if( args.length==4 )
                 badParams=false;
             else if( args.length==5 && args[1].equals("--reverse") )
                 badParams=false;
             
             if( badParams){
                System.err.println("usage: "+OPT_SPLIT_FQ+" [--reverse] <fastq> <fq_out1> <fq_out2>");
                System.exit(-1);
             }

             try{
                 if( args.length==5 ){
                     splitFastq( new File(args[2]), new File(args[3]) , new File(args[4]), true );
                 }
                 else{
                     splitFastq( new File(args[1]), new File(args[2]) , new File(args[3]), false );
                 }
             } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
             }
         }
        else if( args[0] == OPT_DIFF_TOOL ){
            if( args.length!=10 ){
                System.err.println("usage: "+OPT_DIFF_TOOL+" <diffOutDir> <nsamples> <pathToFasta> "+
                    "<fileFeat> <taxonomyId> <serverURL> <repositoryId> <blastURL> <blastDb>");
                System.exit(-1);
            }
            try {
                DiffExpresionFileTool.extractSamplesDiffFiles( args[1], Integer.parseInt(args[2]) );
                DiffExpresionFileTool.generateFeatAndGOAFiles( args[1], Integer.parseInt(args[2]), args[3], 
                    args[4], args[5], args[6], args[7], args[8], args[9] );
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }
        }
        else if( args[0] in [OPT_ANNOT_DB, OPT_BOWTIE_DB] ){
            //example: annotDB /home/annotation
            //example: bowtieDB /home/bowtie
            boolean error = false;

            if( args.length<2 || args.length>3  )
                error = true;
            if( args.length==3 && !args[2].startsWith("org=") )
                error = true;

            if( error ){
                System.err.println("usage: "+OPT_ANNOT_DB+" <OutDir> [ org=code[,code]* ]");
                System.err.println("usage: "+OPT_BOWTIE_DB+" <OutDir> [ org=code[,code]* ]");
                System.exit(-1);
            }

            List<String> codes = null;

            if( args.length==3 ){
                //get organisms codes
                String [] tokens=args[2].substring(4).split(",");
                codes = new ArrayList<String>();
                codes.addAll(Arrays.asList(tokens));
            }

            try {

                if( args[0].equals(OPT_ANNOT_DB) )
                    AnnotationDB.createAnnotation( args[1], codes );
                else
                    AnnotationDB.createIndexes( args[1], codes );

            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }
        }
        else if( args[0] == OPT_JBROW_TRACK ){
            //example: jbTracks /jbrowse_path/ /home/jbrowse_out /home/bowtie_idx /home/annotation 'http://host' [org=codes]
            //example of urlTemplate : http://localhost:8090/NGSEngine/featProxy/show
            //example2: jbTracks /home/victor/Escritorio/JBrowse-1.5.0-full/ /home/victor/test_annot/ 
            // /home/victor/bowtie_indexes/ /home/victor/work_bio/annotation/ "http://localhost/" org=9606

            boolean error = false;

            if( args.length<6 || args.length>7 )
                error = true;
            if( args.length==7 && !args[6].startsWith("org=") )
                error = true;

            if( error ){
                System.err.println("usage: "+OPT_JBROW_TRACK+" <JBrowse bin> <OutDir> <BowtieDir> <AnnotationDir> <urlTemplateBase> [ org=code[,code]* ]");
                System.exit(-1);
            }

            List<String> codes = null;

            if( args.length==7 ){
                //get organisms codes
                String [] tokens=args[6].substring(4).split(",");
                codes = new ArrayList<String>();
                codes.addAll(Arrays.asList(tokens));
            }

            try {
                JBrowseDataManager.config(args[1]);
                JBrowseDataManager.createAnnotationTracks( args[2], args[3], args[4], args[5], codes );
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }
        }
        else if( args[0] in [COMM_ADDRDF,COMM_ADDLLD,COMM_ADDRDFMEM] ){
            //LLD operations
            switch(args[0]){
                case (COMM_ADDRDF): 
                    res = commAddRdf(args)
                    break
                case (COMM_ADDLLD):
                    res = commAddLLD(args)
                    break
                case (COMM_ADDRDFMEM):
                    //add RDF files inside folder to in-memory repository
                    res = commAddRdfMem(args)
                    break
                default:
                    println 'Command not valid.'
                    res = 1
                    break
            }
        }
        else if( args[0] == OPT_CLEAN_FASTQ ){
            if( args.length != 3 ){
                System.err.println("usage: "+OPT_CLEAN_FASTQ+" <fastq_1> <fastq_2>");
                System.exit(-1);
            }
            
            String file1 = args[1]
            String file2 = args[2]
            FastqReadUtil.cleanUnpairedReads(file1, file2)
            res = 0;
        }

        println "End time: ${new Date()}\n"
        
        if(res)
            System.exit(res)
        
    }


    /**
     * joing kegg files to obtain a gene_id-pathway relations file
     *
     * @param keggGeneEnt
     * @param geneList
     * @param outFile
     * @param keggOrg
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void joinKeggGeneFiles( File keggGeneEnt, File geneList, File outFile, String keggOrg)
            throws FileNotFoundException, IOException {

        //indexs file of entry_id/keggs_id
        FileIndex geneListIdx = new FileIndex( geneList, 0, 1, "\t", null );

        //process keggGeneEnt file to get entry ids and associated gene names
        BufferedReader r =  Utils.createReader(keggGeneEnt);

        String l_line="";
        HashMap<String,List<String>> entries=new HashMap<String,List<String>>();

        String entry_id=null;

        while( (l_line=r.readLine())!=null ){

            String [] tokens=null;

            if( l_line.startsWith("ENTRY") ){
                String val=l_line.substring( l_line.indexOf(' ') ).trim();
                entry_id=val.substring(0, val.indexOf(' '));
            }
            else if( l_line.startsWith("NAME") ){
                String val=l_line.substring( l_line.indexOf(' ') ).trim();
                tokens=val.split(",");

                List<String> list=new ArrayList<String>();
                for(int i=0; i<tokens.length; i++){
                    list.add( tokens[i].trim() );
                }

                entries.put( entry_id, list);
            }

        }

        r.close();

        //write the output file. with line format: <gene name> <keggid1>
        PrintStream out=new PrintStream( outFile );

        for( String id : entries.keySet() ){
            List<String> geneNames=entries.get(id);
            List<String> keggIds=geneListIdx.getValue( keggOrg+':'+id );

            if( keggIds==null )
                continue;

            for( String name : geneNames ){
                for( String keggId : keggIds ){
                    //remove keggId prefix
                    keggId=keggId.substring(keggId.indexOf(keggOrg)+keggOrg.length());
                    
                    out.println( name+'\t'+keggId );
                }
            }
        }

        out.close();


    }


    /**
     * calculates freqs., probs. and ic. of GO terms and write them to a file
     *
     * @param goaFile : goa file
     * @param outFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void calcGOProbabilities( File goaFile, File outFile)
            throws FileNotFoundException, IOException {

        FileIndex indexGOA=new FileIndex( goaFile, 4, 2, "\t", "!" );

        double maxFreq=0.0;
        HashMap<String,Double> frequencies=new HashMap<String,Double>();

        double invTotal=1.0/(indexGOA.getKeys().size());

        //calculate frequencies
        for( String key : indexGOA.getKeys() ){
            double freq=indexGOA.getValue(key).size()*invTotal;
            frequencies.put(key, freq );

            if( freq>maxFreq )
                maxFreq=freq;
        }


        //write output file
        maxFreq = 1.0/maxFreq;
        double invLog= 1.0/Math.log(2);


        PrintStream out=new PrintStream( outFile );

        for( String key : indexGOA.getKeys() ){
            double freq=frequencies.get(key);
            double prob=freq*maxFreq;
            double ic= -Math.log(prob)*invLog;

            out.println( key+'\t'+freq+'\t'+prob+'\t'+ic );
        }

        out.close();
        
    }


    

    /**
     *
     * @param file : input fastq file to split
     * @param out1 : out1
     * @param out2 : out2
     * @param reverse
     */
    private static void splitFastq(File file, File out1, File out2, boolean reverse)
            throws FileNotFoundException, IOException  {

        PrintStream pout1=new PrintStream( out1 );
        PrintStream pout2=new PrintStream( out2 );

        BufferedReader r = Utils.createReader(file);

        String [] lines=new String [4];
        int cont=0;


        while( (lines[0]=r.readLine())!=null ){

            lines[1]=r.readLine();
            lines[2]=r.readLine();
            lines[3]=r.readLine();

            pout1.println( lines[0]+"/1");
            pout2.println( lines[0]+"/2");

            int len=lines[1].length()/2;
            pout1.println( lines[1].substring(0, len) );

            if(reverse){
                StringBuilder buffer = new StringBuilder(lines[1].substring(len));
                pout2.println( buffer.reverse().toString() );
            }
            else{
                pout2.println( lines[1].substring(len) );
            }

            pout1.println( lines[2]+"/1");
            pout2.println( lines[2]+"/2");

            pout1.println( lines[3].substring(0, len) );

            if(reverse){
                StringBuilder buffer = new StringBuilder(lines[3].substring(len));
                pout2.println( buffer.reverse().toString() );
            }
            else{
                pout2.println( lines[3].substring(len) );
            }

            cont++;
        }

        r.close();
        pout1.close();
        pout2.close();

        System.out.println( "Processed "+cont+" reads." );
    }

    protected static def printLLDUsages(){
        println 'LLD Usages:'
        println 'Load a rdf file to a local repository:'
        println "${COMM_ADDRDF} <rdf file> <baseURI> <repos. ID>"
        println 'Download and load the LinkedLifeData dataset to a local repository:'
        println "${COMM_ADDLLD} <workdir> <repos. ID>"
         println 'Load a folder containing rdf files to a in-memory repository:'
        println "${COMM_ADDRDFMEM} <rdf dir> <baseURI>"
    }
    
    
    protected static def commAddRdf(String[] args){
        //check inputs
        if( args.length!=4 ){
            printLLDUsages()
            return 1
        }

        String file = args[1]
        String sesameServer = LOCAL_SERVER
        String baseURI = args[2]
        String repositoryID = args[3]
        
        if( !(new File(file).exists()) ){
            println "File ${file} not exists."
            return 1
        }
        
        println "File: ${file}"
        println "BaseURI: ${baseURI}"
        println "RepositoryID: ${repositoryID}"
        println "Server: ${sesameServer}\n"
        
        return addRdf(file, sesameServer, baseURI, repositoryID)
    }
    
    
    protected static def commAddLLD(String[] args){
        //check inputs
        if( args.length!=3 ){
            printLLDUsages()
            return 1
        }
        
        String sesameServer = LOCAL_SERVER
        String dir = args[1]
        String repositoryID = args[2]
        
        if( !(new File(dir).isDirectory()) ){
            println "Workdir ${dir} not exists."
            return 1
        }
        
        println "Workdir: ${dir}"
        println "RepositoryID: ${repositoryID}"
        println "Server: ${sesameServer}\n"
        
        if( !dir.endsWith(File.separator) )
            dir+=File.separator
            
        String tmpSuff = '.trig.gz'
        
        LLD.getBySize(0,RDF_MAX_SIZE).each{ k, v->
            println "Adding ${k} database. ${new Date()}"

            String fileName = v[LLD.ARCHIVE].substring(v[LLD.ARCHIVE].lastIndexOf('/')+1)
            String target = dir+fileName
            boolean error = false
            
            if( new File(target).exists() ){
                println "${v[LLD.ARCHIVE]} already exists"
            } 
            else{
                // download file
                println "Downloading ${v[LLD.ARCHIVE]}"
                
                if( !Utils.download( v[LLD.ARCHIVE], target ) ){
                    println "Error downloading ${v[LLD.ARCHIVE]}"
                    error = true
                }
            }

            
            if( !error ){
                // untar target
                String tmpFile = dir+fileName.substring(0, fileName.indexOf('.'))+tmpSuff
                untarRdfFile(target, tmpFile)
                
                // add file
                addRdf(tmpFile, sesameServer, v[LLD.GRAPH], repositoryID)

                //cleaning out
                "rm -rf ${tmpFile}".execute().waitFor()
            }
        }
        
        return 0
    }
    
    /**
     * utility method
     */
    private static def untarRdfFile(String tarFile, String gzFile){
        def p1 = "tar -Ozxf ${tarFile}".execute()
        def p2 = "gzip".execute()
        p1 | p2
        def outstr = new FileOutputStream(gzFile)
        p2.consumeProcessOutputStream(outstr)
        p2.waitFor()
        outstr.close()
    }
    
    /**
     * 
     * calling example: addRdfMem /home/victor/Escritorio/rdf_libs/lld/entrezgene/ http://localhost
     */
    protected static def commAddRdfMem(String[] args){
        String dir = args[1]
        String baseURI = args[2]
        
        def rdfloader = new RDFLoad(baseURI:baseURI)
        // build list of memcached servers
        def servers = ['localhost:11211']
        def myRepository = RDFLoad.createMemRepository(true, false, servers)
        String tmpSuff = '.trig.gz'
        
        new File(dir).eachFile{ file->
            if( file.isFile() && (file.name.endsWith('.tar.gz') || RDFLoad.isRdf(file.name)) ){
                File tmpFile = file
                
                if( file.name.endsWith('.tar.gz') ){
                    tmpFile = new File(dir+file.name.substring(0, file.name.indexOf('.'))+tmpSuff)
                    untarRdfFile(dir+file.name, dir+tmpFile.name)
                }
                
                println "Adding ${tmpFile.name} to repository [${new Date()}]"
                
                if( (tmpFile.size())<(RDF_MAX_SIZE/2)  )
                    rdfloader.addRdf(dir+tmpFile.name, myRepository)
                else
                    rdfloader.splitAndAdd(dir+tmpFile.name, System.err, myRepository)
                    
                //cleaning out
                if( file.name.endsWith('.tar.gz') )
                    "rm -rf ${dir+tmpFile.name}".execute().waitFor()
            }
        }
        
        return 0
    }
    
    /**
     * Adds a rdf file to server using baseURI and repositoryID
     */
    protected static def addRdf(file, sesameServer, baseURI, repositoryID){
        def rdfloader = new RDFLoad(sesameServer:sesameServer, baseURI:baseURI, repositoryID:repositoryID)
        
        try{
            println "Adding ${file} to ${repositoryID}"

            if( (new File(file).size())<(RDF_MAX_SIZE/2)  )
                rdfloader.addRdf(file)
            else
                rdfloader.splitAndAdd(file, System.err)
            
        } catch(e){
            e.printStackTrace(System.err)
            return 1
        }
        
        return 0
    }
    
}
