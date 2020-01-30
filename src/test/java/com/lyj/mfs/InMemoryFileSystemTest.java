package com.lyj.mfs;

import com.lyj.mfs.InMemoryFileSystem.Stats;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class InMemoryFileSystemTest {

	private FileSystem fsSession;

	@Before
	public void setUp() throws Exception {
		this.fsSession = InMemoryFileSystem.getSession();
//		this.fsSession.rm("/",true);
		this.fsSession.mkdir("/foo");
		this.fsSession.mkdir("/foo/bar");
		this.fsSession.mkdir("/foo/bar1");
		this.fsSession.mkdir("/foo/bar/zzz1");
		this.fsSession.mkdir("/foo/bar/zzz2");

		this.fsSession.touch("/foo/f1");
		this.fsSession.touch("/foo/f2");
	}

	@After
	public void tearDown() throws Exception {
		this.fsSession.rm("/", true);
	}

	@Test
	public void getStats() {
		Stats stats = InMemoryFileSystem.getInstance().getStats();
		System.out.println(stats);
		assertEquals(8,stats.totalPath);
		assertEquals(2,stats.totalFile);
		assertEquals(6,stats.totalDir);
	}
}