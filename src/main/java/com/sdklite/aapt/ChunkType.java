package com.sdklite.aapt;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The types definition of {@link Chunk}
 * 
 * @author johnsonlee
 */
public interface ChunkType {

    /**
     * No chunk
     */
    public static final short NULL = 0x0000;

    /**
     * String pool
     * 
     * @see StringPool
     */
    public static final short STRING_POOL = 0x0001;

    /**
     * Resource table
     * 
     * @see ResourceTable
     */
    public static final short TABLE = 0x0002;

    /**
     * XML resource
     * 
     * @see Xml
     */
    public static final short XML = 0x0003;

    /**
     * The namespace start of XML
     */
    public static final short XML_START_NAMESPACE = 0x0100;

    /**
     * The namespace end of XML
     */
    public static final short XML_END_NAMESPACE = 0x0101;

    /**
     * The element start of XML
     */
    public static final short XML_START_ELEMENT = 0x0102;

    /**
     * The element end of XML
     */
    public static final short XML_END_ELEMENT = 0x0103;

    /**
     * XML CDATA
     */
    public static final short XML_CDATA = 0x0104;

    /**
     * This contains a uint32_t array mapping strings in the string pool back to
     * resource identifiers. It is optional.
     */
    public static final short XML_RESOURCE_MAP = 0x0180;

    /**
     * Resource table package
     * 
     * @see ResourceTable.Package
     */
    public static final short TABLE_PACKAGE = 0x0200;

    /**
     * Resource type in resource table
     */
    public static final short TABLE_TYPE = 0x0201;

    /**
     * Resource type specification in resource table
     */
    public static final short TABLE_TYPE_SPEC = 0x0202;
    public static final short TABLE_LIBRARY = 0x0203;

    /**
     * All chunk types
     */
    public static final Set<Short> ALL_TYPES = Collections.<Short>unmodifiableSet(new HashSet<Short>(Arrays.<Short>asList(
        STRING_POOL,
        TABLE,
        XML,
        XML_START_NAMESPACE,
        XML_END_NAMESPACE,
        XML_START_ELEMENT,
        XML_END_ELEMENT,
        XML_CDATA,
        XML_RESOURCE_MAP,
        TABLE_PACKAGE,
        TABLE_TYPE,
        TABLE_TYPE_SPEC,
        TABLE_LIBRARY
    )));
    
}
