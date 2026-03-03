package ij;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import ij.process.ByteProcessor;

public class ImagePlusOpenerMockingTest {

    // Verify openImage() is called once for local path, openURL() never called
    @Test
    public void testLocalPathCallsOpenImage() {
        ImagePlus.ImageLoader mockLoader = mock(ImagePlus.ImageLoader.class);
        ImagePlus fakeImp = new ImagePlus("fake", new ByteProcessor(10, 10));
        when(mockLoader.openImage("test.tif")).thenReturn(fakeImp);

        new ImagePlus("test.tif", mockLoader);

        verify(mockLoader, times(1)).openImage("test.tif");
        verify(mockLoader, never()).openURL(any());
    }

    // Verify openURL() is called once for URL, openImage() never called
    @Test
    public void testURLCallsOpenURL() {
        ImagePlus.ImageLoader mockLoader = mock(ImagePlus.ImageLoader.class);
        ImagePlus fakeImp = new ImagePlus("fake", new ByteProcessor(10, 10));
        when(mockLoader.openURL("http://example.com/img.tif")).thenReturn(fakeImp);

        new ImagePlus("http://example.com/img.tif", mockLoader);

        verify(mockLoader, times(1)).openURL("http://example.com/img.tif");
        verify(mockLoader, never()).openImage(any());
    }

    // Verify no exception when loader returns null
    @Test
    public void testNullReturnDoesNotThrow() {
        ImagePlus.ImageLoader mockLoader = mock(ImagePlus.ImageLoader.class);
        when(mockLoader.openImage("missing.tif")).thenReturn(null);

        new ImagePlus("missing.tif", mockLoader);

        verify(mockLoader, times(1)).openImage("missing.tif");
    }
}