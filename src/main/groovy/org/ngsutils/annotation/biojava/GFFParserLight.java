/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.biojava;

import java.util.List;
import org.biojava.bio.BioException;
import org.biojava.bio.program.gff.GFFDocumentHandler;
import org.biojava.bio.program.gff.GFFParser;
import org.biojava.bio.program.gff.GFFRecord;
import org.biojava.bio.program.gff.GFFTools;
import org.biojava.bio.program.gff.IgnoreRecordException;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.utils.ParserException;

/**
 * Light version of biojava GFFParser, uses SimpleGFFRecordLight
 * 
 * @author victor
 */
public class GFFParserLight extends GFFParser {


    
  /**
   * Actually turns a list of tokens, some value string and a comment into a
   * <span class="type">GFFRecord</span> and informs
   * <span class="arg">handler</span>.
   *
   * @param handler a <span class="type">GFFDocumentHandler</span> to inform of
   *                any parse errors, and the completed <span class="type">GFFRecord</span>
   * @param aList   a <span class="type">List</span> containing the 8 mandatory GFF columns
   * @param rest    a <span class="type">String</span> representing the unparsed
   *                attribute-value text, or <span class="kw">null</span> if there is none
   * @param comment a <span class="type">String</span> containing the comment (without the
   *                leading '<code>#</code>' character.
   * @throws <span class="type">BioException</span> if <span class="arg">handler</span>
   *         could not correct a parse error
   */
    @Override
    protected GFFRecord createRecord(GFFDocumentHandler handler,
				     List aList,
				     String rest,
				     String comment)
	throws BioException, ParserException, IgnoreRecordException
    {
	SimpleGFFRecordLight record = new SimpleGFFRecordLight();

	record.setSeqName((String) aList.get(0));
	record.setSource((String) aList.get(1));
	record.setFeature((String) aList.get(2));

	int start = -1;
	try {
	    start = Integer.parseInt( (String) aList.get(3));
	} catch (NumberFormatException nfe) {
	    start = getErrorHandler().invalidStart((String) aList.get(3));
	}
	record.setStart(start);

	int end = -1;
	try {
	    end = Integer.parseInt( (String) aList.get(4));
	} catch (NumberFormatException nfe) {
	    end = getErrorHandler().invalidEnd((String) aList.get(4));
	}
	record.setEnd(end);

	String score = (String) aList.get(5);
	if(score == null     ||
	   score.equals("")  ||
	   score.equals(".") ||
	   score.equals("0")
	   )
	{
	    record.setScore(GFFTools.NO_SCORE);
	} else {
	    double sc = 0.0;
	    try {
		sc = Double.parseDouble(score);
	    } catch (NumberFormatException nfe) {
		sc = getErrorHandler().invalidScore(score);
	    }
	    record.setScore(sc);
	}

	String strand = (String) aList.get(6);
	if(strand == null || strand.equals("") || strand.equals(".")) {
	    record.setStrand(StrandedFeature.UNKNOWN);
	} else {
	    if(strand.equals("+")) {
		record.setStrand(StrandedFeature.POSITIVE);
	    } else if(strand.equals("-")) {
		record.setStrand(StrandedFeature.NEGATIVE);
	    } else {
		//record.setStrand(getErrorHandler().invalidStrand(strand));
                record.setStrand(StrandedFeature.UNKNOWN);
	    }
	}

	String frame = (String) aList.get(7);
	if(frame.equals(".")) {
	    record.setFrame(GFFTools.NO_FRAME);
	} else {
	    int fr = 0;
	    try {
		fr = Integer.parseInt(frame);
	    } catch (NumberFormatException nfe) {
		fr = getErrorHandler().invalidFrame(frame);
	    }
	    record.setFrame(fr);
	}

	if (rest != null)
	    record.setGroupAttributes(rest);
        
	record.setComment(comment);

	return record;
    }


    
}
