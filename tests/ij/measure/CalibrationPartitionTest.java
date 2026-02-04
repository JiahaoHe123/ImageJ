package ij.measure;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import ij.IJ;


public class CalibrationPartitionTest {

	private Calibration c;

	@Before
	public void setUp() {
		c = new Calibration();
	}

	/** P1: null → "pixel". */
	@Test
	public void testSetUnit_null_returnsPixel() {
		c.setUnit(null);
		assertEquals("pixel", c.getUnit());
	}

	/** P2: "" → "pixel". */
	@Test
	public void testSetUnit_emptyString_returnsPixel() {
		c.setUnit("");
		assertEquals("pixel", c.getUnit());
	}

	/** P3: "um" → "µm". */
	@Test
	public void testSetUnit_um_returnsMicrometerSymbol() {
		c.setUnit("um");
		assertEquals("\u00B5m", c.getUnit());
	}

	/** P4: "A" → angstrom symbol. */
	@Test
	public void testSetUnit_A_returnsAngstromSymbol() {
		c.setUnit("A");
		assertEquals("" + IJ.angstromSymbol, c.getUnit());
	}

	/** P5: "micron" → stored as-is. */
	@Test
	public void testSetUnit_micron_storedAsIs() {
		c.setUnit("micron");
		assertEquals("micron", c.getUnit());
	}

	/** P5: "inch" → stored as-is. */
	@Test
	public void testSetUnit_inch_storedAsIs() {
		c.setUnit("inch");
		assertEquals("inch", c.getUnit());
	}

	/** P5: "mm" → stored as-is. */
	@Test
	public void testSetUnit_mm_storedAsIs() {
		c.setUnit("mm");
		assertEquals("mm", c.getUnit());
	}

	/** P5: "cm" → stored as-is. */
	@Test
	public void testSetUnit_cm_storedAsIs() {
		c.setUnit("cm");
		assertEquals("cm", c.getUnit());
	}

	/** P5 boundary: "um " → not special, stored as-is. */
	@Test
	public void testSetUnit_umWithSpace_notNormalized() {
		c.setUnit("um ");
		assertEquals("um ", c.getUnit());
	}

	/** P5 boundary: "Um" → not special, stored as-is. */
	@Test
	public void testSetUnit_umCapitalU_notNormalized() {
		c.setUnit("Um");
		assertEquals("Um", c.getUnit());
	}

	/** P5 boundary: "a" → not angstrom, stored as-is. */
	@Test
	public void testSetUnit_lowercaseA_notAngstrom() {
		c.setUnit("a");
		assertEquals("a", c.getUnit());
	}

	/** P5 boundary: "A " → not special, stored as-is. */
	@Test
	public void testSetUnit_AWithSpace_notNormalized() {
		c.setUnit("A ");
		assertEquals("A ", c.getUnit());
	}

	/** P5 boundary: single char "x" → stored as-is. */
	@Test
	public void testSetUnit_singleCharX_storedAsIs() {
		c.setUnit("x");
		assertEquals("x", c.getUnit());
	}

	/** P5 boundary: "u" (not "um") → stored as-is. */
	@Test
	public void testSetUnit_singleCharU_storedAsIs() {
		c.setUnit("u");
		assertEquals("u", c.getUnit());
	}
}