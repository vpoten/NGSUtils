/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils

import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.concurrent.*



/**
 *
 */
class CommandRun implements Runnable {
    def command
    def outFile
    boolean exitIfError = true
    
    public void run(){
        println "Performing ${command}"
        def proc = command.execute()
        
        if( outFile )
            proc.consumeProcessOutputStream( new FileOutputStream(outFile) )
        else
            proc.consumeProcessOutput()

        if( proc.waitFor()!=0 ){
            System.err.println("Error while performing ${command}")
            
            if( exitIfError )
                System.exit(1)
        }
    }
}


/**
 *
 * @author victor
 */
public class Utils {

    protected static String WGET_COMMAND = "wget -nv --no-proxy"
    static final locusRegex = /(\w+):(\d+)-(\d+)/

    /**
     *
     * @param inputFile
     */
    public static void gzipFile( String inputFile ){
        try {
            // Create the GZIP output stream
            String outFilename = inputFile+".gz";
            GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(outFilename));

            // Open the input file
            FileInputStream inp = new FileInputStream(inputFile);

            // Transfer bytes from the input file to the GZIP output stream
            byte[] buf = new byte[4096];
            int len;
            while ((len = inp.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            inp.close();

            // Complete the GZIP file
            out.finish();
            out.close();
        } catch (IOException e) {
        }
    }


    /**
     * Executes a command
     *
     * @param command
     * @return : the command exit value
     */
    public static int execCommand( String command ){

        Process pro=null;
        try {
            pro = Runtime.getRuntime().exec(command);
            pro.waitFor();
        } catch (IOException ex) {
            return -1;
        } catch (InterruptedException ex) {
            return -1;
        }

        return pro.exitValue();
    }

    /**
     * create a buffered reader for a file checkin first if the file is gzipped
     *
     * @param file : a file path, if file doesnt start with '/' is treated as a resource
     * @return
     */
    public static BufferedReader createReader(String file) throws IOException, URISyntaxException{

        if( !file.startsWith(File.separator) ){
            //load as resource
            return new BufferedReader(
                    new InputStreamReader( getClass().getResourceAsStream('/'+file)) );
        }

        return createReader(new File(file));
    }

    public static BufferedReader createReader(File file) throws IOException{
        BufferedReader reader = null;

        if( file.getName().endsWith(".gz") ){
            reader=new BufferedReader(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(file))) );
        }
        else{
            reader=new BufferedReader(new FileReader(file));
        }

