package com.lyj.mfs.utils;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void splitPath() {
		List<String> expectedList = Arrays.asList("foo", "bar", "ccc");

		Iterable<String> paths = null;

		paths =	StringUtils.splitPath("/foo/bar/ccc");
		assertEquals(expectedList, paths);

		paths =	StringUtils.splitPath("/foo/bar/ccc/");
		assertEquals(expectedList, paths);

		paths =	StringUtils.splitPath("/foo  /bar  /  ccc/");
		assertEquals(expectedList, paths);

		paths =	StringUtils.splitPath("///foo///bar///ccc");
		assertEquals(expectedList, paths);

		paths =	StringUtils.splitPath("foo///bar///ccc");
		assertEquals(expectedList, paths);
	}

	@Test
	public void extractParentDir() {
		String expectedVal = "/foo/bar/";
		String parentDir = StringUtils.extractParentDir("/foo/bar/ccc");
		assertEquals(expectedVal, parentDir);

		parentDir = StringUtils.extractParentDir("/foo/bar/ccc/");
		assertEquals(expectedVal, parentDir);


		expectedVal = "/";

		parentDir = StringUtils.extractParentDir("/foo/");
		assertEquals(expectedVal, parentDir);

		parentDir = StringUtils.extractParentDir("/foo");
		assertEquals(expectedVal, parentDir);

		parentDir = StringUtils.extractParentDir("foo");
		assertEquals("", parentDir);
	}

	@Test
	public void checkIsFilePath() {
	}

	@Test
	public void checkIsDirPath() {
	}
}