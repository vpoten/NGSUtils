/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.memcached.model;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author victor
 */
public class MemcachedURI extends URIImpl implements MemcachedResource {

    String keyId;
    
    public MemcachedURI( URI uri ){
        super( uri.toString() );
    }
    
    public MemcachedURI( URI uri, String id ){
        super( uri.toString() );
        keyId = id;
    }
    
    public MemcachedURI(String uriString, String id){
        super(uriString);
        keyId = id;
    }

    public MemcachedURI(String uriString) {
        super(uriString);
    }
    
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String id) {
        keyId = id;
    }
    
}
