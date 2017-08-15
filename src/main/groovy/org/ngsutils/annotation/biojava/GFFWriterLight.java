/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.biojava;

import java.io.PrintWriter;
import java.util.Map;
import org.biojava.bio.program.gff.GFFRecord;
import org.biojava.bio.program.gff.GFFTools;
import org.biojava.bio.program.gff.GFFWriter;
import org.biojava.bio.program.gff.SimpleGFFRecord;
import org.biojava.bio.seq.StrandedFeature;

/**
 * GFFWriter that is able to deal with SimpleGFFRecordLight
 * 
 * @author victor
 */
public class GFFWriterLight extends GFFWriter {

    protected PrintWriter out2;

    public GFFWriterLight(PrintWriter out) {
        super(out);
        out2=out;
    }


    /**
     * Prints <span class="arg">record</span> to the <span class="type">PrintWriter</span>.
     * 
     * @param record
     */
    @Override
    public void recordLine(GFFRecord record) {
        out2.print(
          record.getSeqName() + "\t" +
          record.getSource()  + "\t" +
          record.getFeature() + "\t" +
          record.getStart()   + "\t" +
          record.getEnd()     + "\t"
        );

        double score = record.getScore();
        if(score == GFFTools.NO_SCORE) {
          out2.print(".\t");
        } else {
          out2.print(score + "\t");
        }

        StrandedFeature.Strand strand = record.getStrand();
        if(strand == StrandedFeature.POSITIVE) {
          out2.print("+\t");
        } else if(strand == StrandedFeature.NEGATIVE) {
          out2.print("-\t");
        } else {
          out2.print(".\t");
        }

        int frame = record.getFrame();
        if(frame == GFFTools.NO_FRAME) {
          out2.print(".");
        } else {
          out2.print(frame + "");
        }

        Map gaMap = record.getGroupAttributes();
        String ga;
        
        if( record instanceof SimpleGFFRecordLight )
            ga = SimpleGFFRecordLight.stringifyAttributes(gaMap);
        else
            ga = SimpleGFFRecord.stringifyAttributes(gaMap);

        if(ga != null && ga.length() > 0) {
          out2.print("\t" + ga);
        }

        String comment = record.getComment();
        if(comment != null && comment.length() > 0) {
          if(ga != null && ga.length() > 0) {
            out2.print(" ");
          }
          out2.print(comment);
        }

        out2.println("");
    }


}
