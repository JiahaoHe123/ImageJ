package ij.process;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ColorSpaceConverterTest {

    private ColorSpaceConverter converter;
    private static final double DELTA = 0.001;

    @Before
    public void setUp() {
        converter = new ColorSpaceConverter();
    }

    // constructor function test

    @Test
    public void testDefaultConstructor() {
        ColorSpaceConverter c = new ColorSpaceConverter();
        assertNotNull(c);
        assertArrayEquals(new double[]{95.0429, 100.0, 108.8900}, c.whitePoint, DELTA);
    }

    @Test
    public void testConstructorD50() {
        ColorSpaceConverter c = new ColorSpaceConverter("d50");
        assertArrayEquals(new double[]{96.4212, 100.0, 82.5188}, c.whitePoint, DELTA);
    }

    @Test
    public void testConstructorD55() {
        ColorSpaceConverter c = new ColorSpaceConverter("d55");
        assertArrayEquals(new double[]{95.6797, 100.0, 92.1481}, c.whitePoint, DELTA);
    }

    @Test
    public void testConstructorD65() {
        ColorSpaceConverter c = new ColorSpaceConverter("d65");
        assertArrayEquals(new double[]{95.0429, 100.0, 108.8900}, c.whitePoint, DELTA);
    }

    @Test
    public void testConstructorD75() {
        ColorSpaceConverter c = new ColorSpaceConverter("d75");
        assertArrayEquals(new double[]{94.9722, 100.0, 122.6394}, c.whitePoint, DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidWhitePoint() {
        new ColorSpaceConverter("invalid");
    }

    // RGBtoXYZ test

    @Test
    public void testRGBtoXYZBlack() {
        double[] xyz = converter.RGBtoXYZ(0, 0, 0);
        assertEquals(0.0, xyz[0], DELTA);
        assertEquals(0.0, xyz[1], DELTA);
        assertEquals(0.0, xyz[2], DELTA);
    }

    @Test
    public void testRGBtoXYZWhite() {
        double[] xyz = converter.RGBtoXYZ(255, 255, 255);
        assertEquals(95.047, xyz[0], 0.1);
        assertEquals(100.0, xyz[1], 0.1);
        assertEquals(108.883, xyz[2], 0.1);
    }

    @Test
    public void testRGBtoXYZRed() {
        double[] xyz = converter.RGBtoXYZ(255, 0, 0);
        assertTrue(xyz[0] > 0);
        assertEquals(3, xyz.length);
    }

    @Test
    public void testRGBtoXYZArray() {
        double[] xyz1 = converter.RGBtoXYZ(100, 150, 200);
        double[] xyz2 = converter.RGBtoXYZ(new int[]{100, 150, 200});
        assertArrayEquals(xyz1, xyz2, DELTA);
    }

    // XYZtoRGB test

    @Test
    public void testXYZtoRGBBlack() {
        int[] rgb = converter.XYZtoRGB(0.0, 0.0, 0.0);
        assertEquals(0, rgb[0]);
        assertEquals(0, rgb[1]);
        assertEquals(0, rgb[2]);
    }

    @Test
    public void testXYZtoRGBWhite() {
        int[] rgb = converter.XYZtoRGB(95.047, 100.0, 108.883);
        assertEquals(255, rgb[0], 2);
        assertEquals(255, rgb[1], 2);
        assertEquals(255, rgb[2], 2);
    }

    @Test
    public void testXYZtoRGBArray() {
        int[] rgb1 = converter.XYZtoRGB(20.0, 15.0, 10.0);
        int[] rgb2 = converter.XYZtoRGB(new double[]{20.0, 15.0, 10.0});
        assertArrayEquals(rgb1, rgb2);
    }

    // RGBtoLAB test

    @Test
    public void testRGBtoLABBlack() {
        double[] lab = converter.RGBtoLAB(new int[]{0, 0, 0});
        assertEquals(0.0, lab[0], DELTA);
    }

    @Test
    public void testRGBtoLABWhite() {
        double[] lab = converter.RGBtoLAB(new int[]{255, 255, 255});
        assertEquals(100.0, lab[0], 0.1);
    }

    @Test
    public void testRGBtoLABIntValue() {
        int rgb = (100 << 16) | (150 << 8) | 200;
        double[] lab = converter.RGBtoLAB(rgb);
        assertNotNull(lab);
        assertEquals(3, lab.length);
    }

    // LABtoXYZ test

    @Test
    public void testLABtoXYZ() {
        double[] xyz = converter.LABtoXYZ(50.0, 0.0, 0.0);
        assertNotNull(xyz);
        assertEquals(3, xyz.length);
        assertTrue(xyz[1] > 0);
    }

    @Test
    public void testLABtoXYZArray() {
        double[] xyz1 = converter.LABtoXYZ(50.0, 10.0, -10.0);
        double[] xyz2 = converter.LABtoXYZ(new double[]{50.0, 10.0, -10.0});
        assertArrayEquals(xyz1, xyz2, DELTA);
    }

    // LABtoRGB test

    @Test
    public void testLABtoRGB() {
        int[] rgb = converter.LABtoRGB(50.0, 0.0, 0.0);
        assertNotNull(rgb);
        assertEquals(3, rgb.length);
    }

    @Test
    public void testLABtoRGBArray() {
        int[] rgb1 = converter.LABtoRGB(50.0, 10.0, -10.0);
        int[] rgb2 = converter.LABtoRGB(new double[]{50.0, 10.0, -10.0});
        assertArrayEquals(rgb1, rgb2);
    }

    // XYZtoLAB test

    @Test
    public void testXYZtoLAB() {
        double[] lab = converter.XYZtoLAB(95.047, 100.0, 108.883);
        assertEquals(100.0, lab[0], 0.1);
    }

    @Test
    public void testXYZtoLABArray() {
        double[] lab1 = converter.XYZtoLAB(50.0, 50.0, 50.0);
        double[] lab2 = converter.XYZtoLAB(new double[]{50.0, 50.0, 50.0});
        assertArrayEquals(lab1, lab2, DELTA);
    }

    // XYZtoxyY test

    @Test
    public void testXYZtoxyYNormal() {
        double[] xyY = converter.XYZtoxyY(95.047, 100.0, 108.883);
        assertEquals(3, xyY.length);
        assertTrue(xyY[0] > 0);
        assertTrue(xyY[1] > 0);
    }

    @Test
    public void testXYZtoxyYZeroInput() {
        // when x+y+z==0,should be white point
        double[] xyY = converter.XYZtoxyY(0.0, 0.0, 0.0);
        assertEquals(converter.chromaWhitePoint[0], xyY[0], DELTA);
        assertEquals(converter.chromaWhitePoint[1], xyY[1], DELTA);
    }

    @Test
    public void testXYZtoxyYArray() {
        double[] xyY1 = converter.XYZtoxyY(50.0, 50.0, 50.0);
        double[] xyY2 = converter.XYZtoxyY(new double[]{50.0, 50.0, 50.0});
        assertArrayEquals(xyY1, xyY2, DELTA);
    }

    // xyYtoXYZ test

    @Test
    public void testxyYtoXYZNormal() {
        double[] xyz = converter.xyYtoXYZ(0.3127, 0.3290, 100.0);
        assertNotNull(xyz);
        assertEquals(3, xyz.length);
        assertTrue(xyz[0] > 0);
    }

    @Test
    public void testxyYtoXYZZeroY() {
        // when y==0->[0.0,0.0,0.0]
        double[] xyz = converter.xyYtoXYZ(0.3127, 0.0, 100.0);
        assertEquals(0.0, xyz[0], DELTA);
        assertEquals(0.0, xyz[1], DELTA);
        assertEquals(0.0, xyz[2], DELTA);
    }

    @Test
    public void testxyYtoXYZArray() {
        double[] xyz1 = converter.xyYtoXYZ(0.3127, 0.3290, 100.0);
        double[] xyz2 = converter.xyYtoXYZ(new double[]{0.3127, 0.3290, 100.0});
        assertArrayEquals(xyz1, xyz2, DELTA);
    }

    // RGBtoHSB test

    @Test
    public void testRGBtoHSBRed() {
        double[] hsb = converter.RGBtoHSB(255, 0, 0);
        assertEquals(0.0, hsb[0], DELTA);
        assertEquals(1.0, hsb[1], DELTA);
        assertEquals(1.0, hsb[2], DELTA);
    }

    @Test
    public void testRGBtoHSBArray() {
        double[] hsb1 = converter.RGBtoHSB(100, 150, 200);
        double[] hsb2 = converter.RGBtoHSB(new int[]{100, 150, 200});
        assertArrayEquals(hsb1, hsb2, DELTA);
    }

    // HSBtoRGB test

    @Test
    public void testHSBtoRGBRed() {
        int[] rgb = converter.HSBtoRGB(0.0, 1.0, 1.0);
        assertEquals(255, rgb[0]);
        assertEquals(0, rgb[1]);
        assertEquals(0, rgb[2]);
    }

    @Test
    public void testHSBtoRGBArray() {
        int[] rgb1 = converter.HSBtoRGB(0.5, 0.5, 0.5);
        int[] rgb2 = converter.HSBtoRGB(new double[]{0.5, 0.5, 0.5});
        assertArrayEquals(rgb1, rgb2);
    }

    // round trip test

    @Test
    public void testRGBtoXYZtoRGBRoundTrip() {
        int R = 120, G = 80, B = 200;
        double[] xyz = converter.RGBtoXYZ(R, G, B);
        int[] rgb = converter.XYZtoRGB(xyz);
        assertEquals(R, rgb[0], 2);
        assertEquals(G, rgb[1], 2);
        assertEquals(B, rgb[2], 2);
    }

    @Test
    public void testRGBtoLABtoRGBRoundTrip() {
        int[] original = {100, 150, 200};
        double[] lab = converter.RGBtoLAB(original);
        int[] result = converter.LABtoRGB(lab);
        assertEquals(original[0], result[0], 2);
        assertEquals(original[1], result[1], 2);
        assertEquals(original[2], result[2], 2);
    }
}
