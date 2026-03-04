package ij;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Menu;
import java.awt.MenuItem;

import org.junit.Test;
import org.mockito.ArgumentMatchers;

/**
 * Mockito-based tests for {@link RecentOpener}: verify that runWith calls the opener and menu provider
 * as expected (behavior checking not possible without mocking).
 */
public class RecentOpenerMockingTest {

	@Test
	public void runWithCallsOpenerOpenExactlyOnceWithPath() {
		RecentOpener.OpenerService mockOpener = mock(RecentOpener.OpenerService.class);
		RecentOpener.RecentMenuProvider mockMenuProvider = mock(RecentOpener.RecentMenuProvider.class);
		Menu mockMenu = mock(Menu.class);
		MenuItem mockItem = mock(MenuItem.class);

		when(mockMenuProvider.getOpenRecentMenu()).thenReturn(mockMenu);
		when(mockMenu.getItemCount()).thenReturn(1);
		when(mockMenu.getItem(0)).thenReturn(mockItem);
		when(mockItem.getLabel()).thenReturn("mocked.tif");

		String path = "mocked.tif";
		RecentOpener ro = new RecentOpener(path, true);
		ro.runWith(mockOpener, mockMenuProvider);

		verify(mockOpener).open(path);
		verify(mockMenuProvider).getOpenRecentMenu();
	}

	@Test
	public void runWithDoesNotCallMenuRemoveOrInsertWhenPathIsFirst() {
		RecentOpener.OpenerService mockOpener = mock(RecentOpener.OpenerService.class);
		RecentOpener.RecentMenuProvider mockMenuProvider = mock(RecentOpener.RecentMenuProvider.class);
		Menu mockMenu = mock(Menu.class);
		MenuItem mockItem = mock(MenuItem.class);

		when(mockMenuProvider.getOpenRecentMenu()).thenReturn(mockMenu);
		when(mockMenu.getItemCount()).thenReturn(1);
		when(mockMenu.getItem(0)).thenReturn(mockItem);
		when(mockItem.getLabel()).thenReturn("first.tif");

		RecentOpener ro = new RecentOpener("first.tif", true);
		ro.runWith(mockOpener, mockMenuProvider);

		verify(mockOpener).open("first.tif");
		verify(mockMenu).getItemCount();
		verify(mockMenu).getItem(0);
		verify(mockMenu, never()).remove(ArgumentMatchers.anyInt());
		verify(mockMenu, never()).insert(ArgumentMatchers.any(MenuItem.class), ArgumentMatchers.anyInt());
	}

	@Test
	public void runWithDoesNothingToMenuWhenMenuProviderReturnsNull() {
		RecentOpener.OpenerService mockOpener = mock(RecentOpener.OpenerService.class);
		RecentOpener.RecentMenuProvider mockMenuProvider = mock(RecentOpener.RecentMenuProvider.class);

		when(mockMenuProvider.getOpenRecentMenu()).thenReturn(null);

		RecentOpener ro = new RecentOpener("any.tif", true);
		ro.runWith(mockOpener, mockMenuProvider);

		verify(mockOpener).open("any.tif");
		verify(mockMenuProvider).getOpenRecentMenu();
	}
}
