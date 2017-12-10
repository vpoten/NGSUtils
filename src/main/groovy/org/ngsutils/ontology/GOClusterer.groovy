/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.ontology;

import groovy.json.JsonSlurper
import org.ngsutils.Utils
import org.ngsutils.maths.weka.KernelFactory
import org.ngsutils.maths.weka.GOFMBDistance
import org.ngsutils.maths.weka.clusterer.AbstractKernelFuzzyClusterer
import org.ngsutils.maths.weka.clusterer.CentralClustererUtils
import org.ngsutils.maths.weka.clusterer.Silhouette
import org.ngsutils.semantic.NGSDataResource
import org.ngsutils.semantic.query.GOQueryUtils
import org.ngsutils.semantic.query.GeneQueryUtils
import org.ngsutils.stats.BiNGO.BingoAlgorithm
import org.ngsutils.stats.StatTestParams
import org.openrdf.model.URI
import weka.core.Attribute
import weka.core.DistanceFunction
import weka.core.FastVector
import weka.core.Instance
import weka.core.Instances
import weka.core.matrix.Matrix

/**
 *
 * @author victor
 */
class GOClusterer {
    GOClustererData goData
    def namespaces  // GeneOntology namespaces
    AbstractKernelFuzzyClusterer clusterer
    DistanceFunction distFunc
    Matrix distances
    Instances dataset
    List features
    private clusterType = CentralClustererUtils.CLUST_KFCM
    private int maxTermsPerGroup = 50
    private double enrichPVal = 0.05d  // GO enrichment p-value threshold
    

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
     * @param data: GOClustererData object (semantic data)
     * @param features: list with genes or map {label: group_genes}
     * @param namespaces: list with GO namespaces to include ("molecular_function", ...)
     * @param pvalThr: p value threshold for significance
     */
    public GOClusterer(GOClustererData data, features, _namespaces, pvalThr) {
        goData = data
        namespaces = _namespaces
        enrichPVal = pvalThr
        dataset = this.createInstances((features instanceof List) ? features : features.keySet())
      
        def annotationMap = (features instanceof List) ? buildSingleGeneAnnotation() : buildGroupGeneAnnotation(features)
        
        distFunc = new GOFMBDistance(goData.goManager, annotationMap)
        distances = KernelFactory.calcDistMatrix(dataset, distFunc)
    }
    
    /**
     * 
     *  @param features: list with genes or map {label: group_genes}
     *  @param simFile: JSON file with similarities
     */
    public GOClusterer(features, File simFile) {
        dataset = this.createInstances((features instanceof List) ? features : features.keySet())
        def simJSON = new JsonSlurper().parseText(simFile.text)
        
        distances = new Matrix(dataset.numInstances(), dataset.numInstances(), 0.0d)
        def sortedFeatures = ((features instanceof List) ? features : features.keySet()).sort()
        
        for(simObj in simJSON) {
            double val = 1.0d - simObj['similarity']
            int i = sortedFeatures.indexOf(simObj['product1']['name'])
            int j = sortedFeatures.indexOf(simObj['product2']['name'])
            
            if( i<0 || j<0 ) {
                continue
            }
            
            distances.set(i, j, val)
            distances.set(j, i, val)
        }
    }
    
    /**
     * write similarities between features to JSON file
     */ 
    public writeSimilarities(path) {
        distFunc.endLog(new File(path))
    }
    
    /**
     * 
     * create annotation map using attribute names (genes)
     */
    private buildSingleGeneAnnotation() {
        GOQueryUtils goQuery = new GOQueryUtils(goData.graph)
        GeneQueryUtils geneQuery = new GeneQueryUtils(goData.graph)
        def annotationMap = [:] as TreeMap
        
        int classIdx = dataset.classIndex()
        
        (0..dataset.numAttributes()-1).each{ i ->
            def att = dataset.attribute(i)
            if (i != classIdx) {
                def label = att.name()
                def uri = geneQuery.getGeneByName(label)
                def terms = goQuery.getTerms(uri)
                
                // filter terms in namespaces
                terms = terms.findAll{goData.goManager.getNamespace(it) in namespaces}
                
                //create annotation
                def annot = new OntologyAnnotation(id: label, product:label,
                    terms:(terms ?: [] as Set))
                annotationMap[label] = annot
            }
        }
        
        return annotationMap
    }
    
    /**
     * 
     */ 
    private buildGroupGeneAnnotation(geneGroups) {
        def enrichments = calcEnrichment(geneGroups)
        def annotationMap = [:] as TreeMap
        
        int classIdx = dataset.classIndex()
        def ontology = new FMBGOntologyWrap(goManager: goData.goManager)
        
        (0..dataset.numAttributes()-1).each{ i ->
            def att = dataset.attribute(i)
            if (i != classIdx) {
                def label = att.name()
                def terms = enrichments[label].correctionMap.keySet()
                
                // filter terms in namespaces
                terms = terms.findAll{goData.goManager.getNamespace(it) in namespaces}
                
                // remove redundant terms
                def redundant = terms.findAll{ontology.isAncestor(it, terms)}
                redundant.each{terms.remove(it)}
                
                // limit the max number of term annotations per instance
                // sort by enrichment p-value
                terms = terms.sort{enrichments[label].correctionMap[it]}.take(maxTermsPerGroup)
                
                //create annotation
                def annot = new OntologyAnnotation(id: label,
                    product: geneGroups[label], terms:(terms ?: [] as Set))
                annotationMap[label] = annot
            }
        }
        
        return annotationMap
    }
    
    /**
     * set Kernel Possibilistic C-Means clustering
     */ 
    public void setPossibilistic() {
        clusterType = CentralClustererUtils.CLUST_KPCM
    }
    
