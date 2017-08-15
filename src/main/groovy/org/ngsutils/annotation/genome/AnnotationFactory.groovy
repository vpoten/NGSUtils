/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.genome

import org.ngsutils.Utils
import org.ngsutils.annotation.genome.AnnotationTrack as AnT
import org.ngsutils.annotation.genome.regulation.TFBMotif
import org.htmlcleaner.*

/**
 *
 * @author victor
 */
class AnnotationFactory {
    
    static final def UCSC_DB_URL = 'http://hgdownload.cse.ucsc.edu/goldenPath/{assembly}/database/'
    static final def UCSC_DB_HG19 = UCSC_DB_URL.replace('{assembly}', 'hg19')
    
    protected static _BroadCellLines = [ ['Gm12878'], ['H1hesc'], ['K562'], ['A549'],
        ['Cd20','Cd20ro01794'], ['Helas3'], ['Hepg2'], ['Huvec'],
        ['Monocd14ro1746','Monocytescd14ro01746'], ['Dnd41'], ['Hmec'], 
        ['Hsmm'], ['Hsmmt','Hsmmtube'], ['Nha'], ['Nhdfad'], ['Nhek'],
        ['Nhlf'], ['Osteobl', 'Osteo'] ]
    
    //possible db file names for each type of histone
    static histoneDbFilesRegex  = [ //regex + cell_line group + version
        'H3K4Me1':[/wgEncodeBroadHistone(\w+)H3k0?4me1(Std)?Pk(V\d)?\.txt\.gz/, 1, 3],
        'H3K27Ac':[/wgEncodeBroadHistone(\w+)H3k27ac(Std)?Pk(V\d)?\.txt\.gz/, 1, 3],
        'H3K4Me3':[/wgEncodeBroadHistone(\w+)H3k0?4me3(Std)?Pk(V\d)?\.txt\.gz/, 1, 3],
        'DNase':[/wgEncodeAwgDnase(Uw)?([Dd]uke)?(\w+)UniPk(V\d)?\.txt\.gz/, 3, 4]
        ] as TreeMap
        
    static uniformTfDbFileRegex = /wgEncodeAwgTfbs(\w+)UniPk\.txt\.gz/
    static uniformTfTeams = ['Sydh','Broad','Haib','Uw','Uta','Uchicago']
    
    static regulationTracks = histoneDbFilesRegex.keySet()
    static cellLines = _BroadCellLines.collect{ it[0] }
    
    // available tracks
    
    static def TRK_UCSC_H3K4Me1 = new AnnotationTrack( name:'H3K4Me1',
        url:UCSC_DB_HG19+'wgEncodeBroadHistoneGm12878H3k4me1StdPk.txt.gz',
        source:'UCSC',
        overrideName:'H3K4Me1',
        featureType:AnT.SOFA_TFBS,
        organismId:'9606' )
    
    static def TRK_UCSC_H3K27Ac = new AnnotationTrack( name:'H3K27Ac',
        url:UCSC_DB_HG19+'wgEncodeBroadHistoneGm12878H3k27acStdPk.txt.gz',
        source:'UCSC',
        overrideName:'H3K27Ac',
        featureType:AnT.SOFA_TFBS,
        organismId:'9606' )
    
    static def TRK_UCSC_H3K4Me3 = new AnnotationTrack( name:'H3K4Me3',
        url:UCSC_DB_HG19+'wgEncodeBroadHistoneGm12878H3k4me3StdPk.txt.gz',
        source:'UCSC',
        overrideName:'H3K4Me3',
        featureType:AnT.SOFA_TFBS,
        organismId:'9606' )
    
    static def TRK_UCSC_DNase = new AnnotationTrack( name:'DNase',
        url:UCSC_DB_HG19+'wgEncodeRegDnaseClusteredV2.txt.gz',
        source:'UCSC',
        overrideName:'DNase',
        featureType:AnT.SOFA_TFBS,
        organismId:'9606' )
    
    static def TRK_UCSC_TxnFac = new AnnotationTrack( name:'Txn Fac ChIP',
        url:UCSC_DB_HG19+'wgEncodeRegTfbsClusteredV3.txt.gz',
        source:'UCSC',
        overrideName:null,
        featureType:AnT.SOFA_TFBS,
        organismId:'9606' )
    
    static def TRK_UCSC_SNP138 = new AnnotationTrack( name:'Common SNPs (138)',
        url:UCSC_DB_HG19+'snp138Common.txt.gz',
        source:'UCSC',
        overrideName:null,
        featureType:AnT.SOFA_SNP,
        organismId:'9606' )
    
