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
        Menu recent = mock(Menu.class);
        MenuItem itemA = mock(MenuItem.class);
        MenuItem itemB = mock(MenuItem.class);

        when(recent.getItemCount()).thenReturn(2);
        when(recent.getItem(0)).thenReturn(itemA);
        when(recent.getItem(1)).thenReturn(itemB);
        when(itemA.getLabel()).thenReturn("a.tif");
        when(itemB.getLabel()).thenReturn("b.tif");

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
        Menu recent = mock(Menu.class);
        MenuItem itemA = mock(MenuItem.class);

        when(recent.getItemCount()).thenReturn(1);
        when(recent.getItem(0)).thenReturn(itemA);
        when(itemA.getLabel()).thenReturn("a.tif");

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
        Menu recent = mock(Menu.class);
        MenuItem itemA = mock(MenuItem.class);
        MenuItem itemB = mock(MenuItem.class);

        when(recent.getItemCount()).thenReturn(2);
        when(recent.getItem(0)).thenReturn(itemA);
        when(recent.getItem(1)).thenReturn(itemB);
        when(itemA.getLabel()).thenReturn("a.tif");
        when(itemB.getLabel()).thenReturn("b.tif");

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