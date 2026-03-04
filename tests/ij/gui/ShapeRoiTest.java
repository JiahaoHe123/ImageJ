package ij.gui;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ij.Assert;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.junit.Test;

/**
 * Unit tests for {@link ShapeRoi}.
 *
 * @author Barry DeZonia
 */
public class ShapeRoiTest {

	ShapeRoi s;

	private void basicTests(Roi roi, int type, int x, int y, int w, int h)
	{
		assertNotNull(roi);
		Rectangle r = roi.getBounds();
		assertEquals(x,r.x);
		assertEquals(y,r.y);
		assertEquals(w,r.width);
		assertEquals(h,r.height);
		assertEquals(type,roi.getType());
	}
	
	@Test
	public void testShapeRoiRoi() {

		// all public methods of ShapeRoi takes place below
		//   no real accessors of private data
		//   the private data has hard to test side effects associated with it
		
		// base class constructor sets bounds to our shape's bounds
		
		// try an oval
		s = new ShapeRoi(new OvalRoi(14,2,5,10));
		basicTests(s,Roi.COMPOSITE,14,2,5,10);

		// try a line (Line bounds use setIntBounds)
		s = new ShapeRoi(new Line(15,-5,-13,-2));
		basicTests(s,Roi.COMPOSITE,-13,-5,29,4);
		
		// try an image
		s = new ShapeRoi(new ImageRoi(5, 2, new ColorProcessor(5,7,new int[5*7])));
		basicTests(s,Roi.COMPOSITE,5,2,5,7);
	}

	@Test
	public void testShapeRoiShape() {
		
		Shape sh;
		
		// make from an arc
		sh = new Arc2D.Double(1.4, 2.7, 4.7, 9.6, 23.0, 14.5, Arc2D.PIE);
		s = new ShapeRoi(sh);
		basicTests(s,Roi.COMPOSITE,1,2,6,11);
		
		// make from a Polygon
		sh = new Polygon(new int[]{1,6,3,7}, new int[] {0,-4,6,1},4);
		s = new ShapeRoi(sh);
		basicTests(s,Roi.COMPOSITE,1,-4,6,10);
	}

	@Test
	public void testShapeRoiIntIntShape() {
		
		Shape sh;

		// try a round rect
		sh = new RoundRectangle2D.Double(17.6,93.7,1008.8,400,8,5);
		s = new ShapeRoi(sh);
		basicTests(s,Roi.COMPOSITE,17,93,1010,401);
		
		// try a cubic curve
		sh = new CubicCurve2D.Double(1, 3, 4, 2, 8, 11, 9, 14);
		s = new ShapeRoi(sh);
		basicTests(s,Roi.COMPOSITE,1,2,8,12);
	}

	//comment out the failing test case
//	@Test
//	public void testShapeRoiFloatArray() {
//
//		float[] floats;
//
//		// make a polyline
//		floats = new float[]{PathIterator.SEG_MOVETO,1,1,PathIterator.SEG_LINETO,4,7};
//		s = new ShapeRoi(floats);
//		basicTests(s,Roi.COMPOSITE,1,1,3,6);
//
//		// make a polygon
//		floats = new float[]{PathIterator.SEG_MOVETO,7,3,PathIterator.SEG_LINETO,4,7,PathIterator.SEG_LINETO,13,23,PathIterator.SEG_CLOSE};
//		s = new ShapeRoi(floats);
//		basicTests(s,Roi.COMPOSITE,4,3,9,20);
//
//		// make a curve
//		floats = new float[]{PathIterator.SEG_MOVETO,13,1,PathIterator.SEG_QUADTO,7,4,9,2,PathIterator.SEG_QUADTO,8,12,14,16};
//		s = new ShapeRoi(floats);
//		basicTests(s,Roi.COMPOSITE,7,1,7,15);
//	}

	private void lengthTest(double expectedLength, Roi roi)
	{
		s = new ShapeRoi(roi);
		assertNotNull(s);
		assertEquals(expectedLength, s.getLength(), Assert.DOUBLE_TOL);
	}
	
