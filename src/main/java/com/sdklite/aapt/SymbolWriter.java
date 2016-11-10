package com.sdklite.aapt;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sdklite.aapt.Symbols.Styleable;

/**
 * Represents a writer for R.txt writting
 * 
 * @author johnsonlee
 *
 */
public class SymbolWriter extends FilterWriter {

    /**
     * Instantialize with the specified writer
     * 
     * @param out
     *            The underlying writer
     */
    public SymbolWriter(final Writer out) {
        super(out);
    }

    /**
     * Write the specified symbols into stream
     * 
     * @param symbols
     *            The symbols to be written
     * @throws IOException
     */
    public void write(final Symbols symbols) throws IOException {
        for (final Map.Entry<String, Symbols.Entry> entry : symbols.entries.entrySet()) {
            final Symbols.Entry e = entry.getValue();

            out.write(e.vtype);
            out.write(" ");
            out.write(e.type.name);
            out.write(" ");
            out.write(e.name);
            out.write(" ");

            if (e instanceof Styleable) {
                final List<Integer> values = ((Styleable) e).values;
                out.write("{ ");
                for (final Iterator<Integer> i = values.iterator(); i.hasNext();) {
                    out.write(String.format("0x%08x", i.next()));
                    if (i.hasNext()) {
                        out.write(", ");
                    }
                }
                out.write(" }");
            } else {
                out.write(String.format("0x%08x", e.value));
            }

            out.write("\r\n");
        }
    }
}
