/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.memcached;

import info.aduna.concurrent.locks.Lock;
import info.aduna.concurrent.locks.LockingIteration;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.ngsutils.semantic.memcached.model.MemcachedResource;
import org.ngsutils.semantic.memcached.model.MemcachedURI;
import org.ngsutils.semantic.memcached.model.MemcachedValue;
import org.ngsutils.semantic.memcached.model.MemcachedValueFactory;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.IterativeEvaluationOptimizer;
import org.openrdf.query.algebra.evaluation.impl.OrderLimitOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryModelNormalizer;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailReadOnlyException;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;


/**
 *
 * @author victor
 */
public class MemcachedStoreConnection extends NotifyingSailConnectionBase {
	
    protected final MemcachedStore store;
    
    /**
     * The exclusive transaction lock held by this connection during
     * transactions.
     */
    private Lock txnLock;

    /**
     * A statement list read lock held by this connection during transactions.
     * Keeping this lock prevents statements from being removed from the main
     * statement list during transactions.
     */
    private Lock txnStLock;

    
    public MemcachedStoreConnection(MemcachedStore aStore){
        super(aStore);
        store = aStore;
    }
    
    
    protected CloseableIteration<BindingSet, QueryEvaluationException> evaluateInternal(
                    TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred)
            throws SailException
    {

        logger.trace("Incoming query model:\n{}", tupleExpr);

        // Clone the tuple expression to allow for more aggresive optimizations
        tupleExpr = tupleExpr.clone();

        if (!(tupleExpr instanceof QueryRoot)) {
                // Add a dummy root node to the tuple expressions to allow the
                // optimizers to modify the actual root node
                tupleExpr = new QueryRoot(tupleExpr);
        }

        Lock stLock = store.getStatementsReadLock();
        boolean releaseLock = true;

        try {
            TripleSource tripleSource = new MemcachedTripleSource(includeInferred);
            EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource, dataset);

            new BindingAssigner().optimize(tupleExpr, dataset, bindings);
            new ConstantOptimizer(strategy).optimize(tupleExpr, dataset, bindings);
            new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
            new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset, bindings);
            new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
            new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);
            new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
            new QueryJoinOptimizer(new MemcachedEvaluationStatistics()).optimize(tupleExpr, dataset, bindings);
            ////new SubSelectJoinOptimizer().optimize(tupleExpr, dataset, bindings);
            new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
            new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
            new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);

            logger.trace("Optimized query model:\n{}", tupleExpr);

            CloseableIteration<BindingSet, QueryEvaluationException> iter;
            iter = strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());
            iter = new LockingIteration<BindingSet, QueryEvaluationException>(stLock, iter);
            releaseLock = false;
            return iter;
        }
        catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
        finally {
            if (releaseLock) {
                stLock.release();
            }
        }
    }

   
    protected void closeInternal()
            throws SailException
    {
            // do nothing
    }

   
    protected CloseableIteration<Resource, SailException> getContextIDsInternal()
            throws SailException
    {
        // Note: we can't do this in a streaming fashion due to concurrency
        // issues; iterating over the set of URIs or bnodes while another thread
        // adds statements with new resources would result in
        // ConcurrentModificationException's (issue SES-544).

        // Create a list of all resources that are used as contexts
        ArrayList<Resource> contextIDs = new ArrayList<Resource>(32);

        Lock stLock = store.getStatementsReadLock();

        try {
            MemcachedValueFactory valueFactory = (MemcachedValueFactory) store.getValueFactory();

            synchronized (valueFactory) {
                for (Resource memResource : valueFactory.getContexts()) {
                    contextIDs.add(memResource);
                }
            }
        }
        finally {
            stLock.release();
        }

        return new CloseableIteratorIteration<Resource, SailException>(contextIDs.iterator());
    }


    @Override
    protected CloseableIteration<Statement, SailException> getStatementsInternal(Resource subj,
                    URI pred, Value obj, boolean includeInferred, Resource... contexts)
            throws SailException
    {
        
        Lock stLock = store.getStatementsReadLock();
        boolean releaseLock = true;

        try {

            CloseableIteration<Statement, SailException> iter;
            iter = store.createStatementIterator(subj, pred, obj, !includeInferred, contexts);
            iter = new LockingIteration<Statement, SailException>(stLock, iter);
            releaseLock = false;
            return iter;
        }
        finally {
            if (releaseLock) {
                stLock.release();
            }
        }
    }

    public boolean hasStatement(Resource subj, URI pred, Value obj, boolean includeInferred,
                    Resource... contexts)
            throws SailException
    {
            Lock stLock = store.getStatementsReadLock();

            try {
                return store.hasStatement(subj, pred, obj, !includeInferred, contexts);
            }
            finally {
                stLock.release();
            }
    }

    
    protected long sizeInternal(Resource... contexts)
            throws SailException
    {
        return store.getNumStatements();
    }

    
    protected CloseableIteration<Namespace, SailException> getNamespacesInternal()
            throws SailException
    {
            return new CloseableIteratorIteration<Namespace, SailException>(store.getNamespaceStore().iterator());
    }

    
    protected String getNamespaceInternal(String prefix)
            throws SailException
    {
            return store.getNamespaceStore().getNamespace(prefix);
    }

    
    protected void startTransactionInternal()
            throws SailException
    {
        if (!store.isWritable()) {
            throw new SailReadOnlyException("Unable to start transaction: data file is locked or read-only");
        }

        txnStLock = store.getStatementsReadLock();
        boolean releaseLocks = true;

        try {
            // Prevent concurrent transactions by acquiring an exclusive txn lock
            txnLock = store.getTransactionLock();

            try {
                store.startTransaction();
                releaseLocks = false;
            }
            finally {
                if (releaseLocks) {
                    txnLock.release();
                }
            }
        }
        finally {
            if (releaseLocks) {
                txnStLock.release();
            }
        }
    }

    
    protected void commitInternal()
            throws SailException
    {
        store.commit();
        txnLock.release();
        txnStLock.release();
    }

    
    protected void rollbackInternal()
            throws SailException
    {
        try {
                store.rollback();
        }
        finally {
                txnLock.release();
                txnStLock.release();
        }
    }

    @Override
    protected void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts)
            throws SailException
    {
            addStatementInternal(subj, pred, obj, true, contexts);
    }

    /**
     * Adds the specified statement to this MemoryStore.
     * 
     * @throws SailException
     */
    protected boolean addStatementInternal(Resource subj, URI pred, Value obj, boolean explicit,
                    Resource... contexts)
            throws SailException
    {
        assert txnStLock.isActive();
        assert txnLock.isActive();

        Statement st = null;

        if (contexts.length == 0) {
            st = store.addStatement(subj, pred, obj, null, explicit);
            if (st != null) {
                notifyStatementAdded(st);
            }
        }
        else {
            for (Resource context : contexts) {
                st = store.addStatement(subj, pred, obj, context, explicit);
                if (st != null) {
                    notifyStatementAdded(st);
                }
            }
        }

        // FIXME: this return type is invalid in case multiple contexts were
        // specified
        return st != null;
    }

    @Override
    protected void removeStatementsInternal(Resource subj, URI pred, Value obj, Resource... contexts)
            throws SailException
    {
            removeStatementsInternal(subj, pred, obj, true, contexts);
    }


    
    protected void clearInternal(Resource... contexts)
            throws SailException
    {
            removeStatementsInternal(null, null, null, true, contexts);
    }


    /**
     * Removes the statements that match the specified pattern of subject,
     * predicate, object and context.
     * 
     * @param subj
     *        The subject for the pattern, or <tt>null</tt> for a wildcard.
     * @param pred
     *        The predicate for the pattern, or <tt>null</tt> for a wildcard.
     * @param obj
     *        The object for the pattern, or <tt>null</tt> for a wildcard.
     * @param explicit
     *        Flag indicating whether explicit or inferred statements should be
     *        removed; <tt>true</tt> removes explicit statements that match the
     *        pattern, <tt>false</tt> removes inferred statements that match the
     *        pattern.
     * @throws SailException
     */
    protected boolean removeStatementsInternal(Resource subj, URI pred, Value obj, boolean explicit,
                    Resource... contexts)
            throws SailException
    {
        
        CloseableIteration<Statement, SailException> stIter = store.createStatementIterator(
                        subj, pred, obj, explicit, contexts);
        return removeIteratorStatements(stIter, explicit);
    }

    protected boolean removeIteratorStatements(CloseableIteration<Statement, SailException> stIter,
                    boolean explicit)
            throws SailException
    {
        
        boolean statementsRemoved = false;

        try {
            while (stIter.hasNext()) {
                Statement st = stIter.next();

                if (store.removeStatement(st, explicit)) {
                    statementsRemoved = true;
                    notifyStatementRemoved(st);
                }
            }
        }
        finally {
            stIter.close();
        }

        return statementsRemoved;
    }

    
    protected void setNamespaceInternal(String prefix, String name)
            throws SailException
    {
            // FIXME: changes to namespace prefixes not isolated in transactions yet
            try {
                    store.getNamespaceStore().setNamespace(prefix, name);
            }
            catch (IllegalArgumentException e) {
                    throw new SailException(e.getMessage());
            }
    }

    
    protected void removeNamespaceInternal(String prefix)
            throws SailException
    {
            // FIXME: changes to namespace prefixes not isolated in transactions yet
            store.getNamespaceStore().removeNamespace(prefix);
    }

    
    protected void clearNamespacesInternal()
            throws SailException
    {
            // FIXME: changes to namespace prefixes not isolated in transactions yet
            store.getNamespaceStore().clear();
    }

    /*-----------------------------*
     * Inner class MemTripleSource *
     *-----------------------------*/

    /**
     * Implementation of the TripleSource interface from the Sail Query Model
     */
    protected class MemcachedTripleSource implements TripleSource {

            protected final boolean includeInferred;

            public MemcachedTripleSource(boolean includeInferred) {
                    this.includeInferred = includeInferred;
            }

            public CloseableIteration<Statement, QueryEvaluationException> getStatements(Resource subj,
                            URI pred, Value obj, Resource... contexts)
            {
                    return store.createStatementIterator(subj, pred, obj, !includeInferred, contexts);
            }

            public ValueFactory getValueFactory() {
                    return store.getValueFactory();
            }
    } // end inner class MemcachedTripleSource

    /*-------------------------------------*
     * Inner class MemcachedEvaluationStatistics *
     *-------------------------------------*/

    /**
     * Uses the MemcachedStore's statement sizes to give cost estimates based on the
     * size of the expected results. This process could be improved with
     * repository statistics about size and distribution of statements.
     * 
     * @author Victor
     */
    protected class MemcachedEvaluationStatistics extends EvaluationStatistics {

        @Override
        protected CardinalityCalculator createCardinalityCalculator() {
                return new MemCardinalityCalculator();
        }

        protected class MemCardinalityCalculator extends CardinalityCalculator {

            @Override
            public double getCardinality(StatementPattern sp) {

                Value subj = getConstantValue(sp.getSubjectVar());
                if (!(subj instanceof Resource)) {
                        // can happen when a previous optimizer has inlined a comparison operator. 
                        // this can cause, for example, the subject variable to be equated to a literal value. 
                        // See SES-970 / SES-998
                        subj = null;
                }
                Value pred = getConstantValue(sp.getPredicateVar());
                if (!(pred instanceof URI)) {
                        //  can happen when a previous optimizer has inlined a comparison operator. See SES-970 / SES-998
                        pred = null;
                }
                Value obj = getConstantValue(sp.getObjectVar());
                Value context = getConstantValue(sp.getContextVar());
                if (!(context instanceof Resource)) {
                        //  can happen when a previous optimizer has inlined a comparison operator. See SES-970 / SES-998
                        context = null;
                }

                MemcachedValueFactory valueFactory = (MemcachedValueFactory) store.getValueFactory();

                // Perform look-ups for value-equivalents of the specified values
                MemcachedResource memSubj = valueFactory.getMemResource((Resource)subj);
                MemcachedURI memPred = valueFactory.getMemURI((URI)pred);
                MemcachedValue memObj = valueFactory.getMemValue(obj);
                MemcachedResource memContext = valueFactory.getMemResource((Resource)context);

                if (subj != null && memSubj == null || pred != null && memPred == null || obj != null
                                && memObj == null || context != null && memContext == null)
                {
                        // non-existent subject, predicate, object or context
                        return 0.0;
                }

                // Search for the smallest list that can be used by the iterator
                List<Long> listSizes = new ArrayList<Long>(4);
                if (memSubj != null) {
                    listSizes.add( valueFactory.getStoreSchema().getCount(memSubj,memContext) );
                }
                if (memPred != null) {
                    listSizes.add( valueFactory.getStoreSchema().getCount(memPred,memContext) );
                }
                if (memObj != null) {
                    listSizes.add( valueFactory.getStoreSchema().getCount(memObj,memContext) );
                }
                if (memContext != null) {
                    listSizes.add(Long.MAX_VALUE);//dont use context
                }

                double lcardinality;

                if (listSizes.isEmpty()) {
                        // all wildcards
                        lcardinality = Long.MAX_VALUE;//dont use all wildcards
                }
                else {
                        lcardinality = Collections.min(listSizes);
                }

                return lcardinality;
            }

            protected Value getConstantValue(Var var) {
                if (var != null) {
                    return var.getValue();
                }

                return null;
            }
                
        }
    } // end inner class MemCardinalityCalculator

        
}

