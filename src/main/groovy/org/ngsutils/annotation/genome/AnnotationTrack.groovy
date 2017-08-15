/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.annotation.genome

/**
 *
 * @author victor
 */
class AnnotationTrack {
    
    static final String SOFA_TFBS = 'TF_binding_site'
    static final String SOFA_SNP = 'SNP'
    static final String SOFA_TFBMotif = 'core_promoter_element'
    static final UNI_TF_PREFIX = 'UnifTF_'
    
    // translation of UCSC SNP function to SO ontology ID
    static final Map SNP_FUNC = ['unknown':'', 'coding-synon':'SO:0001819', 
        'intron':'SO:0000188', 'near-gene-3':'SO:0001634', 'near-gene-5':'SO:0001636', 
        'ncRNA':'SO:0000655', 'nonsense':'SO:0001587', 'missense':'SO:0001583',
        'stop-loss':'SO:0001578', 'frameshift':'SO:0000865', 'cds-indel':'SO:0001650',
        'untranslated-3':'SO:0001624', 'untranslated-5':'SO:0001623',
        'splice-3':'SO:0000164', 'splice-5':'SO:0000163'] as TreeMap
    
    String name
    String url
    String source
    String overrideName = null
    String featureType
    String organismId
    String cellLine
}

