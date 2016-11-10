package com.sdklite.aapt;

/**
 * The basic {@link Chunk}
 * 
 * @author johnsonlee
 */
public class ChunkHeader implements Chunk {

    /**
     * The mininum header size of chunk
     */
    public static final int MIN_HEADER_SIZE = 8;

    /**
     * Type identifier for this chunk. The meaning of this value depends on the
     * containing chunk.
     *
     * @see ChunkType#NULL
     * @see ChunkType#STRING_POOL
     * @see ChunkType#TABLE
     * @see ChunkType#TABLE_LIBRARY
     * @see ChunkType#TABLE_PACKAGE
     * @see ChunkType#TABLE_TYPE
     * @see ChunkType#TABLE_TYPE_SPEC
     * @see ChunkType#XML
     * @see ChunkType#XML_CDATA
     * @see ChunkType#XML_END_ELEMENT
     * @see ChunkType#XML_END_NAMESPACE
     * @see ChunkType#XML_RESOURCE_MAP
     * @see ChunkType#XML_START_ELEMENT
     * @see ChunkType#XML_START_NAMESPACE
     */
    protected final short type;

    /**
     * Size of the chunk header (in bytes). Adding this value to the address of
     * the chunk allows you to find its associated data (if any).
     */
    protected short headerSize;

    /**
     * Total size of this chunk (in bytes). This is the chunkSize plus the size
     * of any data associated with the chunk. Adding this value to the chunk
     * allows you to completely skip its contents (including any child chunks).
     * If this value is the same as chunkSize, there is no data associated with
     * the chunk.
     */
    protected int size;

    /**
     * Create a chunk with specific type
     * 
     * @param type
     *            The type of chunk
     * 
     * @see ChunkType#STRING_POOL
     * @see ChunkType#TABLE
     * @see ChunkType#TABLE_LIBRARY
     * @see ChunkType#TABLE_PACKAGE
     * @see ChunkType#TABLE_TYPE
     * @see ChunkType#TABLE_TYPE_SPEC
     * @see ChunkType#XML
     * @see ChunkType#XML_CDATA
     * @see ChunkType#XML_END_ELEMENT
     * @see ChunkType#XML_END_NAMESPACE
     * @see ChunkType#XML_RESOURCE_MAP
     * @see ChunkType#XML_START_ELEMENT
     * @see ChunkType#XML_START_NAMESPACE
     */
    public ChunkHeader(final short type) {
        this.type = type;
        if (!ChunkType.ALL_TYPES.contains(type)) {
            throw new AaptException(String.format("Unexpected chunk type 0x%02x", type));
        }
    }

    @Override
    public short getType() {
        return this.type;
    }

    @Override
    public short getHeaderSize() {
        return this.headerSize;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public void accept(final ChunkVisitor visitor) {
    }
}
