/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.genome

import org.biojava.nbio.genome.parsers.gff.Location

/**
 *
 * @author victor
 */
class Feature {
    String seqname
    String source
    String name
    String type
    Double score
    int frame
    Location location
    def terms
    
    public Feature(String seqname, String source, String type, int start, int end, Double score, int frame){
        this.seqname = seqname
        this.source = source
        this.type = type
        this.location = new Location(start,end)
        this.score = score
        this.frame = frame
    }
}

