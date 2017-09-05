/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils

import org.ngsutils.semantic.LinkedLifeDataFactory
import org.ngsutils.ontology.GOManager
import org.ngsutils.semantic.NGSDataResource
import org.ngsutils.semantic.query.GeneQueryUtils
import weka.core.Attribute
import weka.core.FastVector
import weka.core.Instance
import weka.core.Instances

import org.ngsutils.ontology.GOClusterer

/**
 *
 * @author victor
 */
class GOClustererFactory {
    
    
    public static void doGOCluster(data, workDir, taxonomyId='9606') {
        // load semantic data
        def graph = LinkedLifeDataFactory.loadRepository(LinkedLifeDataFactory.LIST_BASIC_GO, [taxonomyId], workDir)
        def clusterer = new GOClusterer(graph, workDir, taxonomyId,  GOClustererFactory.createInstances(data))
    }
    
    private static Instances createInstances(data) {
        data = data.sort()
        
        def attributes = data.collect{
            FastVector labels = new FastVector();
            labels.addElement("0");
            labels.addElement("1");
            return new Attribute(it, labels)
        }
        
        Instances dataset = new Instances("features-go", attributes, 0);
        
        data.eachWithIndex{ val, i->
            def values = new double [data.size()]
            
            (0..data.size()-1).each{
                values[it] = dataset.attribute(it).indexOf(it==i ? "1" : "0");
            }
            
            dataset.add(new Instance(1.0, values))
        }
        
        return dataset
    }
}
