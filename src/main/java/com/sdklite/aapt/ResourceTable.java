package com.sdklite.aapt;

import static com.sdklite.aapt.Internal.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Represents the android resource table
 * 
 * @author johnsonlee
 *
 */
public abstract class ResourceTable extends ChunkHeader {

    public static class DynamicReferenceTable {

        final byte assignedPackageId;

        final byte[] lookupTable = new byte[256];

        final java.util.Map<String, Byte> entries = new LinkedHashMap<String, Byte>();

        public DynamicReferenceTable(final int packageId) {
            this.assignedPackageId = (byte) packageId;
            this.lookupTable[Constants.APP_PACKAGE_ID] = Constants.APP_PACKAGE_ID;
            this.lookupTable[Constants.SYS_PACKAGE_ID] = Constants.SYS_PACKAGE_ID;
        }

        public void addMapping(final String packageName, final byte packageId) {
            if (this.entries.containsKey(packageName)) {
                this.lookupTable[this.entries.get(packageName).intValue()] = packageId;
            }
        }
    }

    public static class PackageGroup {

        final ResourceTable owner;

        final DynamicReferenceTable dynamicRefTale;

        final List<ResourceTable.Package> packages = new ArrayList<ResourceTable.Package>();

        String name;

        int id;

        public PackageGroup(final ResourceTable owner, final String name, final int id) {
            this.owner = owner;
            this.name = name;
            this.id = id;
            this.dynamicRefTale = new DynamicReferenceTable(id);
        }

        public Package getPackage(final int packageId) {
            for (final ResourceTable.Package pkg : this.packages) {
                if (pkg.id == packageId) {
                    return pkg;
                }
            }

            return null;
        }

        public String getName() {
            return this.name;
        }

        public int getId() {
            return this.id;
        }

        public List<ResourceTable.Package> getPackages() {
            return this.packages;
        }
    }

    public abstract class Component extends ChunkHeader {

        public Component(final short type) {
            super(type);
        }

        public final ResourceTable getResourceTable() {
            return ResourceTable.this;
        }
    }

    public abstract class Package extends Component {

        public static final int HEADER_SIZE = 288;

        final List<ResourceTable.TypeSpec> specs = new ArrayList<ResourceTable.TypeSpec>();

        int id;

        String name;

        /**
         * Offset to a {@link StringPool} defining the resource type symbol
         * table. If zero, this package is inheriting from another base package
         * (overriding specific values in it).
         */
        int typeStrings;

        int lastPublicType;

        /**
         * Offset to a {@link StringPool} defining the resource key symbol
         * table. If zero, this package is inheriting from another base package
         * (overriding specific values in it).
         */
        int keyStrings;

        int lastPublicKey;

        int typeIdOffset;

        public Package() {
            super(TABLE_PACKAGE);
        }

        public int getId() {
            return this.id;
        }

        public void setId(final int id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public abstract StringPool getTypeStringPool();

        public abstract StringPool getKeyStringPool();

        @Override
        public void accept(final Visitor visitor) {
            visitor.visit(this);
        }

    }

    /**
     * Describes a particular resource configuration.
     */
    public final class Config {

        public final class Imsi {

            /**
             * Mobile country code (from SIM). 0 means "any"
             */
            short mcc;

            /**
             * Mobile network code (from SIM). 0 means "any".
             */
            short mnc;
        }

        public final class Locale {

            /**
             * This field can take three different forms:
             *
             * <ul>
             * <li>\0\0 means "any".</li>
             * <li>Two 7 bit ascii values interpreted as ISO-639-1 language
             * codes ('fr', 'en' etc. etc.). The high bit for both bytes is
             * zero.</li>
             * <li>A single 16 bit little endian packed value representing an
             * ISO-639-2 3 letter language code. This will be of the form:
             * 
             * <pre>
             * { 1, t, t, t, t, t, s, s, s, s, s, f, f, f, f, f }
             * </pre>
             * 
             * bit[0, 4] = first letter of the language code<br/>
             * bit[5, 9] = second letter of the language code<br/>
             * bit[10, 14] = third letter of the language code.<br/>
             * bit[15] = 1 always<br/>
             * </li>
             * </ul>
             *
             * For backwards compatibility, languages that have unambiguous two
             * letter codes are represented in that format.
             *
             * The layout is always bigendian irrespective of the runtime
             * architecture.
             */
            final byte[] language = new byte[2];

