package com.sdklite.aapt;

import java.io.File;
import java.io.IOException;

/**
 * Represents a recursive descent parser for Android resource file parsing
 * 
 * @author johnsonlee
 *
 */
public class ChunkParser {

    /**
     * Parses the specified file as asset chunk
     * 
     * @param file
     *            The asset file path
     * @return a chunk
     * @throws IOException
     *             if error occurred
     */
    public <T extends Chunk> T parse(final String file) throws IOException {
        return this.parse(new File(file));
    }

    /**
     * Parses the specified file as asset chunk
     * 
     * @param file
     *            The asset file
     * @return a chunk
     * @throws IOException
     *             if error occurred
     */
    @SuppressWarnings("unchecked")
    public <T extends Chunk> T parse(final File file) throws IOException {
        final AssetEditor parser = new AssetEditor(file);

        try {
            return (T) parser.parse();
        } finally {
            parser.close();
        }
    }
}
