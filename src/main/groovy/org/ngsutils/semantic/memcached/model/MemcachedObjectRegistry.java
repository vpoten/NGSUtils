/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ngsutils.semantic.memcached.model;

import java.util.TreeMap;
import org.openrdf.sail.memory.model.WeakObjectRegistry;

/**
 * An object registry that generates auto increment IDs
 * 
 * @author victor
 */
public class MemcachedObjectRegistry<E> extends WeakObjectRegistry<E> {
    
    protected TreeMap<E,Integer> ids = new TreeMap<E,Integer>();
    protected TreeMap<Integer,E> reverseIds = new TreeMap<Integer,E>();
    protected int current = 0;
    
    public MemcachedObjectRegistry() {
        super();
    }
    
    @Override
    public boolean add(E object)
    {
        setId(object);
        return super.add(object);
    }
    
    /**
     * 
     * @param object
     * @return 
     */
    public Integer getId(E object){
        return this.ids.get(object);
    }
    
    /**
     * 
     * @param id
     * @return 
     */
    public Object getObject(int id){
        return this.reverseIds.get(id);
    }
    
    /**
     * 
     * @param object 
     */
    synchronized protected void setId(E object){
        if( this.ids.containsKey(object) ) {
            return;
        }
        
        this.ids.put(object, current);
        this.reverseIds.put(current, object);
        this.current++;
    }
}
