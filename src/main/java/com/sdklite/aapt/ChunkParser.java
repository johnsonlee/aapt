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

    public ChunkParser() {
    }

    public <T extends Chunk> T parse(final String file) throws IOException {
        return this.parse(new File(file));
    }

    @SuppressWarnings("unchecked")
    public <T extends Chunk> T parse(final File file) throws IOException {
        final AssetParser parser = new AssetParser(file);

        try {
            return (T) parser.parse();
        } finally {
            parser.close();
        }
    }
}
