/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author victor
 */

public class Grep {

    // Charset and decoder for UTF-8
    private static Charset charset = Charset.forName("UTF-8");

    private static CharsetDecoder decoder = charset.newDecoder();

    // Pattern used to parse lines
    private static Pattern linePattern = Pattern.compile(".*\r?\n");



   /**
    * Use the linePattern to break the given CharBuffer into lines, applying
    * the input pattern to each line to see if we have a match
    *
    * @param f
    * @param cb
    * @param pattern
    * @param out
    */
    private static void grep(File f, CharBuffer cb, Pattern pattern, OutputStream out)
          throws IOException {
        Matcher lm = linePattern.matcher(cb); // Line matcher
        Matcher pm = null; // Pattern matcher

        int lines = 0;
        while (lm.find()) {
            lines++;
            CharSequence cs = lm.group(); // The current line

            if (pm == null)
                pm = pattern.matcher(cs);
            else
                pm.reset(cs);
            
            if (pm.find())//if the pattern matchs, write the line
                out.write( cs.toString().getBytes() );

            if (lm.end() == cb.limit())
                break;
        }
    }

    
    /**
     *
     * @param f
     * @param cb
     * @param pattern
     * @param fields
     * @param field
     * @param separator
     * @throws IOException
     */
    private static void grep(File f, CharBuffer cb, Pattern pattern,
            List<String> fields, int field, String separator )
          throws IOException {
        Matcher lm = linePattern.matcher(cb); // Line matcher
        Matcher pm = null; // Pattern matcher

        int lines = 0;
        while (lm.find()) {
            lines++;
            CharSequence cs = lm.group(); // The current line

            if (pm == null)
                pm = pattern.matcher(cs);
            else
                pm.reset(cs);

            if (pm.find()){//if the pattern matchs, append the field
                String tokens [] = cs.toString().split(separator);
                fields.add( tokens[field] );
            }

            if (lm.end() == cb.limit())
                break;
        }
    }


    /**
     * Search for occurrences of the input pattern in the given file and write
     * the lines to output, uses a mapped memory buffer
     * 
     * @param f
     * @param regex
     * @param out
     * @throws IOException
     */
    public static void grep(File f, String regex, OutputStream out)
            throws IOException {

        //compile the regular expression
        Pattern pattern = Pattern.compile(regex);

        // Open the file and then get a channel from the stream
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();

        // Get the file's size and then map it into memory
        int sz = (int) fc.size();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

        // Decode the file into a char buffer
        CharBuffer cb = decoder.decode(bb);

        // Perform the search
        grep(f, cb, pattern, out);

        // Close the channel and the stream
        fc.close();
    }


    /**
     * Search for occurrences of the input pattern in the given file and append
     * the field of the line to the list, uses a mapped memory buffer
     *
     * @param f
     * @param regex : regex to search
     * @param fields : list where the results are appended
     * @param field : field index to return (zero based)
     * @param separator : regex for field separator ("\t")
     * @throws IOException
     */
    public static void grep(File f, String regex, List<String> fields, int field, String separator)
            throws IOException {

        //compile the regular expression
        Pattern pattern = Pattern.compile(regex);

        // Open the file and then get a channel from the stream
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();

        // Get the file's size and then map it into memory
        int sz = (int) fc.size();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

        // Decode the file into a char buffer
        CharBuffer cb = decoder.decode(bb);

        // Perform the search
        grep(f, cb, pattern, fields, field, separator);

        // Close the channel and the stream
        fc.close();
    }

    /**
     *
     * @param f
     * @param key : keyword to find in each line
     * @param fields : list where to append the required fields
     * @param field : 0 based index of requested field (if -1 gets the whole line)
     * @param separator : regex for field separator ("\t")
     * @throws IOException
     */
    public static void grepSimple(File f, String key, List<String> fields, int field, String separator)
            throws IOException {

        BufferedReader r =  Utils.createReader(f);

        String l_line="";

        while( (l_line=r.readLine())!=null ){
            if( l_line.indexOf(key)>=0 ){
                String tokens [] = l_line.split(separator);
                if( field>=0 )
                    fields.add( tokens[field] );
                else
                    fields.add( l_line );
            }
        }

        r.close();

    }

    /**
     *
     * @param f : input file
     * @param regex : regex to find in each line
     * @param out
     * @throws IOException
     */
    public static void grepSimple(File f, String regex, OutputStream out)
            throws IOException {

        //compile the regular expression
        Pattern pattern = Pattern.compile(regex);
        Matcher pm = null; // Pattern matcher

        BufferedReader r =  Utils.createReader(f);

        String l_line="";

        while( (l_line=r.readLine())!=null ){

            if (pm == null)
                pm = pattern.matcher(l_line);
            else
                pm.reset(l_line);

            if( pm.find() ){
                l_line+='\n';
                out.write( l_line.getBytes() );
            }
        }

        r.close();

    }

}