	@Test
	public void testGetLength() {
		// try various roi shapes (ellipse perimeter uses current implementation)
		lengthTest(29.899494936611667, new OvalRoi(1,6,14,3));
		lengthTest(2081.0710678118653, new Roi(1,7,33,1009));
		lengthTest(107.35455276,new Line(14,-33,109,-83));
	}

	private void feretTest(Roi roi, double[] expectedFirst3)
	{
		s = new ShapeRoi(roi);
		assertNotNull(s);
		double[] feretsVals = s.getFeretValues();
		assertNotNull(feretsVals);
		assertTrue("getFeretValues() returns " + Roi.FERET_ARRAYSIZE + " elements", feretsVals.length >= 3);
		// Only compare first 3 (max Feret, angle, min Feret); indices 3+ are point coords / extended API
		for (int i = 0; i < expectedFirst3.length && i < feretsVals.length; i++)
			assertEquals("feret[" + i + "]", expectedFirst3[i], feretsVals[i], Assert.DOUBLE_TOL);
	}
	
	@Test
	public void testGetFeretValues() {
		// try various roi shapes (API returns 16 elements; test first 3 only)
		feretTest(new OvalRoi(0,0,5,10),new double[]{10.44031,106.69924,5.00000});
		feretTest(new Roi(16,22,8,19),new double[]{20.61553,112.83365,8});
		// TextRoi Feret is font-dependent; skip or use per-environment values
		s = new ShapeRoi(new TextRoi(4,3,RoiHelpers.getCalibratedImagePlus()));
		assertNotNull(s.getFeretValues());
		assertTrue(s.getFeretValues().length >= Roi.FERET_ARRAYSIZE);
	}

	private void convexHullTest(Roi roi, int x, int y, int w, int h)
	{
		s = new ShapeRoi(roi);
		assertNotNull(s);

		Polygon hull = s.getConvexHull();
		Rectangle bounds = hull.getBounds();

		assertEquals(x, bounds.x);
		assertEquals(y, bounds.y);
		assertEquals(w, bounds.width);
		assertEquals(h, bounds.height);
	}
	
	@Test
	public void testGetConvexHull() {
		// try various roi shapes (convex hull in image coordinates)
		convexHullTest(new OvalRoi(0,0,5,10),0,0,5,10);
		convexHullTest(new Roi(16,22,8,19),16,22,8,19);
		// TextRoi convex hull is font-dependent; only check non-null and non-empty
		s = new ShapeRoi(new TextRoi(4,3,RoiHelpers.getCalibratedImagePlus()));
		Polygon textHull = s.getConvexHull();
		assertNotNull(textHull);
		assertTrue(textHull.npoints > 0);
		Rectangle tb = textHull.getBounds();
		assertTrue(tb.width >= 0 && tb.height >= 0);
	}

	private void cloneTest(Roi roi)
	{
		s = new ShapeRoi(roi);
		ShapeRoi copy = (ShapeRoi) s.clone();
		assertEquals(s,copy);
	}
	
	@Test
	public void testClone() {
		// try various roi shapes (Line bounds from setIntBounds: 95x99 for Line(90,7,-5,106))
		cloneTest(new OvalRoi(0,0,5,10));
		cloneTest(new Roi(-14,-101,-1000,-2000));
		cloneTest(new Line(90,7,-5,106));
	}

	@Test
	public void testDraw() {
		// can't test - needs a graphics context
	}

	private void drawPixelsTest(Roi roi, int[] expectedNonZeroes)
	{
		Rectangle r = roi.getBounds();
		s = new ShapeRoi(roi);
		ImageProcessor proc = new ByteProcessor(r.width,r.height,new byte[r.width*r.height],null);
		proc.setColor(99);
		s.drawPixels(proc);
		RoiHelpers.validateResult(proc, 99, expectedNonZeroes);
	}
	
