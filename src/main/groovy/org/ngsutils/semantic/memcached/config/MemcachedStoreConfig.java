/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.memcached.config;

import org.openrdf.sail.config.SailImplConfigBase;

/**
 *
 * @author victor
 */
public class MemcachedStoreConfig extends SailImplConfigBase {
    
    public MemcachedStoreConfig() {
        super(MemcachedStoreFactory.SAIL_TYPE);
    }
}
