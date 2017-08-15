/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.maths;

/**
 *
 * @author victor
 */
public class TriangularMatrix {

    private double [] values=null;
    private int size=0;


    public TriangularMatrix(int dim) {
        this.size=dim;
        this.values=new double [(dim*(dim+1))/2];
    }


    public double getValue(int i, int j){
        return this.values[getIndex(i,j)];
    }


    public void setValue(int i, int j, double val){
        this.values[getIndex(i,j)]=val;
    }


    /**
     * gets linear index
     * precondition: i<=j
     * @param i
     * @param j
     * @return
     */
    private int getIndex(int i, int j){
        return ((j*(j+1))/2)+i;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * safe version of getValue (checks i<=j)
     * 
     * @param i
     * @param j
     * @return
     */
    public double getValue2(int i, int j) {
       if( i<j )
           return this.getValue(i,j);

       return this.getValue(j,i);
    }


}