    static def TRK_UCSC_MotifPos = new AnnotationTrack( name:'factorbookMotifPos',
        url:UCSC_DB_HG19+TFBMotif.MotifPos,
        source:'UCSC',
        overrideName:null,
        featureType:AnT.SOFA_TFBMotif,
        organismId:'9606' )
    
    // trackList catalog
    static List ANN_UCSC_SET1 = [TRK_UCSC_H3K4Me1, TRK_UCSC_H3K27Ac, TRK_UCSC_H3K4Me3,
        TRK_UCSC_DNase, TRK_UCSC_TxnFac, TRK_UCSC_SNP138]
    
    static List ANN_UCSC_REGUL = [TRK_UCSC_H3K4Me1, TRK_UCSC_H3K27Ac, TRK_UCSC_H3K4Me3,
        TRK_UCSC_DNase, TRK_UCSC_TxnFac]
    
    static List ANN_UCSC_SNPS138 = [TRK_UCSC_SNP138]
    
    static List ANN_UCSC_H3K4Me1 = [TRK_UCSC_H3K4Me1]
    
    static List ANN_UCSC_H3K4 = [TRK_UCSC_H3K4Me1, TRK_UCSC_H3K4Me3]
    
    static List ANN_UCSC_HG19_TF = [TRK_UCSC_TxnFac]
    static List ANN_UCSC_HG19_Motif = [TRK_UCSC_MotifPos]
	
    /**
     *
     */
    static FeatureIndex createIndex(String workDir, String taxId, List trackList, Set included = null) {
        return new FeatureIndex(workDir, taxId, trackList, included)
    }
    
    /**
     * @return a map with key=cell_line and value=list of tracks
     */
    static Map broadHistoneAndUniDNaseTracks(assembly='hg19') {
        def files = parseUCSCDbFolderLinks('.txt.gz', assembly)
        String urlBase = UCSC_DB_URL.replace('{assembly}', assembly).replace('http://','ftp://')
        def map = [:]
        
        _BroadCellLines.each{ cellNames->
            def name = cellNames[0]
            def list = []
            
            histoneDbFilesRegex.each{ histName, regex->
                def matchFiles = files.findAll{ it==~regex[0] }.findAll{ 
                        def mat = (it=~regex[0])
                        mat ? (mat[0][regex[1]] in cellNames) : false
                    }
                    
                if( matchFiles ){
                    def file = matchFiles.max{ //get the newest version
                        def mat = (it=~regex[0])
                        (mat[0][regex[2]]==null) ? 0 : (mat[0][regex[2]][1]) as Integer
                    }
                    
                    list << new AnnotationTrack( name:histName, url:urlBase+file,
                            source:'UCSC', overrideName:histName, cellLine:name,
                            featureType:AnT.SOFA_TFBS, organismId:'9606' )
                }
            }
            
            // add uniform TF tracks of current cell line
            def tfFiles = files.findAll{ it==~uniformTfDbFileRegex }.findAll{ 
                        def trkInfo = (it=~uniformTfDbFileRegex)[0][1]
                        cellNames.any{ trkInfo.contains(it) }
                    }
            
            tfFiles.each{ file->
                // extract TF name
                def trkInfo = (file=~uniformTfDbFileRegex)[0][1]
                def actGrp = uniformTfTeams.find{ trkInfo.contains(it) }
                def actCell = cellNames.find{ trkInfo.contains(it) }
                
                def tfName = trkInfo.substring(actGrp.length()+actCell.length())
                
                list << new AnnotationTrack( name:AnT.UNI_TF_PREFIX+tfName, url:urlBase+file,
                            source:'UCSC', overrideName:tfName, cellLine:name,
                            featureType:AnT.SOFA_TFBS, organismId:'9606' )
            }
            
            map[name] = list
        }
        
        return map
    }
    
    /**
     *
     */
    static List parseUCSCDbFolderLinks(String suffix, assembly='hg19') {
        def url = UCSC_DB_URL.replace('{assembly}', assembly)
        
        // Clean any messy HTML
        CleanerProperties props = new CleanerProperties();
        props.setOmitComments(true);
        
        // do parsing
        def cleaner = new HtmlCleaner(props)
        TagNode node = cleaner.clean( url.toURL() )

        def list = node.getElementsByName('a',true) as List
        list = list.findAll{ it.getAttributeByName('href').endsWith(suffix) }.collect{ it.getAttributeByName('href') }
        
        return list
    }
    
}

