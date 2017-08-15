/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.memcached.model;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author victor
 */
public class NumericMemcachedLiteral extends MemcachedLiteral {
    
    private final Number number;

    /*--------------*
     * Constructors *
     *--------------*/

    public NumericMemcachedLiteral(String pkeyId, String label, Number number, URI datatype) {
            super(pkeyId, label, datatype);
            this.number = number;
    }

    public NumericMemcachedLiteral(String pkeyId, Number number, URI datatype) {
            this(pkeyId, number.toString(), number, datatype);
    }

    public NumericMemcachedLiteral(String pkeyId, byte number) {
            this(pkeyId, number, XMLSchema.BYTE);
    }

    public NumericMemcachedLiteral(String pkeyId, short number) {
            this(pkeyId, number, XMLSchema.SHORT);
    }

    public NumericMemcachedLiteral(String pkeyId, int number) {
            this(pkeyId, number, XMLSchema.INT);
    }

    public NumericMemcachedLiteral(String pkeyId, long n) {
            this(pkeyId, n, XMLSchema.LONG);
    }

    public NumericMemcachedLiteral(String pkeyId, float n) {
            this(pkeyId, n, XMLSchema.FLOAT);
    }

    public NumericMemcachedLiteral(String pkeyId, double n) {
            this(pkeyId, n, XMLSchema.DOUBLE);
    }

    /*---------*
     * Methods *
     *---------*/

    @Override
    public byte byteValue() {
            return number.byteValue();
    }

    @Override
    public short shortValue() {
            return number.shortValue();
    }

    @Override
    public int intValue() {
            return number.intValue();
    }

    @Override
    public long longValue() {
            return number.longValue();
    }

    @Override
    public float floatValue() {
            return number.floatValue();
    }

    @Override
    public double doubleValue() {
            return number.doubleValue();
    }
}
