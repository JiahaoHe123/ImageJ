package ij;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Menu;
import java.awt.MenuItem;

import org.junit.Test;

/**
 * Unit tests for the testable design of {@link RecentOpener}: runWith(OpenerService, RecentMenuProvider).
 * Uses stubs and mocks so that no real file I/O or AWT (Menu/MenuItem) is used — safe for headless CI.
 */
public class RecentOpenerTest {

	/** Stub opener that records the path passed to open() for assertion. */
	private static class StubOpenerService implements RecentOpener.OpenerService {
		String lastPath;

		@Override
		public void open(String path) {
			this.lastPath = path;
		}
	}

	@Test
	public void runWithCallsOpenerWithPath() {
		String path = "test.tif";
		StubOpenerService stubOpener = new StubOpenerService();
		Menu menu = mock(Menu.class);
		MenuItem item = mock(MenuItem.class);
		when(menu.getItemCount()).thenReturn(1);
		when(menu.getItem(0)).thenReturn(item);
		when(item.getLabel()).thenReturn(path);
		RecentOpener.RecentMenuProvider stubProvider = () -> menu;

		RecentOpener ro = new RecentOpener(path, true);
		ro.runWith(stubOpener, stubProvider);

		assertNotNull(stubOpener.lastPath);
		assertEquals(path, stubOpener.lastPath);
	}

	@Test
	public void runWithMovesPathToTopWhenNotAlreadyFirst() {
		String path = "second.tif";
		StubOpenerService stubOpener = new StubOpenerService();
		Menu menu = mock(Menu.class);
		MenuItem itemFirst = mock(MenuItem.class);
		MenuItem itemSecond = mock(MenuItem.class);
		when(menu.getItemCount()).thenReturn(2);
		when(menu.getItem(0)).thenReturn(itemFirst);
		when(menu.getItem(1)).thenReturn(itemSecond);
		when(itemFirst.getLabel()).thenReturn("first.tif");
		when(itemSecond.getLabel()).thenReturn(path);
		RecentOpener.RecentMenuProvider stubProvider = () -> menu;

		RecentOpener ro = new RecentOpener(path, true);
		ro.runWith(stubOpener, stubProvider);

		assertEquals(path, stubOpener.lastPath);
		verify(menu).remove(1);
		verify(menu).insert(itemSecond, 0);
	}
}
