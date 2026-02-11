package ij.plugin.frame;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Test Cases for ImageJ Recorder Finite State Machine
 * Each test covers one state transition
 *
 */
public class RecorderFSMTest {

    private Recorder recorder;

    @Before
    public void setUp() {
        if (Recorder.getInstance() != null) {
            Recorder.getInstance().close();
        }
    }

    @After
    public void tearDown() {
        if (recorder != null) {
            recorder.close();
            recorder = null;
        }
    }

    /**
     * T1: NOT_CREATED → IDLE
     * Event: new Recorder()
     */
    @Test
    public void testT1_NotCreated_To_Idle() {
        assertNull("Start in NOT_CREATED", Recorder.getInstance());

        recorder = new Recorder(false);

        assertNotNull("Should be in IDLE", Recorder.getInstance());
        assertTrue("record should be true", Recorder.record);
    }

    /**
     * T2: IDLE → RECORDING_COMMAND
     * Event: setCommand()
     */
    @Test
    public void testT2_Idle_To_RecordingCommand() {
        recorder = new Recorder(false);

        Recorder.setCommand("Test Command");

        assertEquals("Should be in RECORDING_COMMAND", "Test Command", Recorder.getCommand());
        assertNull("commandOptions should be null", Recorder.getCommandOptions());
    }

    /**
     * T3: RECORDING_COMMAND → BUILDING_OPTIONS
     * Event: recordOption()
     */
    @Test
    public void testT3_RecordingCommand_To_BuildingOptions() {
        recorder = new Recorder(false);
        Recorder.setCommand("Gaussian Blur...");

        Recorder.recordOption("sigma", "2.0");

        assertNotNull("Should be in BUILDING_OPTIONS", Recorder.getCommandOptions());
        assertTrue(Recorder.getCommandOptions().contains("sigma=2.0"));
    }

    /**
     * T4: BUILDING_OPTIONS → BUILDING_OPTIONS (self-loop)
     * Event: recordOption() again
     */
    @Test
    public void testT4_BuildingOptions_SelfLoop() {
        recorder = new Recorder(false);
        Recorder.setCommand("Test");
        Recorder.recordOption("option1", "value1");

        Recorder.recordOption("option2", "value2");

        assertTrue("Should accumulate options",
                Recorder.getCommandOptions().contains("option1=value1"));
        assertTrue("Should accumulate options",
                Recorder.getCommandOptions().contains("option2=value2"));
    }

    /**
     * T5: RECORDING_COMMAND → IDLE
     * Event: saveCommand() (no options)
     */
    @Test
    public void testT5_RecordingCommand_To_Idle() {
        recorder = new Recorder(false);
        Recorder.setCommand("Invert");

        Recorder.saveCommand();

        assertNull("Should return to IDLE", Recorder.getCommand());
        assertTrue("Should record command", recorder.getText().contains("Invert"));
    }

    /**
     * T6: BUILDING_OPTIONS → IDLE
     * Event: saveCommand() (with options)
     */
    @Test
    public void testT6_BuildingOptions_To_Idle() {
        recorder = new Recorder(false);
        Recorder.setCommand("Gaussian Blur...");
        Recorder.recordOption("sigma", "2.0");

        Recorder.saveCommand();

        assertNull("Should return to IDLE", Recorder.getCommand());
        assertNull("commandOptions cleared", Recorder.getCommandOptions());
    }

    /**
     * T7: RECORDING_COMMAND → IDLE
     * Event: disableCommandRecording()
     */
    @Test
    public void testT7_RecordingCommand_To_Idle_Cancel() {
        recorder = new Recorder(false);
        Recorder.setCommand("Test Command");

        Recorder.disableCommandRecording();

        assertNull("Should return to IDLE", Recorder.getCommand());
    }

    /**
     * T8: BUILDING_OPTIONS → IDLE
     * Event: disableCommandRecording()
     */
    @Test
    public void testT8_BuildingOptions_To_Idle_Cancel() {
        recorder = new Recorder(false);
        Recorder.setCommand("Test");
        Recorder.recordOption("key", "value");

        Recorder.disableCommandRecording();

        assertNull("Should return to IDLE", Recorder.getCommand());
    }

    /**
     * T9: ANY → SUSPENDED
     * Event: suspendRecording() from IDLE
     */
    @Test
    public void testT9_Idle_To_Suspended() {
        recorder = new Recorder(false);

        Recorder.suspendRecording();

        // Try to record - should not work
        Recorder.setCommand("Test");
        assertNull("Should not record in SUSPENDED", Recorder.getCommand());
    }

    /**
     * T10: SUSPENDED → Previous State
     * Event: resumeRecording()
     */
    @Test
    public void testT10_Suspended_To_PreviousState() {
        recorder = new Recorder(false);
        Recorder.suspendRecording();

        Recorder.resumeRecording();

        // Recording should work now
        Recorder.setCommand("Test");
        assertEquals("Should record after resume", "Test", Recorder.getCommand());
    }

    /**
     * T11: IDLE → CLOSED
     * Event: close()
     */
    @Test
    public void testT11_Idle_To_Closed() {
        recorder = new Recorder(false);

        recorder.close();

        assertNull("Should be CLOSED", Recorder.getInstance());
        assertFalse("record should be false", Recorder.record);
    }

    /**
     * T12: RECORDING_COMMAND → CLOSED
     * Event: close()
     */
    @Test
    public void testT12_RecordingCommand_To_Closed() {
        recorder = new Recorder(false);
        Recorder.setCommand("Test");

        recorder.close();

        assertNull("Should be CLOSED", Recorder.getInstance());
    }

    /**
     * T13: BUILDING_OPTIONS → CLOSED
     * Event: close()
     */
    @Test
    public void testT13_BuildingOptions_To_Closed() {
        recorder = new Recorder(false);
        Recorder.setCommand("Test");
        Recorder.recordOption("key", "value");

        recorder.close();

        assertNull("Should be CLOSED", Recorder.getInstance());
    }

    /**
     * T14: SUSPENDED → CLOSED
     * Event: close()
     */
    @Test
    public void testT14_Suspended_To_Closed() {
        recorder = new Recorder(false);
        Recorder.suspendRecording();

        recorder.close();

        assertNull("Should be CLOSED", Recorder.getInstance());
    }
}