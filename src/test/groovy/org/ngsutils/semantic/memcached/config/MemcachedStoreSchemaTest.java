/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.memcached.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ngsutils.Utils;
import org.ngsutils.semantic.memcached.config.MemcachedStoreSchema.MemcachedStatementIterator;
import org.ngsutils.semantic.memcached.model.MemcachedLiteral;
import org.ngsutils.semantic.memcached.model.MemcachedResource;
import org.ngsutils.semantic.memcached.model.MemcachedURI;
import org.ngsutils.semantic.memcached.model.MemcachedValue;
import org.ngsutils.semantic.memcached.model.MemcachedValueFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.sail.SailException;

/**
 *
 * @author victor
 */
public class MemcachedStoreSchemaTest {
    
    static private MemcachedValueFactory valueFactory;
    static private MemcachedClient memClient;
    static private MemcachedStoreSchema storeSchema;
    
    static private final String KEY1 = "key1";
    static private final String VAL1 = "val1";
    static private final String KEY2 = "key2";
    static private final String VAL2 = "val2";
    static private final String COUNT1 = "count1";
    
    static private final String SUBJ_PRE = "http://localhost/subjs#";
    static private final String PRED_PRE = "http://localhost/preds#";
    private static final String CONTEXT = "http://localhost";
    
    static MemcachedURI testSubj = new MemcachedURI(SUBJ_PRE+"test");
    static MemcachedURI testPred = new MemcachedURI(PRED_PRE+"proper1");
    static MemcachedLiteral testObj = new MemcachedLiteral("hello!");
    static MemcachedResource testContext = new MemcachedURI(CONTEXT);
    
    public MemcachedStoreSchemaTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        ArrayList<String> servers = new ArrayList<String>();
        servers.add("localhost:11211");
        
