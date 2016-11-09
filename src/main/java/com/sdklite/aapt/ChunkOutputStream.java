package com.sdklite.aapt;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import com.sdklite.aapt.ResourceTable.MapEntry;
import com.sdklite.aapt.ResourceTable.ValueEntry;

/**
 * The output stream for {@link Chunk} writing
 * 
 * @author johnsonlee
 *
 */
public class ChunkOutputStream extends FilterOutputStream {

    /**
     * Instantialize with the output stream
     * 
     * @param out
     *            The underlying output stream
     */
    public ChunkOutputStream(final OutputStream out) {
        super(out);
    }

    /**
     * Writes the specified chunk
     * 
     * @param chunk
     *            The resource chunk to be written
     * @throws IOException
     */
    public void write(final Chunk chunk) throws IOException {
        final short type = chunk.getType();

        switch (type) {
        case ChunkType.NULL:
            throw new AaptException("Unsupported chunk type 0");
        case ChunkType.STRING_POOL:
            chunk.accept(new ChunkReconcileVisitor());
            write((StringPool) chunk);
            break;
        case ChunkType.TABLE:
            chunk.accept(new ChunkReconcileVisitor());
            write((ResourceTable) chunk);
            break;
        case ChunkType.TABLE_LIBRARY:
            chunk.accept(new ChunkReconcileVisitor());
            writeResourceTableLibrary((ResourceTable.Library) chunk);
            break;
        case ChunkType.TABLE_PACKAGE:
            chunk.accept(new ChunkReconcileVisitor());
            writeResoruceTablePackage((ResourceTable.Package) chunk);
            break;
        case ChunkType.TABLE_TYPE:
            chunk.accept(new ChunkReconcileVisitor());
            writeResourceTableType((ResourceTable.Type) chunk);
            break;
        case ChunkType.TABLE_TYPE_SPEC:
            chunk.accept(new ChunkReconcileVisitor());
            writeResourceTableTypeSpec((ResourceTable.TypeSpec) chunk);
            break;
        case ChunkType.XML:
            chunk.accept(new ChunkReconcileVisitor());
            write((Xml) chunk);
            break;
        case ChunkType.XML_CDATA:
            chunk.accept(new ChunkReconcileVisitor());
            writeXmlCharData((Xml.CharData) chunk);
            break;
        case ChunkType.XML_END_ELEMENT:
            chunk.accept(new ChunkReconcileVisitor());
            writeXmlEndElement((Xml.EndElement) chunk);
            break;
        case ChunkType.XML_END_NAMESPACE:
            chunk.accept(new ChunkReconcileVisitor());
            writeXmlNamespace((Xml.Namespace) chunk);
            break;
        case ChunkType.XML_RESOURCE_MAP:
            chunk.accept(new ChunkReconcileVisitor());
            writeXmlResourceMap((Xml.ResourceMap) chunk);
            break;
        case ChunkType.XML_START_ELEMENT:
            chunk.accept(new ChunkReconcileVisitor());
            writeXmlStartElement((Xml.StartElement) chunk);
            break;
        case ChunkType.XML_START_NAMESPACE:
            chunk.accept(new ChunkReconcileVisitor());
            writeXmlNamespace((Xml.Namespace) chunk);
            break;
        default:
            throw new AaptException(String.format("Unexpected chunk type 0x%04x", type));
        }
    }

    public void write(final ChunkHeader chunk) throws IOException {
        writeShort(chunk.type);
        writeShort(chunk.headerSize);
        writeInt(chunk.size);
    }

    public void write(final StringPool chunk) throws IOException {
        write(chunk);
        writeInt(chunk.strings.size());
        writeInt(chunk.styles.size());
        writeInt(chunk.flags);
        writeInt(chunk.stringsStart);
        writeInt(chunk.stylesStart);

        for (int i = 0, n = chunk.strings.size(); i < n; i++) {
            writeInt(chunk.strings.get(i).index);
        }

        for (int i = 0, n = chunk.styles.size(); i < n; i++) {
            writeInt(chunk.styles.get(i).index);
        }

        if (chunk.strings.size() > 0) {
            int stringsSize = 0;

            if (chunk.isUTF8()) {
                for (int i = 0, n = chunk.strings.size(); i < n; i++) {
                    stringsSize += writeUtf8String(chunk.strings.get(i).value);
                }
            } else {
                for (int i = 0, n = chunk.strings.size(); i < n; i++) {
                    stringsSize += writeUtf16String(chunk.strings.get(i).value);
                }
            }

            while (stringsSize++ % 4 != 0) {
                write(0x00);
            }
        }

        if (chunk.styles.size() > 0) {
            for (int i = 0, n = chunk.styles.size(); i < n; i++) {
                final StringPool.Style style = chunk.styles.get(i).value;

                for (final StringPool.Span span : style) {
                    writeInt(span.name);
                    writeInt(span.firstChar);
                    writeInt(span.lastChar);
                }

                writeInt(StringPool.Span.END);
            }

            writeInt(StringPool.Span.END);
            writeInt(StringPool.Span.END);
        }
    }

