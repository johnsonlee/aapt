package com.sdklite.aapt;

/**
 * Android asset chunk
 * 
 * @author johnsonlee
 */
public interface Chunk extends ChunkType {

    /**
     * The minimum chunk header size
     */
    public static final int MIN_HEADER_SIZE = 8;

    /**
     * Returns the type identifier for this chunk. The meaning of this value
     * depends on the containing chunk.
     * 
     * @return the chunk type
     */
    public short getType();

    /**
     * Returns the size of the chunk header (in bytes). Adding this value to the
     * address of the chunk allows you to find its associated data (if any).
     * 
     * @return the header size in bytes
     */
    public short getHeaderSize();

    /**
     * Returns the total size of this chunk (in bytes). This is the chunkSize
     * plus the size of any data associated with the chunk. Adding this value to
     * the chunk allows you to completely skip its contents (including any child
     * chunks). If this value is the same as chunkSize, there is no data
     * associated with the chunk.
     * 
     * @return the total size of this chunk
     */
    public int getSize();

    /**
     * Accept the specific chunk visitor
     * 
     * @param visitor
     *            The chunk visitor
     */
    public void accept(final ChunkVisitor visitor);

}
