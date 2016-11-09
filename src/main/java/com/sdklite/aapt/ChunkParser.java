package com.sdklite.aapt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Represents a recursive descent parser for Android resource file parsing
 * 
 * @author johnsonlee
 *
 */
public class ChunkParser extends AbstractParser implements ChunkType {

    /**
     * Instantialize with the specified resource file
     * 
     * @param file
     *            The resource file
     * @throws FileNotFoundException
     */
    public ChunkParser(final File file) throws FileNotFoundException {
        super(file, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Instantialized with the specified resource file path
     * 
     * @param file
     *            The resource file path
     * @throws IOException
     */
    public ChunkParser(final String file) throws IOException {
        this(new File(file));
    }

    /**
     * Parse the resource file as a {@link Chunk}
     * 
     * @return The resource chunk
     * @throws IOException
     */
    public Chunk parse() throws IOException {
        this.seek(0);

        switch (peekShort()) {
        case ChunkType.STRING_POOL:
            return parseStringPool();
        case ChunkType.TABLE:
            return parseResourceTable();
        case ChunkType.XML:
            return parseXml();
        default:
            throw new AaptException(String.format("Unsupported chunk type 0x%04x", ChunkType.NULL));
        }
    }

    protected short expectChunkTypes(final short... expected) throws IOException {
        final short actural = peekShort();

        for (int i = 0, n = expected.length; i < n; i++) {
            if (actural == expected[i]) {
                return actural;
            }
        }

        throw new AaptException(String.format("Expect chunk type %s, but 0x%04x found", Internal.hexlify(expected), actural));
    }

    protected <T extends ChunkHeader> T parseChunkHeader(final T chunk) throws IOException {
        final short type = readShort();
        if (chunk.type != type) {
            throw new AaptException(String.format("Expect chunk type 0x%04x, but 0x%04x found", chunk.type, type));
        }

        chunk.headerSize = readShort();
        if (chunk.headerSize < ChunkHeader.MIN_HEADER_SIZE) {
            throw new AaptException(String.format("Chunk header size at least %d bytes", ChunkHeader.MIN_HEADER_SIZE));
        }

        chunk.size = readInt();
        if (chunk.size < ChunkHeader.MIN_HEADER_SIZE || chunk.size < chunk.headerSize) {
            throw new AaptException(String.format("Chunk size at least %d bytes", ChunkHeader.MIN_HEADER_SIZE));
        }

        return chunk;
    }

    protected boolean hasMoreChunks() throws IOException {
        if (this.remaining() < Chunk.MIN_HEADER_SIZE) {
            return false;
        }

        final long p = tell();

        try {
            return ChunkType.ALL_TYPES.contains(this.readShort());
        } finally {
            seek(p);
        }
    }

    ChunkHeader parseChunkHeader() throws IOException {
        return parseChunkHeader(new ChunkHeader(peekShort()));
    }

    ResourceTable parseResourceTable() throws IOException {
        final long p = tell();

        return new ResourceTable() {

            private final StringPool pool;

            {
                parseChunkHeader(this);

                final int npkg = readInt();
                if (npkg <= 0) {
                    throw new AaptException("No packages found");
                }

                this.pool = parseStringPool();

                for (int i = 0; i < npkg; i++) {
                    final ResourceTable.Package pkg = parsePackage(this);
                    final ResourceTable.PackageGroup group;

                    int index = this.packageMap[pkg.id];
                    if (index == 0) {
                        index = this.packageGroups.size() + 1;
                        group = new ResourceTable.PackageGroup(this, pkg.name, pkg.id);
                        this.packageGroups.add(group);
                        this.packageMap[pkg.id] = (byte) index;

                        for (int j = 0, n = this.packageGroups.size(); j < n; j++) {
                            this.packageGroups.get(j).dynamicRefTale.addMapping(pkg.name, (byte) pkg.id);
                        }
                    } else {
                        group = this.packageGroups.get(index - 1);
                        if (null == group) {
                            throw new AaptException("Package group not found");
                        }
                    }

                    group.packages.add(pkg);

                    while (tell() - p < this.size) {
                        switch (expectChunkTypes(TABLE_TYPE, TABLE_TYPE_SPEC, TABLE_LIBRARY)) {
                        case TABLE_TYPE_SPEC: {
                            final ResourceTable.TypeSpec spec = parseResourceTableTypeSpec(pkg);
                            pkg.specs.add(spec);
                            break;
                        }
                        case TABLE_TYPE: {
                            final ResourceTable.Type type = parseResourceTableType(pkg);
                            pkg.specs.get(type.id - 1).configs.add(type);
                            break;
                        }
                        case TABLE_LIBRARY:
                            this.libraries.add(parseResourceTableLibrary(pkg));
                            break;
                        }
                    }
                }
            }

            @Override
            public StringPool getStringPool() {
                return this.pool;
            }
        };
    }

    ResourceTable.Package parsePackage(final ResourceTable table) throws IOException {
        return table.new Package() {

            private final StringPool typePool;
            private final StringPool keyPool;

            {
                parseChunkHeader(this);

                this.id = readInt();
                this.name = parsePackageName();
                this.typeStrings = readInt();
                this.lastPublicType = readInt();
                this.keyStrings = readInt();
                this.lastPublicKey = readInt();

                if (this.headerSize == ResourceTable.Package.HEADER_SIZE) {
                    this.typeIdOffset = readInt();
                } else {
                    this.typeIdOffset = 0;
                }

                if (this.typeStrings != 0) {
                    this.typePool = parseStringPool();
                } else {
                    this.typePool = null;
                }

                if (this.keyStrings != 0) {
                    this.keyPool = parseStringPool();
                } else {
                    this.keyPool = null;
                }
            }

            @Override
            public StringPool getTypeStringPool() {
                return this.typePool;
            }

            @Override
            public StringPool getKeyStringPool() {
                return this.keyPool;
            }
        };
    }

    ResourceTable.TypeSpec parseResourceTableTypeSpec(final ResourceTable.Package pkg) throws IOException {
        final ResourceTable table = pkg.getResourceTable();
        return table.new TypeSpec() {
            {
                parseChunkHeader(this);

                this.id = read();
                if (this.id < 1) {
                    throw new AaptException(String.format("Invalid type specification id %d", this.id));
                }

                this.res0 = read();
                if (0 != res0) {
                    throw new AaptException("res0 expected to be zero");
                }

                this.res1 = readShort();
                if (0 != this.res1) {
                    throw new AaptException("res1 expected to be zero");
                }

                for (int i = 0, entryCount = readInt(); i < entryCount; i++) {
                    this.flags.add(readInt());
                }
            }

            @Override
            public ResourceTable.Package getPackage() {
                return pkg;
            }
        };
    }

    ResourceTable.Type parseResourceTableType(final ResourceTable.Package pkg) throws IOException {
        final long p = tell();
        final ResourceTable table = pkg.getResourceTable();

        return table.new Type() {
            {
                parseChunkHeader(this);

                this.id = read();
                if (this.id < 1) {
                    throw new AaptException(String.format("Invalid type id %d", this.id));
                }

                this.res0 = read();
                if (0 != res0) {
                    throw new AaptException("res0 expected to be zero");
                }

                this.res1 = readShort();
                if (0 != this.res1) {
                    throw new AaptException("res1 expected to be zero");
                }

                final int entryCount = readInt();
                this.entriesStart = readInt();
                this.config.size = readInt();
                this.config.imsi.mcc = readShort();
                this.config.imsi.mnc = readShort();
                this.config.locale.language[1] = read();
                this.config.locale.language[0] = read();
                this.config.locale.country[1] = read();
                this.config.locale.country[0] = read();
                this.config.screenType.orientation = read();
                this.config.screenType.touchscreen = read();
                this.config.screenType.density = readShort();
                this.config.input.keyboard = read();
                this.config.input.navigation = read();
                this.config.input.flags = read();
                this.config.input.pad0 = read();
                this.config.screenSize.width = readShort();
                this.config.screenSize.height = readShort();
                this.config.version.sdk = readShort();
                this.config.version.minor = readShort();

                if (this.config.size >= 32) {
                    this.config.screenConfig.layout = read();
                    this.config.screenConfig.uiMode = read();
                    this.config.screenConfig.smallestWidthDp = readShort();
                }

                if (this.config.size >= 36) {
                    this.config.screenSizeDp.width = readShort();
                    this.config.screenSizeDp.height = readShort();
                }

                if (this.config.size >= 48) {
                    this.config.localeScript[0] = read();
                    this.config.localeScript[1] = read();
                    this.config.localeScript[2] = read();
                    this.config.localeScript[3] = read();
                    this.config.localeVariant[0] = read();
                    this.config.localeVariant[1] = read();
                    this.config.localeVariant[2] = read();
                    this.config.localeVariant[3] = read();
                    this.config.localeVariant[4] = read();
                    this.config.localeVariant[5] = read();
                    this.config.localeVariant[6] = read();
                    this.config.localeVariant[7] = read();
                }

                if (this.config.size >= 52) {
                    this.config.screenConfig2.layout = read();
                    this.config.screenConfig2.pad1 = read();
                    this.config.screenConfig2.pad2 = readShort();
                }

                seek(p + this.headerSize);

                for (int i = 0; i < entryCount; i++) {
                    this.entries.add(new IndexedEntry<ResourceTable.Entry>(readInt(), null));
                }

                final long entriesStart = p + this.entriesStart;

                for (int i = 0; i < entryCount; i++) {
                    final IndexedEntry<ResourceTable.Entry> entry = this.entries.get(i);

                    if (ResourceTable.Entry.NO_ENTRY != entry.index) {
                        seek(entriesStart + entry.index);
                        entry.value = parseResourceTableEntry();
                    }
                }

                seek(p + this.size);
            }

            @Override
            public ResourceTable.Package getPackage() {
                return pkg;
            }

            @Override
            public ResourceTable.Config getConfig() {
                return this.config;
            }
        };
    }

    ResourceTable.Entry parseResourceTableEntry() throws IOException {
        final ResourceTable.Entry entry = new ResourceTable.Entry() {
            {
                this.size = readShort();
                this.flags = readShort();
                this.key = readInt();
            }
        };

        return (entry.flags & ResourceTable.Entry.FLAG_COMPLEX) != 0 ? parseResourceTableMapEntry(entry)
                : parseResourceTableValueEntry(entry);
    }

    ResourceTable.MapEntry parseResourceTableMapEntry(final ResourceTable.Entry base) throws IOException {
        final ResourceTable.MapEntry entry = new ResourceTable.MapEntry(base);
        entry.parent = readInt();

        for (int i = 0, count = readInt(); i < count; i++) {
            entry.values.add(parseResourceTableMap());
        }

        return entry;
    }

    ResourceTable.Map parseResourceTableMap() throws IOException {
        final ResourceTable.Map map = new ResourceTable.Map();
        map.name = readInt();
        parseResourceValue(map.value);
        return map;
    }

    ResourceValue parseResourceValue() throws IOException {
        return parseResourceValue(new ResourceValue());
    }

    ResourceValue parseResourceValue(final ResourceValue value) throws IOException {
        value.size = readShort();
        value.res0 = read();
        value.dataType = read();
        value.data = readInt();
        return value;
    }

    ResourceTable.ValueEntry parseResourceTableValueEntry(final ResourceTable.Entry base) throws IOException {
        final ResourceTable.ValueEntry entry = new ResourceTable.ValueEntry(base);
        parseResourceValue(entry.value);
        return entry;
    }

    ResourceTable.Library parseResourceTableLibrary(final ResourceTable.Package pkg) throws IOException {
        final ResourceTable table = pkg.getResourceTable();
        return table.new Library() {
            {
                parseChunkHeader(this);

                for (int i = 0, count = readInt(); i < count; i++) {
                    this.entries.add(new IndexedEntry<String>(readInt(), parsePackageName()));
                }
            }
        };
    }

    String parsePackageName() throws IOException {
        final long p = tell();
        final StringBuilder name = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            final char c = readChar();
            if (c == 0) {
                break;
            }

            name.append(c);
        }

        seek(p + 256);
        return name.toString();
    }

    Xml parseXml() throws IOException {
        return new Xml() {
            {
                parseChunkHeader(this);

                while (hasRemaining()) {
                    switch (expectChunkTypes(ChunkType.STRING_POOL, ChunkType.XML_RESOURCE_MAP, ChunkType.XML_CDATA, ChunkType.XML_END_ELEMENT, ChunkType.XML_END_NAMESPACE, ChunkType.XML_START_ELEMENT, ChunkType.XML_START_NAMESPACE)) {
                    case ChunkType.STRING_POOL:
                        this.pool = parseStringPool();
                        break;
                    case ChunkType.XML_RESOURCE_MAP:
                        this.resources = parseXmlResourceMap(this);
                        break;
                    case ChunkType.XML_CDATA:
                        this.chunks.add(parseXmlCharData(this));
                        break;
                    case ChunkType.XML_END_ELEMENT:
                        this.chunks.add(parseXmlEndElement(this));
                        break;
                    case ChunkType.XML_END_NAMESPACE:
                        this.chunks.add(parseXmlEndNamespace(this));
                        break;
                    case ChunkType.XML_START_ELEMENT:
                        this.chunks.add(parseXmlStartElement(this));
                        break;
                    case ChunkType.XML_START_NAMESPACE:
                        this.chunks.add(parseXmlStartNamespace(this));
                        break;
                    }
                }
            }
        };
    }

    <T extends Xml.Node> T parseXmlNode(final T node) throws IOException {
        parseChunkHeader(node);
        node.lineNumber = readInt();
        node.commentIndex = readInt();
        return node;
    }

    Xml.StartNamespace parseXmlStartNamespace(final Xml xml) throws IOException {
        return parseXmlNamespace(xml.new StartNamespace());
    }

    Xml.EndNamespace parseXmlEndNamespace(final Xml xml) throws IOException {
        return parseXmlNamespace(xml.new EndNamespace());
    }

    <T extends Xml.Namespace> T parseXmlNamespace(final T namespace) throws IOException {
        parseXmlNode(namespace);
        namespace.prefix = readInt();
        namespace.uri = readInt();
        return namespace;
    }

    <T extends Xml.Element> T parseXmlElement(final T element) throws IOException {
        parseXmlNode(element);
        element.ns = readInt();
        element.name = readInt();
        return element;
    }

    Xml.StartElement parseXmlStartElement(final Xml xml) throws IOException {
        final Xml.StartElement element = parseXmlElement(xml.new StartElement());
        element.attributeStart = readShort();
        element.attributeSize = readShort();
        final short attributeCount = readShort();
        element.idIndex = readShort();
        element.classIndex = readShort();
        element.styleIndex = readShort();

        for (int i = 0; i < attributeCount; i++) {
            element.attributes.add(parseXmlElementAttribute(element));
        }

        return element;
    }

    Xml.EndElement parseXmlEndElement(final Xml xml) throws IOException {
        return parseXmlElement(xml.new EndElement());
    }

    Xml.CharData parseXmlCharData(final Xml xml) throws IOException {
        final Xml.CharData cdata = parseXmlNode(xml.new CharData());
        cdata.data = readInt();
        parseResourceValue(cdata.typedData);
        return cdata;
    }

    Xml.ResourceMap parseXmlResourceMap(final Xml xml) throws IOException {
        final Xml.ResourceMap resourceMap = parseChunkHeader(xml.new ResourceMap());
        for (int i = 0, n = (resourceMap.size - resourceMap.headerSize) / 4; i < n; i++) {
            resourceMap.ids.add(readInt());
        }
        return resourceMap;
    }

    Xml.Attribute parseXmlElementAttribute(final Xml.StartElement element) throws IOException {
        final Xml.Attribute attr = element.getDocument().new Attribute();
        attr.ns = readInt();
        attr.name = readInt();
        attr.rawValue = readInt();
        parseResourceValue(attr.typedValue);
        return attr;
    }

    StringPool parseStringPool() throws IOException {
        final long p = tell();

        return new StringPool() {
            {
                parseChunkHeader(this);

                final int stringCount = readInt();
                final int styleCount = readInt();

                this.flags = readInt();
                this.stringsStart = readInt();
                this.stylesStart = readInt();

                for (int i = 0; i < stringCount; i++) {
                    this.strings.add(new IndexedEntry<String>(readInt(), null));
                }

                for (int i = 0; i < styleCount; i++) {
                    this.styles.add(new IndexedEntry<StringPool.Style>(readInt(), new StringPool.Style()));
                }

                for (int i = 0; i < stringCount; i++) {
                    final IndexedEntry<String> entry = this.strings.get(i);
                    seek(p + this.stringsStart + entry.index);
                    entry.value = isUTF8() ? parseUtf8String() : parseUtf16String();
                }

                for (int i = 0; i < styleCount; i++) {
                    final IndexedEntry<StringPool.Style> entry = this.styles.get(i);
                    seek(p + this.stylesStart + entry.index);

                    for (long p = tell(); StringPool.Span.END != readInt(); p = tell()) {
                        seek(p);

                        final StringPool.Span span = parseStringPoolSpan();
                        entry.value.add(span);
                        if (span.name == StringPool.Span.END) {
                            break;
                        }
                    }
                }

                seek(p + this.size);
            }
        };

    }

    StringPool.Span parseStringPoolSpan() throws IOException {
        final StringPool.Span span = new StringPool.Span();
        span.name = readInt() & 0xffffffff;
        if (span.name != StringPool.Span.END) {
            span.firstChar = readInt();
            span.lastChar = readInt();
        }

        return span;
    }

    String parseUtf8String() throws IOException {
        int nchars = read() & 0xff;
        if ((nchars & 0x80) != 0) {
            nchars = ((nchars & 0x7f) << 8) | (read() & 0xff);
        }

        int nbytes = read() & 0xff;
        if ((nbytes & 0x80) != 0) {
            nbytes = ((nbytes & 0x7f) << 8) | (read() & 0xff);
        }

        final ByteBuffer str = ByteBuffer.allocate(nbytes).order(ByteOrder.LITTLE_ENDIAN);
        read(str);
        str.rewind();

        final int terminator = read();
        if (0 != terminator) {
            throw new AaptException(String.format("Zero terminator expected at position %d, but 0x%02x found", tell() - 1, terminator));
        }

        return StandardCharsets.UTF_8.decode(str).toString();
    }

    String parseUtf16String() throws IOException {
        int nchars = readShort();
        if ((nchars & 0x8000) != 0) {
            nchars = ((nchars & 0x7fff) << 16) | (readShort() & 0xffff);
        }

        final ByteBuffer data = ByteBuffer.allocate(nchars * 2).order(ByteOrder.LITTLE_ENDIAN);
        read(data);
        data.rewind();

        final int terminator = readChar();
        if (0 != terminator) {
            throw new AaptException(String.format("Zero terminator expected at position %d, buf 0x%04x found", (tell() - 2), terminator));
        }

        return StandardCharsets.UTF_16LE.decode(data).toString();
    }

    public void rewritePackageId(final int pp, final Map<Integer, Integer> idMap) throws IOException {
        final long pos = tell();
        int id = readInt();

        if ((id >> 24) != Constants.APP_PACKAGE_ID) {
            return;
        }

        if (null != idMap && idMap.containsKey(id)) {
            id = idMap.get(id);
        } else {
            id = ((pp & 0xff) << 24) | (id & 0x00ffffff);
        }

        seek(pos);
        writeInt(id);
    }
}