        return reader;
    }

    
    /**
     *
     */
    static def createInputStream(file){
        if( file.endsWith('.gz') )
            return new GZIPInputStream(new FileInputStream(file))
        else
            return new FileInputStream(file)
    }
    
    
    /**
     * untar the content of a .tar.gz file to a single file
     */
    static public boolean untarToFile( String fileIn, String fileOut ){

        println "Untar ${fileIn}."

        def output = new BufferedOutputStream(new FileOutputStream( fileOut ))
        def proc="tar -zxO -f ${fileIn}".execute()
        proc.consumeProcessOutputStream(output)

        if( proc.waitFor()!=0 ){
            System.err.println("Error: untar ${fileIn}.")
            return false
        }

        output.close()

        return true
    }
    
    static public InputStream untarToStream( String fileIn ){
        def proc="tar -zxO -f ${fileIn}".execute()
        return proc.getIn()
    }


    /**
     * utility method, create a directory if not exists
     */
    static public boolean createDir(String root){
        File f = new File(root)

        //create out dir
        if( f.exists() ){
            if( !f.isDirectory() ){
                System.err.println("Error: Base dir (${root}) is not a directory.")
                return false
            }
        }
        else if( !f.mkdirs() ){
            System.err.println("Error: Cannot create base dir (${root}).")
            return false
        }

        println "Created ${root} base directory."

        return true
    }

    /**
     * closure that compares two chromosomes names (chrA , chrB). Converts
     * A, B to integer and compares numerically if possible
     */
    static public def compChr = { a, b ->
        String a_str = a
        if( a.toLowerCase().startsWith('chr') )
            a_str = a.substring(3)

        String b_str = b
        if( b.toLowerCase().startsWith('chr') )
            b_str = b.substring(3)

        try{
            return (a_str as Integer)<=>(b_str as Integer)
        } catch(Exception e){}
            
        return a_str<=>b_str
    }
    
    /**
     * closure that compares two loci: <chr>:<start>-<end>
     */
    static public def locusComparator = [
        compare:{a,b->
            def locA = (a =~ Utils.locusRegex)
            def locB = (b =~ Utils.locusRegex)
            
            int res = compChr(locA[0][1],locB[0][1])
            
            if( res==0 ){
                res = (locA[0][2] as Integer)<=>(locB[0][2] as Integer)
                
                if( res==0 )
                    res = (locA[0][3] as Integer)<=>(locB[0][3] as Integer)
            }
            
            return res
        }
    ] as Comparator
    
    
    /**
     * download a file using wget
     */
    static def download(String urlSrc, String urlDst=null){
        String target = urlDst ? "-O ${urlDst}" : ''
        def p = "${WGET_COMMAND} ${target} ${urlSrc}".execute()
        
        if(p.waitFor()!=0){
            "rm ${urlDst ?: ''}".execute()//remove empty output file
            return false
        }
        
        return true
    }
    
    /**
     * get File, if not exists checks the compressed '.gz' file name
     */
    static File getFileIfCompress(String name){
        def file = new File(name)

        if( !file.exists() )
            file = new File(name+'.gz')
        else
            return file

        if( !file.exists() )
            return null

        return file
    }
    
    /**
     * count file lines
     */
    static int countLines(file){
        //get read count
        def p="wc -l ${file}".execute()

        if( p.waitFor()==0 ){
            def reader = new BufferedReader(new StringReader(p.text))
            return reader.readLine().split("\\s")[0] as Integer
        }
        
        return -1
    }
    
    /**
     *
     */
    static runClosures(list, int threads){
        def pool = Executors.newFixedThreadPool(threads)
        list.each{ pool.execute(it) }
        pool.shutdown()
        
        //wait all threads to finish
        while( !pool.isTerminated() ){
            try {
                Thread.sleep(1000)
            } catch (ex) { }
        }
    }
    
    /**
     *
     */
    static runCommands(list, int threads, listOutFiles = null, exitIfError = true){
        def pool = Executors.newFixedThreadPool(threads)
        list.eachWithIndex{ comm, i-> 
            pool.execute( new CommandRun( command:comm, 
                    outFile:((listOutFiles) ? listOutFiles[i] : null),
                    exitIfError:exitIfError )
            ) 
        }
        pool.shutdown()
        
        //wait all threads to finish
        while( !pool.isTerminated() ){
            try {
                Thread.sleep(1000)
            } catch (ex) { }
        }
    }
    
    /**
     * run a process using ProcessBuilder
     */
    static boolean procBuildRun(params, workDir){
        ProcessBuilder pb = new ProcessBuilder(params)
        if( workDir )
            pb.directory(new File(workDir))
            
        pb.redirectErrorStream(true)

        //run command
        Process pro= pb.start()

        //get command output
        BufferedReader r =  new BufferedReader( new InputStreamReader(pro.getInputStream()) );
        def output = new StringBuilder()
        r.eachLine{ output << "${it}\n" }
        r.close()

        try {
            //wait for process termination
            pro.waitFor();
        } catch (InterruptedException ex) {
        }
        
        if( pro.exitValue()!=0 ){
            System.err.println( output.toString() )
            return false
        }
        
        return true
    }
    
    /**
     * adapted from String.hashCode()
     */
    public static long hash(String string) {
        long h = 1125899906842597L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31*h + string.charAt(i);
        }
        return h;
    }
    
    /**
     *
     */
    public static boolean checkUrl(URL url) {
        try{
            def stream = url.openStream()
            stream.close()
        }
        catch(ex){
            //URL error
            return false
        }
        
        return true
    }
}

