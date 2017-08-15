/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.memcached.model;

import org.openrdf.model.BNode;
import org.openrdf.model.impl.BNodeImpl;

/**
 *
 * @author victor
 */
public class MemcachedBNode extends BNodeImpl implements MemcachedResource {

    String keyId;
    
    public MemcachedBNode(String id, String pkeyId){
        super(id);
        keyId = pkeyId;
    }

    public MemcachedBNode(BNode bnode) {
        super(bnode.getID());
    }
    
    public MemcachedBNode(BNode bnode, String pkeyId) {
        super(bnode.getID());
        keyId = pkeyId;
    }
    
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String id) {
        keyId = id;
    }
    
}
