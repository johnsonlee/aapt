package com.sdklite.aapt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;

import com.sdklite.aapt.ResourceTable.ValueEntry;

/**
 * 
 * @author johnsonlee
 *
 */
public class ResourcesVisitor extends SimpleVisitor implements ChunkType, ValueType {

    final PrintWriter out;

    public ResourcesVisitor(final PrintWriter out) {
        this.out = out;
    }

    public ResourcesVisitor(final PrintStream out) {
        this(new PrintWriter(out));
    }

    public ResourcesVisitor(final PrintStream out, final boolean autoFlush) {
        this(new PrintWriter(out, autoFlush));
    }

    public ResourcesVisitor(final OutputStream out) {
        this(new PrintStream(out));
    }

    public ResourcesVisitor(final OutputStream out, final boolean autoFlush) {
        this(new PrintStream(out), autoFlush);
    }

    public ResourcesVisitor(final File file) throws FileNotFoundException {
        this(new PrintStream(file));
    }

    public ResourcesVisitor(final String file) throws FileNotFoundException {
        this(new PrintStream(file));
    }

    @Override
    public void visit(final ResourceTable chunk) {
        final int groupCount = chunk.packageGroups.size();
        this.out.printf("Package Groups (%d)", groupCount).println();

        for (int grpIdx = 0; grpIdx < groupCount; grpIdx++) {
            final ResourceTable.PackageGroup group = chunk.packageGroups.get(grpIdx);
            this.out.printf("Package Group %d id=0x%02x packageCount=%d name=%s", grpIdx, group.id, group.packages.size(), group.name).println();

            final Map<String, Byte> entries = group.dynamicRefTale.entries;
            if (entries.size() > 0) {
                this.out.printf("  DynamicRefTable entryCount=%d", entries.size()).println();

                for (final Map.Entry<String, Byte> entry : entries.entrySet()) {
                    this.out.printf("    0x%02x -> %s", entry.getValue(), entry.getKey());
                }

                this.out.println();
            }

            for (int pkgIdx = 0, pkgCount = group.packages.size(); pkgIdx < pkgCount; pkgIdx++) {
                final ResourceTable.Package pkg = group.packages.get(pkgIdx);
                this.out.printf("  Package %d id=0x%02x name=%s", pkgIdx, pkg.id, pkg.name).println();

                for (final ResourceTable.TypeSpec spec : pkg.specs) {
                    visit(spec);
                }
            }
        }
    }

    @Override
    public void visit(final ResourceTable.TypeSpec chunk) {
        if (chunk.configs.isEmpty()) {
            return;
        }

        final ResourceTable.Package pkg = chunk.getPackage();
        final ResourceTable table = pkg.getResourceTable();
        final StringPool typePool = pkg.getTypeStringPool();
        final StringPool keyPool = pkg.getKeyStringPool();

        this.out.printf("  type %d configCount=%d entryCount=%d", chunk.id - 1, chunk.configs.size(), chunk.flags.size()).println();

        for (int i = 0, entryCount = chunk.flags.size(); i < entryCount; i++) {
            final int config = chunk.flags.get(i);
            final int resId = ((pkg.id << 24) & 0xff000000) | ((chunk.id << 16) & 0x00ff0000) | ((i) & 0x0000ffff);
            this.out.printf("    spec resource 0x%02x%02x%04x %s:%s/%s: flags=0x%08x", pkg.id, chunk.id, i, pkg.name, typePool.getStringAt(chunk.id - 1), table.getResourceName(resId).name, config).println();
        }

        for (int i = 0, n = chunk.configs.size(); i < n; i++) {
            final ResourceTable.Type type = chunk.configs.get(i);
            this.out.printf("    config %s:", type.getConfig()).println();

            if (type.entries.size() > 0) {
                for (int j = 0; j < type.entries.size(); j++) {
                    final ResourceTable.Entry entry = type.getEntryAt(j);

                    if (entry instanceof ResourceTable.MapEntry) {
                        final ResourceTable.MapEntry me = (ResourceTable.MapEntry) entry;

                        for (final ResourceTable.Map map : me.values) {
                            this.out.printf("      resource 0x%02x%02x%04x %s:%s/%s: parent=0x%08x t=0x%02x d=0x%08x n=0x%08x", pkg.id, type.id, j, pkg.name, typePool.getStringAt(type.id - 1), keyPool.getStringAt(entry.key), me.parent, map.value.dataType, map.value.data, map.name).println();
                        }
                    } else if (entry instanceof ResourceTable.ValueEntry) {
                        final ResourceTable.ValueEntry ve = (ValueEntry) entry;
                        this.out.printf("      resource 0x%02x%02x%04x %s:%s/%s: t=0x%02x d=0x%08x", pkg.id, type.id, j, pkg.name, typePool.getStringAt(type.id - 1), keyPool.getStringAt(entry.key), ve.value.dataType, ve.value.data).println();
                    }
                }
            }
        }

        this.out.println();
    }

}
