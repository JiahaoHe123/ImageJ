package ij.measure;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import ij.IJ;


public class CalibrationFSMTest {

	private Calibration c;
	private static final String MICROMETER_SYMBOL = "\u00B5m";

	@Before
	public void setUp() {
		c = new Calibration();
	}

	// ----- State coverage: initial and each state -----

	/** State: Pixel (initial). */
	@Test
	public void testInitialState_isPixel() {
		assertEquals("pixel", c.getUnit());
	}

	/** State: Pixel – observe getUnit(). */
	@Test
	public void testStatePixel_getUnitReturnsPixel() {
		c.setUnit(null);
		assertEquals("pixel", c.getUnit());
	}

	/** State: Micrometer – observe getUnit(). */
	@Test
	public void testStateMicrometer_getUnitReturnsMicrometerSymbol() {
		c.setUnit("um");
		assertEquals(MICROMETER_SYMBOL, c.getUnit());
	}

	/** State: Angstrom – observe getUnit(). */
	@Test
	public void testStateAngstrom_getUnitReturnsAngstromSymbol() {
		c.setUnit("A");
		assertEquals("" + IJ.angstromSymbol, c.getUnit());
	}

	/** State: Custom – observe getUnit() returns stored string. */
	@Test
	public void testStateCustom_getUnitReturnsStoredString() {
		c.setUnit("mm");
		assertEquals("mm", c.getUnit());
	}

	// ----- Transition coverage: each transition type -----

	/** Transition: any → Pixel (null). */
	@Test
	public void testTransition_toPixel_fromNull() {
		c.setUnit("um");
		c.setUnit(null);
		assertEquals("pixel", c.getUnit());
	}

	/** Transition: any → Pixel (empty). */
	@Test
	public void testTransition_toPixel_fromEmpty() {
		c.setUnit("A");
		c.setUnit("");
		assertEquals("pixel", c.getUnit());
	}

	/** Transition: any → Micrometer. */
	@Test
	public void testTransition_toMicrometer() {
		c.setUnit("um");
		assertEquals(MICROMETER_SYMBOL, c.getUnit());
	}

	/** Transition: any → Angstrom. */
	@Test
	public void testTransition_toAngstrom() {
		c.setUnit("A");
		assertEquals("" + IJ.angstromSymbol, c.getUnit());
	}

	/** Transition: any → Custom. */
	@Test
	public void testTransition_toCustom() {
		c.setUnit("cm");
		assertEquals("cm", c.getUnit());
	}

	/** Transition: Custom → Pixel. */
	@Test
	public void testTransition_fromCustom_toPixel() {
		c.setUnit("inch");
		c.setUnit("");
		assertEquals("pixel", c.getUnit());
	}

	/** Transition: Micrometer → Custom. */
	@Test
	public void testTransition_fromMicrometer_toCustom() {
		c.setUnit("um");
		c.setUnit("inch");
		assertEquals("inch", c.getUnit());
	}

	/** Transition: Angstrom → Micrometer. */
	@Test
	public void testTransition_fromAngstrom_toMicrometer() {
		c.setUnit("A");
		c.setUnit("um");
		assertEquals(MICROMETER_SYMBOL, c.getUnit());
	}
}
