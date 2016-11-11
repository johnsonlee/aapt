package com.sdklite.aapt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.android.dex.Dex;

public class ApkFileTest {

    private ApkFile apk;

    @Before
    public void setup() throws IOException {
        this.apk = new ApkFile("src" + File.separator + "test" + File.separator + "data" + File.separator + "app.apk");
    }

    @Test
    public void getAndroidManifestShouldBeOk() throws IOException {
        final Xml xml = this.apk.getAndroidManifest();
        assertNotNull(xml);

        final Xml.Element manifest = xml.getDocumentElement();
        assertEquals("manifest", manifest.getName());
    }

    @Test
    public void getResourceTableShouldBeOk() throws IOException {
        final ResourceTable table = this.apk.getResourceTable();
        assertNotNull(table);
    }

    @Test
    public void getMainDexShouldBeOk() throws IOException {
        final Dex dex = this.apk.getMainDex();
        assertNotNull(dex);
    }

    @Test
    public void getDexesShouldBeOk() throws IOException {
        final Iterable<Dex> dexes = this.apk.dexes();
        assertNotNull(dexes);
        assertTrue(dexes.iterator().hasNext());
    }

    @Test
    public void getClassesShouldBeOk() throws IOException {
        final Iterable<String> classes = this.apk.classes();
        assertNotNull(classes);
        assertTrue(classes.iterator().hasNext());
    }

    @After
    public void teardown() throws IOException {
        this.apk.close();
    }
    
}