    /**
     * 
     * @param options: clusterer options (weka format)
     */ 
    public void runClusterer(options) {
        // create clusterer class
        clusterer = CentralClustererUtils.buildClusterer(clusterType)
        if(options) { clusterer.setOptions(options as String []) }
        clusterer.distances = distances
        clusterer.buildClusterer(dataset)
    }
    
    /**
     * to debug distances calc
     */ 
    private static void printDistances(labels, mat) {
        int n = mat.rowDimension
        
        for(int i=0; i<n; i++) {
            for(int j=i; j<n; j++) {
                System.out.println(labels[i] + ',' + labels[j] + ',' + mat.get(i,j))
            }
        }
    }
    
    /**
     * to debug clustering results
     */ 
    public void printDistributions() {
        (0..dataset.numInstances()-1).each{
            def inst = dataset.instance(it)
            def dist = clusterer.distributionForInstance(inst)
            println "${it}: ${dist}"
        }
    }

    /**
     * Create a dataset on the fly; each feature (gene) is an attribute of the dataset and
     * each pattern has a '1' for the attribute of its gene and '0' otherwise.
     * The generated dataset is valid for clustering of single gene patterns.
     */
    private Instances createInstances(data) {
        features = data.sort()
        
        def attributes = new FastVector()
        
        features.each{
            FastVector labels = new FastVector();
            labels.addElement("0");
            labels.addElement("1");
            attributes.addElement(new Attribute(it, labels))
        }
        
        Instances dataset = new Instances("features-go", attributes, 0);
        
        features.eachWithIndex{ val, i->
            def values = new double [features.size()]
            
            (0..features.size()-1).each{
                values[it] = dataset.attribute(it).indexOfValue(it==i ? "1" : "0");
            }
            
            dataset.add(new Instance(1.0, values))
        }
        
        return dataset
    }
    
    /**
     *
     */
    public static gridSearch(goClusterer, Map parameters, int numExecs, debug=false) {
        // default values for options:
        // ["-C","3","-lambda","2","-gamma","1","-K","0","-stdev","1.0"]
        def optionsValues = [
            '-C': (parameters['-C'] ?: ['3']),
            '-lambda': (parameters['-lambda'] ?: ['2']),
            '-gamma': (parameters['-gamma'] ?: ['1']),
            '-K': (parameters['-K'] ?: ['0']),
            '-stdev': (parameters['-stdev'] ?: ['1.0']),
        ]
        
        def optionsList = null
        def optionKeys = ['-C', '-lambda', '-gamma', '-K', '-stdev']
        
        // generate all variations of options
        for(key in optionKeys) {
            if( optionsList==null ) {
                optionsList = optionsValues[key].collect{[it]}
            }
            else {
                def newOptions = []
                for(options in optionsList) {
                    newOptions += optionsValues[key].collect{options + it}
                }
                optionsList = newOptions
            }
        }
        
        def optionResults = []
        
        for(options in optionsList) {
            def wekaOpts = []
            optionKeys.eachWithIndex{k, i-> wekaOpts += [k, options[i]]}
            
            // add max iterations (-I) and epsilon (-epsilon) to weka options
            ['-I', '-epsilon'].each{
                if(it in parameters) {
                    wekaOpts += [it, parameters[it][0]]
                }
            }
            
            // do several executions with the same parameters
            def silCoeffs = (1..numExecs).collect{
                goClusterer.runClusterer(wekaOpts)
                new Silhouette(goClusterer.clusterer)
            }
            
            def coeffs = silCoeffs.collect{it.overall}
            
            optionResults << ['silhouette': coeffs.max(), 'options': wekaOpts]
            
            // print clustering performance if debug==true
            if(debug) {
                println wekaOpts
                // print max, min, avg of silhouett coeff.
                println "Silhouette: Max=${coeffs.max()} Min=${coeffs.min()} Avg=${coeffs.sum()/(double)numExecs}"
            }
        }
        
        optionResults = optionResults.sort{-it['silhouette']}
        
        return optionResults
    }
    
    /**
     *
     */
    public static Map readTSVGenesGroups(file, int idField, int genesField, boolean header = false) {
        def groups = [:]
        def reader = Utils.createReader(new File(file))
        if( header ){ reader.readLine() }
        
        reader.splitEachLine("\t"){ toks->
            groups[toks[idField]] = toks[genesField].split(',')
        }
        
        reader.close()
        return groups
    }
    
    /**
     *
     */
    public calcEnrichment(geneGroups) {
        def geneQuery = new GeneQueryUtils(goData.graph)
        
        //prepare annotation
        def annotation = NGSDataResource.create(goData.graph)
        annotation.load(goData.taxonomyId)
        
        def calcBingo = { featureSet ->
            //prepare stats params
            StatTestParams statParams = new StatTestParams()
            statParams.significance = enrichPVal
            statParams.annotation = annotation
            statParams.taxonomyId = goData.taxonomyId
        
            //translate features to genes URI
            def genesAsURIs = 
                featureSet.collect{ geneQuery.getGeneByName(it, goData.taxonomyId)?.stringValue() }.findAll{ it!=null }
            
            statParams.annotation.selectedGenes = genesAsURIs

            // call BiNGO for current set
            return BingoAlgorithm.performCalculations(statParams)
        }
        
        //perform enrichment analysis for each set
        def results = [:]
        
        geneGroups.each{ label, features->
            BingoAlgorithm bingo = calcBingo(features)
            results[label] = bingo
        }
        
        return results
    }
    
    /**
     *
     */
    public List getNotIsolatedFeatures() {
        int num = dataset.numInstances()
        
        def indexes = (0..(num-1)).findAll{ i ->
            (0..(num-1)).any{ j -> (i != j) ? distances.get(i, j) < 1.0 : false }
        }
        
        return indexes.collect{ features[it] }
    }
    
}
