/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils;

import java.io.PrintStream;
//import java.text.DecimalFormat;
//import java.text.DecimalFormatSymbols;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Locale;
//import java.util.Set;
//import org.biojava.ontology.Term;
//import org.ngsutils.maths.weka.GOClusterer;
//import org.ngsutils.ontology.GOManager;
//import org.ngsutils.ontology.GOPairDistances;

/**
 *
 * @author victor
 */
public class GOClustererFactory {

    /**
     *
     * @param oboFile : obo GO file
     * @param goaFile : goa file
     * @param diffFile : GO annotated diff file
     * @param out : output stream
     * @param goNamespace : MF|BP|CC
     * @param cutVal : tree height cut value [0,1]
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void doGOCluster( String oboFile, String goaFile, String diffFile,
            PrintStream out, String goNamespace, boolean useOver )
            throws Exception {

//        if( goNamespace.equals("MF") ){
//            goNamespace=GOManager.MF;
//        }
//        else if( goNamespace.equals("CC") ){
//            goNamespace=GOManager.CC;
//        }
//        else if( goNamespace.equals("BP") ){
//            goNamespace=GOManager.BP;
//        }
//        else{
//            throw new RuntimeException("Bad namespace");
//        }
//
//        System.out.println( "Start time = "+String.format( Main.timestampFormat, new Date()) );
//
//
//        GOManager manager = new GOManager( oboFile, goaFile );
//        GOPairDistances dist = new GOPairDistances( diffFile, manager, goNamespace, useOver );
//        dist.normalize();
//
//        String linkType="COMPLETE";
//        ///String linkType="NEIGHBOR_JOINING";
//        int numClusters=1;//single hierarchy
//
//        GOClusterer clusterer= clusterer = new GOClusterer(dist, linkType, numClusters);
//
//        DecimalFormat myFormatter =
//                new DecimalFormat("#.#####",DecimalFormatSymbols.getInstance(Locale.ENGLISH));
//
//        //print results with cutVal= 0.25, 0.5, 0.75
//        for( double cutVal=0.25; cutVal<0.9; cutVal+=0.25){
//
//            Set<Set<String>> groups=clusterer.getClustersGroups( cutVal );
//
//            out.println( "Clusters with cut value="+cutVal+" (size="+groups.size()+") \n" );
//
//            int cont=1;
//
//            for( Set<String> group : groups ){
//                //get clusters and print most informative terms
//
//                out.println( "Cluster "+cont+" (size="+group.size()+") :" );
//
//                Set<Term> annotations=new HashSet<Term>();
//
//                //get annotations for each gene in group
//                for( String gene : group ){
//                    out.println( gene );
//                    annotations.addAll( dist.getAnnotations(gene) );
//                }
//
//                //get common ancestors from the set of annotations and order them by IC
//                Set<Term> ances=manager.getCommonAncestors( annotations );
//                List<Term> list=manager.orderTermsByIC(ances);
//
//                out.println( "Common annotations (size="+list.size()+")");
//
//                for( Term t : list ){
//                    Double ic=manager.getIcontent().get(t);
//                    if(ic==null)
//                        ic=0.0;
//
//                    out.println( t.getName()+" IC="+myFormatter.format(ic)+" : "+t.getDescription() );
//                }
//
//                out.println('\n');
//                cont++;
//            }
//
//            out.println('\n');
//        }
//
//        out.close();
//        System.out.println( "End time = "+String.format( Main.timestampFormat, new Date()));
    }
}
