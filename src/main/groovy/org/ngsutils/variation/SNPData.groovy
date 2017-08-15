/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.variation

/**
 *
 * @author victor
 */
class SNPData implements Comparable<SNPData> {
	
    String id //snp id
    String chr
    String alleles // XY
    int position
    def reference //source of this SNPData (a .ped,.tped,.bgl,... File object)
    String genotypes //tped line, subjects genotypes only
    String eqtl //associated eqtl isoform
    Double eqtlScore = 0.0 //eqtl correlation value
    
    String minor = null //minor allele
    String major = null //major allele
    Double maf = null //minor allele frequency
    
    //frequency calc
    Double freqA1 = null
    Double freqA2 = null
    
    // constants and closures
    static def comparator = [compare:{a,b-> a.position<=>b.position}] as Comparator
    static MISS_CODES = ['N','0','?']
    
    /**
     *
     */
    public int compareTo(SNPData other){
        return this.id<=>other.id
    }
    
    /**
     *
     */
    String getChrNum(){
        (chr.startsWith('chr')) ? chr.substring(3) : chr
    }
    
    /**
     *
     */
    String getLocus(){
        return "chr${getChrNum()}:${position}"
    }
    
    /**
     *
     */
    String getTpedLine(){
        "${getChrNum()}\t${id}\t0\t${position}\t${genotypes}"
    }
   
    /**
     *
     */
    Double encode( String p_allele ){
        if( p_allele.contains('N') || p_allele.contains('0') || alleles.length()<2 )
            return null
        //(0..1).inject(0.0){val, i -> val += (p_allele[i]==alleles[2]) ? 1.0 : 0.0 }
        double val = 0
            
        if( p_allele[0]==alleles[1] ){val += 1}
        if( p_allele[1]==alleles[1] ){val += 1}
        
        return val
    }
    
    /**
     *
     */
    Double mutateEncode( String p_allele, double typingError, double missError){
        String modAllele = ''
        
        (0..1).each{
            double rand = Math.random()
            
            if( rand<typingError ){
                modAllele += (p_allele[it]==alleles[1]) ? alleles[0] : alleles[1]
            }
            else if( rand<missError ){
                modAllele += 'N'
            }
            else{
                modAllele += p_allele[it]
            }
        }
        
        return encode(modAllele)
    }
    
    /**
     *
     */
    String decode(int val){
        switch(val){
            case 0:
                return alleles[0]+alleles[0]
            case 1:
                return alleles[0]+alleles[1]
            case 2:
                return alleles[1]+alleles[1]
            default:
                return 'NN'
        }
    }
    
    /**
     *
     */
    String getAlleles() {
        return alleles
    }
    
    /**
     *
     */
    Double getFreqA1() {
        return freqA1
    }
    
    /**
     *
     */
    Double getFreqA2() {
        return freqA2
    }
    
    /**
     *
     */
    synchronized void setGenotypes(String p_geno) {
        genotypes = p_geno
        alleles = buildAlleles()
        calcFreqs()
    }
    
    /**
     * calculates alleles frequencies
     */ 
    synchronized private void calcFreqs() {
        freqA1 = 0
        int lim = genotypes.length()
        
        for(int i=0; i<lim; i+=2) { 
            if( genotypes[i]==alleles[0] ){ freqA1 += 1.0d }
        }
        
        freqA1 /= (lim+1)*0.5d
        freqA2 = 1.0d - freqA1
        
        minor = alleles[0] //minor allele
        major = alleles[1]
        maf = freqA1 //minor allele frequency
    
        if( freqA1>freqA2 ){
            minor = alleles[1]
            major = alleles[0]
            maf = freqA2
        }
    }
    
    /**
     *
     */
    String getSubjectAlleles(int sbjIdx) {
        sbjIdx = sbjIdx*4
        return "${genotypes[sbjIdx]}${genotypes[sbjIdx+2]}"
    }
    
    /**
     * build alleles String from genotypes
     */
    synchronized private String buildAlleles() {
        def set = [] as Set
        
        for(int i=0; i<genotypes.length(); i+=2) { 
            if( genotypes[i] in ['A','T','G','C'] )
                set << genotypes[i]

            if(set.size()==2)
                break
        }
        
        return set.sort().sum()
    }
    
    /**
     * static Helper method
     */
    public static int countMissing(snpDataMap) {
        int count = 0
        
        snpDataMap.each{ key, data->
            for(int i=0; i<data.genotypes.length(); i+=2) { 
                if( data.genotypes[i] in MISS_CODES ){
                    count++
                }
            }
        }
        
        return count
    }
    
    /**
     * static Helper method. Reads a subject list from .tfam file
     */
    public static def subjectList(Map snpDataMap) {
        def file = snpDataMap.firstEntry()?.value.reference
        return subjectList(file)
    }
    
    /**
     * static Helper method. Reads a subject list from .tfam file
     */
    public static def subjectList(File file) {
        if( !file || !file.name.endsWith('.tped') ){
            return null
        }
        
        def list = []
        
        // read subject id from .tfam file
        def tfamReader = new File( file.absolutePath.replace('.tped','.tfam') ).newReader()
        // fields: FamilyID IndividualID PaternalID MaternalID Sex Phenotype
        tfamReader.splitEachLine("\\s"){ toks-> list << "${toks[0]}:${toks[1]}" }
        tfamReader.close()
        
        return list
    }
    
    /**
     * static Helper method
     */
    public static Map<String,SNPData> createFromTped(file, snpSet, snpDataMap = [:] as TreeMap) {
        def reader = new File(file).newReader()
        
        reader.eachLine{ line->
            def toks = line.split("\\s",5)
            def id = toks[1]
            if( (id in snpSet) && !snpDataMap.containsKey(id) ) {
                // add snp to list
                def data = new SNPData(id:id, chr:toks[0], 
                        position:(toks[3] as Integer), reference:file,
                        genotypes:toks[4])

                snpDataMap[id] = data
            }
        }
        
        reader.close()
        return snpDataMap
    }
    
}

