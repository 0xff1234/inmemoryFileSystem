package com.lyj.mfs;

import static com.lyj.mfs.utils.Const.ROOT_PATH;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InMemoryFileSystemSessionTest {

	private final int TASK_SIZE = 100000;
	private FileSystem fsSession;
	private ThreadPoolExecutor threadpool;

	@Before
	public void setUp() throws Exception {
		this.fsSession = InMemoryFileSystem.getSession();
		this.threadpool = new ThreadPoolExecutor(100, 500, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(TASK_SIZE));

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
		this.threadpool.shutdown();
	}

	@Test
	public void getSessionId() throws InterruptedException {
		final Set<String> countingSet = new ConcurrentSkipListSet<>();
		final CountDownLatch countDownLatch = new CountDownLatch(TASK_SIZE);
		for(int i = 0 ; i < TASK_SIZE; ++i){
			this.threadpool
				.submit(
					() -> {
						countingSet.add(InMemoryFileSystem.getSession().getSessionId());
						countDownLatch.countDown();
					}
				);
		}
		countDownLatch.await(30, TimeUnit.SECONDS);
		assertEquals(TASK_SIZE, countingSet.size());

	}

	@Test
	public void ls() {
	}

	@Test
	public void mkdir() {

		this.fsSession.cd("/foo/bar/zzz1");
		this.fsSession.mkdir("xxx1");

		assertEquals(Arrays.asList("xxx1"), this.fsSession.ls());

	}

	@Test
	public void touch() {

		this.fsSession.cd("/foo");
		this.fsSession.touch("f3");

		assertEquals(Arrays.asList("bar" , "bar1", "f1", "f2", "f3"), this.fsSession.ls());

	}

	@Test
	public void cd() {
		String ret = "";
		ret = this.fsSession.cd("/");
		assertEquals(Arrays.asList("foo"), this.fsSession.ls());
		assertEquals("/", ret);


		ret = this.fsSession.cd("foo");
		assertEquals(Arrays.asList("bar", "bar1", "f1","f2"), this.fsSession.ls());
		assertEquals("/foo", ret);

		ret = this.fsSession.cd(".");
		assertEquals(Arrays.asList("bar", "bar1", "f1","f2"), this.fsSession.ls());
		assertEquals("/foo", ret);

		ret = this.fsSession.cd("bar/zzz1");
		assertEquals(Arrays.asList(), this.fsSession.ls());
		assertEquals("/foo/bar/zzz1", ret);

		ret = this.fsSession.cd("../../");
		assertEquals(Arrays.asList("bar", "bar1","f1","f2"), this.fsSession.ls());
		assertEquals("/foo", ret);

		ret = this.fsSession.cd("../../../../../");
		assertEquals(Arrays.asList("foo"), this.fsSession.ls());
		assertNull(ROOT_PATH, ret);
	}

	@Test
	public void pwd() {

	}

	@Test
	public void rm() {
	}
}