package com.lyj.mfs.domain;

import static org.junit.Assert.*;

import com.lyj.mfs.domain.InfoNode.FileType;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AbsolutePathTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void constructor() {
		boolean ret = false;
		AbsolutePath absPath = new AbsolutePath(new InfoNode("/", FileType.DIRECTORY));
		ret = absPath.appendRelativePath(new InfoNode("foo", FileType.DIRECTORY));
		ret = absPath.appendRelativePath(new InfoNode("bar", FileType.DIRECTORY));
		assertEquals("/foo/bar", absPath.toString());

		AbsolutePath absPath2 = new AbsolutePath(absPath);

		assertEquals("/foo/bar", absPath2.toString());

		absPath2.appendRelativePath(new InfoNode("zzz",FileType.DIRECTORY));
		assertEquals("/foo/bar/zzz", absPath2.toString());
		assertEquals("/foo/bar", absPath.toString());

	}

	@Test
	public void append() {
		AbsolutePath absPath = new AbsolutePath(new InfoNode("/", FileType.DIRECTORY));
		absPath.appendRelativePath(new InfoNode("foo", FileType.DIRECTORY));
		absPath.appendRelativePath(new InfoNode("bar", FileType.DIRECTORY));
		assertEquals("/foo/bar", absPath.toString());
	}

	@Test
	public void removeLast() {
		boolean ret = false;
		AbsolutePath absPath = new AbsolutePath(new InfoNode("/", FileType.DIRECTORY));
		ret = absPath.appendRelativePath(new InfoNode("foo", FileType.DIRECTORY));
		ret = absPath.appendRelativePath(new InfoNode("bar", FileType.DIRECTORY));
		assertEquals("/foo/bar", absPath.toString());

		ret = absPath.removeLowest();
		assertTrue(ret);
		assertEquals("/foo", absPath.toString());

		ret = absPath.removeLowest();
		assertTrue(ret);
		assertEquals("/", absPath.toString());

		ret = absPath.removeLowest();
		assertEquals("/", absPath.toString());
		assertFalse(ret);
	}


	@Test
	public void immutablePaths() {
		boolean ret = false;
		AbsolutePath absPath = new AbsolutePath(new InfoNode("/", FileType.DIRECTORY));
		ret = absPath.appendRelativePath(new InfoNode("foo", FileType.DIRECTORY));
		ret = absPath.appendRelativePath(new InfoNode("bar", FileType.DIRECTORY));
		assertEquals("/foo/bar", absPath.toString());

		List<InfoNode> immutablePaths = absPath.immutablePaths();

		thrown.expect(UnsupportedOperationException.class);
		immutablePaths.add(new InfoNode("zzz", FileType.DIRECTORY));

	}
}