            /**
             * This field can take three different forms:
             *
             * <ul>
             * <li>\0\0 means "any".</li>
             * <li>Two 7 bit ascii values interpreted as 2 letter region codes
             * ('US', 'GB' etc.). The high bit for both bytes is zero.</li>
             * <li>An UN M.49 3 digit region code. For simplicity, these are
             * packed in the same manner as the language codes, though we should
             * need only 10 bits to represent them, instead of the 15.</li>
             * </ul>
             *
             * he layout is always bigendian irrespective of the runtime
             * architecture.
             */
            final byte[] country = new byte[2];

            public boolean isDefined() {
                return this.language[0] == 0 && this.language[1] == 0 && this.country[0] == 0 && this.country[1] == 0;
            }
        }

        public final class ScreenType {

            /**
             * Orientation: not specified
             */
            public static final int ORIENTATION_ANY = 0x0000;
            public static final int ORIENTATION_PORT = 0x0001;
            public static final int ORIENTATION_LAND = 0x0002;
            public static final int ORIENTATION_SQUARE = 0x0003;

            public static final int TOUCHSCREEN_ANY = 0x0000;
            public static final int TOUCHSCREEN_NOTOUCH = 0x0001;
            public static final int TOUCHSCREEN_STYLUS = 0x0002;
            public static final int TOUCHSCREEN_FINGER = 0x0003;

            public static final short DENSITY_DEFAULT = 0;
            public static final short DENSITY_LOW = 120;
            public static final short DENSITY_MEDIUM = 160;
            public static final short DENSITY_TV = 213;
            public static final short DENSITY_HIGH = 240;
            public static final short DENSITY_XHIGH = 320;
            public static final short DENSITY_XXHIGH = 480;
            public static final short DENSITY_XXXHIGH = 640;
            public static final short DENSITY_ANY = (short) 0xfffe;
            public static final short DENSITY_NONE = (short) 0xffff;

            public static final int KEYBOARD_ANY = 0x0000;
            public static final int KEYBOARD_NOKEYS = 0x0001;
            public static final int KEYBOARD_QWERTY = 0x0002;
            public static final int KEYBOARD_12KEY = 0x0003;

            public static final int NAVIGATION_ANY = 0x0000;
            public static final int NAVIGATION_NONAV = 0x0001;
            public static final int NAVIGATION_DPAD = 0x0002;
            public static final int NAVIGATION_TRACKBALL = 0x0003;
            public static final int NAVIGATION_WHEEL = 0x0004;

            public static final int KEYSHIDDEN_ANY = 0x0000;
            public static final int KEYSHIDDEN_NO = 0x0001;
            public static final int KEYSHIDDEN_YES = 0x0002;
            public static final int KEYSHIDDEN_SOFT = 0x0003;

            public static final int NAVHIDDEN_ANY = 0x0000;
            public static final int NAVHIDDEN_NO = 0x0001;
            public static final int NAVHIDDEN_YES = 0x0002;

            public static final int SCREENWIDTH_ANY = 0x0000;

            public static final int SCREENHEIGHT_ANY = 0x0000;

            public static final int SDKVERSION_ANY = 0x0000;

            public static final int MINORVERSION_ANY = 0x0000;

            public static final int SCREENSIZE_ANY = 0x00;
            public static final int SCREENSIZE_SMALL = 0x01;
            public static final int SCREENSIZE_NORMAL = 0x02;
            public static final int SCREENSIZE_LARGE = 0x03;
            public static final int SCREENSIZE_XLARGE = 0x04;

            byte orientation;
            byte touchscreen;
            short density;
        }

        public final class Input {
            byte keyboard;
            byte navigation;
            byte flags;
            byte pad0;
        }

