/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.memcached.config;

import info.aduna.iteration.LookAheadIteration;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.transcoders.SerializingTranscoder;
import org.ngsutils.Utils;
import org.ngsutils.semantic.memcached.model.MemcachedResource;
import org.ngsutils.semantic.memcached.model.MemcachedURI;
import org.ngsutils.semantic.memcached.model.MemcachedValue;
import org.ngsutils.semantic.memcached.model.MemcachedValueFactory;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.sail.SailException;

/**
 * Defines constants for the MemcachedStore schema, also implements base
 * storage operations.
 * 
 * @author victor
 */
public class MemcachedStoreSchema {

    // prefixes for memcached keys associated to values
    static final char PREF_URI = 'U';
    static final char PREF_BNODE = 'B';
    static final char PREF_LITERAL = 'L';
    static final char KSEPARATOR = '/';
    static final char IDXSEPARATOR = ',';
    static final String NULL_URI_KEY = "0"+KSEPARATOR+"0";
    public static final String NULL_URI_KEYID = "U0";
    
    //constants
    static final private int ITEMS_CHUNK = 10000;
    static private final int COMPR_THR = 1024 * 2;
    
    private final MemcachedValueFactory valueFactory;
    private final MemcachedClient memClient;
    private final SerializingTranscoder transcoder;
    private boolean only1Key = true;
    protected long numStatements = 0;
    private long autoIncrement = 0;
    
    /**
     * 
     * @param vf
     * @param mcli 
     */
    public MemcachedStoreSchema(MemcachedValueFactory vf, MemcachedClient mcli){
        valueFactory = vf;
        memClient = mcli;
        transcoder = new SerializingTranscoder();
        transcoder.setCompressionThreshold(COMPR_THR);
        valueFactory.setStoreSchema(MemcachedStoreSchema.this);
    }
    
    /**
     * encodes an integer using Character.MAX_RADIX (base 36)
     * 
     * @param val
     * @return 
     */
    protected static String encodeInteger(int val){
        return Integer.toString(val, Character.MAX_RADIX);
    }
    
    protected static int decodeInteger(String val){
        return Integer.parseInt(val, Character.MAX_RADIX);
    }
    
    protected static String encodeLong(long val){
        return Long.toString(val, Character.MAX_RADIX);
    }
    
    /**
     * 
     * @param uri
     * @return codification of namespace+localName
     */
    protected String encodeURI(URI uri){
        if( uri==null ) {
            return NULL_URI_KEY;
        }
        
        return encodeInteger( getValueFactory().getNamespaceId(uri.getNamespace()) ) + 
                KSEPARATOR + encodeLong(Utils.hash(uri.getLocalName()));
    }
    
    /**
     * 
     * @param literal
     * @return codification of label+lang+datatype
     */
    protected String encodeLiteral(Literal literal) {
        return encodeLong(Utils.hash(literal.getLabel())) + KSEPARATOR +
                ((literal.getLanguage()==null) ? "0" : encodeLong(Utils.hash(literal.getLanguage()))) +
                KSEPARATOR + encodeURI(literal.getDatatype());
    }
    
    /**
     * 
     * @param bnode
     * @return 
     */
    protected String encodeBNode(BNode bnode) {
        return encodeLong(Utils.hash(bnode.toString()));
    }
    
