package aapt;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sdklite.aapt.Aapt;

public class AaptTest {

    @Test
    public void setApplicationDebuggableShouldBeOk() throws IOException {
        assertTrue(Aapt.setApplicationDebuggable(new File("/Users/johnson/Desktop/qq-release/AndroidManifest.xml")));
    }

}
