/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.memcached.model;

import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;

/**
 *
 * @author victor
 */
public class MemcachedLiteral extends LiteralImpl implements MemcachedValue {
    
    String keyId;
    
    public MemcachedLiteral(String label) {
        super(label);
    }
    
    public MemcachedLiteral(String pkeyId, String label) {
        super(label);
        this.keyId = pkeyId;
    }
    
    public MemcachedLiteral(String pkeyId, String label, URI datatype) {
        super(label, datatype);
        this.keyId = pkeyId;
    }
    
    public MemcachedLiteral(String pkeyId, String label, String lang) {
        super(label, lang);
        this.keyId = pkeyId;
    }
    
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String id) {
        keyId = id;
    }
}
