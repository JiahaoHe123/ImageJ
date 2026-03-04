package ij;

import static org.junit.Assert.*;
import ij.process.ByteProcessor;
import org.junit.Test;

public class ImagePlusOpenerTest {

    // Test that loadFromImagePlus sets processor correctly
    @Test
    public void testLoadFromImagePlusSetsProcessor() {
        ByteProcessor bp = new ByteProcessor(10, 10);
        ImagePlus fakeImp = new ImagePlus("fake", bp);

        ImagePlus result = new ImagePlus();
        result.loadFromImagePlus(fakeImp, null);

        assertNotNull(result.getProcessor());
    }

    // Test that loadFromImagePlus sets title correctly
    @Test
    public void testLoadFromImagePlusSetsTitle() {
        ByteProcessor bp = new ByteProcessor(10, 10);
        ImagePlus fakeImp = new ImagePlus("myTitle", bp);

        ImagePlus result = new ImagePlus();
        result.loadFromImagePlus(fakeImp, null);

        assertEquals("myTitle", result.getTitle());
    }
}