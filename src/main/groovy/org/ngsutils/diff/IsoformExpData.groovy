/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.diff

import org.ngsutils.annotation.Isoform
import org.ngsutils.Utils

/**
 * Isoform expression data.
 * 
 * @author victor
 */
class IsoformExpData {
    String id
    String mergedId
    String geneId
    String geneShortName
    int length
    String chr
    int start
    int end
    float coverage
    float fpkm
    Isoform isoform
    
    static final int LOCUS_IDX = 6
    static final int STATUS_IDX = 12
    
    static final String ISO_TRACK_FILE = 'isoforms.fpkm_tracking.gz'
    
    
    /**
     * compare IsoformExpData by locus
     */
    static def locusComparator = [
        compare:{a,b->
            int res = a.chr<=>b.chr
            
            if( res==0 ){
                res = a.start<=>b.start
                if( res==0 )
                    res = a.end<=>b.end
            }
            
            return res
        }
    ] as Comparator
    
    
    /**
     * parses a line of a cufflinks tracking file and returns a new IsoformData object
     */
    static IsoformExpData parse(toks){
        def locus = (toks[LOCUS_IDX] =~ Utils.locusRegex)
        
        return new IsoformExpData( id:toks[0], geneId:toks[3], geneShortName:toks[4],
            length:toks[7] as Integer, chr:locus[0][1], 
            start:locus[0][2] as Integer, end:locus[0][3] as Integer, 
            coverage:toks[8] as Float, fpkm:toks[9] as Float )
    }
    
    
    /**
     * returns true if locStr represents a locus inside the region delimited by
     * chr, start and end
     */
    static boolean isInRegion(String chr, int start, int end, String locStr){
        def locus = (locStr =~ Utils.locusRegex)
        
        if( locus[0][1] != chr )
            return false
            
        if( (locus[0][2] as Integer)>end || (locus[0][3] as Integer)<start )
            return false
            
        return true
    }
    
    /**
     *
     */
    String getChrNum(){
        (chr.startsWith('chr')) ? chr.substring(3) : chr
    }
    
}

