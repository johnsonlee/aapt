package com.sdklite.aapt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.android.dex.ClassDef;
import com.android.dex.Dex;
import com.sdklite.io.IOUtil;

/**
 * Represents an APK file
 * 
 * @author johnsonlee
 *
 */
public class ApkFile {

    private final ZipFile archive;

    /**
     * Instantialize with the specified APK file path
     * 
     * @param apk
     *            APK file path
     * @throws IOException
     *             if file not exists
     */
    public ApkFile(final String apk) throws IOException {
        this(new File(apk));
    }

    /**
     * Instantialize with the specified APK file path
     * 
     * @param apk
     *            APK file
     * @throws IOException
     *             if file not exists
     */
    public ApkFile(final File apk) throws IOException {
        this.archive = new ZipFile(apk);
    }

    /**
     * Returns the decoded {@code AndroidManifest.xml}
     * 
     * @return The XML tree of AndroidManifest.xml
     * @throws IOException
     *             if error occurred
     */
    public Xml getAndroidManifest() throws IOException {
        final ZipEntry entry = this.archive.getEntry(Aapt.ANDROID_MANIFEST_XML);
        final File tmp = File.createTempFile("AndroidManifest", "xml");
        final InputStream in = this.archive.getInputStream(entry);
        final OutputStream out = new FileOutputStream(tmp);

        try {
            IOUtil.copy(in, out);
        } finally {
            IOUtil.closeQuietly(in);
            IOUtil.closeQuietly(out);
        }

        final AssetEditor parser = new AssetEditor(tmp);

        try {
            return parser.parseXml();
        } finally {
            IOUtil.closeQuietly(parser);
            tmp.delete();
        }
    }

    /**
     * Returns the decored resource table
     * 
     * @return The resource table
     * @throws IOException
     *             if error occurred
     */
    public ResourceTable getResourceTable() throws IOException {
        final ZipEntry arsc = this.archive.getEntry(Aapt.RESOURCES_ARSC);
        final File tmp = File.createTempFile("resources", "arsc");
        final InputStream in = this.archive.getInputStream(arsc);
        final OutputStream out = new FileOutputStream(tmp);

        try {
            IOUtil.copy(in, out);
        } finally {
            IOUtil.closeQuietly(in);
            IOUtil.closeQuietly(out);
        }

        final AssetEditor parser = new AssetEditor(tmp);

        try {
            return parser.parseResourceTable();
        } finally {
            IOUtil.closeQuietly(parser);
            tmp.delete();
        }
    }

    /**
     * Returns the main dex
     * 
     * @return the main dex
     * @throws IOException
     *             if error occurred
     */
    public Dex getMainDex() throws IOException {
        final ZipEntry arsc = this.archive.getEntry(Aapt.CLASSES_DEX);
        return new Dex(this.archive.getInputStream(arsc));
    }

    /**
     * Returns an iterable of all dex in this APK
     * 
     * @return a dex iterable
     */
    public Iterable<Dex> dexes() {
        return new DexIterable();
    }

    /**
     * Returns an iterable of all class names in this APK
     * 
     * @return a class name iterable
     */
    public Iterable<String> classes() {
        return new ClassIterable();
    }

    /**
     * Close this APK file
     * 
     * @throws IOException
     *             if error occurred
     */
    public void close() throws IOException {
        this.archive.close();
    }

    private final class DexIterable implements Iterable<Dex> {
        @Override
        public Iterator<Dex> iterator() {
            return new DexIterator();
        }
    }

    private class DexIterator implements Iterator<Dex> {

        private int index = 1;

        public DexIterator() {
        }

        public ZipEntry nextEntry() {
            if (1 == this.index) {
                return archive.getEntry(Aapt.CLASSES_DEX);
            }

            final String dex = String.format("classes%d.dex", this.index);
            return archive.getEntry(dex);
        }

        @Override
        public boolean hasNext() {
            return null != nextEntry();
        }

        @Override
        public Dex next() {
            try {
                return new Dex(archive.getInputStream(nextEntry()));
            } catch (final IOException e) {
                throw new AaptException(e);
            } finally {
                this.index++;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final class ClassIterable implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return new ClassIterator(dexes().iterator());
        }
    }

    private final class ClassIterator implements Iterator<String> {

        final Iterator<Dex> dexes;

        Iterator<ClassDef> classDefs;

        public ClassIterator(final Iterator<Dex> dexes) {
            this.dexes = dexes;
            if (dexes.hasNext()) {
                this.classDefs = dexes.next().classDefs().iterator();
            }
        }

        @Override
        public boolean hasNext() {
            if (null == this.classDefs) {
                return false;
            }

            if (this.classDefs.hasNext()) {
                return true;
            }

            if (this.dexes.hasNext()) {
                this.classDefs = dexes.next().classDefs().iterator();
                return this.classDefs.hasNext();
            }

            return false;
        }

        @Override
        public String next() {
            final char[] descriptor = this.classDefs.next().getDescriptor().toCharArray();
            final int n = descriptor.length - 2;

            for (int i = 1; i < n; i++) {
                if ('/' == descriptor[i]) {
                    descriptor[i] = '.';
                }
            }

            return new String(descriptor, 1, n);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
