/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.memcached.model;

import org.openrdf.model.Value;

/**
 *
 * @author victor
 */
public interface MemcachedValue extends Value {
    
    public String getKeyId();
    public void setKeyId(String id);
}