    public void write(final ResourceTable chunk) throws IOException {
        write(chunk);

        final ResourceTable.Package[] packages = chunk.getPackages();

        writeInt(packages.length);
        write(chunk.getStringPool());

        for (int i = 0, n = packages.length; i < n; i++) {
            writeResoruceTablePackage(packages[i]);
        }

        for (final ResourceTable.Library lib : chunk.libraries) {
            writeResourceTableLibrary(lib);
        }

        for (final ResourceTable.PackageGroup group : chunk.packageGroups) {
            for (final ResourceTable.Package pkg : group.packages) {
                for (final ResourceTable.TypeSpec spec : pkg.specs) {
                    writeResourceTableTypeSpec(spec);
                }
            }
        }
    }

    private void writeResourceTableEntry(final ResourceTable.Entry entry) throws IOException {
        writeShort(entry.size);
        writeShort(entry.flags);
        writeInt(entry.key);

        if (entry instanceof ResourceTable.MapEntry) {
            final ResourceTable.MapEntry me = (MapEntry) entry;

            writeInt(me.parent);
            writeInt(me.values.size());

            for (final ResourceTable.Map map : me.values) {
                writeInt(map.name);
                writeResourceValue(map.value);
            }
        } else if (entry instanceof ResourceTable.ValueEntry) {
            final ResourceTable.ValueEntry ve = (ValueEntry) entry;
            writeResourceValue(ve.value);
        }
    }

    private void writeResourceValue(final ResourceValue value) throws IOException {
        writeShort(value.size);
        write(value.res0);
        write(value.dataType);
        writeInt(value.data);
    }

    private void writeResourceTableConfig(final ResourceTable.Config config) throws IOException {
        writeInt(config.size);
        writeShort(config.imsi.mcc);
        writeShort(config.imsi.mnc);
        write(config.locale.language[1]);
        write(config.locale.language[0]);
        write(config.locale.country[1]);
        write(config.locale.country[0]);
        write(config.screenType.orientation);
        write(config.screenType.touchscreen);
        writeShort(config.screenType.density);
        write(config.input.keyboard);
        write(config.input.navigation);
        write(config.input.flags);
        write(config.input.pad0);
        writeShort(config.screenSize.width);
        writeShort(config.screenSize.height);
        writeShort(config.version.sdk);
        writeShort(config.version.minor);

        if (config.size >= 32) {
            write(config.screenConfig.layout);
            write(config.screenConfig.uiMode);
            writeShort(config.screenConfig.smallestWidthDp);
        }

        if (config.size >= 36) {
            writeShort(config.screenSizeDp.width);
            writeShort(config.screenSizeDp.height);
        }

        if (config.size >= 48) {
            write(config.localeScript);
            write(config.localeVariant);
        }

        if (config.size >= 52) {
            write(config.screenConfig2.layout);
            write(config.screenConfig2.pad1);
            writeShort(config.screenConfig2.pad2);
        }
    }

    private void writeResoruceTablePackage(final ResourceTable.Package chunk) throws IOException {
        write(chunk);
        writeInt(chunk.id);
        writePackageName(chunk.name);
        writeInt(chunk.typeStrings);
        writeInt(chunk.lastPublicType);
        writeInt(chunk.keyStrings);
        writeInt(chunk.lastPublicKey);
        writeInt(chunk.typeIdOffset);

        if (0 != chunk.typeStrings) {
            write(chunk.getTypeStringPool());
        }

        if (0 != chunk.keyStrings) {
            write(chunk.getKeyStringPool());
        }
    }

    private void writeResourceTableTypeSpec(final ResourceTable.TypeSpec chunk) throws IOException {
        write(chunk);
        write(chunk.id);
        write(chunk.res0);
        writeShort(chunk.res1);
        writeInt(chunk.flags.size());

        for (final Integer flag : chunk.flags) {
            writeInt(flag);
        }

        for (final ResourceTable.Type config : chunk.configs) {
            writeResourceTableType(config);
        }
    }

    private void writeResourceTableType(final ResourceTable.Type chunk) throws IOException {
        write(chunk);
        write(chunk.id);
        write(chunk.res0);
        writeShort(chunk.res1);
        writeInt(chunk.entries.size());
        writeInt(chunk.entriesStart);
        writeResourceTableConfig(chunk.getConfig());

        for (final IndexedEntry<ResourceTable.Entry> entry : chunk.entries) {
            writeInt(entry.index);
        }

        for (final IndexedEntry<ResourceTable.Entry> entry : chunk.entries) {
            if (entry.index != ResourceTable.Entry.NO_ENTRY) {
                writeResourceTableEntry(entry.value);
            }
        }
    }

