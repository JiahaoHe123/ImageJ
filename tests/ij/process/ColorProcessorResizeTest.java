package ij.process;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

/**
 * Test for the ColorProcessor resize() method.
 * Tests are organized using partition-based testing to cover
 * different combinations of ROI usage and interpolation methods
 */
public class ColorProcessorResizeTest {
  private ColorProcessor cp;
  int w, h;

  private static int rgb(int r, int g, int b) {
    return 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
  }

  @Before
  public void setUp() {
    w = 4;
    h = 4;
    int[] pixels = new int[w * h];
    for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
            pixels[y * w + x] = rgb(x * 10, y * 10, x + y);
        }
    }
    cp = new ColorProcessor(w, h, pixels);

    cp.setInterpolationMethod(ImageProcessor.NONE);
    cp.resetRoi();
  }

//------------------------------ Global invariant -----------------------------

  // Verifies that calling resize() does not modify the original image pixels.
  @Test
  public void testResizeDoesNotMutateOriginal() {
    int[] before = (int[]) cp.getPixelsCopy();

    ImageProcessor out = cp.resize(6, 6);

    int[] after = (int[]) cp.getPixels();
    assertArrayEquals(
      "resize should not change original processor pixels",
      before,
      after
    );
  }

//---------------------------------- P1 ---------------------------------------

  /**
   * Checks same-size resize without ROI keeps all pixels unchanged.
   */
  @Test
  public void testResizeSameSizeWithNoROI() {
    ImageProcessor out = cp.resize(w, h);
    int[] pixels = (int[])cp.getPixels();

    assertEquals(w, out.getWidth());
    assertEquals(h, out.getHeight());

    int[] outPixels = (int[])out.getPixels();
    assertArrayEquals(pixels, outPixels);
  }

  /**
   * Verifies downscaling without ROI uses existing source pixels.
   */
  @Test
  public void testResizeDownScale4To2() {
    ImageProcessor out = cp.resize(2, 2);
    int[] outPixels = (int[])out.getPixels();

    assertEquals(2, out.getWidth());
    assertEquals(2, out.getHeight());

    int[] src = (int[])cp.getPixels();
    for (int v: outPixels) {
      boolean found = false;
      for (int s: src) {
        if (s == v) { found = true; break;}
      }
      assertTrue("Downscale should pick existing pixels", found);
    }
  }

  /**
   * Verifies upscaling without ROI preserves
   * corner blocks (nearest-neighbor behavior).
   */
  @Test
  public void testResizeUpScale2To4() {
    int[] pixels = new int[] {
      rgb(255, 0, 0), rgb(0, 255, 0),
      rgb(0, 0, 255), rgb(255, 255, 0)
    };

    ColorProcessor small = new ColorProcessor(2, 2, pixels);
    small.resetRoi();
    small.setInterpolationMethod(ImageProcessor.NONE);

    ImageProcessor out = small.resize(4, 4);
    int[] outPixels = (int[])out.getPixels();

    assertEquals(outPixels.length, 16);

    assertEquals(
      "Top left block should be red", rgb(255, 0, 0), outPixels[0 * 4 + 0]
    );
    assertEquals(
      "Top right block should be green",
      rgb(0, 255, 0),
      outPixels[0 * 4 + 3]
    );
    assertEquals(
      "Bottom left block should be blue",
      rgb(0, 0, 255),
      outPixels[3 * 4 + 0]
    );
    assertEquals(
      "Bottom right block should be yellow",
      rgb(255, 255, 0),
      outPixels[3 * 4 + 3]
    );
  }

  /**
   * Tests 1D horizontal image resize without
   * interpolation replicates existing pixels.
   */
  @Test
  public void testResizeOnePixelHeightImageNoInterpolation() {
    int[] pixels = new int[] {
        rgb(10, 20, 30),
        rgb(40, 50, 60),
        rgb(70, 80, 90)
    };

    ColorProcessor line = new ColorProcessor(3, 1, pixels);

    line.resetRoi();
    line.setInterpolationMethod(ImageProcessor.NONE);

    ImageProcessor out = line.resize(6, 1);

    assertEquals(6, out.getWidth());
    assertEquals(1, out.getHeight());

    int[] outPixels = (int[])out.getPixels();

    for (int p : outPixels) {
      boolean found = false;
      for (int s : pixels) {
        if (p == s) {
          found = true;
          break;
        }
      }
      assertTrue(
        "Nearest-neighbor resize should replicate existing pixels",
        found
      );
    }
  }