        public final class ScreenSize {
            short width;
            short height;
        }

        public final class ScreenConfig {
            byte layout;
            byte uiMode;
            short smallestWidthDp;
        }

        public final class ScreenConfig2 {
            byte layout;
            byte pad1;
            short pad2;
        }

        public final class Version {
            short sdk;
            short minor; // always 0
        }

        byte[] unpackLanguageOrRegion(final byte[] data, final byte base) {
            if (0 != (data[0] & 0x80)) {
                final byte first = (byte) (data[1] & 0x1f);
                final byte second = (byte) (((data[1] & 0xe0) >> 5) + ((data[0] & 0x03) << 3));
                final byte third = (byte) ((data[0] & 0x7c) >> 2);
                return new byte[] { (byte) (first + base), (byte) (second + base), (byte) (third + base) };
            }

            if (0 != data[0]) {
                return new byte[] { data[0], data[1] };
            }

            return new byte[0];
        }

        int size;

        final Imsi imsi = new Imsi();

        final Locale locale = new Locale();

        final ScreenType screenType = new ScreenType();

        final Input input = new Input();

        final ScreenSize screenSize = new ScreenSize();

        final Version version = new Version();

        final ScreenConfig screenConfig = new ScreenConfig();

        final ScreenSize screenSizeDp = new ScreenSize();

        /**
         * The ISO-15924 short name for the script corresponding to this
         * configuration. (eg. Hant, Latn, etc.). Interpreted in conjunction
         * with the locale field.
         */
        final byte[] localeScript = new byte[4];

        /**
         * A single BCP-47 variant subtag. Will vary in length between 4 and 8
         * chars. Interpreted in conjunction with the locale field.
         */
        final byte[] localeVariant = new byte[8];

        final ScreenConfig2 screenConfig2 = new ScreenConfig2();

        byte[] unpackLaunguage() {
            return unpackLanguageOrRegion(this.locale.language, (byte) 0x61);
        }

        byte[] unpackRegion() {
            return unpackLanguageOrRegion(this.locale.country, (byte) 0x30);
        }

        void appendLocaleDir(final StringBuilder out) {
            if (0 != this.locale.language[0]) {
                return;
            }

            if (0 == this.localeScript[0] && 0 == this.localeVariant[0]) {
                if (out.length() > 0) {
                    out.append("-");
                }

                out.append(new String(unpackLaunguage()));

                if (0 != this.locale.country[0]) {
                    out.append("-r");
                    out.append(new String(unpackRegion()));
                }

                return;
            }

            if (out.length() > 0) {
                out.append("-");
            }

            out.append("b+");
            out.append(new String(unpackLaunguage()));

            if (0 != this.localeScript[0]) {
                out.append("+");
                out.append(new String(this.localeScript));
            }

            if (0 != this.locale.country[0]) {
                out.append("+");
                out.append(new String(unpackRegion()));
            }
            
            if (0 != this.localeVariant[0]) {
                out.append("+");
                out.append(new String(this.localeVariant));
            }
        }

