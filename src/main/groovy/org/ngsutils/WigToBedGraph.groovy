/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils

/**
 *
 * @author victor
 */
class WigToBedGraph {

    protected static final String VAR_STEP = 'variableStep'
    protected static final String FIX_STEP ='fixedStep'
    protected static final String CHROM ='chrom'
    protected static final String START = 'start'
    protected static final String STEP = 'step'
    protected static final String SPAN = 'span'


    boolean convert( String wigFile, String bedGraphFile ){

        def reader = Utils.createReader(wigFile)
        def writer = new BufferedWriter(new FileWriter(bedGraphFile))

        def params = null
        boolean fixedStep = true
        long cont = 0

        //write header
        writer.writeLine("track type=bedGraph")


        reader.eachLine{ line ->
            if( line.startsWith(VAR_STEP) || line.startsWith(FIX_STEP) ){
                fixedStep = line.startsWith(FIX_STEP)

                def tokens = line.split("\\s")
                params = [:]
                cont = 0

                tokens.each{ val ->
                    int pos = val.indexOf('=')
                    if( pos>0 ){
                        params.put( val.substring(0,pos), val.substring(pos+1) )
                    }
                }
            }
            else{
                Long start
                String value

                if(fixedStep){
                    value = line
                    start = (params[START] as Long)+(params[STEP] as Long)*cont
                }
                else{
                    def tokens = line.split("\\s")
                    start = tokens[0]
                    value = tokens[1]
                }

                Long end = start

                if(params[SPAN])
                    end = start + (params[SPAN] as Long)-1

                String record = "${params[CHROM]}\t${start}\t${end}\t${value}"
                writer.writeLine(record)
                cont++
            }
        }

        reader.close()
        writer.close()

        return true
    }

}

