package ij;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Menu;
import java.awt.MenuItem;

import org.junit.Test;

/**
 * Unit tests for the testable design of {@link RecentOpener}: runWith(OpenerService, RecentMenuProvider).
 * Uses stubs (simple implementations that record or supply data) so that no real file I/O or UI is used.
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

	/** Stub menu provider that returns a real AWT menu (needed for getItemCount/getItem/getLabel/remove/insert). */
	private static class StubRecentMenuProvider implements RecentOpener.RecentMenuProvider {
		private final Menu menu;

		StubRecentMenuProvider(Menu menu) {
			this.menu = menu;
		}

		@Override
		public Menu getOpenRecentMenu() {
			return menu;
		}
	}

	@Test
	public void runWithCallsOpenerWithPath() {
		String path = "test.tif";
		StubOpenerService stubOpener = new StubOpenerService();
		Menu menu = new Menu();
		menu.add(new MenuItem(path));
		RecentOpener.RecentMenuProvider stubProvider = new StubRecentMenuProvider(menu);

		RecentOpener ro = new RecentOpener(path, true);
		ro.runWith(stubOpener, stubProvider);

		assertNotNull(stubOpener.lastPath);
		assertEquals(path, stubOpener.lastPath);
	}

	@Test
	public void runWithMovesPathToTopWhenNotAlreadyFirst() {
		String path = "second.tif";
		StubOpenerService stubOpener = new StubOpenerService();
		Menu menu = new Menu();
		menu.add(new MenuItem("first.tif"));
		menu.add(new MenuItem(path));
		RecentOpener.RecentMenuProvider stubProvider = new StubRecentMenuProvider(menu);

		RecentOpener ro = new RecentOpener(path, true);
		ro.runWith(stubOpener, stubProvider);

		assertEquals(path, stubOpener.lastPath);
		assertEquals(2, menu.getItemCount());
		assertEquals(path, menu.getItem(0).getLabel());
		assertEquals("first.tif", menu.getItem(1).getLabel());
	}
}
