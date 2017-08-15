/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.variation

/**
 * Frequency estimation for 2 loci 2 allele haplotypes
 * 
 * @author victor
 */
class HaplotypeFreq {
    final SNPData snpA
    final SNPData snpB
    
    //phenotype index AA=0, Aa=1, aa=2, BB=0, Bb=1, bb=2
    protected def phenoCounts = [[0,0,0], [0,0,0], [0,0,0]]
    protected def phenoProbs = [[0.0d,0.0d,0.0d], [0.0d,0.0d,0.0d], [0.0d,0.0d,0.0d]]
    
    //haplotype index AB=0, Ab=1, aB=2, ab=3
    def haploFreq = [0.0d, 0.0d, 0.0d, 0.0d]
    protected def newHaploFreq = [0.0d, 0.0d, 0.0d, 0.0d]
    
    //expected genotype frequencies
    protected def expGenoFreq = (0..3).collect{ (0..3).collect{0.0d} }
    
    int iterations = 0
    
    double eps = 1e-3 //convergence constant
    int maxIter = 50000
    
    /**
     * 
     */
    public HaplotypeFreq(SNPData a, SNPData b) {
        snpA = a; snpB = b;
    }
    
    /**
     *
     */
    protected countPhenotypes() {
        
        int lim = snpA.genotypes.length()
        String alleA = snpA.alleles
        String alleB = snpB.alleles
        String genoA = snpA.genotypes
        String genoB = snpB.genotypes
        
        for(int i=0; i<lim; i+=4) {
            boolean homoA = (genoA[i]==genoA[i+2])
            boolean homoB = (genoB[i]==genoB[i+2])
            
            int a = homoA ? (genoA[i]==alleA[0] ? 0 : 2) : 1
            int b = homoB ? (genoB[i]==alleB[0] ? 0 : 2) : 1
            
            phenoCounts[a][b]++
        }
    }
    
    /**
     * initialize frequencies
     */
    protected init() {
        double val = 1.0d/(double)haploFreq.size()
        (0..haploFreq.size()-1).each{ haploFreq[it]=val }
    }
    
    /**
     * returns the indexes of compatible genotypes with a given phenotype.
     * parameters a = [AA=0, Aa=1, aa=2], b = [BB=0, Bb=1, bb=2]
     * haplotype index AB=0, Ab=1, aB=2, ab=3
     */ 
    protected List compatibleGeno(int a, int b) {
        if( a==1 && b==1 ) {
            return [[0,3],[1,2]]
        }
        
        a = (a==2) ? 3 : a
        b = (b==2) ? 3 : b
        int h1 = (a & 0x02) + ((b & 0x02)>>1)
        int h2 = ((a & 0x01)<<1) + (b & 0x01)
        
        return [[h1,h2]]
    }
    
    /**
     * returns the indexes of compatible phenotype with a given genotype.
     * haplotype index (k,l) AB=0, Ab=1, aB=2, ab=3
     */ 
    protected List compatiblePheno(int k, int l) {
        if( (k==0 && l==3) || (k==1 && l==2) ){
            return [1,1]
        }
        
        int a = (k & 0x02) + ((l & 0x02)>>1)
        int b = ((k & 0x01)<<1) + (l & 0x01)
        a = (a==3) ? 2 : a
        b = (b==3) ? 2 : b
        
        return [a,b]
    }
    
    /**
     * returns the number of times haplotype t is present in genotype (k,l)
     * haplotype index (k,l) AB=0, Ab=1, aB=2, ab=3
     * @return 0, 1 or 2
     */ 
    protected int timesHaploInGeno(int t, int k, int l) {
         return (t==k ? 1 : 0) + (t==l ? 1 : 0)
    }
    
    /**
     * EM algorythm for haplotype frequency estimation
     */
    public void em() {
        countPhenotypes()
        init() //initialize frequencies
        
        int n = phenoCounts.flatten().sum()
        double invN = 1.0d/(double)n
        
        while(true) {
            // E -> expectation
            (0..3).each{ k->
                (0..3).each{ l-> 
                    expGenoFreq[k][l] = 
                        (k==l) ? haploFreq[k]*haploFreq[k] : 2.0d*haploFreq[k]*haploFreq[l]
                } 
            }

            // M -> maximization

            // phenotype probability
            (0..2).each{ a->
                (0..2).each{ b->
                    def idxs = compatibleGeno(a,b)
                    phenoProbs[a][b] = idxs.sum{ expGenoFreq[it[0]][it[1]] }
                }
            }

            // standarize genotype frequencies
            (0..3).each{ k->
                (0..3).each{ l-> 
                    def idx = compatiblePheno(k,l)
                    expGenoFreq[k][l] =
                        (phenoCounts[idx[0]][idx[1]]*invN)*expGenoFreq[k][l]/phenoProbs[idx[0]][idx[1]]
                }
            }

            //next haplotype frequencies
            (0..3).each{ t->
                double sum = 0.0d

                (0..2).each{ a->
                    (0..2).each{ b->
                        def idxs = compatibleGeno(a,b)
                        idxs.each{ idx-> 
                            sum += timesHaploInGeno(t, idx[0], idx[1])*expGenoFreq[idx[0]][idx[1]]
                        }
                    }
                }

                newHaploFreq[t] = 0.5d*sum
            }
            
            iterations++
            double diff = (0..3).sum{ Math.abs(newHaploFreq[it]-haploFreq[it]) }
            (0..3).each{ haploFreq[it]=newHaploFreq[it] }
            
            if( iterations>5 && diff<eps) {
                break;//end condition
            }
            if( iterations>maxIter ){
                haploFreq = null
                break;
            }
        }
    }
    
    /**
     *
     */
    public Map haploFreqMap() {
        def map = [:]
        
        (0..3).each {
            def hap = snpA.alleles[((it & 0x02)>>1)] + snpB.alleles[it & 0x01]
            map[hap] = haploFreq[it]
        }
        
        return map
    }
    
    
}

