/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.ontology

/**
 *
 * @author victor
 */
class GOEvidenceCodes {
    
    static final def codesTree = [
        'Experimental Evidence Codes':[
            'EXP': 'Inferred from Experiment',
            'IDA': 'Inferred from Direct Assay',
            'IPI': 'Inferred from Physical Interaction',
            'IMP': 'Inferred from Mutant Phenotype',
            'IGI': 'Inferred from Genetic Interaction',
            'IEP': 'Inferred from Expression Pattern'
        ],
        'Computational Analysis Evidence Codes':[
            'ISS': 'Inferred from Sequence or Structural Similarity',
            'ISO': 'Inferred from Sequence Orthology',
            'ISA': 'Inferred from Sequence Alignment',
            'ISM': 'Inferred from Sequence Model',
            'IGC': 'Inferred from Genomic Context',
            'IBA': 'Inferred from Biological aspect of Ancestor',
            'IBD': 'Inferred from Biological aspect of Descendant',
            'IKR': 'Inferred from Key Residues',
            'IRD': 'Inferred from Rapid Divergence',
            'RCA': 'Inferred from Reviewed Computational Analysis'
        ],
        'Author Statement Evidence Codes':[
            'TAS': 'Traceable Author Statement',
            'NAS': 'Non-traceable Author Statement'
        ],
        'Curator Statement Evidence Codes':[
            'IC': 'Inferred by Curator',
            'ND': 'No biological Data available'
        ],
        'Automatically-assigned Evidence Codes':[
            'IEA': 'Inferred from Electronic Annotation'
        ],
        'Obsolete Evidence Codes':[
            'NR': 'Not Recorded'
        ]
    ]
    
    static def collectCodes = {
        def set = [] as Set
        codesTree.each{ set.addAll(it.value.keySet()) }
        return set
    }
    
    static final def codes = collectCodes()
    
    
    // an 'expert' choice of ecode importance
    static ecodeFactorsSet1 = [
        'EXP': 1.0d,
        'IDA': 1.0d,
        'IPI': 0.8d,
        'IMP': 0.9d,
        'IGI': 0.8d,
        'IEP': 0.8d,
        'ISS': 0.8d,
        'ISO': 0.8d,
        'ISA': 0.8d,
        'ISM': 0.8d,
        'IGC': 0.8d,
        'IBA': 0.8d,
        'IBD': 0.8d,
        'IKR': 0.0d,//NOT
        'IRD': 0.0d,//NOT
        'RCA': 0.9d,
        'TAS': 0.9d,
        'NAS': 0.9d,
        'IC': 0.8d,
        'ND': 0.4d,
        'IEA': 0.6d,
        'NR': 0.4d
    ] as TreeMap
    
}

