package com.sdklite.aapt;

import static com.sdklite.aapt.Internal.map;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.sdklite.aapt.Internal.Mapper;
import com.sdklite.aapt.Symbols.Entry;

/**
 * The parser of aapt-generated text symbols file {@code R.txt}
 * 
 * @author johnsonlee
 *
 */
public final class SymbolParser {

    /**
     * Parses the R.txt file
     * 
     * @param file
     *            The R.txt file path
     * @return An instance of {@link Symbols}
     * @throws IOException
     *             If error occurred
     */
    public Symbols parse(final String file) throws IOException {
        return this.parse(new File(file));
    }

    /**
     * Parses the R.txt file
     * 
     * @param file
     *            The R.txt file
     * @return An instance of {@link Symbols}
     * @throws IOException
     *             If error occurred
     */
    public Symbols parse(final File file) throws IOException {
        final LineNumberReader reader = new LineNumberReader(new FileReader(file));

        try {
            final Symbols symbols = new Symbols();

            for (String line = null; null != (line = reader.readLine());) {
                final Symbols.Entry entry = parseSymbolEntry(line);
                if (null != entry) {
                    symbols.entries.put(entry.key, entry);
                }
            }

            return symbols;
        } finally {
            reader.close();
        }
    }

    static Entry parseSymbolEntry(final String line) {
        if (line.trim().length() <= 0) {
            return null;
        }

        final StringTokenizer tokenizer = new StringTokenizer(line);
        final String vtype = tokenizer.nextToken().trim();
        final String rtype = tokenizer.nextToken().trim();
        final String key = tokenizer.nextToken().trim();
        final String value = tokenizer.nextToken("\r\n").trim();

        if ("styleable".equals(rtype)) {
            final int lbrace = value.indexOf('{');
            final int rbrace = value.indexOf('}');
            
            if (lbrace >= 0 && rbrace >= 0) {
                final String s = value.substring(lbrace + 1, rbrace).trim();
                if (s.length() <= 0) {
                    return new Symbols.Styleable(vtype, rtype, key, new ArrayList<Integer>());
                }

                return new Symbols.Styleable(vtype, rtype, key, map(s.split(",\\s*"), new Mapper<String, Integer>() {
                    @Override
                    public Integer map(final String e) {
                        return e.startsWith("0x") ? Integer.parseInt(e.substring(2), 16) : Integer.parseInt(e);
                    }
                }));
            }
        }

        return new Symbols.Entry(vtype, rtype, key, value.startsWith("0x") ? Integer.parseInt(value.substring(2), 16) : Integer.parseInt(value));
    }

}
