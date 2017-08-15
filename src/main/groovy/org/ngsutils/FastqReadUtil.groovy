/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils

/**
 *
 * @author victor
 */
class FastqReadUtil {
    
    /**
     * cleans unpaired reads presents in paired end fastq files
     * 
     */
    static def cleanUnpairedReads(String file1, String file2){ 
        def fastqIndex  = [[],[]]
        
        //closure that reads a single fastq record (4 lines) and return its hash key
        def readFastq = { reader ->
            String line = reader.readLine()
            
            if( line==null )
                return null
            
            line = line.substring(0, line.lastIndexOf('/'))
            
            reader.readLine()
            reader.readLine()
            reader.readLine()
            
            return Utils.hash(line)
        }
       
        // build key indexes
        def doCreateIndex = { file, i, isIndex->
            def reader = Utils.createReader(file)
            Long hash
            
            if( isIndex ){ //if is index creates a TreeSet
                fastqIndex[i] = new TreeSet<Integer>()
            }
            
            while( (hash = readFastq(reader))!=null ){
                fastqIndex[i] << hash
            }
            
            reader.close()
        }
        
        boolean isMinFile1 = true
        if( (new File(file1)).size() > (new File(file2)).size() ){
            isMinFile1 = false
        }
        
        Utils.runClosures( [{doCreateIndex(file1,0,isMinFile1)},
                {doCreateIndex(file2,1,!isMinFile1)}], 2)
        
        def list = fastqIndex.find{ it instanceof List }
        def set = fastqIndex.find{ it instanceof Set }
        def finalIndex = new TreeSet<Long>()
        
        //removes keys which are not present in the other file from index
        list.each{ key->
            if( set.contains(key) ){
                finalIndex << key
            }
        }
        
        list.clear()
        set.clear()
        fastqIndex = null
        System.gc()
        
        //write fastq records presents in index to .out file
        def doWritePairedFastq = { file->
            def reader = Utils.createReader(file)
            def writer = new FileWriter(file+'.out')
            String line
            
            while( (line=reader.readLine())!=null ){
                String keyLine = line.substring(0, line.lastIndexOf('/'))
                
                if( finalIndex.contains(Utils.hash(keyLine)) ){
                    writer.write( line ); writer.write('\n')
                    writer.write( reader.readLine() ); writer.write('\n')
                    writer.write( reader.readLine() ); writer.write('\n')
                    writer.write( reader.readLine() ); writer.write('\n')
                }
                else{
                    reader.readLine()
                    reader.readLine()
                    reader.readLine()
                }
            }
            
            reader.close()
            writer.close()
            "mv ${file}.out ${file}".execute().waitFor()
        }
        
        Utils.runClosures([{doWritePairedFastq(file1)}, {doWritePairedFastq(file2)}], 2)
        finalIndex.clear()
        finalIndex = null
    }
    
    
}

