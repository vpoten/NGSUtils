/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.memcached.model;

import java.math.BigInteger;
import java.util.Collection;
import java.util.TreeMap;
import javax.xml.datatype.XMLGregorianCalendar;
import org.ngsutils.semantic.memcached.config.MemcachedStoreSchema;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.CalendarLiteralImpl;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.IntegerLiteralImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.NumericLiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryBase;
import org.openrdf.model.vocabulary.XMLSchema;


/**
 * Based on MemValueFactory.
 * 
 * @author victor
 */
public class MemcachedValueFactory extends ValueFactoryBase {
    
    /*------------*
     * Attributes *
     *------------*/
    protected MemcachedStoreSchema storeSchema;
    
    //registry of contexts
    private final TreeMap<String,Resource> contextRegistry = new TreeMap<String,Resource>();
    
    /**
     * Registry containing the set of namespace strings as used by MemURI objects
     * in a MemoryStore. This registry enables the reuse of objects, minimizing
     * the number of objects in main memory.
     */
    private final MemcachedObjectRegistry<String> namespaceRegistry = new MemcachedObjectRegistry<String>();

    /*---------*
     * Methods *
     *---------*/

    public void clear() {
        namespaceRegistry.clear();
    }
        
    /**
     * Returns a previously created MemValue that is equal to the supplied value,
     * or <tt>null</tt> if the supplied value is a new value or is equal to
     * <tt>null</tt>.
     * 
     * @param value
     *        The MemValue equivalent of the supplied value, or <tt>null</tt>.
     * @return A previously created MemValue that is equal to <tt>value</tt>, or
     *         <tt>null</tt> if no such value exists or if <tt>value</tt> is
     *         equal to <tt>null</tt>.
     */
    public MemcachedValue getMemValue(Value value) {
            if (value instanceof Resource) {
                    return getMemResource((Resource)value);
            }
            else if (value instanceof Literal) {
                    return getMemLiteral((Literal)value);
            }
            else if (value == null) {
                    return null;
            }
            else {
                    throw new IllegalArgumentException("value is not a Resource or Literal: " + value);
            }
    }
    

    /**
     * See getMemValue() for description.
     */
    public MemcachedResource getMemResource(Resource resource) {
            if (resource instanceof URI) {
                    return getMemURI((URI)resource);
            }
            else if (resource instanceof BNode) {
                    return getMemBNode((BNode)resource);
            }
            else if (resource == null) {
                    return null;
            }
            else {
                    throw new IllegalArgumentException("resource is not a URI or BNode: " + resource);
            }
    }

    /**
     * 
     * @return 
     */
    public MemcachedURI getNullURI(){
        return new MemcachedURI("", MemcachedStoreSchema.NULL_URI_KEYID);
    }
    
    /**
     * See getMemValue() for description.
     */
    public synchronized MemcachedURI getMemURI(URI uri) {
        String keyId = getStoreSchema().existsValue(uri);
        return (keyId!=null) ? new MemcachedURI(uri, keyId) : null;
    }

    /**
     * See getMemValue() for description.
     */
    public synchronized MemcachedBNode getMemBNode(BNode bnode) {
        String keyId = getStoreSchema().existsValue(bnode);
        return (keyId!=null) ? new MemcachedBNode(bnode, keyId) : null;
    }

    /**
     * See getMemValue() for description.
     */
    public synchronized MemcachedLiteral getMemLiteral(Literal literal) {
        String keyId = getStoreSchema().existsValue(literal);
        return (keyId!=null) ? createNewLiteral(literal, keyId) : null;
    }
    
    /**
     * Gets or creates a MemValue for the supplied Value. If the factory already
     * contains a MemValue object that is equivalent to the supplied value then
     * this equivalent value will be returned. Otherwise a new MemValue will be
     * created, stored for future calls and then returned.
     * 
     * @param value
     *        A Resource or Literal.
     * @return The existing or created MemValue.
     */
    public MemcachedValue getOrCreateMemValue(Value value) {
        if (value instanceof Resource) {
                return getOrCreateMemResource((Resource)value);
        }
        else if (value instanceof Literal) {
                return getOrCreateMemLiteral((Literal)value);
        }
        else {
                throw new IllegalArgumentException("value is not a Resource or Literal: " + value);
        }
    }