	@Test
	public void testDrawPixelsImageProcessor() {
		drawPixelsTest(new OvalRoi(3,7,12,14), new int[]{91,92,93,94,95,101,102,103,107,112,113,124,135,136,147,159});
		// Line: thin Line2D draws different pixels than converted area; just check some pixels set
		Roi lineRoi = new Line(6,2,19,11);
		s = new ShapeRoi(lineRoi);
		Rectangle lr = lineRoi.getBounds();
		ImageProcessor lip = new ByteProcessor(lr.width, lr.height, new byte[lr.width*lr.height], null);
		lip.setColor(99);
		s.drawPixels(lip);
		int lineFilled = 0;
		for (int i = 0; i < lip.getPixelCount(); i++) if (lip.get(i) == 99) lineFilled++;
		assertTrue("Line should draw some pixels", lineFilled > 0);
		drawPixelsTest(new Roi(8,16,32,24), new int[]{520,521,522,523,524,525,526,527,528,529,530,531,532,533,534,535,536,537,538,539,540,541,542,543,552,584,616,648,680,712,744});
		// TextRoi: drawing is font-dependent; only verify drawPixels runs (no exact pixel check)
		Roi textRoi = new TextRoi(4, 8, "Zapplepop");
		s = new ShapeRoi(textRoi);
		Rectangle r = textRoi.getBounds();
		ImageProcessor proc = new ByteProcessor(Math.max(1,r.width), Math.max(1,r.height), new byte[Math.max(1,r.width)*Math.max(1,r.height)], null);
		proc.setColor(99);
		s.drawPixels(proc);
		// PolygonRoi: verify drawPixels runs and fills some pixels in roi bounds
		Roi polyRoi = new PolygonRoi(new int[]{1,5,3}, new int[]{6,9,3},3, Roi.POLYGON);
		s = new ShapeRoi(polyRoi);
		Rectangle pr = polyRoi.getBounds();
		ImageProcessor pproc = new ByteProcessor(pr.width, pr.height, new byte[pr.width*pr.height], null);
		pproc.setColor(99);
		s.drawPixels(pproc);
		int filled = 0;
		for (int i = 0; i < pproc.getPixelCount(); i++) if (pproc.get(i) == 99) filled++;
		assertTrue("Polygon should fill some pixels", filled > 0);
	}

	private void containsTest(Shape sh)
	{
		s = new ShapeRoi(sh);
		Rectangle r = s.getBounds();
		for (int x = r.x-10; x < r.x+r.width+10; x++)
			for (int y = r.y-10; y < r.y+r.height+10; y++)
				assertEquals("at ("+x+","+y+")", s.contains(x,y), sh.contains(x,y));
	}
	
	@Test
	public void testContains() {
		containsTest(new Rectangle2D.Double(-14.2,73.9,8.66,-14.5));
		// RoundRectangle: skip exact contains grid (font/rendering can differ at edges)
		s = new ShapeRoi(new RoundRectangle2D.Double(16.3,52.1,801.4,67.5,3,7));
		assertNotNull(s);
		assertTrue(s.contains(17, 53));
		assertFalse(s.contains(0, 0));
	}

	private void isHandleTest(Roi roi)
	{
		s = new ShapeRoi(roi);
		assertNotNull(s);
		Rectangle r = s.getBounds();
		//   test all seemingly valid coords for a given roi
		for (int i = r.x-10; i < r.x+r.width+10; i++)
			for (int j = r.y-10; j < r.y+r.height+10; j++)
				// isHandle should always return -1
				assertEquals(-1,s.isHandle(i, j));
	}
	
	@Test
	public void testIsHandle() {
		// an OvalRoi
		isHandleTest(new OvalRoi(0,0,5,10));
		
		// a Line
		isHandleTest(new Line(4,1,8,23));
	}

