package ij;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertArrayEquals;

import ij.gui.Overlay;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Test;

/**
 * JUnit tests for the ImageJ Undo (ij.Undo) finite-state behavior.
 */
public class UndoFSMTest {

  /**
   * Cleanup runs after every test:
   * - Clear the "current image" used by Undo.undo()
   * - Reset Undo internal buffers/state
   */
  @After
  public void cleanup() {
    WindowManager.setTempCurrentImage(null);
    Undo.reset();
  }

  /**
   * If setup is called with a null ImagePlus,
   * Undo should clear state and end in NOTHING.
   */
  @Test
  public void testSetupNullClearsState() throws Exception {
    Undo.setup(Undo.FILTER, null);
    assertEquals(Undo.NOTHING, getWhatToUndo());
    assertEquals(0, getImageId());
  }

  /**
   * Rule: While in COMPOUND_FILTER mode,
   * a subsequent setup(FILTER, imp) is ignored.
   */
  @Test
  public void testCompoundFilterIgnoresFilterSetup() throws Exception {
    ImagePlus imp = createImage();
    Undo.setup(Undo.COMPOUND_FILTER, imp);
    Undo.setup(Undo.FILTER, imp);
    assertEquals(Undo.COMPOUND_FILTER, getWhatToUndo());
  }

  /**
   * In the implementation, this is represented by
   * whatToUndo becoming COMPOUND_FILTER_DONE.
   */
  @Test
  public void testCompoundFilterDoneTransitions() throws Exception {
    ImagePlus imp = createImage();
    Undo.setup(Undo.COMPOUND_FILTER, imp);
    Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
    assertEquals(Undo.COMPOUND_FILTER_DONE, getWhatToUndo());
  }

  /**
   * If no ROI exists, setup(ROI) sets whatToUndo
   * back to NOTHING and does not create roiCopy.
   */
  @Test
  public void testRoiSetupWithoutRoiClears() throws Exception {
    ImagePlus imp = createImage();
    Undo.setup(Undo.ROI, imp);
    assertEquals(Undo.NOTHING, getWhatToUndo());
    assertNull(getRoiCopy());
  }

  /**
   * Should store a cloned ROI into roiCopy and set whatToUndo to ROI.
   */
  @Test
  public void testRoiSetupWithRoiCopies() throws Exception {
    ImagePlus imp = createImage();
    imp.setRoi(new Roi(0, 0, 1, 1));
    Undo.setup(Undo.ROI, imp);
    assertEquals(Undo.ROI, getWhatToUndo());
    assertNotNull(getRoiCopy());
  }

  /**
   * saveOverlay() sets overlayCopy to null if overlay is absent.
   */
  @Test
  public void testOverlaySetupWithoutOverlay() throws Exception {
    ImagePlus imp = createImage();
    Undo.setup(Undo.OVERLAY, imp);
    assertNull(getOverlayCopy());
  }

  /**
   * saveOverlay() should duplicate the overlay
   * into overlayCopy and record overlayImageID.
   */
  @Test
  public void testOverlaySetupWithOverlay() throws Exception {
    ImagePlus imp = createImage();
    Overlay overlay = new Overlay();
    overlay.add(new Roi(0, 0, 1, 1));
    imp.setOverlay(overlay);
    Undo.setup(Undo.OVERLAY, imp);
    assertNotNull(getOverlayCopy());
    assertEquals(imp.getID(), getOverlayImageId());
  }

  /**
   * reset() does NOT clear the undo buffer
   * for COMPOUND_FILTER (compound-in-progress).
   * So calling reset() while in compound mode
   * is effectively a no-op for state.
   */
  @Test
  public void testResetSkipsCompoundFilter() throws Exception {
    ImagePlus imp = createImage();
    Undo.setup(Undo.COMPOUND_FILTER, imp);
    Undo.reset();
    assertEquals(Undo.COMPOUND_FILTER, getWhatToUndo());
  }

  /**
   * reset() does NOT clear the undo buffer for OVERLAY_ADDITION mode.
   * This supports repeated overlay-element undos.
   */
  @Test
  public void testResetSkipsOverlayAddition() throws Exception {
    ImagePlus imp = createImage();
    Undo.setup(Undo.OVERLAY_ADDITION, imp);
    Undo.reset();
    assertEquals(Undo.OVERLAY_ADDITION, getWhatToUndo());
  }

