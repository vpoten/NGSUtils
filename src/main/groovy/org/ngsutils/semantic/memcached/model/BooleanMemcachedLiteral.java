/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.memcached.model;

import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author victor
 */
public class BooleanMemcachedLiteral extends MemcachedLiteral {
    
    private final boolean b;

    /*--------------*
     * Constructors *
     *--------------*/

    public BooleanMemcachedLiteral(String keyId, boolean b) {
            this(keyId, Boolean.toString(b), b);
    }

    public BooleanMemcachedLiteral(String keyId, String label, boolean b) {
            super(keyId, label, XMLSchema.BOOLEAN);
            this.b = b;
    }

    /*---------*
     * Methods *
     *---------*/

    @Override
    public boolean booleanValue() {
            return b;
    }
}
