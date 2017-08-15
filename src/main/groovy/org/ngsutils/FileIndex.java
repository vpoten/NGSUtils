/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.Set;

/**
 * class that indexes file lines using a field value
 *
 * @author victor
 */
public class FileIndex {

    private TreeMap<String,List<String>> tableIndex=new TreeMap<String,List<String>>();

    /**
     *
     * @param f
     * @param keyField : key field index (zero based)
     * @param valField : value field index (zero based)
     * @param fieldSep : regex for fields separator (i.e. "\t")
     * @param comment : comment start character (skip comments)
     * @throws FileNotFoundException
     * @throws IOException
     */
    public FileIndex( File f, int keyField, int valField, String fieldSep, String comment )
            throws FileNotFoundException, IOException {

        BufferedReader r =  Utils.createReader(f);

        String l_line="";

        while( (l_line=r.readLine())!=null ){

            if( comment!=null && l_line.startsWith(comment) )
                continue;//skip comments

            String [] tokens=l_line.split(fieldSep,-1);

            if( tableIndex.containsKey(tokens[keyField]) ){
                tableIndex.get(tokens[keyField]).add( tokens[valField] );
            }
            else{
                List list=new ArrayList<String>();
                list.add( tokens[valField] );
                tableIndex.put( tokens[keyField], list);
            }
            
        }

        r.close();
    }

    /**
     * 
     * @param key
     * @return
     */
    public List<String> getValue( String key ){
        return tableIndex.get(key);
    }

    /**
     * 
     * @return
     */
    public Set<String> getKeys(){
        return this.tableIndex.keySet();
    }

    
}