//---------------------------------- P2 -------------------------------------

  /**
   * Verifies same-size resize with ROI produces identical output to crop().
   */
  @Test
  public void testResizeSameSizeWithROI() {
    cp.setRoi(1, 1, 2, 2);

    ImageProcessor out = cp.resize(2, 2);

    assertEquals(2, out.getWidth());
    assertEquals(2, out.getHeight());

    ImageProcessor crop = cp.crop();
    assertArrayEquals((int[])out.getPixels(), (int[])crop.getPixels());
  }

  /**
   * Tests ROI zoom-in behavior maps ROI corner pixels to output corners.
   */
  @Test
  public void testResizeROIZoomIn() {
    cp.setRoi(1, 1, 2, 2);

    ImageProcessor roiCrop = cp.crop();
    int[] roiPixels = (int[]) roiCrop.getPixels();

    ImageProcessor out = cp.resize(4, 4);
    assertEquals(4, out.getWidth());
    assertEquals(4, out.getHeight());

    int[] outPixels = (int[])out.getPixels();

    int roiTL = roiPixels[0 * 2 + 0];
    int roiTR = roiPixels[0 * 2 + 1];
    int roiBL = roiPixels[1 * 2 + 0];
    int roiBR = roiPixels[1 * 2 + 1];

    assertEquals(
      "Resized top left should match ROI top left",
      roiTL,
      outPixels[0 * 4 + 0]
    );
    assertEquals(
      "Resized top right should match ROI top right",
      roiTR,
      outPixels[0 * 4 + 3]
    );
    assertEquals(
      "Resized bottom left should match ROI bottom left",
      roiBL,
      outPixels[3 * 4 + 0]
    );
    assertEquals(
      "Resized bottom right should match ROI bottom right",
      roiBR,
      outPixels[3 * 4 + 3]
    );
  }

  /**
   * Verifies ROI downscaling selects pixels only from the ROI region.
   */
  @Test
  public void testResizeROIDownScale() {
    cp.setRoi(1, 1, 2, 2);

    ImageProcessor out = cp.resize(1, 1);

    assertEquals(1, out.getWidth());
    assertEquals(1, out.getHeight());

    ImageProcessor roiCrop = cp.crop();
    int[] roiPixels = (int[])roiCrop.getPixels();
    int[] outPixels = (int[])out.getPixels();
    assertEquals(
      "Downscaled ROI pixel should come from ROI",
      roiPixels[0],
      outPixels[0]
    );
  }

//---------------------------------- P3 -------------------------------------

/**
 * Verifies bilinear interpolation produces at least one new interpolated pixel.
 */
  @Test
  public void testResizeBilinearNormalImage() {
    cp.setInterpolationMethod(ImageProcessor.BILINEAR);

    ImageProcessor out = cp.resize(6, 6);

    assertEquals(6, out.getWidth());
    assertEquals(6, out.getHeight());

    int[] outPixels = (int[]) out.getPixels();
    int[] srcPixels = (int[]) cp.getPixels();

    java.util.HashSet<Integer> srcSet = new java.util.HashSet<>();
    for (int s : srcPixels) srcSet.add(s);

    boolean foundNewValue = false;
    for (int p : outPixels) {
      if (!srcSet.contains(p)) {
        foundNewValue = true;
        break;
      }
    }

    assertTrue(
      "Bilinear resize should produce at" +
      "least one interpolated (new) pixel value",
      foundNewValue
    );
  }

  /**
   * Tests bilinear interpolation on 1D vertical image produces variation.
   */
  @Test
  public void testResizeOnePixelWideImageLinearBranch() {
    int[] pixels = new int[] {
      rgb(10, 20, 30),
      rgb(40, 50, 60),
      rgb(70, 80, 90)
    };
    ColorProcessor line = new ColorProcessor(1, 3, pixels);
    line.setInterpolationMethod(ImageProcessor.BILINEAR);

    ImageProcessor out = line.resize(1, 6);

    assertEquals(1, out.getWidth());
    assertEquals(6, out.getHeight());

    int[] outPixels = (int[])out.getPixels();
    boolean allSame = true;
    for (int i = 0; i < outPixels.length; i++) {
      if (outPixels[i] != outPixels[0]) {
        allSame = false;
        break;
      }
    }

    assertFalse(
      "Linear resize on 1xN image should produce variation",
      allSame
    );
  }

  /**
   * Verifies bicubic interpolation returns
   * correct output size and processor type.
   */
  @Test
  public void testResizeBicubicBranchReturnsCorrectSize() {
    cp.setInterpolationMethod(ImageProcessor.BICUBIC);

    ImageProcessor out = cp.resize(7, 5);

    assertEquals(7, out.getWidth());
    assertEquals(5, out.getHeight());
    assertTrue(
      "Bicubic branch should return a ColorProcessor",
      out instanceof ColorProcessor
    );
  }

