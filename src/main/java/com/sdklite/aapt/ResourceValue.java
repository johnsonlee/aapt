package com.sdklite.aapt;

/**
 * Representation of a value in a resource, supplying type information.
 * 
 * @author johnsonlee
 *
 */
public final class ResourceValue {

    /**
     * Number of bytes in this structure.
     */
    public short size;

    /**
     * Always 0
     */
    public byte res0;

    /**
     * Type of the data value.
     * 
     * @see ValueType#NULL
     * @see ValueType#ATTRIBUTE
     * @see ValueType#DIMENSION
     * @see ValueType#DYNAMIC_ATTRIBUTE
     * @see ValueType#DYNAMIC_REFERENCE
     * @see ValueType#FLOAT
     * @see ValueType#FRACTION
     * @see ValueType#INT_BOOLEAN
     * @see ValueType#INT_COLOR_ARGB4
     * @see ValueType#INT_COLOR_ARGB8
     * @see ValueType#INT_COLOR_RGB4
     * @see ValueType#INT_COLOR_RGB8
     * @see ValueType#INT_DEC
     * @see ValueType#INT_HEX
     * @see ValueType#REFERENCE
     * @see ValueType#STRING
     */
    public byte dataType;

    /**
     * The data for this item, as interpreted according to dataType.
     */
    public int data;

}
