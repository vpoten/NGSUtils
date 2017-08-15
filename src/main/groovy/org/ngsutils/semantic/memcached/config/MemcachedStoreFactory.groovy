/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.memcached.config

import org.openrdf.sail.config.SailFactory
import org.openrdf.sail.config.SailImplConfig
import org.openrdf.sail.config.SailConfigException
import org.openrdf.sail.Sail
import org.ngsutils.semantic.memcached.MemcachedStore

/**
 *
 * @author victor
 */
class MemcachedStoreFactory implements SailFactory {
    
    /**
     * The type of repositories that are created by this factory.
     * 
     * @see SailFactory#getSailType()
     */
    public static final String SAIL_TYPE = "openrdf:MemcachedStore";

    /**
     * Returns the Sail's type: <tt>openrdf:MemoryStore</tt>.
     */
    public String getSailType() {
        return SAIL_TYPE;
    }

    public SailImplConfig getConfig() {
        return new MemcachedStoreConfig();
    }

    public Sail getSail(SailImplConfig config)
            throws SailConfigException
    {
        if (!SAIL_TYPE.equals(config.getType())) {
                throw new SailConfigException("Invalid Sail type: " + config.getType());
        }

        MemcachedStore memoryStore = new MemcachedStore();

        if (config instanceof MemcachedStoreConfig) {
                MemcachedStoreConfig memConfig = (MemcachedStoreConfig)config;
                // maybe will be completed
        }

        return memoryStore;
    }
}

