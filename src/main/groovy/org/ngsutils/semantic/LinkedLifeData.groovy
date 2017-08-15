/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.semantic

import org.ngsutils.semantic.rdfutils.SimpleGraph

/**
 * LinkedLifeData.com databases info
 * 
 * @author victor
 */
class LinkedLifeData {
    
    static String urlBase = 'http://linkedlifedata.com/sparql.xml'
    
    //databases field constants
    public static final String NAME = 'name'
    public static final String GRAPH = 'graph'
    public static final String SIZE = 'size' //file size in KB
    public static final String STATEMENTS = 'statements'
    public static final String TYPE = 'type'
    public static final String ARCHIVE = 'archive'
    
    SimpleGraph graph = null // if not null then query the graph instead of web
    
    public static final String GO_PREF = 'GO:'//GO term prefix
    
    //predicates
    public static final String SKOS_PREF = 'http://www.w3.org/2004/02/skos/core#'
    public static final String RDF_PREF = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
    public static final String GOURI_PREF = 'http://linkedlifedata.com/resource/geneontology'
    public static final String GENEURI_PREF = 'http://linkedlifedata.com/resource/entrezgene'
    public static final String UNIPROTURI_PREF = 'http://linkedlifedata.com/resource/uniprot'
    public static final String GENEURI_TAXON = "${GENEURI_PREF}/taxon"
    public static final String GENEURI_XREF_ENSEMBL = "${GENEURI_PREF}/xref-Ensembl"
    public static final String GENEURI_XREF_VEGA = "${GENEURI_PREF}/xref-Vega"
    public static final String GENEURI_XREF_MIM = "${GENEURI_PREF}/xref-MIM"
    public static final String GENEURI_ENSEMBL = "${GENEURI_PREF}/ensembl"
    public static final String GENEURI_REFSEQ = "${GENEURI_PREF}/refseq"
    
    //entrez gene predicates
    public static final String GENE_GOTERM = "${GENEURI_PREF}/goTerm"
    public static final String GENE_PROTACC = "${GENEURI_PREF}/proteinAccession"
    public static final String GENE_TYPE = "${GENEURI_PREF}/Gene"
    public static final String GENE_ID = "${GENEURI_PREF}/hasGeneId"
    public static final String GENE_CHR = "${GENEURI_PREF}/chromosome"
    public static final String GENE_GENETYPE = "${GENEURI_PREF}/geneType"
    public static final String GENE_DESC = "${GENEURI_PREF}/description"
    public static final String GENE_MAPLOC = "${GENEURI_PREF}/mapLocation"
    public static final String GENE_SYMBOL = "${GENEURI_PREF}/geneSymbol"
    public static final String GENE_EXPRIN = "${GENEURI_PREF}/expressedIn"
    public static final String GENE_SYNONYM = "${GENEURI_PREF}/synonym"
    public static final String GENE_DBXREF = "${GENEURI_PREF}/dbXref"
    public static final String GENE_ENSEMBLREF = "${GENEURI_PREF}/ensemblReference"
    public static final String GENE_RNAACC = "${GENEURI_PREF}/nucleotideAccession"
    public static final String GENE_SPLICING = "${GENEURI_PREF}/custom/splicing" //custom predicate
    public static final String GENE_UNIPROTACC = "${GENEURI_PREF}/uniprotAccession"
    
    //uniprot predicates
    public static final String UNIPROT_TYPE = "${UNIPROTURI_PREF}/Protein"
    public static final String UNIPROT_SEQ = "${UNIPROTURI_PREF}/sequence"
    public static final String UNIPROT_SEQ_TYPE = 'http://purl.uniprot.org/core/Simple_Sequence'
    public static final String UNIPROT_ID = 'http://purl.uniprot.org/uniprot/'
    public static final String UNIPROT_ISOID = 'http://purl.uniprot.org/isoforms/'
    
    //database names
    public static final String DB_ENTREZ_GENE = 'NCBI Entrez-Gene'
    public static final String DB_GENE_ONTOLOGY = 'Gene Ontology'
    public static final String DB_UNIPROT = 'Uniprot'

