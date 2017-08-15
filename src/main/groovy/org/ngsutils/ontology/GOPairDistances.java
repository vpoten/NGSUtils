/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.ontology;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.ngsutils.Utils;
import org.ngsutils.maths.TriangularMatrix;

/**
 * Computes pairwise GO distances (similarity) between diff. expressed genes
 *
 * @deprecated use GOFMBDistance instead.
 * @author victor
 */
@Deprecated
public class GOPairDistances {

    private HashMap<String,Integer> geneIndex=new HashMap<String,Integer>();
    private ArrayList<List<String>> annotations=new ArrayList<List<String>>();
    private TriangularMatrix similarities=null;



    /**
     *
     * @param diffAnnotFile : GO annotated diff file
     * @param manager
     * @param namespace : GO namespace to use GOManager.{MF,BP,CC}
     * @param useOver : if true calc distances for over express. genes else calc distances
     *  for under express. genes
     * @throws FileNotFoundException
     * @throws IOException
     * @throws java.net.URISyntaxException
     */
    public GOPairDistances( String diffAnnotFile , GOManager manager, String namespace, boolean useOver )
            throws FileNotFoundException, IOException, URISyntaxException {

        //read diff annotated file to get the diff expressed genes
        BufferedReader reader = Utils.createReader(diffAnnotFile);

        String l_line;
        int cont=0;

        String type="under";

        if( useOver )
            type="over";

        while( (l_line=reader.readLine())!=null ){

            if( l_line.isEmpty() ){
                continue;
            }

            String [] tokens = l_line.split("\t",-1);

            if( tokens[2].equals("yes") && tokens[3].equals(type) && !tokens[4].isEmpty() ){
                //if is differentially expressed and has GO annotation
                String [] terms=tokens[4].split(",");
                ArrayList<String> list=null;

                for (String goterm : terms) {
                    if( manager.getNamespace(goterm).equals(namespace) ){
                        if( list==null )
                            list=new ArrayList<String>();
                        list.add(goterm);
                    }
                }

                if( list==null )
                    continue;

                //save the gene and respective annotations
                geneIndex.put(tokens[0], cont);
                annotations.add(list);
                cont++;
            }

        }

        reader.close();

        //calculates pairwise distances
        HashMap<Integer,Double> notZeroCache=new HashMap<Integer,Double>();
        int lim=annotations.size();
        this.similarities=new TriangularMatrix(lim);

        for(int i=0; i<lim; i++ ){
            for(int j=i; j<lim; j++ ){
                List<String> terms1=annotations.get(i);
                List<String> terms2=annotations.get(j);

                double mean=0.0;

                for( String t1 : terms1 ){
                    for( String t2 : terms2 ){
                        Integer id=getPairId(t1, t2);
                        Double cached=notZeroCache.get(id);

                        if( cached==null ){
                            //use Lin similarity
                            double sim=manager.simLin(t1, t2 );
                            if( sim!=0.0 && !Double.isInfinite(sim) ){
                                notZeroCache.put(id, sim);
                                mean += sim;
                            }
                        }
                        else{
                            mean += cached;
                        }
                    }
                }

                mean /= (terms1.size()*terms2.size());

                this.similarities.setValue( i, j, mean );
            }
        }

    }


    private Integer getPairId( String t1, String t2 ){
        Integer id;

        if( t1.compareTo(t2)<0 ){
            id=t1.hashCode() * 17 + t2.hashCode();
        }
        else{
            id=t2.hashCode() * 17 + t1.hashCode();
        }

        return id;
    }

    
    public Double getSimilarity(int idx1, int idx2) {
        if( idx1<=idx2 )
            return this.similarities.getValue(idx1, idx2);

        return this.similarities.getValue(idx2, idx1);
    }

    
    public Double getSimilarity(String gene1, String gene2) {
        int idx1=getGeneIndex(gene1);
        int idx2=getGeneIndex(gene2);

        return this.getSimilarity(idx1, idx2);
    }

    /**
     *
     * @param name
     * @return the index (zero based) for this name
     */
    public Integer getGeneIndex(String name){
        return this.geneIndex.get(name);
    }


    /**
     *
     * @return the set of gene names
     */
    public Set<String> getGeneNames(){
        return this.geneIndex.keySet();
    }


    /**
     *
     * @return the size
     */
    public int size(){
        return this.geneIndex.size();
    }


    /**
     * normalizes similarities
     */
    public void normalize(){

        double max=0.0;

        for(int i=0; i<this.size(); i++ ){
            for( int j=i; j<this.size(); j++){
                if( this.similarities.getValue(i, j)>max )
                    max=this.similarities.getValue(i, j);
            }
        }

        max=1.0/max;

        for(int i=0; i<this.size(); i++ ){
            for( int j=i; j<this.size(); j++){
                this.similarities.setValue(i, j,
                        this.similarities.getValue(i,j)*max );
            }
        }

    }


    /**
     * get annotations of a gene (or null if not present)
     *
     * @param gene
     * @return
     */
    public List<String> getAnnotations(String gene){
        Integer index=this.getGeneIndex(gene);

        if( index!=null )
            return this.annotations.get(index);

        return null;
    }

}
