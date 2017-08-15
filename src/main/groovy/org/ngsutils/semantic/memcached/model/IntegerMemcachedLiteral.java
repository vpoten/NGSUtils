/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.memcached.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author victor
 */
public class IntegerMemcachedLiteral extends MemcachedLiteral {
    
    private final BigInteger value;

    /*--------------*
     * Constructors *
     *--------------*/

    public IntegerMemcachedLiteral(String pkeyId, BigInteger value) {
            this(pkeyId, value, XMLSchema.INTEGER);
    }

    public IntegerMemcachedLiteral(String pkeyId, BigInteger value, URI datatype) {
            this(pkeyId, value.toString(), value, datatype);
    }

    public IntegerMemcachedLiteral(String pkeyId, String label, BigInteger value, URI datatype) {
            super(pkeyId, label, datatype);
            this.value = value;
    }

    /*---------*
     * Methods *
     *---------*/

    @Override
    public byte byteValue() {
            return value.byteValue();
    }

    @Override
    public short shortValue() {
            return value.shortValue();
    }

    @Override
    public int intValue() {
            return value.intValue();
    }

    @Override
    public long longValue() {
            return value.longValue();
    }

    @Override
    public float floatValue() {
            return value.floatValue();
    }

    @Override
    public double doubleValue() {
            return value.doubleValue();
    }

    @Override
    public BigInteger integerValue() {
            return value;
    }

    @Override
    public BigDecimal decimalValue() {
            return new BigDecimal(value);
    }
}
