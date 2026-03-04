package ij;
import ij.io.*;
import java.awt.*;
import java.io.*;

/** Opens, in a separate thread, files selected from the File/Open Recent submenu.*/
public class RecentOpener implements Runnable {

	/** Abstraction for opening a path (allows tests to stub file I/O). */
	public interface OpenerService {
		void open(String path);
	}

	/** Abstraction for obtaining the Open Recent menu (allows tests to stub UI). */
	public interface RecentMenuProvider {
		Menu getOpenRecentMenu();
	}

	private final String path;

	RecentOpener(String path) {
		this.path = path;
		Thread thread = new Thread(this, "RecentOpener");
		thread.start();
	}

	/** For tests: same path but does not start a thread; use with runWith(opener, menuProvider). */
	RecentOpener(String path, boolean noThread) {
		this.path = path;
		if (noThread) return;
		Thread thread = new Thread(this, "RecentOpener");
		thread.start();
	}

	/** Open the file and move the path to top of the submenu. */
	@Override
	public void run() {
		runWith(
			new OpenerService() {
				@Override
				public void open(String p) {
					new Opener().open(p);
				}
			},
			new RecentMenuProvider() {
				@Override
				public Menu getOpenRecentMenu() {
					return Menus.getOpenRecentMenu();
				}
			}
		);
	}

	/**
	 * Testable entry point: performs open and move-to-top using injected services.
	 * Used by tests to avoid real Opener (file I/O) and real Menus (UI).
	 */
	void runWith(OpenerService opener, RecentMenuProvider menuProvider) {
		opener.open(path);
		Menu menu = menuProvider.getOpenRecentMenu();
		if (menu == null) return;
		int n = menu.getItemCount();
		int index = 0;
		for (int i = 0; i < n; i++) {
			if (menu.getItem(i).getLabel().equals(path)) {
				index = i;
				break;
			}
		}
		if (index > 0) {
			MenuItem item = menu.getItem(index);
			menu.remove(index);
			menu.insert(item, 0);
		}
	}
}

