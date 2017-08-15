/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.memcached.model;

import java.io.IOException;
import javax.xml.datatype.XMLGregorianCalendar;
import org.openrdf.model.URI;
import org.openrdf.model.datatypes.XMLDatatypeUtil;

/**
 *
 * @author victor
 */
public class CalendarMemcachedLiteral extends MemcachedLiteral {
    
    transient private XMLGregorianCalendar calendar;
    
    /*--------------*
     * Constructors *
     *--------------*/

    public CalendarMemcachedLiteral(String keyId, XMLGregorianCalendar calendar) {
            this(keyId, calendar.toXMLFormat(), calendar);
    }

    public CalendarMemcachedLiteral(String keyId, String label, XMLGregorianCalendar calendar) {
            this(keyId, label, XMLDatatypeUtil.qnameToURI(calendar.getXMLSchemaType()), calendar);
    }

    public CalendarMemcachedLiteral(String keyId, String label, URI datatype, XMLGregorianCalendar calendar) {
            super(keyId, label, datatype);
            this.calendar = calendar;
    }

    /*---------*
     * Methods *
     *---------*/

    @Override
    public XMLGregorianCalendar calendarValue() {
            return calendar;
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException
    {
            try {
                    in.defaultReadObject();
                    calendar = XMLDatatypeUtil.parseCalendar(this.getLabel());
            }
            catch (ClassNotFoundException e) {
                    throw new IOException(e.getMessage());
            }
    }
}