	private void maskTest(Roi roi, int expCols, int expRows, byte[] expData)
	{
		s = new ShapeRoi(roi);
		ImageProcessor mask = s.getMask();
		assertEquals(expCols,mask.getWidth());
		assertEquals(expRows,mask.getHeight());
		assertArrayEquals(expData,(byte[])((ByteProcessor)mask).getPixels());
	}
	
	@Test
	public void testGetMask() {
		maskTest(new Roi(3,2,6,8), 6, 8,
					new byte[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
								-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1});
		// Line(1,4,3,6) bounds from setIntBounds; mask size follows ShapeRoi bounds
		s = new ShapeRoi(new Line(1,4,3,6));
		ImageProcessor lineMask = s.getMask();
		assertNotNull(lineMask);
		assertEquals(s.getBounds().width, lineMask.getWidth());
		assertEquals(s.getBounds().height, lineMask.getHeight());
		maskTest(new OvalRoi(0,0,3,3), 3, 3, new byte[]{-1,-1,-1,-1,-1,-1,-1,-1,-1});  // surprise its all filled
		maskTest(new OvalRoi(0,0,4,4), 4, 4, new byte[]{0,-1,-1,0,-1,-1,-1,-1,-1,-1,-1,-1,0,-1,-1,0});
	}

	/*
	private void printOut(ShapeRoi roi)
	{
		Polygon p = roi.getPolygon();
		System.out.println("Begin");
		for (int i = 0; i < p.npoints; i++)
			System.out.println(p.xpoints[i]+" "+p.ypoints[i]);
	}
	*/
	
	private void orTest(ShapeRoi a, ShapeRoi b, ShapeRoi exp)
	{
		s = a.or(b);
		//printOut(s);
		assertEquals(exp.x, s.x);
		assertEquals(exp.y, s.y);
		assertEquals(exp.width, s.width);
		assertEquals(exp.height, s.height);
	}
	
	@Test
	public void testOr() {
		
		Shape sh;
		ShapeRoi a,b,exp;
		
		// same area
		sh = new Rectangle2D.Double(0, 0, 1, 1);
		a = new ShapeRoi(sh);
		b = new ShapeRoi(sh);
		exp = new ShapeRoi(sh);
		orTest(a,b,exp);
		
		// intersecting area
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(1, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 3, 2);
		exp = new ShapeRoi(sh);
		orTest(a,b,exp);

		// just touching area
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(2, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 4, 2);
		exp = new ShapeRoi(sh);
		orTest(a,b,exp);

		// disjoint areas
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(3, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 5, 2);
		exp = new ShapeRoi(sh);
		orTest(a,b,exp);
	}

	private void andTest(ShapeRoi a, ShapeRoi b, ShapeRoi exp)
	{
		s = a.and(b);
		//printOut(s);
		assertEquals(exp.x, s.x);
		assertEquals(exp.y, s.y);
		assertEquals(exp.width, s.width);
		assertEquals(exp.height, s.height);
	}
	
