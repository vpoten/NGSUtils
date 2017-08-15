/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.loader

import org.ngsutils.Utils
import org.ngsutils.semantic.rdfutils.SimpleGraph
import org.openrdf.model.Resource

/**
 * Base class for semantic database loaders
 * 
 * @author victor
 */
abstract class BaseLoader {
	
    SimpleGraph graph
    Resource context
    List taxonomyIds
    
    
    abstract public boolean load(String workDir);
    
    
    /**
     *
     */
    String getFileName(String url) {
        return url.substring( url.lastIndexOf('/')+1 )
    }
    
    /**
     * downloads the urlSrc into file if not exists
     */
    boolean downloadFile(String urlSrc, File file) {
        if( !file.exists() ){
            if( !Utils.download(urlSrc, file.absolutePath) ){
                System.err.println("Cannot download ${urlSrc}")
                return false
            }
        }
        
        return true
    }
    
}

