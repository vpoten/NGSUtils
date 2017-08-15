/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.genome

import org.ngsutils.Utils
import org.biojava.nbio.genome.parsers.gff.Location
import org.ngsutils.annotation.genome.AnnotationTrack as AnT
import org.ngsutils.annotation.genome.regulation.TFBMotif

/**
 *
 * @author victor
 */
class FeatureIndex {
	
    String taxonomyId
    def chromosomes = [:] as TreeMap
    def names = [:] as TreeMap
    def included = null
    
    static def featLocusComp = [
        compare:{ a,b-> 
            int res = a.location.start()<=>b.location.start()
            
            if( res==0 ){
                res = a.location.end()<=>b.location.end()
            }
            
            return res
        }
    ] as Comparator
    
    
    
    /**
     *
     */
    public FeatureIndex(String workDir, String taxId, List trackList, Set included) {
        this.taxonomyId = taxId
        this.included = included
        load(workDir, trackList)//load data from external databases
        this.chromosomes.each{chr, list-> Collections.sort(list, featLocusComp) }//sort features
    }
    
    /**
     *
     */
    protected String getFileName(String url) {
        return url.substring( url.lastIndexOf('/')+1 )
    }
    
    /**
     * downloads the urlSrc into file if not exists
     */
    static boolean downloadFile(String urlSrc, File file) {
        if( !file.exists() ){
            if( !Utils.download(urlSrc, file.absolutePath) ){
                System.err.println("Cannot download ${urlSrc}")
                return false
            }
        }
        
        return true
    }
    
    /**
     *
     */
    protected boolean load(String workDir, trackList){
        // prepare work dir
        if( !Utils.createDir("${workDir}${taxonomyId}") ) {
            return false
        }
        
        trackList.each{ trk->
            def name = getFileName(trk.url)
            println "Loading ${trk.name} annotation for organism [${taxonomyId},${trk.cellLine?:''}]"
            
            def file = new File("${workDir}${taxonomyId}/${name}")

            if( !downloadFile(trk.url, file) ){
                return false
            }
            
            if( trk.source=='UCSC' && trk.featureType==AnT.SOFA_TFBS ){
                parseUcscDBRegulationFile(file, trk.source, trk.featureType, trk.overrideName)
            }
            else if( trk.source=='UCSC' && trk.featureType==AnT.SOFA_SNP ){
                parseUcscDBSnpFile(file, trk.source, trk.featureType)
            }
            else if( trk.source=='UCSC' && trk.featureType==AnT.SOFA_TFBMotif  ){
                parseUcscDBRegulationFile(file, trk.source, trk.featureType, trk.overrideName)
            }
        }
        
        return true
    }
    
    /**
     *
     */
    protected def addFeature(Feature feat) {
        String chr = feat.seqname
        String name = feat.name
        
        if( name ){
            def list = names[name]
            // add it to name index
            if( list==null ){
                list = []
                names[name] = list
            }

            list << feat
        }
        
        //add to chromosomes index
        def list = chromosomes[chr]
        
        if( list==null ){
            list = []
            chromosomes[chr] = list
        }
        
        list << feat
    }
    
    /**
     * get features that contains the given locus
     */
    List getFeatsByPos(String locus, int backward=0) {
        def loc = (locus =~ Utils.locusRegex)[0]
        Feature key = new Feature(loc[1], null, null, loc[2] as Integer, loc[3] as Integer, 0.0, -1)
        def list = chromosomes[key.seqname]
      
         //returns the index of the search key, if it is contained in the list; otherwise, (-(insertion point) - 1)
        int idx = Collections.binarySearch(list, key, featLocusComp)
      
        if( idx<0 ){
            idx = -(idx+1)
        }
        
        def results = []
        int start = key.location.start()
        int end = key.location.end()
        
        int i = idx-1
        if( backward==0 ) {
            while( i>=0 && list[i].location.end()>=end ){
                results << list[i--]
            }
        }
        else {
            int limit = start-backward
            while( i>=0 && list[i].location.start()>limit ){
                if( list[i].location.end()>=end ) { results << list[i] }
                i--
            }
        }
        
        i = idx
        while( i<list.size() && list[i].location.start()==start ){
            results << list[i++]
        }
        
        return results
    }
    
    /**
     *
     */
    List getFeatsByName(name) {
        return names[name]
    }
    
    /**
     *
     */
    protected def parseUcscDBRegulationFile(File file, String src, String featType, String overrName) {
        def reader = Utils.createReader(file)
        
        //fields: bin chrom chromStart chromEnd name score
        reader.splitEachLine("\t"){ toks->
            String seqname = toks[1]
            Double score = toks[5] as Double
            def name = (overrName ?: toks[4])
            
            if( included==null || (name in included) ) {
                def feat = new Feature(seqname, src, featType, toks[2] as Integer, toks[3] as Integer, score, -1)
                feat.name = name
                addFeature(feat)
            }
        }
        
        reader.close()
    }
    
    /**
     *
     */
    protected def parseUcscDBSnpFile(File file, String src, String featType) {
        def reader = Utils.createReader(file)
        
        //fields: bin chrom chromStart chromEnd name score ... func(15)
        reader.splitEachLine("\t"){ toks->
            String seqname = toks[1]
            Double score = toks[5] as Double
            def name = toks[4]
            
            if( included==null || (name in included) ) {
                def feat = new Feature(seqname, src, featType, toks[2] as Integer, toks[3] as Integer, score, -1)
                feat.name = name
                feat.terms = (toks[15].split(',')) as TreeSet
                addFeature(feat)
            }
        }
        
        reader.close()
    }
    
}