        try {
            memClient = new MemcachedClient( AddrUtil.getAddresses(servers)  );
        } catch (IOException ex) {
            Logger.getLogger(MemcachedStoreSchemaTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        valueFactory = new MemcachedValueFactory();
        
        storeSchema = new MemcachedStoreSchema(valueFactory, memClient);
        
        //clean previous keys
        storeSchema.getMemClient().delete(KEY1);
        storeSchema.getMemClient().delete(KEY2);
        storeSchema.getMemClient().delete(COUNT1);
        
        //add URIs (namespace) and context
        testSubj = valueFactory.getOrCreateMemURI( testSubj );
        testPred = valueFactory.getOrCreateMemURI( testPred );
        testObj = valueFactory.getOrCreateMemLiteral( testObj );
        testContext = valueFactory.getOrCreateMemURI( (MemcachedURI)testContext );
                
        valueFactory.addToContexts(testContext);
        try {
            storeSchema.addStatement(testSubj, testPred, testObj, testContext);
        } catch (SailException ex) {
            Logger.getLogger(MemcachedStoreSchemaTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
        Statement st = new ContextStatementImpl(testSubj,testPred,testObj,testContext);
        try {
            while( storeSchema.hasStatement(testSubj, testPred, testObj, testContext)) {
                storeSchema.removeStatement(st);
            }
            
            storeSchema.removeValue(testSubj);
            storeSchema.removeValue(testPred);
            storeSchema.removeValue(testObj);
            storeSchema.removeValue(testContext);
        } catch (SailException ex) {
            Logger.getLogger(MemcachedStoreSchemaTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of encodeInteger method, of class MemcachedStoreSchema.
     */
    @Test
    public void testEncodeInteger() {
        System.out.println("encodeInteger");
        int val = 0;
        String expResult = "0";
        String result = MemcachedStoreSchema.encodeInteger(val);
        assertEquals(expResult, result);
        
        val = 10;
        result = MemcachedStoreSchema.encodeInteger(val);
        assertEquals("a", result);
    }
    
    @Test
    public void testDecodeInteger() {
        System.out.println("decodeInteger");
        int val = 0;
        String expResult = "0";
        String result = MemcachedStoreSchema.encodeInteger(val);
        int val2 = MemcachedStoreSchema.decodeInteger(result);
        assertEquals(val, val2);
        
        val = 10;
        result = MemcachedStoreSchema.encodeInteger(val);
        val2 = MemcachedStoreSchema.decodeInteger(result);
        assertEquals(val, val2);
    }

    /**
     * Test of encodeLong method, of class MemcachedStoreSchema.
     */
    @Test
    public void testEncodeLong() {
        System.out.println("encodeLong");
        long val = 0L;
        String expResult = "0";
        String result = MemcachedStoreSchema.encodeLong(val);
        assertEquals(expResult, result);
        
        val = 10L;
        result = MemcachedStoreSchema.encodeLong(val);
        assertEquals("a", result);
    }

    /**
     * Test of encodeURI method, of class MemcachedStoreSchema.
     */
    @Test
    public void testEncodeURI() {
        System.out.println("encodeURI");
        URI uri = null;
        MemcachedStoreSchema instance = storeSchema;
        String expResult = "0/0";
        String result = instance.encodeURI(uri);
        assertEquals(expResult, result);
    }

    /**
     * Test of encodeLiteral method, of class MemcachedStoreSchema.
     */
    @Test
    public void testEncodeLiteral() {
        System.out.println("encodeLiteral");
        String str = "1";
        Literal literal = new LiteralImpl(str);
        MemcachedStoreSchema instance = storeSchema;
        String expResult = MemcachedStoreSchema.encodeLong(Utils.hash(str))+"/0/0/0";
        String result = instance.encodeLiteral(literal);
        assertEquals(expResult, result);
    }

    /**
     * Test of encodeKey method, of class MemcachedStoreSchema.
     */
    @Test
    public void testEncodeKey() {
        System.out.println("encodeKey");
        Value val = null;
        MemcachedStoreSchema instance = storeSchema;
        String expResult = "U0/0";
        String result = instance.encodeKey(val);
        assertEquals(expResult, result);
    }

    /**
     * Test of setValue method, of class MemcachedStoreSchema.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        
        MemcachedStoreSchema instance = storeSchema;
        
        boolean result = instance.setValue(KEY1, VAL1);
        assertTrue(result);
        
        String strresult = (String) instance.getValue(KEY1);
        assertEquals(VAL1, strresult);
    }

    /**
     * Test of getValueAsString method, of class MemcachedStoreSchema.
     */
    @Test
    public void testGetValueAsString() throws Exception {
        System.out.println("getValueAsString");
        
        String text = "Text to gzip!";
        MemcachedStoreSchema instance = storeSchema;
        
        instance.setValue(KEY2, text);
        
        String result = instance.getValueAsString(KEY2);
        assertEquals(text, result);
    }

    /**
     * Test of getValueAsync method, of class MemcachedStoreSchema.
     */
    @Test
    public void testGetValueAsync() {
        System.out.println("getValueAsync");
        
        int seconds = 2;
        MemcachedStoreSchema instance = storeSchema;
        
        boolean result = instance.setValue(KEY1, VAL1);
        assertTrue(result);
        
        String strresult = (String) instance.getValueAsync(KEY1, seconds);
        assertEquals(VAL1, strresult);
    }

    /**
     * Test of buildKey method, of class MemcachedStoreSchema.
     */
    @Test
    public void testBuildKey_3args() {
        System.out.println("buildKey_3args");
        
        MemcachedValue part1 = testSubj;
        MemcachedValue part2 = testPred;
        
        MemcachedStoreSchema instance = storeSchema;
        
        String expResult = instance.getKeyId(testContext) + 
                instance.getKeyId(part1) + instance.getKeyId(part2);
        
        String result = instance.buildKey(part1, part2, testContext);
        assertEquals(expResult, result);
    }

    /**
     * Test of buildKey method, of class MemcachedStoreSchema.
     */
    @Test
    public void testBuildKey_Value_Resource() {
        System.out.println("buildKey");
        
        MemcachedValue part1 = testSubj;
        
        MemcachedStoreSchema instance = storeSchema;
        
        String expResult = instance.getKeyId(testContext) + 
                instance.getKeyId(part1);
        
        String result = instance.buildKey(part1, testContext);
        assertEquals(expResult, result);
    }

    /**
     * Test of addToIndexContent method, of class MemcachedStoreSchema.
     */
    @Test
    public void testAddToIndexContent() throws Exception {
        System.out.println("addToIndexContent");
        
        MemcachedValue part1 = testSubj;
        MemcachedValue part2 = testPred;
        MemcachedValue obj = testObj;
        
        MemcachedStoreSchema instance = storeSchema;
        
        String key = instance.buildKey(part1, part2, testContext);
        String val = instance.encodeKey(obj); 
        
        boolean expResult = true;
        boolean result = instance.addToIndexContent(key, val);
        assertEquals(expResult, result);
    }

    /**
     * Test of isKeyPrefix method, of class MemcachedStoreSchema.
     */
    @Test
    public void testIsKeyPrefix() {
        System.out.println("isKeyPrefix");
        
        MemcachedStoreSchema instance = storeSchema;
        
        assertTrue( instance.isKeyPrefix('U') );
        assertTrue( instance.isKeyPrefix('B') );
        assertTrue( instance.isKeyPrefix('L') );
        assertFalse( instance.isKeyPrefix('u') );
        assertFalse( instance.isKeyPrefix('p') );
    }

    /**
     * Test of removeFromIndexContent method, of class MemcachedStoreSchema.
     */
    @Test
    public void testRemoveFromIndexContent() throws Exception {
        System.out.println("removeFromIndexContent");
        
        MemcachedValue part1 = testSubj;
        MemcachedValue part2 = testPred;
        MemcachedValue obj = testObj;
        
        MemcachedStoreSchema instance = storeSchema;
        
        String key = instance.buildKey(part1, part2, testContext);
        String val = instance.encodeKey(obj); 
        int keyLen = 2;
        
        boolean expResult = true;
        boolean result = instance.removeFromIndexContent(key, val, keyLen);
        assertEquals(expResult, result);
    }

    /**
     * Test of getCounter method, of class MemcachedStoreSchema.
     */
    @Test
    public void testGetCounter() {
        System.out.println("getCounter");
        
        MemcachedStoreSchema instance = storeSchema;
        
        Long expResult = 1L;
        
        Long result = instance.incrCounter(COUNT1, 1);
        assertEquals(expResult, result);
        
        result = instance.getCounter(COUNT1);
        assertEquals(expResult, result);
        
        instance.incrCounter(COUNT1, 1);
       
        expResult = 2L;
        result = instance.getCounter(COUNT1);
        assertEquals(expResult, result);
        
        expResult = 0L;
        result = instance.decrCounter(COUNT1, 2);
        assertEquals(expResult, result);
    }


    /**
     * Test of getLessIndexContentKey method, of class MemcachedStoreSchema.
     */
    @Test
    public void testGetLessIndexContentKey() {
        System.out.println("getLessIndexContentKey");
        
        MemcachedStoreSchema instance = storeSchema;
        String result = instance.getLessIndexContentKey(testSubj, testPred, testObj, testContext);
        assertNotNull(result);
    }

    /**
     * Test of getLessIndexSearchPattern method, of class MemcachedStoreSchema.
     */
    @Test
    public void testGetLessIndexSearchPattern() {
        System.out.println("getLessIndexSearchPattern");
        
        MemcachedStoreSchema instance = storeSchema;
        MemcachedStatementIterator result = 
                instance.getLessIndexSearchPattern(testSubj, testPred, testObj, testContext);
        assertNotNull(result);
    }

    /**
     * Test of addValue method, of class MemcachedStoreSchema.
     */
    @Test
    public void testAddValue() {
        System.out.println("addValue");
        
        MemcachedValue val = testObj;
        
        MemcachedStoreSchema instance = storeSchema;
        String expResult = instance.getKeyId(val);
        String result = instance.addValue(val);
        assertEquals(expResult, result);
    }

    /**
     * Test of existsValue method, of class MemcachedStoreSchema.
     */
    @Test
    public void testExistsValue() {
        System.out.println("existsValue");
        
        MemcachedStoreSchema instance = storeSchema;
        
        assertNotNull( instance.existsValue(testObj) );
        
        assertNull( instance.existsValue(new LiteralImpl("ABCDE")) );
    }

    /**
     * Test of removeValue method, of class MemcachedStoreSchema.
     */
    @Test
    public void testRemoveValue() {
        System.out.println("removeValue");
        
        MemcachedStoreSchema instance = storeSchema;
        MemcachedLiteral obj = new MemcachedLiteral("to remove!");
        instance.addValue( obj );
        
        assertTrue( instance.removeValue( obj ) );
    }

    /**
     * Test of getModelValue method, of class MemcachedStoreSchema.
     */
    @Test
    public void testGetModelValue() {
        System.out.println("getModelValue");
        
        MemcachedStoreSchema instance = storeSchema;
        String key = instance.getKeyId( testSubj );
        
        Value result = instance.getModelValue(key);
        assertTrue(result instanceof URI);
        assertFalse( ((URI)result).getNamespace().isEmpty() );
        assertFalse( ((URI)result).getLocalName().isEmpty() );
    }

    /**
     * Test of addStatement method, of class MemcachedStoreSchema.
     */
    @Test
    public void testAddStatement() throws Exception {
        System.out.println("addStatement");
        
        MemcachedStoreSchema instance = storeSchema;
        
        assertTrue( instance.addStatement(testSubj, testPred, testObj, testContext) );
    }

    /**
     * Test of hasStatement method, of class MemcachedStoreSchema.
     */
    @Test
    public void testHasStatement() throws Exception {
        System.out.println("hasStatement");
        
        MemcachedStoreSchema instance = storeSchema;
        
        assertTrue( instance.hasStatement(testSubj, testPred, testObj, testContext) );
        
        assertFalse( instance.hasStatement(testSubj, testPred, new MemcachedLiteral("other"), testContext) );
        assertFalse( instance.hasStatement(testSubj, null, new MemcachedLiteral("other"), testContext) );
        
        assertTrue( instance.hasStatement(testSubj, testPred, null, testContext) );
        assertTrue( instance.hasStatement(testSubj, null, testObj, testContext) );
        assertTrue( instance.hasStatement(null, testPred, testObj, testContext) );
        assertTrue( instance.hasStatement(null, null, testObj, testContext) );
        
    }

    /**
     * Test of removeStatement method, of class MemcachedStoreSchema.
     */
    @Test
    public void testRemoveStatement() throws Exception {
        System.out.println("removeStatement");
        
        Statement st = new ContextStatementImpl(testSubj,testPred,testObj,testContext);
        
        MemcachedStoreSchema instance = storeSchema;
        
        assertTrue( instance.removeStatement(st) );
    }

//    /**
//     * Test of getNumStatements method, of class MemcachedStoreSchema.
//     */
//    @Test
//    public void testGetNumStatements() {
//        System.out.println("getNumStatements");
//        MemcachedStoreSchema instance = null;
//        long expResult = 0L;
//        long result = instance.getNumStatements();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCount method, of class MemcachedStoreSchema.
//     */
//    @Test
//    public void testGetCount() {
//        System.out.println("getCount");
//        Value val = null;
//        Resource context = null;
//        MemcachedStoreSchema instance = null;
//        Long expResult = null;
//        Long result = instance.getCount(val, context);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
