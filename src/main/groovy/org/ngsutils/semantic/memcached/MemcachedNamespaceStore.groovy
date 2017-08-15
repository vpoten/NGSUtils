/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic.memcached

import org.openrdf.model.impl.NamespaceImpl

/**
 * Based on MemNamespaceStore
 * 
 * @author victor
 */
class MemcachedNamespaceStore implements Iterable<NamespaceImpl>  {
    /*-----------*
     * Variables *
     *-----------*/

    /**
     * Map storing namespace information by their prefix.
     */
    private final Map<String, NamespaceImpl> namespacesMap = new LinkedHashMap<String, NamespaceImpl>(16);

    /*---------*
     * Methods *
     *---------*/

    public String getNamespace(String prefix) {
            String result = null;
            NamespaceImpl namespace = namespacesMap.get(prefix);
            if (namespace != null) {
                    result = namespace.getName();
            }
            return result;
    }

    public void setNamespace(String prefix, String name) {
            NamespaceImpl ns = namespacesMap.get(prefix);

            if (ns != null) {
                    ns.setName(name);
            }
            else {
                    namespacesMap.put(prefix, new NamespaceImpl(prefix, name));
            }
    }

    public void removeNamespace(String prefix) {
            namespacesMap.remove(prefix);
    }

    public Iterator<NamespaceImpl> iterator() {
            return namespacesMap.values().iterator();
    }

    public void clear() {
            namespacesMap.clear();
    }
}