//--------------------------------- P4 ---------------------------------------

  /**
   * Verifies ROI + bilinear interpolation produces
   * different results than nearest neighbor.
   */
  @Test
  public void testResizeROIZoomInBilinearProducesDifferentResultThanNearest() {
    cp.setRoi(1, 1, 2, 2);

    cp.setInterpolationMethod(ImageProcessor.NONE);
    ImageProcessor outNearest = cp.resize(6, 6);
    int[] nearestPixels = (int[])outNearest.getPixels();

    cp.setInterpolationMethod(ImageProcessor.BILINEAR);
    ImageProcessor outBilinear = cp.resize(6, 6);
    int[] bilinearPixels = (int[])outBilinear.getPixels();

    assertEquals(6, outBilinear.getWidth());
    assertEquals(6, outBilinear.getHeight());

    assertFalse(
      "ROI + bilinear should produce different " +
      "pixels than ROI + nearest-neighbor",
      java.util.Arrays.equals(nearestPixels, bilinearPixels)
    );
  }

  /**
   * Ensures ROI + bilinear interpolation generates
   * new pixel values beyond ROI source.
   */
  @Test
  public void testResizeROIWithBilinearProducesInterpolatedNewPixelValue() {
    cp.setRoi(1, 1, 2, 2);
    cp.setInterpolationMethod(ImageProcessor.BILINEAR);

    ImageProcessor roiCrop = cp.crop();
    int[] roiPixels = (int[]) roiCrop.getPixels();
    java.util.HashSet<Integer> roiSet = new java.util.HashSet<>();
    for (int v : roiPixels) roiSet.add(v);

    ImageProcessor out = cp.resize(6, 6);
    int[] outPixels = (int[])out.getPixels();

    boolean foundNew = false;
    for (int p : outPixels) {
      if (!roiSet.contains(p)) {
        foundNew = true;
        break;
      }
    }

    assertTrue(
      "ROI + bilinear should produce at least " +
      "one interpolated pixel not in ROI source",
      foundNew
    );
  }

  /**
   * Verifies ROI + bicubic interpolation returns
   * correct size and processor type.
   */
  @Test
  public void testResizeROIZoomInWithBicubic() {
    cp.setRoi(1, 1, 2, 2);
    cp.setInterpolationMethod(ImageProcessor.BICUBIC);

    ImageProcessor roiCrop = cp.crop();
    int[] roiPixels = (int[])roiCrop.getPixels();

    ImageProcessor out = cp.resize(7, 7);
    assertEquals(7, out.getWidth());
    assertEquals(7, out.getHeight());
    assertTrue(
      "Bicubic branch should return a ColorProcessor",
      out instanceof ColorProcessor
    );

    int[] outPixels = (int[])out.getPixels();

    int roiTL = roiPixels[0];
    int roiTR = roiPixels[1];
    int roiBL = roiPixels[2];
    int roiBR = roiPixels[3];

  }
}
