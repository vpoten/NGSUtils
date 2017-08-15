/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.biojava;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.biojava.bio.BioException;
import org.biojava.bio.program.gff.GFFEntrySet;
import org.biojava.bio.program.gff.GFFFilterer;
import org.biojava.bio.program.gff.GFFParser;
import org.biojava.bio.program.gff.GFFRecordFilter;
import org.biojava.bio.program.gff.GFFTools;
import org.biojava.utils.ParserException;

/**
 * Light version of biojava GFFTools, uses GFFParserLight
 *
 * @author victor
 */
public class GFFToolsLight extends GFFTools {


 /**
  * Reads a <code>GFFEntrySet</code> from a file with no filtering.
  *
  * @param inFile the File containing the GFF
  * @throws FileNotFoundException if file is not found
  * @throws ParserException if format is wrong
  * @throws BioException if format is wrong
  * @throws IOException if file reading error occurs
  * @return a <code>GFFEntrySet</code> encapsulating the records read from the file
  */
  public static GFFEntrySet readGFF(File inFile)
    throws FileNotFoundException, ParserException, BioException, IOException
  {
    return GFFToolsLight.readGFF(inFile, GFFRecordFilter.ACCEPT_ALL);
  }

  /**
   * Reads a GFFEntrySet from a file with the specified filter.
   *
   * @param inFile the File containing the GFF
   * @param recFilt the filter to use
   * @throws FileNotFoundException if file is not found
   * @throws ParserException if format is wrong
   * @throws BioException if format is wrong
   * @throws IOException if file reading error occurs
   * @return a <code>GFFEntrySet</code> encapsulating the records read from the file
   */
  public static GFFEntrySet readGFF(File inFile, GFFRecordFilter recFilt)
    throws FileNotFoundException, ParserException, BioException, IOException
  {
        GFFEntrySet gffEntries = new GFFEntrySet();
        GFFFilterer filterer = new GFFFilterer(gffEntries.getAddHandler(),recFilt);
        GFFParser parser = new GFFParserLight();
        parser.parse(new BufferedReader(new FileReader(inFile)),filterer);
        return gffEntries;
  }

  /**
   * Read all GFF entries from a buffered reader.
   *
   * This will read up untill the end of the reader.
   *
   * @param gffIn  the BufferedReader to read text from
   * @return a GFFEntrySet containing all of the GFF that could be read
   * @throws parserException  if the text could not be parsed as GFF
   * @throws BioException if there was some error reading the GFF
   * @throws IOException if there was an error with the reader
   */
  public static GFFEntrySet readGFF(BufferedReader gffIn)
    throws ParserException, BioException, IOException
  {
    return GFFToolsLight.readGFF(gffIn, GFFRecordFilter.ACCEPT_ALL);
  }
  

  /**
   * Read all GFF entries matching a filter from a buffered reader.
   *
   * This will read up untill the end of the reader.
   *
   * @param gffIn  the BufferedReader to read text from
   * @return a GFFEntrySet containing all of the GFF that could be read
   * @throws parserException  if the text could not be parsed as GFF
   * @throws BioException if there was some error reading the GFF
   * @throws IOException if there was an error with the reader
   */
  public static GFFEntrySet readGFF(BufferedReader gffIn, GFFRecordFilter recFilt)
    throws ParserException, BioException, IOException
  {
        GFFEntrySet gffEntries = new GFFEntrySet();
        GFFFilterer filterer = new GFFFilterer(gffEntries.getAddHandler(),recFilt);
        GFFParser parser = new GFFParserLight();
        parser.parse(gffIn, filterer);
        return gffEntries;
  }

}
