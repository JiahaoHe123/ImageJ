package ij.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.Test;

/**
 * Test suite for ij.util.ArrayUtil
 */
public class ArrayUtilTest {
  /**
   * test constructor ArrayUtil(int) initializes
   * sorted=false and sets internal size correctly
   */
  @Test
  public void testConstructorBySizeInitializesArray() throws Exception {
    ArrayUtil au = new ArrayUtil(3);
    assertFalse(au.sorted);
    assertEquals(3, getSize(au));
  }

  /**
   * test constructor ArrayUtil(float[]) uses the
   * same backing array reference and sets sorted=false and size correctly
   */
  @Test
  public void testConstructorByArrayUsesSameDataReference() throws Exception {
    float[] data = new float[] { 3f, 1f, 2f };
    ArrayUtil au = new ArrayUtil(data);

    assertFalse(au.sorted);
    assertEquals(3, getSize(au));
    assertTrue(au.values == data);
  }

  /**
   * test putValue in-bounds updates stored values
   * and downstream mean/min/max calculations
   */
  @Test
  public void testPutValueInBoundsUpdatesAndReturnsTrue() {
    ArrayUtil au = new ArrayUtil(2);
    assertTrue(au.putValue(0, 5f));
    assertTrue(au.putValue(1, -1f));

    assertEquals(2.0, au.getMean(), 1e-9);
    assertEquals(-1.0, au.getMinimum(), 1e-9);
    assertEquals(5.0, au.getMaximum(), 1e-9);
  }

  /**
   * test putValue resets sorted=false after
   * a previous sort/medianSort sets sorted=true
   */
  @Test
  public void testPutValueSetSortedToBeFalse() {
    ArrayUtil au = new ArrayUtil(5);

    au.putValue(0, 5f);
    au.putValue(1, 1f);

    au.medianSort();

    assertTrue(au.sorted);

    au.putValue(0, 7f);
    assertFalse(au.sorted);
  }

  /**
   * test putValue out-of-bounds returns false
   * and does not change existing values/statistics
   */
  @Test
  public void testPutValueOutOfBoundsReturnsFalseAndDoesNotChange() {
    ArrayUtil au = new ArrayUtil(2);
    assertTrue(au.putValue(0, 7f));
    assertTrue(au.putValue(1, 1f));

    assertFalse("Index out of bound should fail", au.putValue(2, 999f));
    assertEquals(4.0, au.getMean(), 1e-9);
  }

  /**
   * test medianSort on odd length triggers sort
   * when unsorted and returns the correct median element
   */
  @Test
  public void testMedianSortOddSizeTriggersSortWhenUnsorted() {
    ArrayUtil au = new ArrayUtil(new float[] { 10f, 2f, 7f });
    double med = au.medianSort();
    assertEquals("Median of [2,7,10] should be 7", 7.0, med, 1e-9);

    assertEquals(7.0, au.medianSort(), 1e-9);
  }

  /**
   * test medianSort on even length returns
   * average of the two middle sorted elements
   */
  @Test
  public void testMedianSortEvenSizeUsesAverageOfMiddleTwo() {
    ArrayUtil au = new ArrayUtil(new float[] { 4f, 1f, 8f, 2f });
    double med = au.medianSort();
    assertEquals(
      "Median should be average of middle two: (2+4)/2 = 3",
      3.0,
      med,
      1e-9
    );
  }

  /**
   * test sort uses the partial array branch (size < values.length)
   * after shrinking size via setSize
   */
  @Test
  public void testSortUsesPartialArrayBranchWhenSizeLessThanValuesLength(
  ) throws Exception {
    ArrayUtil au = new ArrayUtil(5);

    au.putValue(0, 5f);
    au.putValue(1, 1f);
    au.putValue(2, 3f);
    au.putValue(3, 9f);
    au.putValue(4, 7f);

    assertEquals(5, getSize(au));

    au.setSize(3);
    au.sort();

    assertEquals(3, getSize(au));
    assertEquals("{1.0, 3.0, 5.0}", au.toString());
  }

  /**
   * test isMaximum returns true
   * when val is >= every element (loop reaches i==size)
   */
  @Test
  public void testIsMaximumTrueWhenValGreaterOrEqualAll() {
    ArrayUtil au = new ArrayUtil(new float[] { 1f, 2f, 3f });
    assertTrue("3 should be a maximum threshold", au.isMaximum(3.0));
    assertTrue("100 should be a maximum threshold", au.isMaximum(100.0));
  }

  /**
   * test isMaximum returns false when
   * some element is > val (loop exits early with i<size)
   */
  @Test
  public void testIsMaximumFalseWhenValLessThanSomeElement() {
    ArrayUtil au = new ArrayUtil(new float[] { 1f, 2f, 3f });
    assertFalse("2.5 is not >= all elements (3 exists)", au.isMaximum(2.5));
    assertFalse("0 is not >= all elements", au.isMaximum(0.0));
  }

  /**
   * test getMinimum returns the smallest element in the array
   */
  @Test
  public void testGetMinimum() {
    ArrayUtil au = new ArrayUtil(new float[] { 1f, -1f, 0f });
    assertEquals(-1, au.getMinimum(), 1e-9);
  }

  /**
   * test getMaximum returns the largest element in the array
   */
  @Test
  public void testGetMaximum() {
    ArrayUtil au = new ArrayUtil(new float[] { -3f, 1f, 0f });
    assertEquals(1, au.getMaximum(), 1e-9);
  }

  /**
   * test getVariance returns 0 for size==1 early-return branch
   */
  @Test
  public void testVarianceSizeOneReturnsZero() {
    ArrayUtil au = new ArrayUtil(new float[] { 42f });
    assertEquals(0.0, au.getVariance(), 1e-9);
  }

  /**
   * test getVariance matches the sample variance formula on a simple dataset
   */
  @Test
  public void testVarianceMatchesSampleVarianceFormula() {
    ArrayUtil au = new ArrayUtil(new float[] { 1f, 2f, 3f });
    assertEquals(1.0, au.getVariance(), 1e-9);
  }

  /**
   * test empty-array edge behavior: size=0, sorted=false,
   * mean/variance are NaN, and isMaximum returns true
   */
  @Test
  public void testEmptyArrayBehavior() throws Exception {
    ArrayUtil au = new ArrayUtil(0);
    assertEquals(0, getSize(au));
    assertFalse(au.sorted);
    assertEquals(0, au.values.length);

    assertTrue(Double.isNaN(au.getMean()));
    assertTrue(Double.isNaN(au.getVariance()));

    assertTrue(au.isMaximum(-100.0));
    assertTrue(au.isMaximum(0.0));
    assertTrue(au.isMaximum(100.0));
  }

  /**
   * test sort on empty array sets sorted=true
   */
  @Test
  public void testSortOnEmptySetsSorted() {
    ArrayUtil au = new ArrayUtil(0);
    au.sort();
    assertTrue(au.sorted);
  }

  /**
   * test reflection helper reads the
   * private size field for white-box assertions
   */
  private static int getSize(ArrayUtil au) throws Exception {
    Field field = ArrayUtil.class.getDeclaredField("size");
    field.setAccessible(true);
    return (int) field.get(au);
  }

}
