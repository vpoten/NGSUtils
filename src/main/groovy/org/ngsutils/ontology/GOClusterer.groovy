/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.ontology;

import org.ngsutils.AnnotationDB
import org.ngsutils.semantic.LinkedLifeDataFactory
import org.ngsutils.maths.weka.KernelFactory
import org.ngsutils.maths.weka.GOFMBDistance
import org.ngsutils.maths.weka.clusterer.AbstractKernelFuzzyClusterer
import org.ngsutils.maths.weka.clusterer.CentralClustererUtils
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
     * @param workDir
     * @param taxId : taxonomy id; i.e. 9606
     * @param data: list with genes
     */
    public GOClusterer(String workDir, String taxId, data, namespaces) {
        def dataset = GOClusterer.createInstances(data)
        
        // load semantic data
        def graph = LinkedLifeDataFactory.loadRepository(LinkedLifeDataFactory.LIST_BASIC_GO, [taxId], workDir)
        
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
                def label = att.name()
                def uri = geneQuery.getGeneByName(label)
                def terms = goQuery.getTerms(uri)
                
                // filter terms in namespaces
                terms = terms.findAll{goManager.getNamespace(it) in namespaces}
                
                //create annotation
                def annot = new OntologyAnnotation(product:label, terms:(terms ?: [] as Set))
                annotationMap[label] = annot
            }
        }
        
        // create clusterer class
        clusterer = CentralClustererUtils.buildClusterer(CentralClustererUtils.CLUST_KFCM)
        
        DistanceFunction distFunc = new GOFMBDistance(goManager, annotationMap)
        clusterer.distances = KernelFactory.calcDistMatrix(dataset, distFunc)
        printDistances(data.sort(), clusterer.distances)
        
        clusterer.buildClusterer(dataset)
    }
    
    private static void printDistances(labels, mat) {
        int n = mat.rowDimension
        
        for(int i=0; i<n; i++) {
            for(int j=i; j<n; j++) {
                System.out.println(labels[i] + ',' + labels[j] + ',' + mat.get(i,j))
            }
        }
    }

    /**
     *
     */
    private static Instances createInstances(data) {
        data = data.sort()
        
        def attributes = new FastVector()
        
        data.each{
            FastVector labels = new FastVector();
            labels.addElement("0");
            labels.addElement("1");
            attributes.addElement(new Attribute(it, labels))
        }
        
        Instances dataset = new Instances("features-go", attributes, 0);
        
        data.eachWithIndex{ val, i->
            def values = new double [data.size()]
            
            (0..data.size()-1).each{
                values[it] = dataset.attribute(it).indexOfValue(it==i ? "1" : "0");
            }
            
            dataset.add(new Instance(1.0, values))
        }
        
        return dataset
    }
}