        @Override
        public String toString() {
            final StringBuilder res = new StringBuilder();

            if (this.imsi.mcc != 0) {
                if (res.length() > 0) {
                    res.append("-");
                }
                res.append(String.format("mcc%d", this.imsi.mcc));
            }

            if (this.imsi.mnc != 0) {
                if (res.length() > 0) {
                    res.append("-");
                }
                res.append(String.format("mnc%d", this.imsi.mnc));
            }

            appendLocaleDir(res);

            if (this.screenConfig.smallestWidthDp != 0) {
                if (res.length() > 0) {
                    res.append("-");
                }
                res.append(String.format("sw%ddp", this.screenConfig.smallestWidthDp));
            }

            if (this.screenSizeDp.width != 0) {
                if (res.length() > 0) {
                    res.append("-");
                }
                res.append(String.format("w%ddp", this.screenSizeDp.width));
            }

            if (this.screenSizeDp.height != 0) {
                if (res.length() > 0) {
                    res.append("-");
                }
                res.append(String.format("h%ddp", this.screenSizeDp.width));
            }

            if (this.screenType.orientation != ScreenType.ORIENTATION_ANY) {
                if (res.length() > 0) {
                    res.append("-");
                }

                switch (this.screenType.orientation) {
                case ScreenType.ORIENTATION_PORT:
                    res.append("port");
                    break;
                case ScreenType.ORIENTATION_LAND:
                    res.append("land");
                    break;
                case ScreenType.ORIENTATION_SQUARE:
                    res.append("square");
                    break;
                default:
                    res.append(String.format("orientation=%d", this.screenType.orientation));
                }
            }

            if (this.screenType.density != ScreenType.DENSITY_DEFAULT) {
                if (res.length() > 0) {
                    res.append("-");
                }

                final short density = this.screenType.density;

                switch (density) {
                case ScreenType.DENSITY_LOW:
                    res.append("ldpi");
                    break;
                case ScreenType.DENSITY_MEDIUM:
                    res.append("mdpi");
                    break;
                case ScreenType.DENSITY_TV:
                    res.append("tvdpi");
                    break;
                case ScreenType.DENSITY_HIGH:
                    res.append("hdpi");
                    break;
                case ScreenType.DENSITY_XHIGH:
                    res.append("xhdpi");
                    break;
                case ScreenType.DENSITY_XXHIGH:
                    res.append("xxhdpi");
                    break;
                case ScreenType.DENSITY_XXXHIGH:
                    res.append("xxxhdpi");
                    break;
                case ScreenType.DENSITY_NONE:
                    res.append("nodpi");
                    break;
                case ScreenType.DENSITY_ANY:
                    res.append("anydpi");
                    break;
                default:
                    res.append(String.format("%ddpi", density));
                    break;
                }
            }

            if (this.screenSize.width != 0 || this.screenSize.height != 0) {
                if (res.length() > 0) {
                    res.append("-");
                }
                res.append(String.format("%dx%d", this.screenSize.width, this.screenSize.height));
            }

            if (this.version.sdk != 0 || this.version.minor != 0) {
                if (res.length() > 0) {
                    res.append("-");
                }
                res.append(String.format("v%d", this.version.sdk));
                if (this.version.minor != 0) {
                    res.append(String.format(".%d", this.version.minor));
                }
            }

            return res.length() <= 0 ? "(default)" : res.toString();
        }
    }

    /**
     * This is the beginning of information about an entry in the resource
     * table. It holds the reference to the name of this entry, and is
     * immediately followed by one of:
     * <ul>
     * <li>A {@link ResourceValue} structure, if {@link #FLAG_COMPLEX} is not
     * set.</li>
     * <li>An array of {@link Map} structures, if {@link #FLAG_COMPLEX} is set.
     * </li>
     * </ul>
     * These supply a set of name/value mappings of data.
     */
    public static abstract class Entry {

        public static final int NO_ENTRY = 0xffffffff;

        public static final int FLAG_COMPLEX = 0x0001;

        public static final int FLAG_PUBLIC = 0x0002;

        public static final int FLAG_WEAK = 0x0004;

        /**
         * Number of bytes in this structure
         */
        short size;

        /**
         * @see #FLAG_COMPLEX
         * @see #FLAG_PUBLIC
         * @see #FLAG_WEAK
         */
        public short flags;

        /**
         * Reference into {@link ResourceTable.Package#keyStrings} identifying
         * this entry
         */
        public int key;

        public Entry() {
        }

        public Entry(final Entry entry) {
            this.size = entry.size;
            this.flags = entry.flags;
            this.key = entry.key;
        }
    }

    /**
     * Extended form of a {@link Entry} for map entries, defining a parent map
     * resource from which to inherit values.
     */
    public static final class MapEntry extends Entry {

        final List<Map> values = new ArrayList<Map>();

        public int parent;

        public MapEntry() {
        }

        public MapEntry(final Entry parent) {
            super(parent);
        }

        public int getValueCount() {
            return this.values.size();
        }

        public Map getValueAt(final int index) {
            return this.values.get(index);
        }

        public void setValueAt(final int index, final Map value) {
            this.values.set(index, value);
        }

