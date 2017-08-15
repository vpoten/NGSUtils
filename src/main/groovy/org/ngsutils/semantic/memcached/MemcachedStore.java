/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.memcached;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.ReadPrefReadWriteLockManager;
import info.aduna.concurrent.locks.ReadWriteLockManager;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import org.ngsutils.semantic.memcached.config.MemcachedStoreSchema;
import org.ngsutils.semantic.memcached.model.*;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;

/**
 * An implementation of the Sail interface that stores its data in a memcached
 * server.
 * 
 * @author victor
 */
public class MemcachedStore extends SailBase {
	
    protected MemcachedClient memClient;
    protected MemcachedValueFactory valueFactory = new MemcachedValueFactory();
    protected MemcachedNamespaceStore namespaceStore = new MemcachedNamespaceStore();
    protected MemcachedStoreSchema storeSchema;
    
    /**
     * Lock manager used to give the snapshot cleanup thread exclusive access to
     * the statement list.
     */
    private final ReadWriteLockManager statementListLockManager = new ReadPrefReadWriteLockManager(
                    debugEnabled());

    /**
     * Lock manager used to prevent concurrent transactions.
     */
    private final ExclusiveLockManager txnLockManager = new ExclusiveLockManager(debugEnabled());
    
    
    /**
     *
     */
    public MemcachedStore(List servers) throws IOException{
        memClient = new MemcachedClient( AddrUtil.getAddresses(servers)  );
        storeSchema = new MemcachedStoreSchema(valueFactory, memClient);
    }
    
    
    /**
     * Initializes this repository. 
     * 
     * @throws SailException
     *         when initialization of the store failed.
     */
    @Override
    protected void initializeInternal() throws SailException {
        // nothing to do
    }
    
    
    protected void shutDownInternal() throws SailException {
        // nothing to do
    }
    
    /**
     * Checks whether this Sail object is writable.
     */
    public boolean isWritable() {
        return true;
    }
    
    public ValueFactory getValueFactory(){
        return valueFactory;
    }
    
    public MemcachedNamespaceStore getNamespaceStore(){
        return namespaceStore;
    }
    
   
    protected SailConnection getConnectionInternal() 
    throws SailException
    {
        return new MemcachedStoreConnection(this);
    }

    /**
     * 
     * @param subj
     * @param pred
     * @param obj
     * @param explicitOnly
     * @param contexts
     * @return 
     */
    protected boolean hasStatement(Resource subj, URI pred, Value obj, boolean explicitOnly,
                    Resource... contexts) throws SailException
    {
        MemcachedResource memSubj = valueFactory.getMemResource(subj);
        MemcachedURI memPred = valueFactory.getMemURI(pred);
        MemcachedValue memObj = valueFactory.getMemValue(obj);
        MemcachedResource memCtx = ( (contexts==null) ? valueFactory.getNullURI() : valueFactory.getMemResource(contexts[0]) );
        
        return storeSchema.hasStatement(memSubj, memPred, memObj, memCtx );
    }

    /**
     * Creates a StatementIterator that contains the statements matching the
     * specified pattern of subject, predicate, object, context. Inferred
     * statements are excluded when <tt>explicitOnly</tt> is set to <tt>true</tt>
     * . Statements from the null context are excluded when
     * <tt>namedContextsOnly</tt> is set to <tt>true</tt>.
     */
    protected <X extends Exception> CloseableIteration<Statement, X> createStatementIterator(
                    Resource subj, URI pred, Value obj, boolean explicitOnly, 
                    Resource... contexts)
    {
        if ( subj==null && pred==null && obj==null ) {
            // non permitted pattern
            return new EmptyIteration<Statement, X>();
        }
        
        // Perform look-ups for value-equivalents of the specified values
        MemcachedResource memSubj = valueFactory.getMemResource(subj);
        if (subj != null && memSubj == null) {
            // non-existent subject
            return new EmptyIteration<Statement, X>();
        }

        MemcachedURI memPred = valueFactory.getMemURI(pred);
        if (pred != null && memPred == null) {
            // non-existent predicate
            return new EmptyIteration<Statement, X>();
        }

        MemcachedValue memObj = valueFactory.getMemValue(obj);
        if (obj != null && memObj == null) {
            // non-existent object
            return new EmptyIteration<Statement, X>();
        }

        ArrayList<MemcachedResource> contextsList = new ArrayList<MemcachedResource>();

        if (contexts.length == 1 && contexts[0] != null) {
            MemcachedResource memContext = valueFactory.getMemResource(contexts[0]);
            if (memContext == null) {
                // non-existent context
                return new EmptyIteration<Statement, X>();
            }

            contextsList.add( memContext );
        }
        else if (contexts.length > 1) {
            Set<MemcachedResource> contextSet = new LinkedHashSet<MemcachedResource>(2 * contexts.length);

            for (Resource context : contexts) {
                MemcachedResource memContext = valueFactory.getMemResource(context);
                if (context == null || memContext != null) {
                        contextSet.add(memContext);
                }
            }

            if (contextSet.isEmpty()) {
                // no known contexts specified
                return new EmptyIteration<Statement, X>();
            }

            contextsList.addAll(contextSet);
        }

        return storeSchema.getLessIndexSearchPattern( memSubj, memPred, memObj, 
                contextsList.get(0) );
    }

    
    protected Statement addStatement(Resource subj, URI pred, Value obj, Resource context, boolean explicit)
            throws SailException
    {
        // Get or create Values for the operands
        MemcachedResource memSubj = valueFactory.getOrCreateMemResource(subj);
        MemcachedURI memPred = valueFactory.getOrCreateMemURI(pred);
        MemcachedValue memObj = valueFactory.getOrCreateMemValue(obj);
        MemcachedResource memContext = (context == null) ? valueFactory.getNullURI() : valueFactory.getOrCreateMemResource(context);

        if ( storeSchema.hasStatement(memSubj, memPred, memObj, memContext) ){
            // statement is already present
            return null;
        }
        
        if( memContext!=null ){
            //add to contexts registry
            valueFactory.addToContexts(memContext);
        }

        // completely new statement
        storeSchema.addStatement(memSubj, memPred, memObj, memContext);

        assert storeSchema.hasStatement(memSubj, memPred, memObj, memContext);

        return new ContextStatementImpl(memSubj, memPred, memObj, memContext);
    }

    
    protected boolean removeStatement(Statement st, boolean explicit) throws SailException {
        return storeSchema.removeStatement(st);
    }

    protected void startTransaction() throws SailException {
        // nothing to do
    }

    protected void commit() throws SailException {
        // nothing to do
    }

    
    protected void rollback() throws SailException {
        // nothing to do
    }

    
    protected Lock getStatementsReadLock()
            throws SailException {
        try {
            return statementListLockManager.getReadLock();
        }
        catch (InterruptedException e) {
            throw new SailException(e);
        }
    }

    
    protected Lock getTransactionLock()
            throws SailException {
        try {
            return txnLockManager.getExclusiveLock();
        }
        catch (InterruptedException e) {
            throw new SailException(e);
        }
    }

    /**
     * 
     * @return 
     */
    long getNumStatements() {
        return storeSchema.getNumStatements();
    }

}

