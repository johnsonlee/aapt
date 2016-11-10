package com.sdklite.aapt;

/**
 * Represents a chunk visitor for chunk data reconciling
 * 
 * @author johnsonlee
 *
 */
public class ReconcileChunkVisitor implements ChunkVisitor {

    @Override
    public void visit(final StringPool chunk) {
        chunk.size = chunk.headerSize = StringPool.HEADER_SIZE;

        int stringsSize = 0;
        int stylesSize = 0;


        if (chunk.strings.size() > 0) {
            chunk.stringsStart = chunk.headerSize + chunk.strings.size() * 4 + chunk.styles.size() * 4;

            for (final IndexedEntry<String> entry : chunk.strings) {
                entry.index = stringsSize;
                stringsSize += chunk.sizeOf(entry.value);
            }

            while (stringsSize % 4 != 0) {
                stringsSize++;
            }
        } else {
            chunk.stringsStart = 0;
        }

        if (chunk.styles.size() > 0) {
            chunk.stylesStart = chunk.stringsStart + stringsSize;

            for (final IndexedEntry<StringPool.Style> entry : chunk.styles) {
                entry.index = stylesSize;
                stylesSize += chunk.sizeOf(entry.value);
            }

            stylesSize += 8;
        } else {
            chunk.stylesStart = 0;
        }

        chunk.size = chunk.headerSize + chunk.strings.size() * 4 + chunk.styles.size() * 4 + stringsSize + stylesSize;
    }

    @Override
    public void visit(final ResourceTable chunk) {
        chunk.size = chunk.headerSize = ResourceTable.HEADER_SIZE;

        final StringPool pool = chunk.getStringPool();
        visit(pool);
        chunk.size += pool.size;

        for (final ResourceTable.Library lib : chunk.libraries) {
            visit(lib);
            chunk.size += lib.size;
        }

        for (final ResourceTable.PackageGroup pg : chunk.packageGroups) {
            for (final ResourceTable.Package pkg : pg.packages) {
                visit(pkg);
                chunk.size += pkg.size;
            }
        }
    }

    @Override
    public void visit(final ResourceTable.Package chunk) {
        chunk.size = chunk.headerSize = ResourceTable.Package.HEADER_SIZE;

        final StringPool keyPool = chunk.getKeyStringPool();
        if (null != keyPool) {
            visit(keyPool);
            chunk.size += keyPool.size;
        } else {
            chunk.keyStrings = 0;
        }

        final StringPool typePool = chunk.getTypeStringPool();
        if (null != typePool) {
            visit(typePool);
            chunk.size += typePool.size;
        } else {
            chunk.typeStrings = 0;
        }

        for (final ResourceTable.TypeSpec spec : chunk.specs) {
            visit(spec);

            chunk.size += spec.size;

            for (final ResourceTable.Type config : spec.configs) {
                chunk.size += config.size;
            }
        }
    }

    @Override
    public void visit(final ResourceTable.Type chunk) {
        chunk.headerSize = ResourceTable.Type.HEADER_SIZE;
        chunk.size = chunk.entriesStart = chunk.headerSize + chunk.entries.size() * 4;

        int entriesSize = 0;

        for (final IndexedEntry<ResourceTable.Entry> entry : chunk.entries) {
            if (ResourceTable.Entry.NO_ENTRY != entry.index) {
                entry.index = entriesSize;

                if (entry.value instanceof ResourceTable.ValueEntry) {
                    final ResourceTable.ValueEntry ve = (ResourceTable.ValueEntry) entry.value;
                    ve.size = 8;
                    entriesSize += 8 + ve.size;
                } else if (entry.value instanceof ResourceTable.MapEntry) {
                    final ResourceTable.MapEntry me = (ResourceTable.MapEntry) entry.value;
                    me.size = 16;
                    entriesSize += me.size;
    
                    for (final ResourceTable.Map map: me.values) {
                        map.value.size = 8;
                        entriesSize += 4 + map.value.size;
                    }
                }
            }
        }

        chunk.size += entriesSize;
    }

    @Override
    public void visit(final ResourceTable.TypeSpec chunk) {
        chunk.size = chunk.headerSize + chunk.flags.size() * 4;

        for (final ResourceTable.Type config : chunk.configs) {
            visit(config);
            
        }
    }

    @Override
    public void visit(final ResourceTable.Library chunk) {
    }

    @Override
    public void visit(final Xml chunk) {
    }

    @Override
    public void visit(final Xml.Node chunk) {
    }

    @Override
    public void visit(final Xml.ResourceMap chunk) {
    }

}