    static def final databases = [
        'BioGRID': [
            (NAME): 'BioGRID',
            (GRAPH): 'http://linkedlifedata.com/resource/biogrid',
            (SIZE): 60438,
            (STATEMENTS): 12660756,
            (TYPE): 'biopax-2:entity',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/biogrid.tar.gz'
        ],
        'CALBC': [
            (NAME): 'CALBC',
            (GRAPH): '',
            (SIZE): 6938,
            (STATEMENTS): null,
            (TYPE): '',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/calbc.tar.gz'
        ],
        'CAUSALITY': [
            (NAME): 'CAUSALITY',
            (GRAPH): '',
            (SIZE): 340351,
            (STATEMENTS): null,
            (TYPE): '',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/causality.tar.gz'
        ],
        'CellMap': [
            (NAME): 'CellMap',
            (GRAPH): 'http://linkedlifedata.com/resource/cell-map',
            (SIZE): 1219,
            (STATEMENTS): 149175,
            (TYPE): 'biopax-2:entity',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/cell-map.tar.gz'
        ],
        'ChEBI': [
            (NAME): 'ChEBI',
            (GRAPH): 'http://linkedlifedata.com/resource/chebi',
            (SIZE): 3691,
            (STATEMENTS): 323212,
            (TYPE): 'owl:Thing',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/chebi.tar.gz'
        ], 	
        'DailyMed': [
            (NAME): 'DailyMed',
            (GRAPH): 'http://linkedlifedata.com/resource/dailymed',
            (SIZE): 24753,
            (STATEMENTS): 162972,
            (TYPE): 'dailymed:drugs',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/dailymed.tar.gz'
        ], 	
        'Disease Ontology': [
            (NAME): 'Disease Ontology',
            (GRAPH): 'http://linkedlifedata.com/resource/diseaseontology',
            (SIZE): 1145,
            (STATEMENTS): 144812,
            (TYPE): 'diseaseontology:DiseaseOntologyConcept',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/diseaseontology.tar.gz'
        ],
        'Diseasome': [
            (NAME): 'Diseasome',
            (GRAPH): 'http://linkedlifedata.com/resource/diseasome',
            (SIZE): 516,
            (STATEMENTS): 72445,
            (TYPE): 'diseasome:diseases',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/diseasome.tar.gz'
        ],
        'DrugBank': [
            (NAME): 'DrugBank',
            (GRAPH): 'http://linkedlifedata.com/resource/drugbank',
            (SIZE): 8140,
            (STATEMENTS): 517023,
            (TYPE): 'drugbank:drugs',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/drugbank.tar.gz'
        ],
        (DB_ENTREZ_GENE): [
            (NAME): 'NCBI Entrez-Gene',
            (GRAPH): 'http://linkedlifedata.com/resource/entrezgene',
            (SIZE): 989697,
            (STATEMENTS): 161563100,
            (TYPE): 'entrezgene:Gene',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/entrezgene.tar.gz'
        ],
        'EXPLICIT': [
            (NAME): 'EXPLICIT',
            (GRAPH): '',
            (SIZE): 1,
            (STATEMENTS): null,
            (TYPE): '',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/explicit.tar.gz'
        ],
        'Freebase': [
            (NAME): 'Freebase',
            (GRAPH): 'http://linkedlifedata.com/resource/freebase',
            (SIZE): 3586116,
            (STATEMENTS): 395958356,
            (TYPE): '',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/freebase.tar.gz'
        ],
        (DB_GENE_ONTOLOGY): [
            (NAME): 'Gene Ontology',
            (GRAPH): 'http://linkedlifedata.com/resource/geneontology',
            (SIZE): 3755,
            (STATEMENTS): 320182,
            (TYPE): 'geneontology:id',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/geneontology.tar.gz'
        ],
        'HapMap': [
            (NAME): 'HapMap',
            (GRAPH): 'http://linkedlifedata.com/resource/hapmap',
            (SIZE): 201083,
            (STATEMENTS): 22462178,
            (TYPE): '',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/hapmap.tar.gz'
        ],
        'HPRD': [
            (NAME): 'HPRD',
            (GRAPH): 'http://linkedlifedata.com/resource/hprd',
            (SIZE): 12458,
            (STATEMENTS): 1961200,
            (TYPE): 'biopax-2:entity',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/hprd.tar.gz'
        ],
        'HumanCYC': [
            (NAME): 'HumanCYC',
            (GRAPH): 'http://linkedlifedata.com/resource/humancyc',
            (SIZE): 3205,
            (STATEMENTS): 327218,
            (TYPE): 'biopax-2:entity',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/humancyc.tar.gz'
        ],
        'IMID': [
            (NAME): 'IMID',
            (GRAPH): 'http://linkedlifedata.com/resource/imid',
            (SIZE): 689,
            (STATEMENTS): 83091,
            (TYPE): 'biopax-2:entity',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/imid.tar.gz'
        ],
        'IMPLICIT': [
            (NAME): 'IMPLICIT',
            (GRAPH): '',
            (SIZE): 2245040,
            (STATEMENTS): null,
            (TYPE): '',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/implicit.tar.gz'
        ],
        'IntAct': [
            (NAME): 'IntAct',
            (GRAPH): 'http://linkedlifedata.com/resource/intact',
            (SIZE): 87178,
            (STATEMENTS): 16669066,
            (TYPE): 'biopax-2:entity',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/intact.tar.gz'
        ],
        'LHGDN': [
            (NAME): 'LHGDN',
            (GRAPH): 'http://linkedlifedata.com/resource/lhgdn',
            (SIZE): 3884,
            (STATEMENTS): 316020,
            (TYPE): '',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/lhgdn.tar.gz'
        ],
        'LIFESKIM': [
            (NAME): 'LIFESKIM',
            (GRAPH): '',
            (SIZE): 2604301,
            (STATEMENTS): null,
            (TYPE): '',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/lifeskim.tar.gz'
        ],
        'LinkedCT': [
            (NAME): 'LinkedCT',
            (GRAPH): 'http://linkedlifedata.com/resource/linkedct',
            (SIZE): 105069,
            (STATEMENTS): 7031859,
            (TYPE): 'linkedct:condition',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/linkedct.tar.gz'
        ],
        'MAPPINGS': [
            (NAME): 'MAPPINGS',
            (GRAPH): '',
            (SIZE): 17527,
            (STATEMENTS): null,
            (TYPE): '',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/mappings.tar.gz'
        ],
        'MetaCyc': [
            (NAME): 'MetaCyc',
            (GRAPH): 'http://linkedlifedata.com/resource/metacyc',
            (SIZE): 15194,
            (STATEMENTS): 1709326,
            (TYPE): 'biopax-2:entity',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/metacyc.tar.gz'
        ],
        'MINT': [
            (NAME): 'MINT',
            (GRAPH): 'http://linkedlifedata.com/resource/mint',
            (SIZE): 93647,
            (STATEMENTS): 21353848,
            (TYPE): 'biopax-2:entity',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/mint.tar.gz'
        ],
        'NCI Nature': [
            (NAME): 'NCI Nature',
            (GRAPH): 'http://linkedlifedata.com/resource/nci-nature',
            (SIZE): 4012,
            (STATEMENTS): 610689,
            (TYPE): 'biopax-2:entity',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/nci-nature.tar.gz'
        ],
        'Human Phenotype Ontology': [
            (NAME): 'Human Phenotype Ontology',
            (GRAPH): 'http://linkedlifedata.com/resource/phenotype',
            (SIZE): 640,
            (STATEMENTS): 84378,
            (TYPE): 'skos:Concept',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/phenotype.tar.gz'
        ],
        'PubMed': [
            (NAME): 'PubMed',
            (GRAPH): 'http://linkedlifedata.com/resource/pubmed',
            (SIZE): 14201197,
            (STATEMENTS): 1371818500,
            (TYPE): 'pubmed:Citation',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/pubmed.tar.gz'
        ],
        'Reactome': [
            (NAME): 'Reactome',
            (GRAPH): 'http://linkedlifedata.com/resource/reactome',
            (SIZE): 6909,
            (STATEMENTS): 814807,
            (TYPE): 'biopax-2:entity',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/reactome.tar.gz'
        ],
        'SIDER': [
            (NAME): 'SIDER',
            (GRAPH): 'http://linkedlifedata.com/resource/sider',
            (SIZE): 637,
            (STATEMENTS): 101542,
            (TYPE): 'sider:drugs',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/sider.tar.gz'
        ],
        'Symptom Ontology': [
            (NAME): 'Symptom Ontology',
            (GRAPH): 'http://linkedlifedata.com/resource/symptom',
            (SIZE): 27,
            (STATEMENTS): 4163,
            (TYPE): 'skos:Concept',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/symptom.tar.gz'
        ],
        'UMLS': [
            (NAME): 'UMLS',
            (GRAPH): 'http://linkedlifedata.com/resource/umls',
            (SIZE): 578854,
            (STATEMENTS): 121438271,
            (TYPE): 'skos:Concept',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/umls.tar.gz'
        ],
        (DB_UNIPROT): [
            (NAME): 'UniProt',
            (GRAPH): 'http://linkedlifedata.com/resource/uniprot',
            (SIZE): 16357838,
            (STATEMENTS): 2354085964,
            (TYPE): 'uniprot:Protein',
            (ARCHIVE): 'ftp://ftp.ontotext.com/pub/lld/uniprot.tar.gz'
        ]
    ]

    
    static def getBySize(long low, long high){
        return databases.findAll{ 
            long size = it.value[SIZE]*1000
            (size>=low && size<=high)
        }
    }

    
    /**
     *
     */
    protected static encodeQuery(String sparql){
        def tokens = sparql.split('\\s') as List
        return tokens.sum{ URLEncoder.encode(it)+'+' }
    }
    
