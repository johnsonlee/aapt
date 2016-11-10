package com.sdklite.aapt;

/**
 * Represents the chunk visitor
 * 
 * @author johnsonlee
 *
 */
public interface ChunkVisitor {

    /**
     * Visit string pool chunk
     * 
     * @param chunk
     *            The string pool chunk
     */
    public void visit(final StringPool chunk);

    /**
     * Visit resource table chunk
     * 
     * @param chunk
     *            The resource table chunk
     */
    public void visit(final ResourceTable chunk);

    /**
     * Visit resource table package
     * 
     * @param chunk
     *            The resource table package chunk
     */
    public void visit(final ResourceTable.Package chunk);

    /**
     * Visit resource table type
     * 
     * @param chunk
     *            The resource table type chunk
     */
    public void visit(final ResourceTable.Type chunk);

    /**
     * Visit resource table type specification
     * 
     * @param chunk
     *            The resource table type specification chunk
     */
    public void visit(final ResourceTable.TypeSpec chunk);

    /**
     * Visit resource table library
     * 
     * @param chunk
     *            The resource table library chunk
     */
    public void visit(final ResourceTable.Library chunk);

    /**
     * Visit xml resource
     * 
     * @param chunk
     *            The xml resource chunk
     */
    public void visit(final Xml chunk);

    /**
     * Visit xml resource map
     * 
     * @param chunk
     *            The xml tree node
     */
    public void visit(final Xml.ResourceMap chunk);

    public void visit(final Xml.Node chunk);
}