        public void addValue(final Map value) {
            this.values.add(value);
        }
    }

    public static final class ValueEntry extends Entry {

        final ResourceValue value = new ResourceValue();

        public ValueEntry() {
        }

        public ValueEntry(final Entry parent) {
            super(parent);
        }

        public ResourceValue getValue() {
            return this.value;
        }

        public void setValue(final ResourceValue value) {
            this.value.size = value.size;
            this.value.res0 = value.res0;
            this.value.dataType = value.dataType;
            this.value.data = value.data;
        }
    }

    /**
     * A single name/value mapping that is part of a complex resource entry.
     */
    public static final class Map {

        final ResourceValue value = new ResourceValue();

        public int name;
    }

    /**
     * Resource configuration
     * 
     * @author johnsonlee
     *
     */
    public abstract class Type extends Component {

        public static final short HEADER_SIZE = 72;

        final Config config = new Config();

        final List<IndexedEntry<Entry>> entries = new ArrayList<IndexedEntry<Entry>>();

        /**
         * The type identifier this chunk is holding. Type IDs start at 1
         * (corresponding to the value of the type bits in a resource
         * identifier). 0 is invalid.
         */
        byte id;

        byte res0;

        short res1;

        int entriesStart;

        public Type() {
            super(TABLE_TYPE);
        }

        public abstract ResourceTable.Config getConfig();

        public abstract ResourceTable.Package getPackage();

        public boolean hasAvailableEntries() {
            for (final IndexedEntry<Entry> entry : this.entries) {
                if (entry.value != null) {
                    return true;
                }
            }

            return false;
        }

        public List<IndexedEntry<Entry>> getEntries() {
            return this.entries;
        }

        public Entry getEntryAt(final int index) {
            return this.entries.get(index).value;
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visit(this);
        }

    }

    /**
     * Resource type specification
     * 
     * @author johnsonlee
     *
     */
    public abstract class TypeSpec extends Component {

        /**
         * Configuration change flags
         */
        final List<Integer> flags = new ArrayList<Integer>();

        final List<Type> configs = new ArrayList<Type>();

        /**
         * The type identifier this chunk is holding. Type IDs start at 1
         * (corresponding to the value of the type bits in a resource
         * identifier). 0 is invalid.
         */
        byte id;

        byte res0;

        short res1;

        public TypeSpec() {
            super(TABLE_TYPE_SPEC);
        }

        public List<Integer> getFlags() {
            return this.flags;
        }

        public List<Type> getConfigs() {
            return this.configs;
        }

        public int getEntryCount() {
            return this.flags.size();
        }

        public boolean isEntryAvailable(final int entryIndex) {
            if (entryIndex >= this.flags.size()) {
                throw new IllegalArgumentException(
                        String.format("Entry index out of range [0, %d]", this.flags.size()));
            }

            return false;
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visit(this);
        }

        public abstract ResourceTable.Package getPackage();

        public java.util.Map<ResourceName, Integer> entries() {
            final Package pkg = getPackage();
            final StringPool typePool = pkg.getTypeStringPool();
            final StringPool keyPool = pkg.getKeyStringPool();
            final java.util.Map<ResourceName, Integer> entries = new LinkedHashMap<ResourceName, Integer>();

            for (final Type config : this.configs) {
                for (int i = 0, n = config.entries.size(); i < n; i++) {
                    final IndexedEntry<Entry> entry = config.entries.get(i);
                    if (Entry.NO_ENTRY == entry.index || null == entry.value) {
                        continue;
                    }

                    final String typeName = typePool.getStringAt(this.id - 1);
                    final String keyName = keyPool.getStringAt(entry.value.key);
                    entries.put(new ResourceName(pkg.name, typeName, keyName), i);
                }
            }

            return entries;
        }
    }

    /**
     * Shared library
     * 
     * @author johnsonlee
     *
     */
    public class Library extends Component {

        final List<IndexedEntry<String>> entries = new ArrayList<IndexedEntry<String>>();

        public Library() {
            super(TABLE_LIBRARY);
        }

