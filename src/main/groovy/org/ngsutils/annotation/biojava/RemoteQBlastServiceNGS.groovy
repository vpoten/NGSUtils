/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.biojava

import org.biojava.bio.BioException
import org.biojavax.bio.alignment.RemotePairwiseAlignmentProperties
import org.biojavax.bio.alignment.RemotePairwiseAlignmentService
import org.biojavax.bio.alignment.RemotePairwiseAlignmentOutputProperties
import org.biojava.bio.seq.Sequence

/**
 * Class that fixes RemoteQBlastService.sendActualAlignementRequest
 * 
 * @author victor
 */
class RemoteQBlastServiceNGS {
	
    String baseUrl = "http://www.ncbi.nlm.nih.gov/blast/Blast.cgi";
    private URL aUrl;
    private URLConnection uConn;

    String email = "anonymous@biojava.org";
    String tool = "biojavax";
    int numHits = 50

    private String rid;
    private long step;
    private long start;
    private HashMap<String, Long> holder = new HashMap<String, Long>();
        
    /**
     * constructor
     */
    public RemoteQBlastServiceNGS(String newUrl = null) throws BioException {
        if( newUrl)
            baseUrl = newUrl
        
        try {
            aUrl = new URL(baseUrl);
            uConn = setQBlastServiceProperties(aUrl.openConnection());
        }
        /*
         * Needed but should never be thrown since the URL is static and known
         * to exist
         */
        catch (MalformedURLException e) {
            throw new BioException(
                        "It looks like the URL for NCBI QBlast service is wrong.\n");
        }
        /*
         * Intercept if the program can't connect to QBlast service
         */
        catch (IOException e) {
            throw new BioException(
                        "Impossible to connect to QBlast service at this time. Check your network connection.\n");
        }
    }
    
    
    /**
     * This class is the actual worker doing all the dirty stuff related to
     * sending the Blast request to the CGI_BIN interface. It should never be
     * used as is but any method wanting to send a Blast request should manage
     * to use it by feeding it the right parameters.
     * 
     * @param str : a <code>String</code> representation of a sequence from either of the
     *              three wrapper methods
     * @param rpa :a <code>RemotePairwiseAlignmentProperties</code> object
     * @return rid : the ID of this request on the NCBI QBlast server
     * @throws BioException if unable to connect to the NCBI QBlast service
     */
    private String sendActualAlignementRequest(String str,
                    RemotePairwiseAlignmentProperties rpa) throws BioException {

        String seq = null
        String prog = null
        String db = null
    
        seq = "QUERY=" + str;
        prog = "PROGRAM=" + rpa.getAlignmentOption("PROGRAM");
        db = "DATABASE=" + rpa.getAlignmentOption("DATABASE");

        if (prog == null || db == null || str == null || str.length() == 0) {
            throw new BioException(
                    "Impossible to execute QBlast request. One or more of sequence|database|program has not been set correctly.\n");
        }

        String cmd = "CMD=Put&SERVICE=plain&${seq}&${prog}&${db}&HITLIST_SIZE=${numHits}&FORMAT_TYPE=HTML&TOOL=${tool}&EMAIL=${email}"

        // This is a not so good hack to be fix by forcing key 
        // checking in RemoteQBlastAlignmentProperties
        try{
            cmd += cmd + "&" + rpa.getAlignmentOption("OTHER_ADVANCED");
        } catch(BioException e){}
        

        try {

            uConn = setQBlastServiceProperties(aUrl.openConnection());

            def fromQBlast = new OutputStreamWriter(uConn.getOutputStream());

            fromQBlast.write(cmd);
            fromQBlast.flush();

            // Get the response
            def rd = new BufferedReader(new InputStreamReader(uConn.getInputStream()));

            rd.eachLine { line->
                if (line.contains("RID")) {
                    String[] arr = line.split("=");
                    rid = arr[1].trim();
                } else if (line.contains("RTOE")) {
                    String[] arr = line.split("=");
                    step = Long.parseLong(arr[1].trim()) * 1000;
                    start = System.currentTimeMillis() + step;
                }
                holder.put(rid, start);
            }
            
            fromQBlast.close()
            rd.close()
        } catch (IOException e) {
            throw new BioException(
                        "Can't submit sequence to BLAST server at this time.\n");
        }

        return rid;
    }
    
    
    private URLConnection setQBlastServiceProperties(URLConnection conn) {

        URLConnection tmp = conn;

        conn.setDoOutput(true);
        conn.setUseCaches(false);

        tmp.setRequestProperty("User-Agent", "Biojava/RemoteQBlastService");
        tmp.setRequestProperty("Connection", "Keep-Alive");
        tmp.setRequestProperty("Content-type",
                        "application/x-www-form-urlencoded");
        tmp.setRequestProperty("Content-length", "200");

        return tmp;
    }

    
    public String sendAlignmentRequest(String str, RemotePairwiseAlignmentProperties rpa) 
        throws BioException {
        return rid = sendActualAlignementRequest(str, rpa);
    }
    
    
    public String sendAlignmentRequest(Sequence objSeq, RemotePairwiseAlignmentProperties rpa)
        throws BioException {
        return rid = sendActualAlignementRequest(objSeq.seqString(), rpa)
    }
    
    
    public String sendAlignmentRequest(int gid, RemotePairwiseAlignmentProperties rpa)
        throws BioException {
        return rid = sendActualAlignementRequest(gid as String, rpa);
    }
    
    
    public boolean isReady(String id, long present) throws BioException {
        boolean ready = false;
        String check = "CMD=Get&RID=${id}"

        if (holder.containsKey(id)) {
            /*
             * If present time is less than the start of the search added to
             * step obtained from NCBI, just do nothing ;-)
             * 
             * This is done so that we do not send zillions of requests to the
             * server. We do the waiting internally first.
             */
            if (present < start) {
                ready = false;
            }
            /*
             * If we are at least step seconds in the future from the actual
             * call sendAlignementRequest()
             */
            else {
                try {
                    uConn = setQBlastServiceProperties(aUrl.openConnection());

                    def fromQBlast = new OutputStreamWriter(uConn.getOutputStream());
                    fromQBlast.write(check);
                    fromQBlast.flush();

                    def rd = new BufferedReader(new InputStreamReader(uConn.getInputStream()));
                    
                    rd.eachLine { line->
                        if (line.contains("READY")) {
                            ready = true;
                        } else if (line.contains("WAITING")) {
                            /*
                             * Else, move start forward in time... for the next
                             * iteration
                             */
                            start = present + step;
                            holder.put(id, start);
                        }
                    }
                    
                    fromQBlast.close()
                    rd.close()
                } catch (IOException e) {
                    e.printStackTrace()
                    throw new BioException("IO exception caught\n")
                }
            }
        } else {
            throw new BioException("Impossible to check for request ID named "
                            + id + " because it does not exists!\n");
        }
        return ready;
    }
    
    
    public InputStream getAlignmentResults(String id,
                    RemotePairwiseAlignmentOutputProperties rb) throws BioException {
        if (holder.containsKey(id)) {
            String srid = "CMD=Get&RID=${id}&" +
                            rb.getOutputOption("FORMAT_TYPE") + "&" +
                            rb.getOutputOption("ALIGNMENT_VIEW") + "&" +
                            rb.getOutputOption("DESCRIPTIONS") + "&" +
                            rb.getOutputOption("ALIGNMENTS") +
                           "&TOOL=${tool}&EMAIL=${email}";

            try {
                uConn = setQBlastServiceProperties(aUrl.openConnection());

                def fromQBlast = new OutputStreamWriter(uConn.getOutputStream());
                fromQBlast.write(srid);
                fromQBlast.flush();
                fromQBlast.close()

                return uConn.getInputStream();

            } catch (IOException ioe) {
                throw new BioException(
                        "It is not possible to fetch Blast report from NCBI at this time.\n");
            }
        } else {
            throw new BioException(
                        "Impossible to get output for request ID named " + id
                                        + " because it does not exists!\n");
        }
    }

    
    public void printRemoteBlastInfo() throws BioException {
        try {
            OutputStreamWriter out = new OutputStreamWriter(uConn.getOutputStream());

            out.write("CMD=Info");
            out.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(uConn.getInputStream()));

            rd.eachLine{
                println it
            }

            out.close();
            rd.close();
        } catch (IOException e) {
            throw new BioException(
                    "Impossible to get info from QBlast service at this time. Check your network connection.\n");
        }
    }
    
     public boolean deleteRequest(String id){
         try {
            OutputStreamWriter out = new OutputStreamWriter(uConn.getOutputStream())

            out.write("CMD=Delete&RID=${id}")
            out.flush()

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(uConn.getInputStream()))

            rd.eachLine{}

            out.close()
            rd.close()
        } catch (IOException e) {
            throw new BioException("Impossible to Delete for request ID ${id}")
            return false
        }
        
        return true
     }
    
}