    /**
     * closure for lld result URI fixing
     */
    def fixUri = { strUri-> 
        if( strUri.startsWith('https') )
            strUri.replace('https','http') 
        else
            return strUri
    }
        
    /**
     * returns a list of rows each containing a hashmap of bindings
     */
    def convertToMap(xmlResult){
        def list = []
        
        xmlResult.results.result.each{ res->
            def map = [:]
            
            res.binding.each{ bind->
                String var = bind.@name
                
                if( bind?.literal.text() )
                    map[var] = fixUri( bind.literal.text() )
                else if( bind?.uri.text() )
                    map[var] = fixUri( bind.uri.text() )
            }
            
            list << map
        }
        
        return list
    }
    
    
    /**
     * web query
     */
    protected def webQuery(String sparql){
        // TODO web query changed
        throw new UnsupportedOperationException()
        
//        String newquery = encodeQuery(sparql)
//        String urlstr = "${urlBase}?query=${newquery}&_implicit=false&implicit=true&_form=%2Fsparql"
//        def reader
//        
//        try{
//            URL url = new URL(urlstr)
//            reader = url.openStream()
//            def parser = new XmlParser()
//            parser.setFeature('http://apache.org/xml/features/nonvalidating/load-external-dtd', false);
//            parser.setFeature('http://xml.org/sax/features/namespaces', false) 
//            return convertToMap( parser.parse(reader) )
//        } catch(IOException e){
//            return null
//        } finally{
//            reader?.close()
//        }
    }
    
