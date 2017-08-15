/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.ontology;

import org.ngsutils.semantic.LinkedLifeData
import org.ngsutils.semantic.rdfutils.SimpleGraph
import org.openrdf.model.URI
import org.openrdf.model.vocabulary.XMLSchema as XML
import org.ngsutils.FileIndex
import org.ngsutils.Utils

/**
 * Manages GO hierarchy and calculates probabilities and some similarity
 * measures between sets of terms
 *
 * @author victor
 */
public class GOManager {

    protected SimpleGraph graph
    protected LinkedLifeData lifeData = null

    // root terms and synonyms
    protected static final ROOT_TERMS = [
        'GO:0008150','GO:0007582','GO:0000004',//BP
        'GO:0003674','GO:0005554',//MF
        'GO:0005575','GO:0008372'//CC
    ]
    
    protected static final double EXCLUDED_IC = 0.0

    //name of some predicates
    protected static final String IS_A = "broader";
    protected static final String NAMESPACE = "namespace";
    protected static final String LABEL = "prefLabel";
    protected static final String ALT_LABEL = "altLabel";
    protected static final String NOTATION = "notation";
    protected static final String DEFINITION = "definition";
    protected static final String IN_SCHEME = "inScheme";
    protected static final String SYNONYM = "relatedSynonym";
    
    protected def goTerms = null//all GO terms
    
    // icontent hash map key=GO_TERM
    protected def icontent = [:] as TreeMap
    // evidence codes hash map key=geneGoKey(gene, goterm)
    protected def ecodes = [:] as TreeMap
    // ecode factors: map of ecode->importance_factor
    def ecodeFactors
    
    URI is_a_predicate //frequently used
    
    //namespaces literals
    public static final String MF = "molecular_function";
    public static final String BP = "biological_process";
    public static final String CC = "cellular_component";
    
    public static final String GOURI_PREF = LinkedLifeData.GOURI_PREF
    public static final String SKOS_PREF = LinkedLifeData.SKOS_PREF
    protected static final String GO_PREF = LinkedLifeData.GO_PREF


    /**
     * compare terms based on IC (descendt)
     */
    def termComparator = [
        compare: { o1, o2 ->
            Double ic1 = getIcontent(o1);
            Double ic2 = getIcontent(o2);

            if( ic1==null )
                return 1;
            if( ic2==null )
                return -1;

            if( ic1<ic2 )
                return 1;
            if( ic1>ic2 )
                return -1;

            return 0;
        }
    ] as Comparator

    /**
     * constructor: get data from LLD website
     */
    public GOManager(){
        this.graph = new SimpleGraph(false)
        this.lifeData = new LinkedLifeData()
        is_a_predicate = this.graph.URIref(SKOS_PREF+IS_A)
    }
    
    /**
     * constructor
     * 
     * @param dataSource : a Linked Life Data GO file path (latest if possible)
     * or InputStream, or a ready-to-use SimpleGraph
     */
    public GOManager( dataSource ) {
        
        if( dataSource instanceof SimpleGraph ){
            this.graph = dataSource
        }
        else{
            this.graph = new SimpleGraph(false)

            if( dataSource instanceof InputStream )
                this.graph.addInputStream( dataSource, SimpleGraph.TRIG, null)
            else if( dataSource instanceof String )
                this.graph.addInputStream( Utils.untarToStream(dataSource), SimpleGraph.TRIG, null)
        }
        
        is_a_predicate = this.graph.URIref(SKOS_PREF+IS_A)
    }

    /**
     * creates a valid URI for a GO term id
     */
    protected URI createTermURI(term){
        if(term instanceof URI)
            return term
        else if( term.startsWith(GOURI_PREF) )
            return this.graph.URIref(term)
        else
            return this.graph.URIref(GOURI_PREF+'/id/'+term)
    }
    
    /**
     * get a GO term as a String in format GO:NNNNNNN
     */
    public String goTermStr(term){
        if(term instanceof URI)
            return term.localName
            
        return term.substring( term.indexOf(GO_PREF) )
    }
    
    /**
     * get a GO term as an URI
     */
    public URI goTermUri(String term){
        createTermURI(term)
    }
    
