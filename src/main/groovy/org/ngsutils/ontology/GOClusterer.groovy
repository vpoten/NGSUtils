/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.ontology;

import org.ngsutils.AnnotationDB
import org.ngsutils.maths.weka.KernelFactory
import org.ngsutils.maths.weka.GOFMBDistance
import org.ngsutils.maths.weka.clusterer.AbstractKernelFuzzyClusterer
import org.ngsutils.semantic.query.GOQueryUtils
import org.ngsutils.semantic.query.GeneQueryUtils
import org.ngsutils.semantic.rdfutils.SimpleGraph
import org.openrdf.model.URI
import weka.core.Attribute
import weka.core.DistanceFunction
import weka.core.FastVector
import weka.core.Instance
import weka.core.Instances

/**
 *
 * @author victor
 */
class GOClusterer {

    GOFMBDistance distances
    AbstractKernelFuzzyClusterer clusterer
    

//    /**
//     *
//     * @param distances :  GOPairDistances object (distance matrix)
//     * @param linkType : link type, see HierarchicalCluster doc.
//     * @param numberClusters : 1 normally
//     * @throws Exception
//     */
//    public GOClusterer( GOPairDistances distances, String linkType, int numberClusters )
//            throws Exception {
//        this.distances=distances;
//
//        //create the instances
//        Instances dataSet=null;
//        
//        //set up attributes
//        ArrayList<Attribute> atts = new ArrayList<Attribute>();
//        atts.add(new Attribute("label",(ArrayList<String>) null));//string
//        atts.add(new Attribute("index"));//numeric
//
//        dataSet=new Instances( linkType, atts, 0);
//
//        for(String name : this.distances.getGeneNames()){
//            double [] vals=new double [2];
//            vals[0]=dataSet.attribute(0).addStringValue(name);
//            vals[1]=this.distances.getGeneIndex(name);
//
//            dataSet.add( new Instance(1.0, vals) );
//        }
//
//        String [] options = ['-L',linkType] as String []
//
//        this.clusterer = new HierarchicalClusterer2();// new instance of clusterer
//        this.clusterer.setOptions(options);// set the options
//        
//        this.clusterer.setDistanceFunction( new GODistanceFunction(this.distances) );
//
//        if( numberClusters>0 )
//            this.clusterer.setNumClusters(numberClusters);
//
//        this.clusterer.buildClusterer(dataSet); // build the clusterer
//
//    }
    
    /**
     *
     * @param graph : semantic data graph
     * @param workDir
     * @param taxId : taxonomy id; i.e. 9606
     * @param dataset : weka instances
     */
    public GOClusterer(SimpleGraph graph, String workDir, String taxId, Instances dataset) {
        
        // get GOA file
        def urlSrc = AnnotationDB.goAssocUrl(taxId)
        def name = urlSrc.substring( urlSrc.lastIndexOf('/')+1 )
        def goaFile = "${workDir}${taxId}/${name}"
        
        // create GO manager
        GOManager goManager = new GOManager( graph )
        goManager.calculateProbTerms(goaFile)
        goManager.setEcodeFactors( GOEvidenceCodes.ecodeFactorsSet1 )
        
        GOQueryUtils goQuery = new GOQueryUtils(graph)
        GeneQueryUtils geneQuery = new GeneQueryUtils(graph)
        def annotationMap = [:] as TreeMap
        
        // create annotation map using attribute names (genes)
        int classIdx = dataset.classIndex()
        
        (0..dataset.numAttributes()-1).each{ i ->
            def att = dataset.attribute(i)
            if (i != classIdx) {
                def uri = geneQuery.getGeneByName(att.name())
                def terms = goQuery.getTerms(uri)
                //create annotation
                def annot = new OntologyAnnotation(product:att.name(), terms:(terms ?: [] as Set))
                annotationMap[label] = annot
            }
        }
        
        // TODO
        
        DistanceFunction distFunc = new GOFMBDistance(goManager, annotationMap)
        
        def kernel = KernelFactory.linear(instances, distFunc)
        clusterer.setKernel(kernel)
        
        clusterer.buildClusterer(instances)
    }

}