    /**
     * 
     * @param val
     * @return prefix + codification(val)
     */
    protected String encodeKey(Value val){
        
        if( val==null || val instanceof URI ) {
            return PREF_URI + encodeURI( (URI)val );
        }
        else if(val instanceof BNode) { //prefix+hash(id)
            return PREF_BNODE + encodeBNode( (BNode)val );
        }
        else if(val instanceof Literal) {
            return PREF_LITERAL + encodeLiteral( (Literal)val );
        }
        
        return null;
    }
    
    
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    protected boolean setValue(String key, Object value){
        OperationFuture<Boolean> opf = getMemClient().set(key, 0, value, transcoder);
        
        try {
            return opf.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(MemcachedStoreSchema.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(MemcachedStoreSchema.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    protected boolean appendValue(String key, Object value){
        OperationFuture<Boolean> opf = getMemClient().append(0L, key, value, transcoder);
        
        try {
            if( opf.get()==false ){
                return setValue(key, value);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(MemcachedStoreSchema.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(MemcachedStoreSchema.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }
    
    /**
     * 
     * @param key
     * @return 
     */
    protected Object getValue(String key){
        return getMemClient().get(key, transcoder);
    }
    
    /**
     * get uncompressed String
     * 
     * @param key
     * @return
     * @throws IOException 
     */
    protected String getValueAsString(String key) {
        Object val = getValue(key);
        
        if( val instanceof byte[] ){
            return new String((byte [])val);
        }
            
        return (String) val;
    }
    
    /**
     * 
     * @param key
     * @param seconds
     * @return 
     */
    protected Object getValueAsync(String key, int seconds){
        Future<Object> f = getMemClient().asyncGet(key, transcoder);
        Object object = null;

        try {
            object = f.get(seconds, TimeUnit.SECONDS);
        } catch (Exception ex) {
            f.cancel(false);
        } 
        
        return object;
    }
    
    
    /**
     * creates a composed context key 
     * 
     * @param part1
     * @param part2
     * @param contextKey
     * @return 
     */
    protected String buildKey(MemcachedValue part1, MemcachedValue part2, MemcachedResource context){
        return context.getKeyId() + part1.getKeyId() + part2.getKeyId();
    }
    
    /**
     * creates a composed context key 
     * 
     * @param part1
     * @param contextKey
     * @return 
     */
    protected String buildKey(MemcachedValue part1, MemcachedResource context){
        return context.getKeyId() + part1.getKeyId();
    }
    
    /**
     * 
     * @param numItems
     * @return 
     */
    private int lastChunkIndex(long numItems){
        return (int) ((numItems-1)/ITEMS_CHUNK);
    }
    
    private String chunkKey(int idx, String key){
        return idx + key;
    }
    
    /**
     * add to index internal operation
     * 
     * @param key
     * @param val
     * @return 
     */
    protected boolean addToIndexContent(String key, String val) throws IOException, SailException {
        Long numItems = incrCounter(key, 1L);
        
        if( numItems<0 ) {
            throw new SailException("memcached: cannot incr counter");
        }
        
        //get the idx of the last chunk
        int chunks = lastChunkIndex(numItems);
        
        String chunkkey = chunkKey(chunks,key);
        return appendValue(chunkkey, val);
    }
    
    /**
     * 
     * @param ch a Character
     * @return true if the character is a key prefix (defined in constants: PREF_*)
     */
    protected boolean isKeyPrefix(Character ch){
        return (ch==PREF_URI || ch==PREF_BNODE || ch==PREF_LITERAL);
    }
    
    /**
     * used by removeFromIndexContent
     * 
     * @param val
     * @param keyLen
     * @return 
     */
    private int getFirstKeyEnd(String data, int keyLen, int begin) {
        int end = data.length();
        
        for(int i=begin; i<data.length(); i++){
            if( isKeyPrefix(data.charAt(i)) ){
                if( keyLen==2 ){
                    return i;
                }
                else{
                    keyLen++;
                }
            }
        }
        
        return end;
    }
    
    /**
     * 
     * @param key
     * @param val
     * @param keyLen
     * @return
     * @throws IOException 
     */
    protected boolean removeFromIndexContent(String key, String val, int keyLen) throws IOException {
        // iterate over chunks
        IndexChunkIterator iter = new IndexChunkIterator(key);
        int chunkIdx = -1;

        while( iter.hasNext() ){
            String content = iter.nextContent();
            if( content.contains(val) ) {
                chunkIdx = iter.getIndex();
                break;
            }
        }
            
        if( chunkIdx<0 ) {
            return false; //not found
        }
        
        String newVal = "";
        
        if( chunkIdx != iter.getLastChunk() ){
            //get the first key from the last chunk to replace the key which is going to be removed
            String data = getValueAsString( chunkKey(iter.getLastChunk(),key) );
            int end = getFirstKeyEnd(data, keyLen, 1);
            
            newVal = data.substring(0, end);
            
            if( end==data.length() ){
                //remove chunk
                getMemClient().delete(chunkKey(iter.getLastChunk(),key));
            }
            else{
                data = data.substring(end);
                setValue( chunkKey(iter.getLastChunk(),key), data );
            }
        }
        
        Long numItems = decrCounter(key, 1L);
        
        String data = getValueAsString( chunkKey(chunkIdx, key) );
        data = data.replace(val, newVal);
        
        return setValue( chunkKey(chunkIdx,key), data );
    }
    
    
    /**
     * 
     * @param key
     * @return 
     */
    protected Long getCounter(String key) {
        String val = (String) getValue(key);
        return (val!=null) ? Long.parseLong(val) : null;
    }
    
    /**
     * 
     * @param key
     * @param by
     * @return 
     */
    protected long incrCounter(String key, long by) {
        return getMemClient().incr(key, by, by);
    }
    
    /**
     * 
     * @param key
     * @param by
     * @return 
     */
    protected long decrCounter(String key, long by) {
        return getMemClient().decr(key, by, 0L);
    }
    
    
    /**
     * converts statement key to string key
     * 
     * @param st
     * @return 
     */
    private String stmToKey(ContextTriple st){
        if( st==null ){
            return null;
        }
        
        int numKeys = calcNumKeys(st.getSubject(), st.getPredicate(), st.getObject());
        
        if(numKeys==2){
            if( st.getSubject()!=null && st.getPredicate()!=null ) {
                return buildKey(st.getSubject(), st.getPredicate(), st.getContext());
            }
            else if( st.getSubject()!=null && st.getObject()!=null ){
                return buildKey(st.getSubject(), st.getObject(), st.getContext());
            }
            
            return buildKey(st.getPredicate(), st.getObject(), st.getContext());
        }
        
        if( st.getSubject()!=null ) {
            return buildKey(st.getSubject(), st.getContext());
        }
        else if( st.getPredicate()!=null ){
            return buildKey(st.getPredicate(), st.getContext());
        }
        else if( st.getObject()!=null ){
            return buildKey(st.getObject(), st.getContext());
        }
        
        return null;
    }
    
    /**
     * gets the index key with less instances (better performance for 
     * iteration over index)
     * 
     * @param subjKey
     * @param predKey
     * @param objKey
     * @param contextKey
     * @return 
     */
    private ContextTriple getLessIndexContentKeyInternal( MemcachedResource subj, MemcachedURI pred, 
            MemcachedValue obj, MemcachedResource context) {
        Long [] values = new Long [] {Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE};
        ContextTriple [] keys = new ContextTriple [3];
        
        int keyLen = calcNumKeys(subj, pred, obj);
        
        if( keyLen==2 ){
            keys[0] = new ContextTriple(subj, pred, null, context);
            keys[1] = new ContextTriple(null, pred, obj, context);
            keys[2] = new ContextTriple(subj, null, obj, context);
        }
        else if( keyLen==1 ){
            keys[0] = new ContextTriple(subj, null, null, context);
            keys[1] = new ContextTriple(null, pred, null, context);
            keys[2] = new ContextTriple(null, null, obj, context);
        }
        
        for(int i=0; i<3; i++) {
            String key = stmToKey(keys[i]);
            if( key!=null )  {      
                values[i] = getCounter(key);
            }
        }
        
        if( values[0]==null || values[1]==null || values[2]==null ) {
            return null;
        }
        
        if( values[0]<=values[1] && values[0]<=values[2] ) {
            return keys[0];
        }
        else if( values[1]<=values[0] && values[1]<=values[2] ) {
            return keys[1];
        }
        
        return keys[2];
    }
    
    /**
     * calculates the number of keys that index the search
     * 
     * @param subj
     * @param pred
     * @param obj
     * @return 
     */
    private int calcNumKeys(Resource subj, URI pred, Value obj){
        int numKeys = 0;
        if( subj!=null ){ numKeys++; }
        if( pred!=null ){ numKeys++; }
        if( obj!=null ){ numKeys++; }
        
        if( only1Key ){
            return 1;
        }
        
        return ((numKeys>2) ? 2 : numKeys);
    }
    
    
    /**
     * 
     * @return the current autoIncrement
     */
    private synchronized long getAutoIncrement() {
        autoIncrement++;
        return autoIncrement;
    }

    protected synchronized String newKeyId(Value val) {
        long newId = getAutoIncrement();
        
        if( val instanceof URI ) {
            return PREF_URI + encodeLong(newId);
        }
        else if(val instanceof BNode) {
            return PREF_BNODE + encodeLong(newId);
        }
        else if(val instanceof Literal) {
            return PREF_LITERAL + encodeLong(newId);
        }
        
        return null;
    }
    
    /**
     * 
     * @param val
     * @return 
     */
    protected String getKeyId(Value val){
        if(val==null) {
            return null;
        }
        
        String key = encodeKey(val);
        return (String)getValue(key);
    }
    
    /**
     * 
     * @param subject
     * @param predicate
     * @param object
     * @param contexts
     * @return 
     */
    public String getLessIndexContentKey( MemcachedResource subj, MemcachedURI pred, 
            MemcachedValue obj, MemcachedResource context ) {
        return stmToKey(getLessIndexContentKeyInternal(subj, pred, obj, context));
    }
    
    /**
     * 
     * @param subj
     * @param pred
     * @param obj
     * @return 
     */
    public MemcachedStatementIterator getLessIndexSearchPattern( MemcachedResource subj, MemcachedURI pred, 
            MemcachedValue obj, MemcachedResource context ) {
        return new MemcachedStatementIterator(subj, pred, obj, context);
    }
    
    /**
     * 
     * @param val
     * @return the associated id if created or existing, null if error
     */
    public String addValue(MemcachedValue val){
        String key = encodeKey(val);
        String id = (String) getValue(key);
        
        if( id==null ){
            id = newKeyId(val);
            
            setValue( key, id);
            
            String strVal;
            
            if( val instanceof Literal ){
                strVal = val.toString();
            }
            else if( val instanceof URI ){
                URI uri = (URI) val;
                strVal = encodeInteger( getValueFactory().getNamespaceId(uri.getNamespace()) )
                        +  KSEPARATOR + uri.getLocalName();
            }
            else{
                strVal = val.stringValue();
            }
            
            if( !setValue(id, strVal) ) {
                return null;
            }
        }
        
        val.setKeyId(id);
       
        return id;
    }
    
    /**
     * 
     * @param val
     * @return tke keyId if exists else return null
     */
    public String existsValue(Value val){
        return getKeyId(val);
    }
    
    /**
     * 
     * @param val
     * @return 
     */
    public boolean removeValue(Value val){
        String key = encodeKey(val);
        String id = (String) getValue(key);
        getMemClient().delete(key);
        OperationFuture<Boolean> opf = getMemClient().delete(id);
        
        try {
            return opf.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(MemcachedStoreSchema.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(MemcachedStoreSchema.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    /**
     * 
     * @param keyId
     * @return 
     */
    protected Value getModelValue(String keyId){
        String str = (String) getValue(keyId);
        
        if( str==null ) {
            return null;
        }
        
        if( keyId.charAt(0)==PREF_URI ){
            int pos = str.indexOf(KSEPARATOR);
            int nameId = decodeInteger( str.substring(0, pos) );
            return getValueFactory().parseURI( getValueFactory().getNamespace(nameId) + 
                    '#' + str.substring(pos+1) );
        } 
        else if( keyId.charAt(0)==PREF_BNODE ){
            return getValueFactory().parseBNode(str);
        }
        else if( keyId.charAt(0)==PREF_LITERAL ){
            return getValueFactory().parseLiteral(str);
        }
           
        return null;
    }
    
    
    /**
     * @return the memClient
     */
    public MemcachedClient getMemClient() {
        return memClient;
    }

    /**
     * @return the valueFactory
     */
    public MemcachedValueFactory getValueFactory() {
        return valueFactory;
    }

    
    /**
     * 
     * @param subj
     * @param pred
     * @param obj
     * @param context
     * @return 
     */
    public boolean addStatement(MemcachedResource subj, MemcachedURI pred, MemcachedValue obj, MemcachedResource context) throws SailException{
        String subjKeyId = subj.getKeyId();
        String predKeyId = pred.getKeyId();
        String objKeyId = obj.getKeyId();
        
        try {
            if( !only1Key ){
                //add to sp? index
                addToIndexContent( buildKey(subj,pred,context), objKeyId);
                //add to ?po index
                addToIndexContent( buildKey(pred,obj,context), subjKeyId);
                //add to s?o index
                addToIndexContent( buildKey(subj,obj,context), predKeyId);
            }
            
            // add to s?? index
            addToIndexContent( buildKey(subj,context), predKeyId+objKeyId);
            // add to ?p? index
            addToIndexContent( buildKey(pred,context), subjKeyId+objKeyId);
            // add to ??o index
            addToIndexContent( buildKey(obj,context), subjKeyId+predKeyId);
            
        } catch (IOException ex) {
            Logger.getLogger(MemcachedStoreSchema.class.getName()).log(Level.SEVERE, null, ex);
            throw new SailException(ex);
        }
        numStatements++;
        
        return true;
    }
    
    /**
     * 
     * @param subj
     * @param pred
     * @param obj
     * @param context
     * @return 
     */
    public boolean hasStatement(MemcachedResource subj, MemcachedURI pred, MemcachedValue obj, MemcachedResource context) throws SailException{
        try {
            MemcachedStatementIterator iter = new MemcachedStatementIterator(subj, pred, obj, context);
            return (iter.getNextElement()!=null);
        } catch (Exception ex) {
            Logger.getLogger(MemcachedStoreSchema.class.getName()).log(Level.SEVERE, null, ex);
            throw new SailException(ex);
        } 
    }
    
    /**
     * 
     * @param st
     * @return 
     */
    public boolean removeStatement(Statement st) throws SailException{
        MemcachedResource subj = (MemcachedResource) st.getSubject();
        MemcachedURI pred = (MemcachedURI) st.getPredicate();
        MemcachedValue obj = (MemcachedValue) st.getObject();
        MemcachedResource context = (MemcachedResource) st.getContext();
        
        String subjKeyId = subj.getKeyId();
        String predKeyId = pred.getKeyId();
        String objKeyId = obj.getKeyId();
        
        try {
            if( !only1Key ){
                //add to sp? index
                removeFromIndexContent( buildKey(subj,pred,context), objKeyId, 2);
                //add to ?po index
                removeFromIndexContent( buildKey(pred,obj,context), subjKeyId, 2);
                //add to s?o index
                removeFromIndexContent( buildKey(subj,obj,context), predKeyId, 2);
            }
            
            //add to s?? index
            removeFromIndexContent( buildKey(subj,context), predKeyId+objKeyId, 1);
            //add to ?p? index
            removeFromIndexContent( buildKey(pred,context), subjKeyId+objKeyId, 1);
            //add to ??o index
            removeFromIndexContent( buildKey(obj,context), subjKeyId+predKeyId, 1);
        } catch (IOException ex) {
            Logger.getLogger(MemcachedStoreSchema.class.getName()).log(Level.SEVERE, null, ex);
            throw new SailException(ex);
        }
        
        numStatements--;
        
        return true;
    }

    /**
     * @return the numStatements
     */
    public long getNumStatements() {
        return numStatements;
    }

    /**
     * gets the number of statements where the Value is present
     * 
     * @param val
     * @return 
     */
    public Long getCount(MemcachedValue val, MemcachedResource context) {
        return this.getCounter( buildKey(val, context) );
    }

    /**
     * Inner class for index chunks iteration
     * 
     */
    private class IndexChunkIterator implements Iterator {
        
        private final int lastChunk;
        private int current = -1;
        private final String baseKey;
        
        public IndexChunkIterator(String key){
            baseKey = key;
            Long numItems = (key==null) ? null : getCounter(key);
            
            if( numItems==null ){
                lastChunk = -1;
                return;
            }
            
            lastChunk = lastChunkIndex(numItems);
        }
        
        public boolean hasNext() {
            return (current < getLastChunk());
        }

        /**
         * returns next valid index key
         * 
         * @return a String
         */
        public Object next() {
            current++;
            return chunkKey(current,baseKey);
        }
        
        /**
         * returns next uncompressed index content
         * 
         * @return a String
         * @throws IOException 
         */
        public String nextContent() throws IOException {
            current++;
            return getValueAsString(chunkKey(current,baseKey));
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /**
         * 
         * @return the current index
         */
        public int getIndex() {
            return current;
        }

        /**
         * @return the lastChunk index
         */
        public int getLastChunk() {
            return lastChunk;
        }
        
    } // end of IndexChunkIterator class
 
    
    /**
     * 
     */
    public class MemcachedStatementIterator<X extends Exception> extends LookAheadIteration<Statement, X> {
        
        private IndexChunkIterator chunkIterator = null;
        private int chunkDataIdx = 0;
        private String chunkData = null;
        private final int keyLen;
        private final ContextTriple stKey;
        
        private String [] keyPattern = new String [] {null, null};
        
    
        public MemcachedStatementIterator(MemcachedResource subj, MemcachedURI pred, MemcachedValue obj, 
                MemcachedResource context) {
            stKey = getLessIndexContentKeyInternal(subj, pred, obj, context);
            keyLen = calcNumKeys(subj, pred, obj);
            
            chunkIterator = new IndexChunkIterator( stmToKey(stKey) );
            int cont = 0;
            
            if( stKey==null ){
                return;
            }
            
            // encode search pattern (null for wildcards)
            if( stKey.getSubject()==null ){
                if( subj!=null ){
                    keyPattern[cont] = subj.getKeyId();
                }
                cont++;
            }
            
            if( stKey.getPredicate()==null ){
                if( pred!=null ){
                    keyPattern[cont] = pred.getKeyId();
                }
                cont++;
            }
            
            if( stKey.getObject()==null ){
                if( obj!=null ){
                    keyPattern[cont] = obj.getKeyId();
                }
                cont++;
            }
        }
        
        /**
         * utility method used by getNextElement()
         * @return
         * @throws IOException 
         */
        private boolean moveForward() throws IOException {
            if( chunkData==null || chunkDataIdx>=chunkData.length() ){
                if( chunkIterator.hasNext() ){
                    chunkData = chunkIterator.nextContent();
                    chunkDataIdx = 0;
                }
                else{
                    return false;
                }
            }
            
            return true;
        }
        
        /**
         * utility method used by getNextElement()
         * @param key1
         * @param key2
         * @return 
         */
        private Statement buildStatement(String matchKey1, String matchKey2){
            Resource subj = stKey.getSubject();
            URI pred = stKey.getPredicate();
            Value obj = stKey.getObject();
            Resource ctxt = stKey.getContext(); 
                 
            if( keyLen==1 ){
                if( subj!=null ){
                    return new ContextStatementImpl(subj, (URI)getModelValue(matchKey1), 
                            getModelValue(matchKey2), ctxt);
                }
                else if( pred!=null ){
                    return new ContextStatementImpl((Resource)getModelValue(matchKey1), pred, 
                            getModelValue(matchKey2), ctxt);
                }
                else{
                    return new ContextStatementImpl((Resource)getModelValue(matchKey1), 
                            (URI)getModelValue(matchKey2), obj, ctxt);
                }
            }
            
            if( subj!=null && pred!=null ){
                return new ContextStatementImpl(subj, pred, getModelValue(matchKey1), ctxt);
            }
            else if( pred!=null && obj!=null ){
                return new ContextStatementImpl((Resource)getModelValue(matchKey1), pred, obj, ctxt);
            }
            
            return new ContextStatementImpl(subj, (URI)getModelValue(matchKey1), obj, ctxt);
        }
        

        @Override
        protected Statement getNextElement() throws X {
            try {
                while( moveForward() ){
                    int idx = getFirstKeyEnd(chunkData, keyLen, chunkDataIdx+1);
                    String content = chunkData.substring(chunkDataIdx, idx);
                    Statement st = null;
                    
                    if( keyLen==1 ){
                        int pos = getFirstKeyEnd(content, 2, 1);
                        String part1 = content.substring(0, pos);
                        String part2 = content.substring(pos);
                        
                        if( keyPattern[0]==null || keyPattern[0].equals(part1) ){
                            if( keyPattern[1]==null || keyPattern[1].equals(part2) ){
                                st = buildStatement(part1, part2); //matches
                            }
                        }
                    }
                    else{
                        if( keyPattern[0]==null || keyPattern[0].equals(content) ){
                            st = buildStatement(content, null); //matches
                        }
                    }
                    
                    chunkDataIdx = idx;
                    
                    if( st!=null ){
                        return st;
                    }
                }
                
            } catch (IOException ex) {
                Logger.getLogger(MemcachedStoreSchema.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return null;
        }
        
    } // end of MemcachedStatementIterator class
 
    /**
     * 
     */
    private class ContextTriple {
        protected final MemcachedResource subject;
        protected final MemcachedURI predicate;
        protected final MemcachedValue object;
        protected final MemcachedResource context;

        public ContextTriple(MemcachedResource subj, MemcachedURI pred, 
                MemcachedValue obj, MemcachedResource ctx) {
            this.subject = subj;
            this.predicate = pred;
            this.object = obj;
            this.context = ctx;
        }
                
        /**
         * @return the subject
         */
        public MemcachedResource getSubject() {
            return subject;
        }

        /**
         * @return the predicate
         */
        public MemcachedURI getPredicate() {
            return predicate;
        }

        /**
         * @return the object
         */
        public MemcachedValue getObject() {
            return object;
        }

        /**
         * @return the context
         */
        public MemcachedResource getContext() {
            return context;
        }
    }// end of ContextTriple
    
}

