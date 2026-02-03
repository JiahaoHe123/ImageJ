package ij.process;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import java.awt.Color;
import java.util.Arrays;

/**
 * Partition testing for the rotate() method in ColorProcessor.
 * Tests cover 8 representative values and 9 boundary values across different angle ranges.
 */
public class ColorProcessorRotateTest {

    private static final int IMAGE_SIZE = 100;
    private ColorProcessor cp;

    /**
     * Creates a test image with an asymmetric L-shaped pattern.
     * This ensures rotation effects are always detectable, even for 180° rotation.
     */
    @Before
    public void setUp() {
        cp = new ColorProcessor(IMAGE_SIZE, IMAGE_SIZE);
        cp.setColor(Color.WHITE);
        cp.fill();

        // Create an asymmetric L-shape pattern
        cp.setColor(Color.BLACK);
        cp.fillRect(10, 10, 40, 5);   // Horizontal bar
        cp.fillRect(10, 10, 5, 40);   // Vertical bar

        // Add a small marker in bottom-right to ensure asymmetry
        cp.setColor(Color.RED);
        cp.fillRect(80, 80, 5, 5);
    }

    // ==================== REPRESENTATIVE VALUES ====================

    /**
     * TC1: Test zero rotation (P1 - angle%360==0)
     * Expected: Pixels should remain unchanged
     */
    @Test
    public void testRotate_TC1_ZeroAngle() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(0);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertArrayEquals("0° rotation should not change pixels",
                pixelsBefore, pixelsAfter);
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC2: Test small positive angle (P2 - 0°<angle<90°)
     * Representative value: 45°
     */
    @Test
    public void testRotate_TC2_45Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(45);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("45° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC3: Test right angle (P3 - angle=90°)
     * Representative value: 90°
     */
    @Test
    public void testRotate_TC3_90Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(90);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("90° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC4: Test obtuse angle (P4 - 90°<angle<180°)
     * Representative value: 135°
     */
    @Test
    public void testRotate_TC4_135Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(135);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("135° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC5: Test straight angle (P5 - angle=180°)
     * Representative value: 180°
     */
    @Test
    public void testRotate_TC5_180Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(180);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("180° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC6: Test reflex angle (P6 - 180°<angle<360°)
     * Representative value: 270°
     */
    @Test
    public void testRotate_TC6_270Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(270);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("270° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC7: Test negative angle (P7 - angle<0°)
     * Representative value: -45°
     */
    @Test
    public void testRotate_TC7_Negative45Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(-45);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("-45° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC8: Test angle exceeding 360° (P8 - angle>360°)
     * Representative value: 405°
     */
    @Test
    public void testRotate_TC8_405Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(405);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("405° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    // ==================== BOUNDARY VALUES ====================

    /**
     * TC9: Test P1/P2 boundary
     * Boundary value: 1°
     */
    @Test
    public void testRotate_TC9_Boundary_1Degree() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(1);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("1° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC10: Test P2/P3 boundary
     * Boundary value: 89.99°
     */
    @Test
    public void testRotate_TC10_Boundary_89_99Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(89.99);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("89.99° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC11: Test P3/P4 boundary
     * Boundary value: 90.01°
     */
    @Test
    public void testRotate_TC11_Boundary_90_01Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(90.01);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("90.01° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC12: Test P4/P5 boundary
     * Boundary value: 179.99°
     */
    @Test
    public void testRotate_TC12_Boundary_179_99Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(179.99);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("179.99° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC13: Test P5/P6 boundary
     * Boundary value: 180.01°
     */
    @Test
    public void testRotate_TC13_Boundary_180_01Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(180.01);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("180.01° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC14: Test P6/P1 boundary
     * Boundary value: 359°
     */
    @Test
    public void testRotate_TC14_Boundary_359Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(359);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("359° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC15: Test P1/P7 boundary
     * Boundary value: -1°
     */
    @Test
    public void testRotate_TC15_Boundary_Negative1Degree() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(-1);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("-1° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC16: Test P1/P8 boundary
     * Boundary value: 361°
     */
    @Test
    public void testRotate_TC16_Boundary_361Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(361);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("361° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }

    /**
     * TC17: Test P7 extreme boundary
     * Boundary value: -359°
     */
    @Test
    public void testRotate_TC17_Boundary_Negative359Degrees() {
        int[] pixelsBefore = (int[])cp.getPixelsCopy();
        int widthBefore = cp.getWidth();
        int heightBefore = cp.getHeight();

        cp.rotate(-359);

        int[] pixelsAfter = (int[])cp.getPixels();
        assertFalse("-359° rotation should change pixels",
                Arrays.equals(pixelsBefore, pixelsAfter));
        assertEquals(widthBefore, cp.getWidth());
        assertEquals(heightBefore, cp.getHeight());
    }
}