    /**
     * See {@link #getOrCreateMemValue(Value)} for description.
     */
    public MemcachedResource getOrCreateMemResource(Resource resource) {
        if (resource instanceof URI) {
                return getOrCreateMemURI((URI)resource);
        }
        else if (resource instanceof BNode) {
                return getOrCreateMemBNode((BNode)resource);
        }
        else {
                throw new IllegalArgumentException("resource is not a URI or BNode: " + resource);
        }
    }

    /**
     * See {@link #getOrCreateMemValue(Value)} for description.
     */
    public synchronized MemcachedURI getOrCreateMemURI(URI uri) {
        // check namespace
        String namespace = uri.getNamespace();
        String sharedNamespace = namespaceRegistry.get(namespace);

        if (sharedNamespace == null) {
            // New namespace, add it to the registry
            namespaceRegistry.add(namespace);
        }
            
        MemcachedURI memURI = getMemURI(uri);

        if (memURI == null) {
            // Create a MemURI and add it to the registry
            memURI = new MemcachedURI(uri);
            getStoreSchema().addValue(memURI);
        }

        return memURI;
    }
    
    /**
     * 
     * @param namespace
     * @return 
     */
    public Integer getNamespaceId(String namespace){
        Integer id = namespaceRegistry.getId(namespace);
        
        if( id==null ){
            namespaceRegistry.add(namespace);
            id = namespaceRegistry.getId(namespace);
        }
        
        return id;
    }
    
    /**
     * 
     * @param id
     * @return 
     */
    public String getNamespace(int id){
        return (String) namespaceRegistry.getObject(id);
    }

    /**
     * See {@link #getOrCreateMemValue(Value)} for description.
     */
    public synchronized MemcachedBNode getOrCreateMemBNode(BNode bnode) {
        MemcachedBNode memBNode = getMemBNode(bnode);

        if (memBNode == null) {
            memBNode = new MemcachedBNode(bnode);
            getStoreSchema().addValue(memBNode);
        }

        return memBNode;
    }

    /**
     * See {@link #getOrCreateMemValue(Value)} for description.
     */
    public synchronized MemcachedLiteral getOrCreateMemLiteral(Literal literal) {
        MemcachedLiteral memLiteral = getMemLiteral(literal);

        if (memLiteral==null) {
            memLiteral = createNewLiteral(literal, null);
            getStoreSchema().addValue(memLiteral);
        }

        return memLiteral;
    }
        
    /**
     * 
     * @param literal
     * @return 
     */
    protected MemcachedLiteral createNewLiteral(Literal literal, String keyId){
        MemcachedLiteral memLiteral;
        String label = literal.getLabel();
        URI datatype = literal.getDatatype();

        if (datatype != null) {
            try {
                if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
                    memLiteral = new IntegerMemcachedLiteral(keyId, literal.integerValue(), datatype);
                }
                else if (datatype.equals(XMLSchema.DECIMAL)) {
                    memLiteral = new DecimalMemcachedLiteral(keyId, literal.decimalValue(), datatype);
                }
                else if (datatype.equals(XMLSchema.FLOAT)) {
                    memLiteral = new NumericMemcachedLiteral(keyId, literal.floatValue(), datatype);
                }
                else if (datatype.equals(XMLSchema.DOUBLE)) {
                    memLiteral = new NumericMemcachedLiteral(keyId, literal.doubleValue(), datatype);
                }
                else if (datatype.equals(XMLSchema.BOOLEAN)) {
                    memLiteral = new BooleanMemcachedLiteral(keyId, literal.booleanValue());
                }
                else if (datatype.equals(XMLSchema.DATETIME)) {
                    memLiteral = new CalendarMemcachedLiteral(keyId, literal.calendarValue());
                }
                else {
                    memLiteral = new MemcachedLiteral(keyId, label, datatype);
                }
            }
            catch (IllegalArgumentException e) {
                // Unable to parse literal label to primitive type
                memLiteral = new MemcachedLiteral(keyId, literal.getLabel());
            }
        }
        else if (literal.getLanguage() != null) {
            memLiteral = new MemcachedLiteral(keyId, literal.getLabel());
        }
        else {
            memLiteral = new MemcachedLiteral(keyId, literal.getLabel());
        }

