/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.query

import org.ngsutils.semantic.rdfutils.SimpleGraph

/**
 *
 * @author victor
 */
class BaseQueryUtils {
	
    SimpleGraph graph = null
    
    /**
     *
     */
    public BaseQueryUtils(SimpleGraph p_graph) {
        graph = p_graph
    }
    
    /**
     *
     */
    protected String stringValue(obj){
        return (obj instanceof String) ? obj : obj.stringValue()
    }
    
    /**
     *
     */
    protected String localName(String str){
        return str.substring( str.lastIndexOf('/')+1 );
    }
}

