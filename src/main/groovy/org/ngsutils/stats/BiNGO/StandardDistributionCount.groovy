/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.stats.BiNGO

import org.ngsutils.semantic.NGSDataResource

/**
 * ************************************************************
 * DistributionCount.java   Steven Maere & Karel Heymans (c) March 2005
 * ----------------------
 * <p/>
 * class that counts the small n, big N, small x, big X which serve as input for the statistical tests.
 * *************************************************************
 */


public class StandardDistributionCount implements DistributionCount {

    /*--------------------------------------------------------------
    FIELDS.
    --------------------------------------------------------------*/

    /**
     * the annotation.
     */
    NGSDataResource dataResource
    
    /**
     * HashSet of selected nodes
     */
    Set selectedNodes;
    /**
     * HashSet of reference nodes
     */
    Set refNodes;
    /**
     * hashmap with values of small n ; keys GO labels.
     */
    Map mapSmallN;
    /**
     * hashmap with values of small x ; keys GO labels.
     */
    Map mapSmallX;
    /**
     * hashmap with values of big N.
     */
    Map mapBigN;
    /**
     * hashmap with values of big X.
     */
    Map mapBigX;
    
    

    /*--------------------------------------------------------------
    CONSTRUCTOR.
    --------------------------------------------------------------*/

    public StandardDistributionCount( NGSDataResource res,
                             Set selectedNodes,
                             Set refNodes ) {
        this.dataResource = res
        this.selectedNodes = selectedNodes
        this.refNodes = refNodes
    }

    /*--------------------------------------------------------------
      METHODS.
    --------------------------------------------------------------*/

    /**
     * method for compiling GO classifications for given node
     * 
     * @param: node, a String with a feature URI
     */
    public Set getNodeClassifications(String node) {
        return dataResource.getGOTerms(node)
    }


    /**
     * method for making the hashmap for small n.
     */
    public void countSmallN() {
        mapSmallN = this.count(refNodes);
    }


    /**
     * method for making the hashmap for the small x.
     */
    public void countSmallX() {
        mapSmallX = this.count(selectedNodes);
    }


    /**
     * method that counts for small n and small x.
     */
    public Map count(Set nodes) {

        HashMap map = new HashMap();

        nodes.each { node->
            HashSet classifications = getNodeClassifications(node);

            // puts the classification counts in a map
            classifications.each { id->
                if (map.containsKey(id)) 
                    map.put(id, map.get(id) + 1)
                else 
                    map.put(id, 1)
            }
        }

        return map;
    }

    /**
     * counts big N. unclassified nodes are not counted ; no correction for function_unknown nodes (yet)(requires user input)
     */
    public void countBigN() {
        mapBigN = new HashMap();
        int bigN = refNodes.size();
        
        refNodes.each {
            if ( !getNodeClassifications(it) )
                bigN--
        }
        
        mapSmallX.keySet().each{ id->
            mapBigN.put(id, bigN);
        }
    }

    /**
     * counts big X. unclassified nodes are not counted ; no correction for function_unknown nodes (yet)(requires user input)
     */
    public void countBigX() {
        mapBigX = new HashMap();
        int bigX = selectedNodes.size();
        
        selectedNodes.each {
            if ( !getNodeClassifications(it) )
                bigX--
        }
        
        mapSmallX.keySet().each { id->
            mapBigX.put(id, bigX);
        }
    }

    
    
    public void calculate() {
        countSmallX();
        countSmallN();
        countBigX();
        countBigN();
    }
    
    
    public Map getTestMap() {
        return mapSmallX;
    }
    
}