    /**
     *
     */
    protected String stringValue(obj){
        return (obj instanceof String) ? obj : obj.stringValue()
    }

    
    /**
     * get all GO terms
     */
    protected def getGOTerms(){
        def statements = 
            this.graph.tuplePattern(null, graph.URIref(SKOS_PREF+IN_SCHEME), graph.URIref(GOURI_PREF))
        
        def terms = [] as Set
        def prefix = GOURI_PREF+'/id/GO:'
        
        statements.each{ 
            def str = stringValue(it.subject) 
            if( str.startsWith(prefix) ){ terms << str }
        }
        
        return terms
    }
   
    /**
     * returns a list  of statements (fields:[subject, predicate, object])
     */
    protected def tuplePattern(subject, predicate, object){
        if( this.lifeData )
            return lifeData.tuplePattern(subject, predicate, object)
        
        return this.graph.tuplePattern(subject, predicate, object)
    }
        

    /**
     * Calculates probability terms (ic) using a GO annotation file.
     * Also, this method sets the evidence codes for each annotation in GOA file:
     *  (gene,go)->evidCode
     *
     * @param goaFile
     * @throws IOException
     */
    public void calculateProbTerms(String goaFile) throws IOException {

        // parse goa file
        def indexGOA = [:] as TreeMap
        def reader = Utils.createReader(new File(goaFile))
        
        reader.eachLine{ line->
            if( !line.startsWith('!') ){
                def toks = line.split("\t",-1)
                def gene = toks[2]
                def go = toks[4]
                def ec = toks[6]
                ecodes[geneGoKey(gene, go)] = ec
                
                def list = indexGOA[go]
                
                if( list==null ){
                    list = []
                    indexGOA[go] = list
                }
                
                list << gene
            }
        }
        
        reader.close()
        
        // calculate total of terms in corpus
        double total = indexGOA.keySet().sum{ indexGOA.get(it).size() }
        double invTotal=1.0/total;
        
        def probMap = [:]
        double minProb = Double.MAX_VALUE;
        
        goTerms = getGOTerms()
        
        //calculate frequencies
        goTerms.each{ term->
            def key = goTermStr(term)
            
            double countTerm = indexGOA.get(key)?.size() ?: 0.0
            
            Double countChildren = getAllOffspring(key).sum{ 
                    def list = indexGOA.get(goTermStr(it))
                    (list) ? list.size() : 0.0 
                }
            
            countChildren = countChildren ?: 0.0
            
            double prob=(countTerm+countChildren)*invTotal
            probMap.put(key, prob)
            
            if( prob>0.0 && prob<minProb )
                minProb=prob;
        }
        
        double invMaxIC = 1.0/Math.abs(Math.log(minProb));

        //calculates information content (normalized)
        probMap.each{ key, prob ->
            double ic = (prob==0.0) ? EXCLUDED_IC : Math.abs(Math.log(prob))*invMaxIC;
            icontent[key] = ic
        }
        
        //set IC=0 for root terms
        ROOT_TERMS.each{ icontent[it]=0.0 }
    }
    
    /**
     *
     */
    protected long geneGoKey(String gene, String go) {
        return Utils.hash("${gene}:${go}")
    }

    /**
     * get evidence code for a pair gene-term annotation
     */
    public String getECode(String gene, term) {
        ecodes[geneGoKey(gene, goTermStr(term))]
    }
    
    /**
     * get evidence code importance for a pair gene-term annotation
     */
    public Double getEvidence(String gene, term) {
        def ecode = ecodes[geneGoKey(gene, goTermStr(term))]
        (ecode) ? ecodeFactors[ecode] : null
    }
    
    /**
     * 
     * @param term
     * @return 
     */
    public Set getDirectAncestors(term){
        def statements = tuplePattern( createTermURI(term), is_a_predicate, null )
        
        return statements.collect{ it.object } as Set
    }
    
    /**
     * 
     */ 
    protected boolean isChild(term, termChild){
        def statements = tuplePattern( termChild, is_a_predicate, term)
            
        return statements ? true : false
    }
    
    /**
     * get direct offspring of a term
     * @param term
     * @return 
     */
    public Set getOffspring(term){
        def statements = tuplePattern( null, is_a_predicate, createTermURI(term) )
        
        return statements.collect{ it.subject } as Set
    }
    
     
    public Set getAllOffspring(term){ 
        def offspring = [] as Set
        ArrayDeque deque = new ArrayDeque()

        deque.addAll( getOffspring(term) )             

        while( !deque.isEmpty() ){
            def current=deque.poll()
            offspring << current
            deque.addAll( getOffspring(current) )
        }
        
        return offspring
     }
    