    private void writeResourceTableLibrary(final ResourceTable.Library chunk) throws IOException {
        write(chunk);
        writeInt(chunk.entries.size());

        for (final IndexedEntry<String> entry : chunk.entries) {
            writeInt(entry.index);
            writePackageName(entry.value);
        }
    }

    public void writePackageName(final String packageName) throws IOException {
        final CharBuffer buffer = CharBuffer.allocate(128).put(packageName, 0, packageName.length());
        buffer.rewind();
        write(StandardCharsets.UTF_16LE.encode(buffer));
    }

    public void write(final Xml xml) throws IOException {
        write(xml);

        if (null != xml.pool) {
            write(xml.pool);
        }

        if (null != xml.resources) {
            writeXmlResourceMap(xml.resources);
        }

        for (final Xml.Node node : xml.chunks) {
            switch (node.type) {
            case ChunkType.XML_CDATA:
                writeXmlCharData((Xml.CharData) node);
                break;
            case ChunkType.XML_END_ELEMENT:
                writeXmlEndElement((Xml.EndElement) node);
                break;
            case ChunkType.XML_END_NAMESPACE:
                writeXmlNamespace((Xml.EndNamespace) node);
                break;
            case ChunkType.XML_START_ELEMENT:
                writeXmlStartElement((Xml.StartElement) node);
                break;
            case ChunkType.XML_START_NAMESPACE:
                writeXmlNamespace((Xml.StartNamespace) node);
                break;
            default:
                throw new AaptException(String.format("Unexpected chunk type 0x%04x", node.type));
            }
        }
    }

    private void writeXmlResourceMap(final Xml.ResourceMap resources) throws IOException {
        write(resources);
        for (final Integer id : resources.ids) {
            writeInt(id.intValue());
        }
    }

    private void writeXmlNode(final Xml.Node node) throws IOException {
        write(node);
        writeInt(node.lineNumber);
        writeInt(node.commentIndex);
    }

    private void writeXmlStartElement(final Xml.StartElement node) throws IOException {
        writeXmlNode(node);
        writeShort(node.attributeStart);
        writeShort(node.attributeSize);
        writeShort(node.attributes.size());
        writeShort(node.idIndex);
        writeShort(node.classIndex);
        writeShort(node.styleIndex);

        for (final Xml.Attribute attr : node.attributes) {
            writeXmlElementAttribute(attr);
        }
    }

    private void writeXmlElementAttribute(final Xml.Attribute attr) throws IOException {
        writeInt(attr.ns);
        writeInt(attr.name);
        writeInt(attr.rawValue);
        writeResourceValue(attr.typedValue);
    }

    private void writeXmlNamespace(final Xml.Namespace node) throws IOException {
        writeXmlNode(node);
        writeInt(node.prefix);
        writeInt(node.uri);
    }

    private void writeXmlEndElement(final Xml.EndElement node) throws IOException {
        writeXmlNode(node);
        writeInt(node.ns);
        writeInt(node.name);
    }

    private void writeXmlCharData(final Xml.CharData node) throws IOException {
        writeXmlNode(node);
        writeInt(node.data);
        writeResourceValue(node.typedData);
    }

    public final int writeUtf8String(final String s) throws IOException {
        final byte[] data = s.getBytes(StandardCharsets.UTF_8);
        final int nchars = Unicode.utf8_to_utf16_length(data);

        int nbytes = data.length;

        if (nchars > 0x7f) {
            write(((nchars - (nchars & 0xff)) >> 8) | 0x80);
            write(nchars & 0xff);
            nbytes += 2;
        } else {
            write(nchars);
            nbytes += 1;
        }

        if (data.length > 0x7f) {
            write(((data.length - (data.length & 0xff)) >> 8) | 0x80);
            write(data.length & 0xff);
            nbytes += 2;
        } else {
            write(data.length);
            nbytes += 1;
        }

        write(data);
        write(0x0);

        return nbytes + 1;
    }

    public final int writeUtf16String(final String s) throws IOException {
        final byte[] data = s.getBytes(StandardCharsets.UTF_16LE);

        int nbytes = data.length;

        if (data.length > 0x7fff) {
            writeShort(((data.length - (data.length & 0xffff)) >> 16) | 0x8000);
            writeShort(data.length & 0xffff);
            nbytes += 4;
        } else {
            writeShort(data.length);
            nbytes += 2;
        }

        write(data);
        write('\0');

        return nbytes + 2;
    }

    public final void write(final ByteBuffer buffer) throws IOException {
        write(buffer.array());
    }

    public final void write(final char c) throws IOException {
        write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putChar(c).array());
    }

    public final void writeShort(final int v) throws IOException {
        write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) v).array());
    }

    public final void writeInt(final int v) throws IOException {
        write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(v).array());
    }

    public final void writeLong(final long v) throws IOException {
        write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(v).array());
    }
}
