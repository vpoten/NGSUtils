/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.genome.regulation

import org.ngsutils.Utils
import org.ngsutils.annotation.genome.AnnotationFactory as AnF
import org.ngsutils.annotation.genome.FeatureIndex

    
/**
 *
 * @author victor
 */
class TFBMotif {
	
    String name
    int colCount
    def probs = [:]
    
    //TFBMotif database files
    static final String MotifCanonical = 'factorbookMotifCanonical.txt.gz'
    static final String MotifPwm = 'factorbookMotifPwm.txt.gz'
    static final String MotifPos = 'factorbookMotifPos.txt.gz'
    
    /**
     * parses a MotifPwm file line
     */
    protected static TFBMotif createFromLine(String line) {
        def toks = line.split("\t")
        def motif = new TFBMotif(name:toks[0], colCount:toks[1] as Integer)
        
        motif.probs['A'] = (toks[2].split(",") as List).collect{it as Double}
        motif.probs['C'] = (toks[3].split(",") as List).collect{it as Double}
        motif.probs['G'] = (toks[4].split(",") as List).collect{it as Double}
        motif.probs['T'] = (toks[5].split(",") as List).collect{it as Double}
        
        return motif
    }
    
    /**
     * load a map of TFBMotif from UCSC
     */
    static load(workDir, assembly, taxonomyId) {
        def name = MotifPwm
        def file = new File("${workDir}${taxonomyId}/${name}")
        def url = AnF.UCSC_DB_URL.replace('{assembly}', assembly)+name

        if( !FeatureIndex.downloadFile(url, file) ){
            return null
        }
        
        def reader = Utils.createReader(file)
        def map = [:] as TreeMap
        
        reader.eachLine{ line-> 
            def motif = createFromLine(line) 
            map[motif.name] = motif
        }
        
        reader.close()
        
        return map
    }
    
    /**
     *
     */
    double getMaxProb(int pos) {
        Math.max( Math.max(probs['A'][pos],probs['C'][pos]), Math.max(probs['G'][pos],probs['T'][pos]) )
    }
    
    /**
     *
     */
    double getMinProb(int pos) {
        Math.min( Math.min(probs['A'][pos],probs['C'][pos]), Math.min(probs['G'][pos],probs['T'][pos]) )
    }
    
}