        public List<IndexedEntry<String>> getEntries() {
            return this.entries;
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visit(this);
        }

    }

    public static final int HEADER_SIZE = MIN_HEADER_SIZE + 4;

    final List<ResourceTable.PackageGroup> packageGroups = new ArrayList<ResourceTable.PackageGroup>();

    final List<Integer> resourceIdMap = new ArrayList<Integer>();

    final List<ResourceTable.Library> libraries = new ArrayList<ResourceTable.Library>();

    final byte[] packageMap = new byte[256];

    int cookie;

    public ResourceTable() {
        super(TABLE);
    }

    public List<PackageGroup> getPackageGroups() {
        return this.packageGroups;
    }

    public abstract StringPool getStringPool();

    public ResourceName getResourceName(final int resId) {
        final int packageIndex = getPackageIndex(resId);
        final int typeIndex = Internal.getType(resId);
        final int entryIndex = Internal.getEntry(resId);

        if (packageIndex < 0) {
            throw new AaptException("Unknown resource package");
        }

        if (typeIndex < 0) {
            throw new AaptException("Unknown resource type");
        }

        final PackageGroup group = this.packageGroups.get(packageIndex);
        if (null == group) {
            throw new AaptException(String.format("Unknown resource package index %d", packageIndex));
        }

        final ResourceTable.Package pkg = group.getPackage(Internal.getPackage(resId) + 1);
        final ResourceTable.TypeSpec spec = pkg.specs.get(typeIndex);

        for (final ResourceTable.Type type : spec.configs) {
            final IndexedEntry<ResourceTable.Entry> entry = type.entries.get(entryIndex);
            if (Entry.NO_ENTRY == entry.index || null == entry.value) {
                continue;
            }

            final StringPool typePool = type.getPackage().getTypeStringPool();
            final StringPool keyPool = type.getPackage().getKeyStringPool();
            return new ResourceName(group.name, typePool.getStringAt(spec.id - 1), null == entry ? "null" : keyPool.getStringAt(entry.value.key));
        }

        return null;
    }