    /**
     * graph query
     */
    protected def graphQuery(String sparql){
        def list = this.graph.runSPARQL(sparql)
        
        list.each{ map-> //convert URIs to String
            map.each{ k, v-> map[k] = v.stringValue() }
        }
        
        return list
    }
    
    /**
     *
     */
    def query(String sparql){
        if( graph )
            return graphQuery(sparql)
        else
            return webQuery(sparql)
    }
    
    
    /**
     *
     */
    def tuplePattern(subject, predicate, object){
       
        if( graph )
            return graph.tuplePattern(subject, predicate, object)
            
        def s = subject ? "<${subject.stringValue()}>" : '?subject'
        def p = predicate ? "<${predicate.stringValue()}>" : '?predicate'
        def o = object ? "<${object.stringValue()}>" : '?object'
        
        String sparql = "SELECT * WHERE { ${s} ${p} ${o} . }"
        
        return query(sparql)
    }
    
    
    /**
     * 
     * @return a list of maps with keys = {go}
     */
    def queryProtGOTerms(String refseqAccesion){
        String sparql = 
"""PREFIX psys: <http://proton.semanticweb.org/2006/05/protons#>
PREFIX rdf: <${RDF_PREF}>
PREFIX gene: <${GENEURI_PREF}/>
PREFIX refseq: <${GENEURI_PREF}/refseq/>
SELECT ?go
WHERE {
    ?gene rdf:type gene:Gene;
    gene:proteinAccession refseq:${refseqAccesion} .
    ?gene gene:goTerm ?go .
}"""
        return query(sparql)
    }
    
    
    /**
     * 
     * @return a list of maps with keys = {gene}
     */
    def getAllGenes(taxId){
        String sparql =
"""PREFIX gene: <${GENEURI_PREF}/>
PREFIX rdf: <${RDF_PREF}>
SELECT ?gene
WHERE {
    ?gene rdf:type gene:Gene .
    ?gene gene:expressedIn <${GENEURI_PREF}/taxon/${taxId}> .
}"""
        return query(sparql)
    }
    
    /**
     * 
     * @return a list of maps with keys = {gene,go}
     */
    def getAllGenesAndGO(taxId){
        String sparql =
"""PREFIX gene: <${GENEURI_PREF}/>
PREFIX rdf: <${RDF_PREF}>
SELECT ?gene ?go
WHERE {
    ?gene rdf:type gene:Gene .
    ?gene gene:goTerm ?go .
    ?gene gene:expressedIn <${GENEURI_PREF}/taxon/${taxId}> .
}"""
        return query(sparql)
    }
    
    /**
     * 
     * @return a list of maps with keys = {go}
     */
    def getGOTerms(entrezId){
        String sparql =
"""PREFIX gene: <${GENEURI_PREF}/>
PREFIX rdf: <${RDF_PREF}>
SELECT ?go
WHERE {
    <${entrezId}> gene:goTerm ?go .
}"""    
        return query(sparql)
    }
    
    /**
     * 
     * @return a list of maps with keys = {go}
     */
    def getGOTermsBySymbol(symbol, taxId=null){
        String taxFilter = taxId ? "?gene gene:expressedIn <${GENEURI_PREF}/taxon/${taxId}> ." : ''
        
        String sparql =
"""PREFIX gene: <${GENEURI_PREF}/>
PREFIX rdf: <${RDF_PREF}>
SELECT distinct ?go
WHERE {
    ?gene rdf:type gene:Gene .
    ?gene gene:geneSymbol "${symbol}" .
    ?gene gene:goTerm ?go .
    ${taxFilter}
}"""    
        return query(sparql)
    }
    
}

