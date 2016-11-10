package aapt;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.sdklite.aapt.Aapt;

public class AaptTest {

    @Test
    public void setApplicationDebuggableShouldBeOk() throws IOException {
        assertTrue(Aapt.setApplicationDebuggable("./src/test/data/AndroidManifest.xml"));
    }

}