        return memLiteral;
    }
        
    public Statement createStatement(Resource subject, URI predicate, Value object){
        return new StatementImpl(subject, predicate, object);
    }
    
    
    public Statement createStatement(Resource subject, URI predicate, Value object, Resource context){
        if (context == null) {
            return new StatementImpl(subject, predicate, object);
        }
        else {
            return new ContextStatementImpl(subject, predicate, object, context);
        }
    }
    
    
    public URI createURI(String namespace, String localname) {
        return createURI(namespace+"#"+localname);
    }

    public URI createURI(String uri) {
        URI tempURI = new URIImpl(uri);
        return getOrCreateMemURI(tempURI);
    }

    public BNode createBNode(String nodeID) {
        BNode tempBNode = new BNodeImpl(nodeID);
        return getOrCreateMemBNode(tempBNode);
    }

    public Literal createLiteral(String value) {
        Literal tempLiteral = new LiteralImpl(value);
        return getOrCreateMemLiteral(tempLiteral); 
    }

    public Literal createLiteral(String value, String language) {
        Literal tempLiteral = new LiteralImpl(value, language);
        return getOrCreateMemLiteral(tempLiteral);
    }

    public Literal createLiteral(String value, URI datatype) {
        Literal tempLiteral = new LiteralImpl(value, datatype);
        return getOrCreateMemLiteral(tempLiteral);
    }
    
    @Override
    public Literal createLiteral(boolean value) {
        Literal newLiteral = new BooleanLiteralImpl(value);
        return getOrCreateMemLiteral(newLiteral);
    }

    @Override
    protected Literal createIntegerLiteral(Number n, URI datatype) {
        Literal newLiteral = new IntegerLiteralImpl(BigInteger.valueOf(n.longValue()), datatype);
        return getOrCreateMemLiteral(newLiteral);
    }

    @Override
    protected Literal createFPLiteral(Number n, URI datatype) {
        Literal newLiteral = new NumericLiteralImpl(n, datatype);
        return getOrCreateMemLiteral(newLiteral);
    }

    @Override
    public Literal createLiteral(XMLGregorianCalendar calendar) {
        Literal newLiteral = new CalendarLiteralImpl(calendar);
        return getOrCreateMemLiteral(newLiteral);
    }

    /**
     * @return the storeSchema
     */
    public MemcachedStoreSchema getStoreSchema() {
        return storeSchema;
    }

    /**
     * @param storeSchema the storeSchema to set
     */
    public void setStoreSchema(MemcachedStoreSchema storeSchema) {
        this.storeSchema = storeSchema;
    }

    /**
     * 
     * @param val
     * @return 
     */
    public BNode parseBNode(String val) {
        return new BNodeImpl(val);
    }
    
    /**
     * 
     * @param val
     * @return 
     */
    public URI parseURI(String val) {
        return new URIImpl(val);
    }
    
    /**
     * 
     * @param val String with format: "label"[@lang][^^<datatype>]
     * @return 
     */
    public Literal parseLiteral(String val) {
        if( val.charAt(0)!='"' ){
            return null;
        }
        
        int quoteIdx = val.indexOf('"',1);
        String label = val.substring(1, quoteIdx );
        String lang = null;
        URI datatype = null;
        
        // get type string
        int typeIdx = val.indexOf("^^<");
        if( typeIdx<0 ) {
            typeIdx = val.length();
        }
        else{
            datatype = new URIImpl(val.substring(typeIdx+3, val.length()-1));
        }
        
        //get lang string
        if( (quoteIdx+1)<val.length() && val.charAt(quoteIdx+1)=='@' ){
            lang = val.substring(quoteIdx+2, typeIdx);
        }
        
        if( datatype!=null ) {
            return createNewLiteral( new LiteralImpl(label, datatype), null );
        }
        
        if( lang!=null ) {
            createNewLiteral( new LiteralImpl(label, lang), null );
        }
        
        return createNewLiteral( new LiteralImpl(label), null );
    }
    
    /**
     * Adds a Resource to context registry
     * 
     * @param res 
     */
    public void addToContexts(Resource res){
        if( !contextRegistry.containsKey(res.toString()) ) {
            contextRegistry.put(res.toString(), res);
        }
    }
    
    /**
     * gets context registry content
     * 
     * @return 
     */
    public Collection<Resource> getContexts(){
        return contextRegistry.values();
    }

}