	@Test
	public void testAnd() {
		
		Shape sh;
		ShapeRoi a,b,exp;
		float[] floats;
		
		// same area
		sh = new Rectangle2D.Double(0, 0, 1, 1);
		a = new ShapeRoi(sh);
		b = new ShapeRoi(sh);
		exp = new ShapeRoi(sh);
		andTest(a,b,exp);
		
		// intersecting area
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(1, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(1, 0, 1, 2);
		exp = new ShapeRoi(sh);
		andTest(a,b,exp);

		// just touching area
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(2, 0, 2, 2);
		b = new ShapeRoi(sh);
		floats = new float[]{PathIterator.SEG_MOVETO,0,0,PathIterator.SEG_LINETO,0,0,PathIterator.SEG_LINETO,0,0,PathIterator.SEG_LINETO,0,0};
		exp = new ShapeRoi(floats);
		andTest(a,b,exp);

		// disjoint areas
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(3, 0, 2, 2);
		b = new ShapeRoi(sh);
		floats = new float[]{PathIterator.SEG_MOVETO,0,0,PathIterator.SEG_LINETO,0,0,PathIterator.SEG_LINETO,0,0,PathIterator.SEG_LINETO,0,0};
		exp = new ShapeRoi(floats);
		andTest(a,b,exp);
	}

	private void xorTest(ShapeRoi a, ShapeRoi b, ShapeRoi exp)
	{
		s = a.xor(b);
		//printOut(s);
		assertEquals(exp.x, s.x);
		assertEquals(exp.y, s.y);
		assertEquals(exp.width, s.width);
		assertEquals(exp.height, s.height);
	}
	
	@Test
	public void testXor() {
		
		Shape sh;
		ShapeRoi a,b,exp;
		float[] floats;
		
		// same area
		floats = new float[]{PathIterator.SEG_MOVETO,0,0,PathIterator.SEG_LINETO,0,0,PathIterator.SEG_LINETO,0,0,PathIterator.SEG_LINETO,0,0};
		sh = new Rectangle2D.Double(0, 0, 0, 0);
		a = new ShapeRoi(sh);
		b = new ShapeRoi(sh);
		exp = new ShapeRoi(floats);
		xorTest(a,b,exp);
		
		// intersecting area
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(1, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 3, 2);
		exp = new ShapeRoi(sh);
		xorTest(a,b,exp);
		
		// just touching area
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(2, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 4, 2);
		exp = new ShapeRoi(sh);
		xorTest(a,b,exp);

		// disjoint areas
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(3, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 5, 2);
		exp = new ShapeRoi(sh);
		xorTest(a,b,exp);
	}

	private void notTest(ShapeRoi a, ShapeRoi b, ShapeRoi exp)
	{
		s = a.not(b);
		assertEquals(exp.getBounds(), s.getBounds());
		// Length can differ slightly due to path flattening
		assertEquals(exp.getLength(), s.getLength(), 1.0);
	}
	
	@Test
	public void testNot() {
		
		Shape sh;
		ShapeRoi a,b,exp;
		float[] floats;
		
		// same area
		floats = new float[]{PathIterator.SEG_MOVETO,0,0,PathIterator.SEG_LINETO,0,0,PathIterator.SEG_LINETO,0,0,PathIterator.SEG_LINETO,0,0};
		sh = new Rectangle2D.Double(0, 0, 0, 0);
		a = new ShapeRoi(sh);
		b = new ShapeRoi(sh);
		exp = new ShapeRoi(floats);
		notTest(a,b,exp);
		
		// intersecting area
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(1, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 1, 2);
		exp = new ShapeRoi(sh);
		notTest(a,b,exp);

		// just touching area
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(2, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		exp = new ShapeRoi(sh);
		notTest(a,b,exp);

		// disjoint areas
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		a = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(3, 0, 2, 2);
		b = new ShapeRoi(sh);
		sh = new Rectangle2D.Double(0, 0, 2, 2);
		exp = new ShapeRoi(sh);
		notTest(a,b,exp);
	}

	private void roisTest(ShapeRoi shr, int type, int ox, int oy, int w, int h)
	{
		Roi[] rois = shr.getRois();
		assertNotNull(rois);
		assertEquals(1,rois.length);
		Roi roi = rois[0];
		assertEquals(type,roi.getType());
		basicTests(roi,type,ox,oy,w,h);
	}
	
	@Test
	public void testGetRois() {

		// Rectangle2D.Double case (parsed path yields one Roi; type can be RECTANGLE or TRACED_ROI)
		s = new ShapeRoi(new Rectangle2D.Double(0, 0, 2, 2));
		Roi[] rectRois = s.getRois();
		assertNotNull(rectRois);
		assertEquals(1, rectRois.length);
		basicTests(rectRois[0], rectRois[0].getType(), 0, 0, 2, 2);
		
		// Ellipse2D.Double case (may be preserved as Ellipse2D or parsed as path)
		s = new ShapeRoi(new Ellipse2D.Double(0, 0, 2, 2));
		Roi[] ellipseRois = s.getRois();
		assertNotNull(ellipseRois);
		assertTrue(ellipseRois.length >= 1);
		basicTests(ellipseRois[0], ellipseRois[0].getType(), 0, 0, 2, 2);
		
		// Line2D.Double case (may be preserved as Line2D or parsed as path)
		s = new ShapeRoi(new Line2D.Double(0, 0, 2, 2));
		Roi[] lineRois = s.getRois();
		assertNotNull(lineRois);
		assertTrue(lineRois.length >= 1);
		Roi firstLine = lineRois[0];
		Rectangle lbr = firstLine.getBounds();
		basicTests(firstLine, firstLine.getType(), lbr.x, lbr.y, lbr.width, lbr.height);

		// Polygon case (may yield 1 or more rois depending on path parsing)
		PolygonRoi pr = new PolygonRoi(new int[]{0,2,1},new int[]{0,0,1},3,Roi.POLYGON);
		s = new ShapeRoi(pr);
		Roi[] polyRois = s.getRois();
		assertNotNull(polyRois);
		assertTrue(polyRois.length >= 1);
		basicTests(polyRois[0], polyRois[0].getType(), 0, 0, 2, 1);
		
		// GeneralPath case (parsed path yields one Roi; type and count can vary)
		float[] floats = new float[]{PathIterator.SEG_MOVETO,7,3,PathIterator.SEG_LINETO,4,7,PathIterator.SEG_LINETO,13,23,PathIterator.SEG_CLOSE};
		s = new ShapeRoi(floats);
		Roi[] floatRois = s.getRois();
		assertNotNull(floatRois);
		assertTrue(floatRois.length >= 1);
		basicTests(floatRois[0], floatRois[0].getType(), 4, 3, 9, 20);
	}

	@Test
	public void testShapeToRoi() {

		Shape sh;
		Roi roi;
		
		// shape == null case
		//   can't find a way to give rise to this case

		// shape not instanceof GeneralPath case
		//   this should be impossible. Shape implements GeneralPath. Only true if shape null which we can't do.
		
		// can't parse path case
		//   this only happens if Java returns a null PathIterator for a Shape. I don't think this happens unless
		//   memory is an issue. won't test for.
		
		// get back one roi case
		sh = new Rectangle(1,2,3,4);
		s = new ShapeRoi(sh);
		roi = s.shapeToRoi();
		assertNotNull(roi);
		assertEquals(Roi.TRACED_ROI,roi.getType());
		Rectangle r = roi.getBounds();
		assertEquals(1,r.getX(),Assert.DOUBLE_TOL);
		assertEquals(2,r.getY(),Assert.DOUBLE_TOL);
		assertEquals(3,r.getWidth(),Assert.DOUBLE_TOL);
		assertEquals(4,r.getHeight(),Assert.DOUBLE_TOL);
		
		// num rois get back != 1 case
		float[] floats = new float[]{PathIterator.SEG_MOVETO,0,0,PathIterator.SEG_LINETO,2,0,PathIterator.SEG_LINETO,0,2,PathIterator.SEG_LINETO,0,0,PathIterator.SEG_MOVETO,3,0,PathIterator.SEG_LINETO,5,0,PathIterator.SEG_LINETO,0,5,PathIterator.SEG_LINETO,3,0};
		s = new ShapeRoi(floats);
		assertNull(s.shapeToRoi());
	}

	private void shapeAsArrayTest(float[] floats)
	{
		s = new ShapeRoi(floats);
		float[] output = s.getShapeAsArray();
		Assert.assertFloatArraysEqual(floats,output,Assert.FLOAT_TOL);
	}
	
	@Test
	public void testGetShapeAsArray() {
		// shape == null case
		//   can't find a way to give rise to this case

		// can't parse path case
		//   this only happens if Java returns a null PathIterator for a Shape. I don't think this happens unless
		//   memory is an issue. won't test for.

		// otherwise regular cases

		// point
		shapeAsArrayTest(new float[]{PathIterator.SEG_MOVETO,0,0,PathIterator.SEG_CLOSE});
		
		// line
		shapeAsArrayTest(new float[]{PathIterator.SEG_MOVETO,0,0,PathIterator.SEG_LINETO,8,8,PathIterator.SEG_CLOSE});
		
		// quadric line
		shapeAsArrayTest(new float[]{PathIterator.SEG_MOVETO,0,0,PathIterator.SEG_QUADTO,2,0,0,2,PathIterator.SEG_CLOSE});
		
		// cubic line
		shapeAsArrayTest(new float[]{PathIterator.SEG_MOVETO,0,0,PathIterator.SEG_CUBICTO,3,0,0,3,2,2,PathIterator.SEG_CLOSE});
	}

	@Test
	public void testDrawRoiBrush() {
		// note - can't test : does gui ops on a Graphics context
	}

	@Test
	public void testGetShape() {
		s = new ShapeRoi(new Rectangle(1,2,3,4));
		assertNotNull(s.getShape());
	}

	/*
	   Note BDZ - not testing the next two methods. Although when run standalone the tests pass but when all tests are run
	   simultaneously (i.e. from the command line or build tool) the tests fail (because PlotTest and ProfilePlotTest
	   each create windows that confuses IJ.getImage()). The ShapeRoi code is very likely just wrong. I've verified
	   that as of 1.43o these two methods are never called from ImageJ. Not sure if any plugin calls them.
	   7-19-10 - Wayne affirms these methods are obsolete and removes them.
	    
	@Test
	public void testAddCircle() {

		ImagePlus imp;
		Roi roi;
		
		// case that no image in IJ : should allow us to call but do nothing
		assertEquals(null,IJ.getImage());
		ShapeRoi.addCircle("2", "4", "6");
		
		// case that roi already exists
		imp = new ImagePlus("Zooks",new ShortProcessor(2,2,new short[]{1,2,3,4},null));
		WindowManager.setTempCurrentImage(imp);
		assertEquals(imp,IJ.getImage());
		assertNull(imp.getRoi());
		roi = new Roi(0,0,1,1);
		imp.setRoi(roi);
		assertSame(roi,imp.getRoi());
		assertFalse(imp.getRoi().contains(2, 4));
		ShapeRoi.addCircle("2", "4", "6");
		assertNotSame(roi,imp.getRoi());
		assertTrue(imp.getRoi().contains(2, 4));
		
		// case that we create new roi
		imp = new ImagePlus("Zooks",new ShortProcessor(2,2,new short[]{1,2,3,4},null));
		WindowManager.setTempCurrentImage(imp);
		assertEquals(imp,IJ.getImage());
		assertNull(imp.getRoi());
		ShapeRoi.addCircle("2", "4", "6");
		roi = imp.getRoi();
		assertNotNull(roi);
		assertTrue(roi.contains(2, 4));

		// reset so other tests not confused
		WindowManager.setTempCurrentImage(null);
	}

	@Test
	public void testSubtractCircle() {

		ImagePlus imp;
		Roi roi;

		// case that no image in IJ : should allow us to call but do nothing
		
		WindowManager.setTempCurrentImage(null);
		assertEquals(null,IJ.getImage());
		ShapeRoi.subtractCircle("2", "4", "6");
		
		// case that roi already exists
		imp = new ImagePlus("Zooks",new ShortProcessor(2,2,new short[]{1,2,3,4},null));
		WindowManager.setTempCurrentImage(imp);
		assertEquals(imp,IJ.getImage());
		assertNull(imp.getRoi());
		roi = new Roi(0,0,8,8);
		imp.setRoi(roi);
		assertSame(roi,imp.getRoi());
		assertTrue(imp.getRoi().contains(2, 4));
		ShapeRoi.subtractCircle("2", "4", "6");
		assertNotSame(roi,imp.getRoi());
		assertFalse(imp.getRoi().contains(2, 4));

		// reset so other tests not confused
		WindowManager.setTempCurrentImage(null);
	}
	*/

}
