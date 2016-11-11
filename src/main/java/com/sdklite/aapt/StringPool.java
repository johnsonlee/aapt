package com.sdklite.aapt;

import static com.sdklite.aapt.Internal.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Definition for a pool of strings. The data of this chunk is an array of
 * uint32_t providing indices into the pool, relative to {@link #stringsStart}.
 * At {@link #stringsStart} are all of the strings concatenated together.
 * 
 * For UTF-8 strings, each starts with a uint8_t of the string's length in
 * UTF-16 and a uint8_t of the string's length in UTF-8, and each ends with a
 * 0x00 terminator. If the length is &gt; 0x7f characters, the high bit of the
 * length is set meaning to take those 7 bits as a high word and it will be
 * followed by another uint8_t containing the low word.
 * 
 * For UTF-16 strings, each starts with a uint16_t of the string's length and
 * each ends with a 0x0000 terminator. If a string is &gt; 0x7fff characters,
 * the high bit of the length is set meaning to take those 15 bits as a high
 * word and it will be followed by another uint16_t containing the low word.
 *
 * If styleCount is not zero, then immediately following the array of uint32_t
 * indices into the string table is another array of indices into a style table
 * starting at {@link #stylesStart}. Each entry in the style table is an array
 * of {@link Span} structures.
 */
public class StringPool extends ChunkHeader {

    /**
     * If set, the string index is sorted by the string values (based on
     * strcmp16()).
     */
    public static final int FLAG_SORTED = 1 << 0;

    /**
     * String pool is encoded in UTF-8
     */
    public static final int FLAG_UTF8 = 1 << 8;

    /**
     * The header size of string pool
     */
    public static final short HEADER_SIZE = MIN_HEADER_SIZE + 20;

    public static class Span {

        public static final int END = 0xffffffff;

        public int name;

        public int firstChar;

        public int lastChar;

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("{");
            builder.append("name=").append(this.name);
            builder.append(", firstChar=").append(this.firstChar);
            builder.append(", lastChar=").append(this.lastChar);
            builder.append("}");
            return builder.toString();
        }
    }

    @SuppressWarnings("serial")
    public static final class Style extends ArrayList<Span> {
    }

    final List<IndexedEntry<String>> strings = new ArrayList<IndexedEntry<String>>();

    final List<IndexedEntry<StringPool.Style>> styles = new ArrayList<IndexedEntry<StringPool.Style>>();

    /**
     * @see StringPool#FLAG_SORTED
     * @see StringPool#FLAG_UTF8
     */
    public int flags;

    int stringsStart;

    int stylesStart;

    public StringPool() {
        super(STRING_POOL);
    }

    public int getStringCount() {
        return this.strings.size();
    }

    public String getStringAt(final int index) {
        return this.strings.get(index).value;
    }

    public int getStyleCount() {
        return this.styles.size();
    }

    public StringPool.Style getStyleAt(final int index) {
        return this.styles.get(index).value;
    }

    public boolean isSorted() {
        return 0 != (this.flags & FLAG_SORTED);
    }

    public boolean isUTF8() {
        return 0 != (this.flags & FLAG_UTF8);
    }

    public List<String> strings() {
        return map(this.strings, new Mapper<IndexedEntry<String>, String>() {
            @Override
            public String map(final IndexedEntry<String> e) {
                return e.value;
            }
        });
    }

    @Override
    public void accept(final ChunkVisitor visitor) {
        visitor.visit(this);
    }

    public final int sizeOf(final String s) {
        if (isUTF8()) {
            final byte[] data = s.getBytes(StandardCharsets.UTF_8);
            final int nchars = Unicode.utf8_to_utf16_length(data);
            return (nchars > 0x7f ? 2 : 1) + (data.length > 0x7f ? 2 : 1) + data.length + 1;
        } else {
            final byte[] data = s.getBytes(StandardCharsets.UTF_16LE);
            return (data.length > 0x7fff ? 4 : 2) + data.length + 2;
        }
    }

    public final int sizeOf(final Style s) {
        int stylesSize = 0;

        for (final StringPool.Span span : s) {
            stylesSize += 4;

            if (span.name != StringPool.Span.END) {
                stylesSize += 8;
            }
        }

        stylesSize += 4;

        return stylesSize;
    }

    /**
     * Purge the specified strings from string pool
     * 
     * @param retainedIndices
     *            The index to be retained
     * @return the index map of old index to new index
     */
    public Map<Integer, Integer> purge(final int... retainedIndices) {
        if (null == retainedIndices || retainedIndices.length <= 0) {
            return Collections.<Integer, Integer>emptyMap();
        }

        Arrays.sort(retainedIndices);

        // Re-arrange string pool
        final Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
        final List<IndexedEntry<String>> strings = new ArrayList<IndexedEntry<String>>();
        final List<IndexedEntry<Style>> styles = new ArrayList<IndexedEntry<Style>>();

        for (int i = 0, n = retainedIndices.length, stringOffset = 0, styleOffset = 0; i < n; i++) {
            final int oldIndex = retainedIndices[i];
            indexMap.put(oldIndex, strings.size());

            final IndexedEntry<String> string = this.strings.get(oldIndex);
            string.index = stringOffset;
            strings.add(string);
            stringOffset += sizeOf(string.value);

            if (oldIndex < this.styles.size()) {
                final IndexedEntry<Style> style = this.styles.get(oldIndex);
                style.index = styleOffset;
                styles.add(style);
                styleOffset += sizeOf(style.value);
            }
        }

        // Update span name
        for (final IndexedEntry<Style> style : styles) {
            for (final Span span : style.value) {
                span.name = indexMap.get(span.name);
            }
        }

        this.strings.clear();
        this.strings.addAll(strings);

        this.styles.clear();
        this.styles.addAll(styles);

        return indexMap;
    }
}
