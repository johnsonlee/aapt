package com.sdklite.aapt;

import static com.sdklite.aapt.Internal.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Representation of text symbols
 * 
 * @author johnson
 *
 */
public class Symbols implements Cloneable, Iterable<Map.Entry<String, Symbols.Entry>> {

    public static final class Type implements Cloneable {

        public final String name;

        int id;

        public Type(final String name) {
            this.name = name;
        }

        public Type(String name, int id) {
            this(name);
            this.id = id;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof Type)) {
                return false;
            }

            final Type type = (Type) obj;
            return this.name.equals(type.name) && this.id == type.id;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode() * 31 + id;
        }

        @Override
        public Type clone() {
            return new Type(this.name, this.id);
        }

        @Override
        public String toString() {
            return this.name + " : " + id;
        }
    }

    public static class Entry implements Cloneable {

        public final String vtype;
        public final Type type;
        public final String name;
        public final String key;

        int value;

        public Entry(final String vtype, final String type, final String name) {
            this.vtype = vtype;
            this.type = new Type(type);
            this.name = name;
            this.key = type + "/" + name;
        }

        public Entry(final String vtype, final Type type, final String name) {
            this.vtype = vtype;
            this.type = type;
            this.name = name;
            this.key = type.name + "/" + name;
        }

        public Entry(final String vtype, final String type, final String name, final int value) {
            this(vtype, new Type(type, (value >> 16) & 0xff), name);
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;

            if (!(obj instanceof Entry)) {
                return false;
            }

            final Entry entry = (Entry) obj;
            return this.vtype.equals(entry.vtype)
                    && this.type.equals(entry.type)
                    && this.name.equals(entry.name)
                    && this.value == entry.value;
        }

        @Override
        public int hashCode() {
            int hash = this.key.hashCode();
            hash = hash * 31 + this.vtype.hashCode();
            hash = hash * 31 + this.type.hashCode();
            hash = hash * 31 + this.name.hashCode();
            hash = hash * 31 + this.value;
            return hash;
        }

        @Override
        protected Entry clone() {
            return new Entry(this.vtype, this.type.name, this.name, this.value);
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(this.vtype).append(" ");
            builder.append(this.type.name).append(" ");
            builder.append(this.name).append(" ");
            builder.append(String.format("0x%08x", this.value));
            return builder.toString();
        }
    }

    public static class Styleable extends Entry {

        final List<Integer> values;

        public Styleable(final String valueType, final String type, final String key, final List<Integer> values) {
            super(valueType, type, key);
            this.values = values;
        }

        public Styleable(final String valueType, final Type type, final String key, final List<Integer> values) {
            super(valueType, type, key);
            this.values = values;
        }

        @Override
        public Styleable clone() {
            final List<Integer> values = Arrays.asList(this.values.toArray(new Integer[this.values.size()]));
            return new Styleable(this.vtype, this.type.clone(), this.key, values);
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(this.vtype).append(" ");
            builder.append(this.type).append(" ");
            builder.append(this.name).append(" { ");

            for (final Iterator<Integer> i = this.values.iterator(); i.hasNext();) {
                builder.append(String.format("0x%08x", i.next()));

                if (i.hasNext()) {
                    builder.append(", ");
                }
            }

            return builder.append(" }").toString();
        }
    }

    int packageId = Constants.APP_PACKAGE_ID;

    final Map<String, Entry> entries = new TreeMap<String, Entry>();

    Symbols() {
    }

    public void setPackageId(final int packageId) {
        this.packageId = packageId & 0xff;
    }

    /**
     * Dump symbols to R.txt
     * 
     * @param r
     *            The R.txt file
     * 
     * @throws IOException
     */
    public void dump(final File r) throws IOException {
        if (!r.getParentFile().exists()) {
            r.getParentFile().mkdirs();
        }

        if (!r.exists()) {
            r.createNewFile();
        }

        final PrintWriter out = new PrintWriter(r);

        try {
            for (final Map.Entry<String, Symbols.Entry> entry : this.entries.entrySet()) {
                final Symbols.Entry e = entry.getValue();
                out.printf(e.toString()).println();
            }
        } finally {
            out.flush();
            out.close();
        }

    }

    /**
     * Merge the specified symbols into this
     * 
     * @param symbols
     *            The symbols to merge
     * @return The merged symbols
     */
    public Symbols merge(final Symbols symbols) {
        final Symbols clone = this.clone();
        clone.entries.putAll(symbols.entries);
        return clone.compact();
    }

    /**
     * Split the specified symbols from this
     * 
     * @param provided
     *            The provided symbols
     * @return the split symbols
     */
    public Symbols split(final Symbols provided) {
        final Symbols clone = this.clone();
        clone.entries.keySet().removeAll(provided.entries.keySet());
        return clone.compact();
    }

    public Collection<Map.Entry<Symbols.Entry, Symbols.Entry>> diff(final Symbols symbols) {
        final Collection<Map.Entry<Entry, Entry>> diff = new ArrayList<Map.Entry<Entry, Entry>>();

        for (final Map.Entry<String, Entry> entry : this.entries.entrySet()) {
            final String key = entry.getKey();
            final Entry lEntry = entry.getValue();

            if (symbols.entries.containsKey(key)) {
                final Entry rEntry = symbols.entries.get(key);
                if (!lEntry.equals(rEntry)) {
                    diff.add(new AbstractMap.SimpleEntry<Entry, Entry>(lEntry, rEntry));
                }
            } else {
                diff.add(new AbstractMap.SimpleEntry<Entry, Entry>(lEntry, null));
            }
        }

        for (final String key : symbols.entries.keySet()) {
            if (!this.entries.containsKey(key)) {
                diff.add(new AbstractMap.SimpleEntry<Entry, Entry>(null, symbols.entries.get(key)));
            }
        }

        return diff;
    }

    /**
     * Compact the type id and entry id of symbols
     */
    public Symbols compact() {
        // Re-arrange the retained resource id and type id
        int nextTypeId = 2; // 1 is reserved for `attr`
        final Map<String, Integer> typeIds = new HashMap<String, Integer>(); // typeName => typeId
        final Map<String, Integer> nextEntryIds = new HashMap<String, Integer>(); // typeName => entryId

        typeIds.put("attr", 1);

        for (final Map.Entry<String, Entry> pair : this.entries.entrySet()) {
            final Entry entry = pair.getValue();

            if ("styleable".equals(entry.type.name)) {
                continue;
            }

            if (typeIds.containsKey(entry.type.name)) {
                entry.type.id = typeIds.get(entry.type.name);
            } else {
                entry.type.id = nextTypeId++;
                typeIds.put(entry.type.name, entry.type.id);
            }

            if (nextEntryIds.containsKey(entry.type.name)) {
                nextEntryIds.put(entry.type.name, nextEntryIds.get(entry.type.name) + 1);
            } else {
                nextEntryIds.put(entry.type.name, 0);
            }

            final int newEntryId = nextEntryIds.get(entry.type.name);
            entry.value = ((this.packageId & 0xff) << 24) | (entry.type.id << 16) | newEntryId;
        }

        final List<Entry> styleables = findAll(this.entries.values(), new Filter<Entry>() {
            @Override
            public boolean accept(final Entry it) {
                return it instanceof Styleable && ((Styleable) it).values.size() > 0;
            }
        });

        // Update the attr reference
        for (final Map.Entry<String, Entry> pair : this.entries.entrySet()) {
            final Entry entry = pair.getValue();

            if ("attr".equals(entry.type)) {
                for (final Entry se : styleables) {
                    final Styleable styleable = (Styleable) se;
                    final int index = styleable.values.indexOf(entry.value);

                    if (index >= 0 && styleable.values.get(index) != entry.value) {
                        styleable.values.set(index, entry.value);
                    }
                }
            }
        }

        return this;
    }

    public boolean hasType(final String type) {
        for (final Entry entry : this.entries.values()) {
            if (entry.type.name.equals(type)) {
                return true;
            }
        }

        return false;
    }

    public void put(final Symbols.Entry entry) {
        this.entries.put(entry.key, entry);
    }

    public void remove(final String key) {
        this.entries.remove(key);
    }

    @Override
    public Iterator<Map.Entry<String, Symbols.Entry>> iterator() {
        return this.entries.entrySet().iterator();
    }

    public Collection<Entry> entries() {
        return this.entries.values();
    }

    public Collection<Entry> entries(final Type type) {
        return findAll(this.entries.values(), new Filter<Entry>() {
            @Override
            public boolean accept(final Entry it) {
                return it.type.equals(type);
            }
        });
    }

    public Collection<Entry> entries(final int typeId) {
        return findAll(this.entries.values(), new Filter<Entry>() {
            @Override
            public boolean accept(final Entry it) {
                return it.type.id == typeId;
            }
        });
    }

    public Collection<Entry> entries(final String type) {
        return findAll(this.entries.values(), new Filter<Entry>() {
            @Override
            public boolean accept(final Entry it) {
                return it.type.name.equals(type);
            }
        });
    }

    public Collection<Type> types() {
        final Set<Type> types = new TreeSet<Type>(new Comparator<Type>() {
            @Override
            public int compare(final Type left, final Type right) {
                final int diff = left.id - right.id;
                if (diff != 0) {
                    return diff;
                }

                return left.name.compareTo(right.name);
            }
        });

        types.add(new Type("attr", 1));

        for (final Entry entry : this.entries.values()) {
            types.add(entry.type);
        }

        return types;
    }

    /**
     * Retrieve symbol entry with the specified key
     * 
     * @param key
     *            The entry key
     * @return a symbol entry
     */
    public Symbols.Entry getEntry(final String key) {
        return this.entries.get(key);
    }

    @Override
    protected Symbols clone() {
        final Symbols symbols = new Symbols();
        symbols.packageId = this.packageId;

        for (final Map.Entry<String, Symbols.Entry> pair : this.entries.entrySet()) {
            symbols.entries.put(pair.getKey(), pair.getValue().clone());
        }

        return symbols;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        
        for (final Map.Entry<String, Symbols.Entry> entry : this.entries.entrySet()) {
            final Symbols.Entry e = entry.getValue();
            builder.append(e.vtype);
            builder.append(" ").append(e.type.name);
            builder.append(" ").append(e.name);
            builder.append(" ").append(String.format("0x%08x", e.value));
            builder.append("\r\n");
        }

        return builder.toString();
    }
}
