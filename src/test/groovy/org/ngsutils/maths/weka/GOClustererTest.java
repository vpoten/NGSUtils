/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths.weka;

//import java.awt.BorderLayout;
//import java.util.Set;
//import javax.swing.JFrame;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
//import org.ngsutils.ontology.GOManager;
//import org.ngsutils.ontology.GOPairDistances;
//import weka.gui.hierarchyvisualizer.HierarchyVisualizer;
import static org.junit.Assert.*;

/**
 *
 * @author victor
 */
public class GOClustererTest {

    public GOClustererTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

//    @Test
//    public void testSomeMethod() throws Exception {
//        System.out.println("GOGenesClusterer");
//
//        String file="/home/victor/work_bio/annotation/gene_ontology_ext.obo";
//        String probFile="/home/victor/work_bio/annotation/9606/gene_association.goa";
//        GOManager manager = new GOManager( file, probFile );
//
//        String filediff="/home/victor/Escritorio/0_1_splicing.diff.goa.gz";
//
//
//        GOPairDistances dist = new GOPairDistances( filediff, manager, GOManager.MF, false );
//        dist.normalize();
//
//        String linkType="COMPLETE";
//        //String linkType="NEIGHBOR_JOINING";
//        int numClusters=1;
//
//        GOClusterer clusterer=new GOClusterer( dist, linkType, numClusters);
//
//        System.out.println(clusterer.getClusterer().graph());
//
////        HierarchyVisualizer viz=new HierarchyVisualizer( clusterer.getClusterer().graph() );
////
////        JFrame jf = new JFrame("Weka Classifier Visualizer: Dendogram");
////        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
////        jf.setSize(800, 600);
////        jf.getContentPane().setLayout(new BorderLayout());
////        jf.getContentPane().add(viz, BorderLayout.CENTER);
////        jf.setVisible(true);
////
////        // adjust tree
////        viz.fitToScreen();
//
//        Set<Set<String>> groups=clusterer.getClustersGroups( 0.75 );
//
//        assertTrue( groups.size()==14 );
//    }

}