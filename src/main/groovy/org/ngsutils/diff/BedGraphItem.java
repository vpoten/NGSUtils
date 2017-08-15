/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ngsutils.diff;

import java.util.Comparator;

/**
 * Item of BedGraph file
 * 
 * @author victor
 */
public class BedGraphItem<K> {

    public enum LimitType {down,up};

    
    public class BedGraphLimit<K> {
        private final String chr;
        private final LimitType type;
        private final int coord;
        private final K value;

        public BedGraphLimit( LimitType type, String chr, int coord, K value) {
            this.type=type;
            this.coord=coord;
            this.value=value;
            this.chr=chr;
        }

        /**
         * @return the type
         */
        public LimitType getType() {
            return type;
        }

        /**
         * @return the coord
         */
        public int getCoord() {
            return coord;
        }

        /**
         * @return the value
         */
        public K getValue() {
            return value;
        }

        /**
         * @return the chr
         */
        public String getChr() {
            return chr;
        }

    }//

    /**
     * comparator for BedGraphItem<K>.BedGraphLimit<K> sort
     */
    public class BGLimitComparator implements Comparator<BedGraphItem<K>.BedGraphLimit<K>> {

        /**
         * Returns a negative integer, zero, or a positive integer as the first
         * argument is less than, equal to, or greater than the second
         * @param o1
         * @param o2
         * @return
         */
        public int compare( BedGraphItem<K>.BedGraphLimit<K> o1,
                BedGraphItem<K>.BedGraphLimit<K> o2 ) {

            int res=o1.getChr().compareTo(o2.getChr());

            if( res<0 )
                return -1;
            
            if( res>0 )
                return 1;

            if( o1.getCoord()<o2.getCoord() )
                return -1;

            if( o1.getCoord()>o2.getCoord() )
                return 1;

            if( o1.getType()==o2.getType() )
                return 0;

            if( o1.getType()==LimitType.down )
                return -1;

            return 1;
        }

    }///




    private String chrom="";
    private int chromStart=0;
    private int chromEnd=0;
    private K dataValue=null;

    public BedGraphItem() {
    }

    public BedGraphItem( String line ){
        setValues( line );
    }

    public BedGraphItem( String chrom, int start, int end, K value ) {
        this.setChrom(chrom);
        this.setChromStart(start);
        this.setChromEnd(end);
        this.setDataValue(value);
    }

    public BedGraphLimit createBGLimit(LimitType type, String chr, int coord, K value){
        return new BedGraphLimit( type, chr, coord, value);
    }

    public BGLimitComparator createBGLimitComp(){
        return new BGLimitComparator();
    }

    public void setValues( String line ){

        String [] tokens=line.split("\\s");

        this.setChrom( tokens[0] );
        this.setChromStart( Integer.parseInt(tokens[1]) );
        this.setChromEnd( Integer.parseInt(tokens[2]) );
        this.setDataValue( (K) Integer.valueOf(tokens[3]));
    }

    @Override
    public String toString(){
        return getChrom()+"\t"+getChromStart()+"\t"+getChromEnd()+"\t"+getDataValue();
    }
    
    /**
     * checks if this overlaps with other
     * assumes than this.start is less than other.start
     *
     * @param other
     * @return true if other overlaps with this
     */
    public final boolean overlaps( BedGraphItem other ){
        if( this.getChromEnd()>other.getChromStart() )
            return true;

        return false;
    }

    /**
     * @return the chrom
     */
    public final String getChrom() {
        return chrom;
    }

    /**
     * @param chrom the chrom to set
     */
    public final void setChrom(String chrom) {
        this.chrom = chrom;
    }

    /**
     * @return the chromStart
     */
    public final int getChromStart() {
        return chromStart;
    }

    /**
     * @param chromStart the chromStart to set
     */
    public final void setChromStart(int chromStart) {
        this.chromStart = chromStart;
    }

    /**
     * @return the chromEnd
     */
    public final int getChromEnd() {
        return chromEnd;
    }

    /**
     * @param chromEnd the chromEnd to set
     */
    public final void setChromEnd(int chromEnd) {
        this.chromEnd = chromEnd;
    }

    /**
     * @return the dataValue
     */
    public final K getDataValue() {
        return dataValue;
    }

    /**
     * @param dataValue the dataValue to set
     */
    public final void setDataValue(K dataValue) {
        this.dataValue = dataValue;
    }



}