    public int getPackageIndex(final int resId) {
        return this.packageMap[Internal.getPackage(resId) + 1] - 1;
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    public ResourceTable.Package[] getPackages() {
        final List<ResourceTable.Package> packages = new ArrayList<ResourceTable.Package>();

        for (ResourceTable.PackageGroup group : this.packageGroups) {
            packages.addAll(group.packages);
        }

        return packages.toArray(new ResourceTable.Package[packages.size()]);
    }

    /**
     * Purge resource entries with the specified symbols
     * 
     * @param symbols
     *            The resource symbols
     */
    public void purge(final Symbols symbols) {
        this.purge(symbols, java.util.Collections.<Integer, Integer>emptyMap());
    }

    /**
     * Purge resource entries with the specified symbols and replace resource id
     * with the specified id mapping
     * 
     * @param symbols
     *            The resource symbols
     * @param idMap
     *            The resource id mapping, oldId =&gt; newId
     */
    public void purge(final Symbols symbols, java.util.Map<Integer, Integer> idMap) {
        final List<Integer> libPackageIds = new ArrayList<Integer>();

        final List<Integer> retainedStringIds = new ArrayList<Integer>(); // index of string in string pool
        final StringPool pool = getStringPool();
        for (final IndexedEntry<StringPool.Style> style : pool.styles) {
            for (final StringPool.Span span : style.value) {
                if (!retainedStringIds.contains(span.name)) {
                    retainedStringIds.add(span.name);
                }
            }
        }

        final Collection<Symbols.Type> uncompactedTypes = symbols.types();
        final java.util.Map<String, Integer> uncompactedTypeIds = new HashMap<String, Integer>();
        for (final Symbols.Type type : uncompactedTypes) {
            uncompactedTypeIds.put(type.name, type.id);
        }

        final Collection<Symbols.Type> compactedTypes = symbols.clone().compact().types();
        final java.util.Map<String, Integer> compactedTypeIds = new HashMap<String, Integer>();
        for (final Symbols.Type type : compactedTypes) {
            compactedTypeIds.put(type.name, type.id);
        }

        final Set<String> typeNames = new HashSet<String>(map(uncompactedTypes, new Mapper<Symbols.Type, String>() {
            @Override
            public String map(final Symbols.Type e) {
                return e.name;
            }
        }));

        for (final ResourceTable.PackageGroup pg : this.packageGroups) {
            for (final ResourceTable.Package pkg : pg.packages) {
                final List<Integer> retainedKeyIds = new ArrayList<Integer>(); // index of string in key string pool
                final List<ResourceTable.TypeSpec> retainedTypeSpecs = findAll(pkg.specs, new Filter<ResourceTable.TypeSpec>() {
                    @Override
                    public boolean accept(final TypeSpec it) {
                        return typeNames.contains(pkg.getTypeStringPool().getStringAt(it.id - 1));
                    }
                });

                for (final ResourceTable.TypeSpec spec : retainedTypeSpecs) {
                    final String typeName = pkg.getTypeStringPool().getStringAt(spec.id - 1);
                    final Set<String> retainedEntryNames = new HashSet<String>(map(symbols.entries(typeName), new Mapper<Symbols.Entry, String>() {
                        @Override
                        public String map(final Symbols.Entry e) {
                            return e.name;
                        }
                    }));

                    // Purge flags
                    final java.util.Map<ResourceName, Integer> entries = spec.entries();
                    final List<Integer> flags = new ArrayList<Integer>();
                    for (final ResourceName rn : entries.keySet()) {
                        if (!retainedEntryNames.contains(rn.name)) {
                            continue;
                        }

                        final int entryIndex = entries.get(rn);
                        flags.add(spec.flags.get(entryIndex));
                    }

                    spec.flags.clear();
                    spec.flags.addAll(flags);
                    spec.id = compactedTypeIds.get(typeName).byteValue();

                    // Purge config entries
                    final List<ResourceTable.Type> configs = new ArrayList<ResourceTable.Type>();
                    for (int i = 0, configCount = spec.configs.size(); i < configCount; i++) {
                        final ResourceTable.Type config = spec.configs.get(i);
                        if (config.entries.isEmpty()) {
                            continue;
                        }

                        final List<IndexedEntry<ResourceTable.Entry>> retainedEntries = new ArrayList<IndexedEntry<ResourceTable.Entry>>();

                        for (int j = 0, entryCount = config.entries.size(); j < entryCount; j++) {
                            final IndexedEntry<ResourceTable.Entry> entry = config.entries.get(j);
                            if (Entry.NO_ENTRY == entry.index || entry.value == null) {
                                continue;
                            }

                            final String entryName = pkg.getKeyStringPool().getStringAt(entry.value.key);
                            if (!retainedEntryNames.contains(entryName)) {
                                continue;
                            }

                            retainedEntries.add(entry);

                            if (!retainedKeyIds.contains(entry.value.key)) {
                                retainedKeyIds.add(entry.value.key);
                            }

                            if (entry.value instanceof ResourceTable.ValueEntry) {
                                final ResourceTable.ValueEntry ve = (ResourceTable.ValueEntry) entry.value;

                                switch (ve.value.dataType) {
                                case ValueType.STRING: {
                                    final int oldId = ve.value.data;
                                    final int newId = retainedStringIds.indexOf(oldId);

                                    if (newId < 0) {
                                        retainedStringIds.add(oldId);
                                        ve.value.data = retainedStringIds.size() - 1;
                                    } else {
                                        ve.value.data = newId;
                                    }

                                    break;
                                }
                                case ValueType.REFERENCE: {
                                    if (idMap.containsKey(ve.value.data)) {
                                        ve.value.data = idMap.get(ve.value.data);
                                    }
                                    break;
                                }
                                default:
                                    break;
                                }
                            } else if (entry.value instanceof ResourceTable.MapEntry) {
                                final ResourceTable.MapEntry me = (ResourceTable.MapEntry) entry.value;
                                if (idMap.containsKey(me.parent)) {
                                    me.parent = idMap.get(me.parent);
                                }

                                for (final ResourceTable.Map map : me.values) {
                                    if (idMap.containsKey(map.name)) {
                                        map.name = idMap.get(map.name);
                                    }

                                    switch (map.value.dataType) {
                                    case ValueType.STRING: {
                                        final int oldId = map.value.data;
                                        final int newId = retainedStringIds.indexOf(oldId);

                                        if (newId < 0) {
                                            retainedStringIds.add(oldId);
                                            map.value.data = retainedStringIds.size() - 1;
                                        } else {
                                            map.value.data = newId;
                                        }

                                        break;
                                    }
                                    case ValueType.REFERENCE: {
                                        if (idMap.containsKey(map.value.data)) {
                                            final int id = idMap.get(map.value.data);
                                            map.value.data = id;

                                            final int pkgId = (id >> 24) & 0xff;
                                            if (pkgId != Constants.APP_PACKAGE_ID && pkgId != Constants.SYS_PACKAGE_ID && pkgId != symbols.packageId) {
                                                libPackageIds.add(pkgId);
                                            }
                                        }

                                        break;
                                    }
                                    default:
                                        break;
                                    }
                                }
                            }
                        }

                        if (retainedEntries.isEmpty()) {
                            continue;
                        }

                        config.id = spec.id;
                        config.entries.clear();
                        config.entries.addAll(retainedEntries);
                        configs.add(config);
                    }

                    spec.configs.clear();
                    spec.configs.addAll(configs);
                }

                // Reset entry key reference
                final java.util.Map<Integer, Integer> keyMap = new HashMap<Integer, Integer>();
                for (int i = 0, n = retainedKeyIds.size(); i < n; i++) {
                    keyMap.put(retainedKeyIds.get(i), i);
                }

                for (final ResourceTable.TypeSpec spec : retainedTypeSpecs) {
                    for (final ResourceTable.Type config : spec.configs) {
                        for (final IndexedEntry<ResourceTable.Entry> entry : config.entries) {
                            if (Entry.NO_ENTRY == entry.index || null == entry.value) {
                                continue;
                            }

                            entry.value.key = keyMap.get(entry.value.key);
                        }
                    }
                }

                java.util.Collections.sort(retainedTypeSpecs, new Comparator<ResourceTable.TypeSpec>() {
                    @Override
                    public int compare(final TypeSpec ts1, final TypeSpec ts2) {
                        return ts1.id - ts2.id;
                    }
                });
                pkg.specs.clear();
                pkg.specs.addAll(retainedTypeSpecs);

                // Purge type string pool
                final int[] retainedTypes = toArray(map(uncompactedTypes, new Mapper<Symbols.Type, Integer>() {
                    @Override
                    public Integer map(final Symbols.Type e) {
                        return e.id - 1;
                    }
                }), int.class);
                pkg.getTypeStringPool().purge(retainedTypes);

                // Purge key string pool
                final int[] retainedKeys = toArray(retainedKeyIds, int.class);
                pkg.getKeyStringPool().purge(retainedKeys);
            }
        }

        // Purge string pool
        final int[] retainedStrings = toArray(retainedStringIds, int.class);
        this.getStringPool().purge(retainedStrings);
    }

    public void dump(final OutputStream output) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ChunkOutputStream cos = new ChunkOutputStream(baos);
        cos.write(this);
        cos.flush();
        cos.close();

        final PrintWriter out = new PrintWriter(output, true);
        final byte[] data = baos.toByteArray();
        final byte[] line = new byte[16];

        for (int i = 0, n = data.length; i < n; i++) {
            if (i % 0x10 == 0) {
                if (i > 0) {
                    out.print(" ");

                    for (int j = 0; j < line.length; j++) {
                        final byte b = line[j];
                        out.printf("%c", (Internal.isVisibleCharacter(b)) ? b : '.');
                    }

                    out.println();
                }

                out.printf("%07x:", i);
            }

            if (i % 2 == 0) {
                out.print(" ");
            }

            line[i % 16] = data[i];
            out.printf("%02x", data[i]);
        }
    }
}
