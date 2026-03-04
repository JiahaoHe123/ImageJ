package ij.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link WildcardMatch}.
 * Covers pattern matching with '?' (single char) and '*' (zero or more chars),
 * escape character, custom wildcards, and case sensitivity.
 */
public class WildcardMatchTest {

	private WildcardMatch wm;

	/** Before each test, create a fresh WildcardMatch with default '?' and '*'. */
	@Before
	public void setUp() {
		wm = new WildcardMatch();
	}

	/** Exact match: same string matches; prefix or longer string does not match. */
	@Test
	public void testMatchExact() {
		assertTrue(wm.match("hello", "hello"));
		assertFalse(wm.match("hello", "hell"));
		assertFalse(wm.match("hello", "helloo"));
	}

	/** Single-char wildcard '?': one '?' matches exactly one character; wrong length returns false. */
	@Test
	public void testMatchSingleCharWildcard() {
		assertTrue(wm.match("A", "?"));
		assertTrue(wm.match("ab", "??"));
		assertTrue(wm.match("hello", "h?llo"));
		assertTrue(wm.match("xyz", "???"));
		assertFalse(wm.match("hello", "????"));
	}

	/** Multiple-char wildcard '*': matches zero or more chars; e.g. "C*.class", "*", "*o", empty string. */
	@Test
	public void testMatchMultipleCharWildcard() {
		assertTrue(wm.match("CfgOptions.class", "C*.class"));
		assertTrue(wm.match("hello", "*"));
		assertTrue(wm.match("hello", "*o"));
		assertTrue(wm.match("hello", "h*"));
		assertTrue(wm.match("hello", "he*o"));
		assertTrue(wm.match("", "*"));
		assertTrue(wm.match("x", "*"));
	}

	/** Combined '?' and '*': pattern "??gOpti*c?ass" matches "CfgOptions.class"; wrong count fails. */
	@Test
	public void testMatchCombinedWildcards() {
		assertTrue(wm.match("CfgOptions.class", "??gOpti*c?ass"));
		assertFalse(wm.match("CfgOptions.class", "?gOpti*c?as?"));
	}

	/** Escape with backslash: "What*\\?" matches "What's this?"; ".*\\\\?back*" matches "A \\ backslash". */
	@Test
	public void testMatchEscapeBackslash() {
		assertTrue(wm.match("What's this?", "What*\\?"));
		assertTrue(wm.match("What's this?", "What*?"));
		assertTrue(wm.match("A \\ backslash", "*\\\\?back*"));
	}

	/** Escaped '?' and '*' in pattern match literal ? and * in string (e.g. "file\\?.txt" vs "file?.txt"). */
	@Test
	public void testMatchEscapeRemovesWildcardMeaning() {
		WildcardMatch wm2 = new WildcardMatch('?', '*');
		assertTrue(wm2.match("file?.txt", "file\\?.txt"));
		assertTrue(wm2.match("file*.txt", "file\\*.txt"));
	}

	/** Custom wildcards via constructor or setWildcardChars: '.' for one char, '#' for many; getters return them. */
	@Test
	public void testCustomWildcardChars() {
		WildcardMatch custom = new WildcardMatch('.', '#');
		custom.setWildcardChars('.', '#');
		assertTrue(custom.match("hello", "h.llo"));
		assertTrue(custom.match("hello", "h#"));
		assertTrue(custom.match("test", "t#t"));
		assertEquals('.', custom.getSingleWildcardChar());
		assertEquals('#', custom.getMultipleWildcardChar());
	}

	/** setEscapeChar and getEscapeChar: after set to '@', getter returns '@'. */
	@Test
	public void testSetEscapeChar() {
		wm.setEscapeChar('@');
		assertEquals('@', wm.getEscapeChar());
	}

	/** Case insensitive: setCaseSensitive(false) then "HELLO" matches "hello", "MiXeD" matches "m*x*d". */
	@Test
	public void testCaseInsensitive() {
		wm.setCaseSensitive(false);
		assertTrue(wm.match("HELLO", "hello"));
		assertTrue(wm.match("Hello", "h*"));
		assertTrue(wm.match("MiXeD", "m*x*d"));
	}

	/** Default is case sensitive: getCaseSensitive() true; "HELLO" does not match "hello". */
	@Test
	public void testCaseSensitiveByDefault() {
		assertTrue(wm.getCaseSensitive());
		assertFalse(wm.match("HELLO", "hello"));
		assertTrue(wm.match("hello", "hello"));
	}

	/** Empty string and pattern: match("","") true; non-empty string with empty pattern (or vice versa) false. */
	@Test
	public void testEmptyPatternAndString() {
		assertTrue(wm.match("", ""));
		assertFalse(wm.match("x", ""));
		assertFalse(wm.match("", "x"));
	}

	/** Constructor WildcardMatch(singleChar, multipleChar): getters return the two chars passed in. */
	@Test
	public void testConstructorWithCustomChars() {
		WildcardMatch wm2 = new WildcardMatch('.', '#');
		assertEquals('.', wm2.getSingleWildcardChar());
		assertEquals('#', wm2.getMultipleWildcardChar());
	}
}
