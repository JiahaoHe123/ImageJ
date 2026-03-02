package ij;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.Menu;
import java.awt.MenuItem;

import org.junit.Test;
import org.mockito.MockedStatic;

public class ExecuterMockingTest {

    @Test
    public void openRecentCallsIJOpenExactlyOnceWhenMenuContainsCmd() {
        Menu recent = new Menu("Open Recent");
        recent.add(new MenuItem("a.tif"));
        recent.add(new MenuItem("b.tif"));

        Executer ex = new Executer("ignored");

        try (MockedStatic<Menus> menusMock = mockStatic(Menus.class);
             MockedStatic<IJ> ijMock = mockStatic(IJ.class)) {

            menusMock.when(Menus::getOpenRecentMenu).thenReturn(recent);

            boolean result = ex.openRecent("b.tif");

            assertTrue(result);
            ijMock.verify(() -> IJ.open("b.tif"), times(1));
            ijMock.verifyNoMoreInteractions();
            menusMock.verify(Menus::getOpenRecentMenu, times(1));
        }
    }

    @Test
    public void openRecentNeverCallsIJOpenWhenCmdMissing() {
        Menu recent = new Menu("Open Recent");
        recent.add(new MenuItem("a.tif"));

        Executer ex = new Executer("ignored");

        try (MockedStatic<Menus> menusMock = mockStatic(Menus.class);
             MockedStatic<IJ> ijMock = mockStatic(IJ.class)) {

            menusMock.when(Menus::getOpenRecentMenu).thenReturn(recent);

            boolean result = ex.openRecent("missing.tif");

            assertFalse(result);
            ijMock.verifyNoInteractions();
            menusMock.verify(Menus::getOpenRecentMenu, times(1));
        }
    }

    @Test
    public void openRecentNeverCallsIJOpenWhenMenuIsNull() {
        Executer ex = new Executer("ignored");

        try (MockedStatic<Menus> menusMock = mockStatic(Menus.class);
             MockedStatic<IJ> ijMock = mockStatic(IJ.class)) {

            menusMock.when(Menus::getOpenRecentMenu).thenReturn(null);

            boolean result = ex.openRecent("anything.tif");

            assertFalse(result);
            ijMock.verifyNoInteractions();
            menusMock.verify(Menus::getOpenRecentMenu, times(1));
        }
    }

    @Test
    public void openRecentCallsGetOpenRecentMenuTwiceWhenInvokedTwice() {
        Menu recent = new Menu("Open Recent");
        recent.add(new MenuItem("a.tif"));
        recent.add(new MenuItem("b.tif"));

        Executer ex = new Executer("ignored");

        try (MockedStatic<Menus> menusMock = mockStatic(Menus.class);
             MockedStatic<IJ> ijMock = mockStatic(IJ.class)) {

            menusMock.when(Menus::getOpenRecentMenu).thenReturn(recent);

            assertTrue(ex.openRecent("a.tif"));
            assertTrue(ex.openRecent("b.tif"));

            menusMock.verify(Menus::getOpenRecentMenu, times(2));

            ijMock.verify(() -> IJ.open("a.tif"), times(1));
            ijMock.verify(() -> IJ.open("b.tif"), times(1));
            ijMock.verifyNoMoreInteractions();
        }
    }
}