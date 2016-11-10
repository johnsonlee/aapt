package com.sdklite.aapt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A chunk visitor for string pool traversing
 * 
 * @author johnsonlee
 *
 */
public class StringPoolVisitor extends SimpleVisitor {

    final PrintWriter out;

    public StringPoolVisitor(final PrintWriter out) {
        this.out = out;
    }

    public StringPoolVisitor(final PrintStream out) {
        this(new PrintWriter(out));
    }

    public StringPoolVisitor(final PrintStream out, final boolean autoFlush) {
        this(new PrintWriter(out, autoFlush));
    }

    public StringPoolVisitor(final OutputStream out) {
        this(new PrintStream(out));
    }

    public StringPoolVisitor(final OutputStream out, final boolean autoFlush) {
        this(new PrintStream(out), autoFlush);
    }

    public StringPoolVisitor(final File file) throws FileNotFoundException {
        this(new PrintStream(file));
    }

    public StringPoolVisitor(final String file) throws FileNotFoundException {
        this(new PrintStream(file));
    }

    @Override
    public void visit(final ResourceTable chunk) {
        final StringPool pool = chunk.getStringPool();

        this.out.printf("String pool of %d unique %s %s strings, %d entries and %d styles using %d bytes:", 
                pool.strings.size(),
                pool.isUTF8() ? "UTF-8" : "UTF-16", 
                pool.isSorted() ? "sorted" : "non-sorted",
                pool.strings.size(),
                pool.styles.size(),
                pool.size).println();

        for (int i = 0, n = pool.strings.size(); i < n; i++) {
            final IndexedEntry<String> string = pool.strings.get(i);
            this.out.printf("String #%d: %s [0x%08x]", i, string.value, string.index).println();
        }

        this.out.println();

        for (int i = 0, n = pool.styles.size(); i < n; i++) {
            final IndexedEntry<StringPool.Style> style = pool.styles.get(i);
            this.out.printf("Style #%d: %s [0x%08x]", i, style.value, style.index).println();
        }

        this.out.println();

        for (final ResourceTable.Package pkg : chunk.getPackages()) {
            final StringPool typePool = pkg.getTypeStringPool();
            this.out.printf("Type string pool of %d unique %s %s strings, %d entries and %d styles using %d bytes:", 
                    typePool.strings.size(),
                    typePool.isUTF8() ? "UTF-8" : "UTF-16", 
                    typePool.isSorted() ? "sorted" : "non-sorted",
                    typePool.strings.size(),
                    typePool.styles.size(),
                    typePool.size).println();
            for (int i = 0, n = typePool.strings.size(); i < n; i++) {
                final IndexedEntry<String> string = typePool.strings.get(i);
                this.out.printf("String #%d: %s [0x%08x]", i, string.value, string.index).println();
            }

            this.out.println();

            final StringPool keyPool = pkg.getKeyStringPool();
            this.out.printf("Key string pool of %d unique %s %s strings, %d entries and %d styles using %d bytes:", 
                    keyPool.strings.size(),
                    keyPool.isUTF8() ? "UTF-8" : "UTF-16", 
                    keyPool.isSorted() ? "sorted" : "non-sorted",
                    keyPool.strings.size(),
                    keyPool.styles.size(),
                    keyPool.size).println();
            for (int i = 0, n = keyPool.strings.size(); i < n; i++) {
                final IndexedEntry<String> string = keyPool.strings.get(i);
                this.out.printf("String #%d: %s [0x%08x]", i, string.value, string.index).println();
            }
        }
        
    }

}