    /**
     *
     * @param term
     * @return
     */
    public def getAncestors(term){

        def ancest = [] as Set
        ArrayDeque deque = new ArrayDeque()

        deque.addAll( getDirectAncestors(term) )             

        while( !deque.isEmpty() ){
            def current=deque.poll()
            ancest << current
            deque.addAll( getDirectAncestors(current) )
        }
        
        return ancest
    }
    
    /**
     * 
     * @param term
     * @return 
     */
    public Double getIcontent(term){
        return icontent[goTermStr(term)]
    }
    
    /**
     * 
     * @param gene : a gene or feature
     * @param term : a go term
     */
    public Double getImportance(gene, term){
        String goterm = goTermStr(term)
        Double ic = icontent[goterm]
        String ecode = ecodes[geneGoKey(gene,goterm)]
        double factor = ecodeFactors[ecode]
        return ic*factor
    }

    /**
     *
     */
    public def getNamespace(term){
        def statements = tuplePattern( createTermURI(term), 
                this.graph.URIref(GOURI_PREF+'/'+NAMESPACE), null )
        
        return statements ? stringValue(statements[0].object) : null
    }
     
    /**
     *
     */
    public def getNotations(term){
        def statements = tuplePattern( createTermURI(term), 
                this.graph.URIref(SKOS_PREF+NOTATION), null )
            
        return statements.collect{ stringValue(it.object) }
    }
     
    /**
     * get common ancestors between all terms in a set
     * 
     * @param terms
     * @return
     */
    public Set getCommonAncestors( terms ){

        def commons = [] as Set
        terms = terms as List
        
        terms.each{ term->
            def ancs = this.getAncestors(term)
            ancs << createTermURI(term)
            
            if( !commons )
                commons.addAll(ancs)
            else
                commons = commons.intersect(ancs)
        }
        
        return commons
    }
    
    /**
     * do the same that getAncestor but returning a List
     */
    protected List getAncestorsList(term, boolean includet=false){
        
        def ancest = includet ? [goTermUri(term)] : []
        
        ArrayDeque deque = new ArrayDeque()
        deque.addAll( getDirectAncestors(term) )             

        while( !deque.isEmpty() ){
            def current=deque.poll()
            
            if( !(current in ancest) ){
                ancest << current
                deque.addAll( getDirectAncestors(current) )
            }
        }

        return ancest
    }
        
    /**
     * get Nearest Common Ancestor
     * 
     * @return a term
     */
    public def getNCAncestor(term1, term2) {
        def anc1 = getAncestorsList(term1, true)
        def anc2 = getAncestorsList(term2, true)
        
        return anc1.find{it in anc2}
    }

    
    /**
     * order terms by IC (desc)
     *
     * @param set : a set of Term
     * @return a list with ordered Term
     */
    public List orderTermsByIC(Set set){
        def list = set as List
        Collections.sort(list, termComparator )
        return list
    }

    
    /**
     * computes Resnik similarity between two terms
     * 
     * @param t1 a ontology term
     * @param t2 a ontology term
     * @return
     */
    public double simResnik( t1, t2 ){
        def anc = getCommonAncestors( [t1, t2] )

        if( !anc )
            return 0.0
            
        if( t1==t2 ) {
            anc << t1
        }

        //get the maximun IC of common directAncest
        return anc.collect{ getIcontent(it)?:0.0 }.max()
    }


    /**
     * calculates Lin similarity
     * 
     * @param t1 a ontology term
     * @param t2 a ontology term
     * @return
     */
    public double simLin( t1, t2 ){

        double icA=simResnik(t1,t2)

        if( icA==0.0 )
            return 0.0

        Double ic1=getIcontent(t1)
        Double ic2=getIcontent(t2)

        if( ic1==null || ic2==null )
            return 0.0

        return (2.0*icA)/(ic1+ic2)
    }
    
    /**
     *
     */
    public List getAncestorLevels(term) {
        Set ancestors = getAncestors(term)
        
        if( !ancestors ){ return [] }
        
        // get root term
        def root = ancestors.find{ goTermStr(it) in ROOT_TERMS }
        def levels = [[root]]
        ancestors.remove(root)
        
        while(ancestors) {
            def current = levels[levels.size()-1]
            def children = ancestors.findAll{ child-> current.any{ isChild(it,child) } }
            
            levels << children
            children.each{ ancestors.remove(it) }
        }
        
        return levels
    }
    
}