  /**
   * If Undo.undo() is called when there is no current image (imp == null),
   * it should reset internal undo state back to NOTHING.
   */
  @Test
  public void testUndoWithNoCurrentImageResetsState() throws Exception {
    ImagePlus imp = createImage();
    Undo.setup(Undo.TYPE_CONVERSION, imp);
    WindowManager.setTempCurrentImage(null);
    Undo.undo();
    assertEquals(Undo.NOTHING, getWhatToUndo());
  }

  /**
   * Must call snapshot() before modifying pixels
   * (FILTER undo uses swapPixelArrays()).
   * After undo(), pixel data should match the original bytes.
   */
  @Test
  public void testSimpleUndoRestoresPixels() {
    ImagePlus imp = createImage();
    WindowManager.setTempCurrentImage(imp);

    byte[] original = (byte[]) imp.getProcessor().getPixelsCopy();

    imp.getProcessor().snapshot();
    imp.getProcessor().invert();
    Undo.setup(Undo.FILTER, imp);

    Undo.undo();

    assertArrayEquals(original, (byte[]) imp.getProcessor().getPixels());
  }

  /**
   * setup(COMPOUND_FILTER) stores a duplicate of the starting pixels in ipCopy
   * intermediate setup(FILTER) calls are ignored
   * setup(COMPOUND_FILTER_DONE) marks compound completion
   * undo() should restore the pixels back to the initial state
   */
  @Test
  public void testCompoundUndoRestoresInitialSnapshot() throws Exception {
    ImagePlus imp = createImage();
    WindowManager.setTempCurrentImage(imp);
    byte[] original = (byte[]) imp.getProcessor().getPixelsCopy();

    Undo.setup(Undo.COMPOUND_FILTER, imp);
    imp.getProcessor().invert();
    Undo.setup(Undo.FILTER, imp); // ignored
    imp.getProcessor().invert(); // another change
    Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);

    Undo.undo();
    assertArrayEquals(original, (byte[]) imp.getProcessor().getPixels());
  }

  /**
   * setup(OVERLAY_ADDITION) enables "remove last overlay item" behavior
   * each Undo.undo() removes the most recently added Roi
   * state does not reset after a successful removal (supports multiple undos)
   */
  @Test
  public void testOverlayAdditionSuccessiveUndoRemovesLast() throws Exception {
    ImagePlus imp = createImage();
    WindowManager.setTempCurrentImage(imp);
    Overlay overlay = new Overlay();
    Roi roi1 = new Roi(0, 0, 1, 1);
    Roi roi2 = new Roi(1, 1, 1, 1);
    overlay.add(roi1);
    overlay.add(roi2);
    imp.setOverlay(overlay);

    Undo.setup(Undo.OVERLAY_ADDITION, imp);
    Undo.undo(); // remove roi2
    assertEquals(1, imp.getOverlay().size());
    assertEquals(roi1.getBounds(), imp.getOverlay().get(0).getBounds());

    Undo.undo(); // remove roi1
    assertEquals(0, imp.getOverlay().size());
  }

  // Helper: create a tiny 2x2 grayscale image for deterministic tests.
  private static ImagePlus createImage() {
    return new ImagePlus("test", new ByteProcessor(2, 2));
  }

  // Reflection helper: read Undo.whatToUndo (private static).
  private static int getWhatToUndo() throws Exception {
    return getIntField("whatToUndo");
  }
  // Reflection helper: read Undo.imageID (private static).
  private static int getImageId() throws Exception {
    return getIntField("imageID");
  }

  // Reflection helper: read Undo.overlayImageID (private static).
  private static int getOverlayImageId() throws Exception {
    return getIntField("overlayImageID");
  }

  /**
   * Generic reflection helper for private static int fields in Undo.
   */
  private static int getIntField(String name) throws Exception {
    Field field = Undo.class.getDeclaredField(name);
    field.setAccessible(true);
    return field.getInt(null);
  }

  // Reflection helper: read Undo.roiCopy (private static).
  private static Roi getRoiCopy() throws Exception {
    Field field = Undo.class.getDeclaredField("roiCopy");
    field.setAccessible(true);
    return (Roi) field.get(null);
  }

  // Reflection helper: read Undo.overlayCopy (private static).
  private static Overlay getOverlayCopy() throws Exception {
    Field field = Undo.class.getDeclaredField("overlayCopy");
    field.setAccessible(true);
    return (Overlay) field.get(null);
  }
}
