package com.lyj.mfs;

import static com.lyj.mfs.utils.Const.ROOT_PATH;
import static org.junit.Assert.*;

import com.lyj.mfs.InMemoryFileSystem.Stats;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InMemoryFileSystemSessionTest {

	/* total task for multi thread test each time*/
	private final int TASK_SIZE = 100000;

	/* use for single thread test */
	private FileSystem fsSession;

	/* use for multi thread test */
	private ExecutorService threadpool;

	@Before
	public void setUp() throws Exception {
		this.fsSession = InMemoryFileSystem.getSession();
		this.threadpool = new ThreadPoolExecutor(100, 500, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(TASK_SIZE));

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
	public void ls() {
		this.fsSession.cd("/");
		assertEquals(Arrays.asList("foo"), this.fsSession.ls());
	}

	@Test
	public void mkdir() {
		boolean ret = false;

		this.fsSession.cd("/foo/bar/zzz1");
		this.fsSession.mkdir("xxx1");
		assertEquals(Arrays.asList("xxx1"), this.fsSession.ls());

		ret = this.fsSession.mkdir("/foo/f1");
		assertFalse("there is already a same name file", ret);
	}

	@Test(expected = IllegalArgumentException.class)
	public void mkdir1() {
		this.fsSession.mkdir(null);
	}

	@Test
	public void touch() {
		String ret = null;
		this.fsSession.cd("/foo");
		ret = this.fsSession.touch("f3");
		assertEquals("/foo/f3", ret);

		ret = this.fsSession.touch("/foo/f3");
		assertEquals("/foo/f3", ret);

		ret = this.fsSession.touch("/foo");
		assertNull( ret);

	}

	@Test(expected = IllegalArgumentException.class)
	public void touch1() {
		this.fsSession.touch(null);
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

	}

	@Test(expected = IllegalArgumentException.class)
	public void cd1() {
		this.fsSession.cd(null);
	}


	@Test
	public void cd2() {
		String ret = "";

		ret = this.fsSession.cd("../../../../../");
		assertEquals(Arrays.asList("foo"), this.fsSession.ls());
		assertEquals(ROOT_PATH, ret);

		ret = this.fsSession.cd("/foo");
		ret = this.fsSession.cd("/foo/sodfodfdfd");
		assertEquals("/foo", ret);
	}

	@Test
	public void pwd() {
		String ret = "";
		this.fsSession.cd("/foo/bar");
		ret = this.fsSession.pwd();
		assertEquals("/foo/bar", ret);

		this.fsSession.cd("../bar/zzz1");
		ret = this.fsSession.pwd();
		assertEquals("/foo/bar/zzz1", ret);
	}

	@Test
	public void rm() {
		boolean ret;
		ret = this.fsSession.rm("/foo/f1", false);
		assertTrue(ret);

		ret = this.fsSession.rm("/foo/f1/dfdfd", false);
		assertFalse("delete invalid path",ret);

		ret = this.fsSession.rm("/foo/dfdfd", false);
		assertFalse("delete a random file", ret);

		ret = this.fsSession.rm("/foo/bar", false);
		assertFalse("directory can not be remove without recursive", ret);

		ret = this.fsSession.rm("/foo/bar", true);
		assertTrue("directory must be remove with recursive flag", ret);

		this.fsSession.mkdir("/foo/bar");
		ret = this.fsSession.rm("/foo/bar/", true);
		assertTrue("directory must be remove with recursive flag", ret);

		ret = this.fsSession.rm("/", true);
		assertTrue("delete all files in root path", ret);

		ret = this.fsSession.rm("/", false);
		assertFalse( ret);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rm1() {
		this.fsSession.rm(null, false);
	}

	@Test
	public void rm2() {
		boolean ret;
		this.fsSession.cd("/foo/bar/");
		this.fsSession.rm("/foo", true);
		assertEquals(Arrays.asList(), this.fsSession.ls());
	}


	/**
	 * test if the sessionId might be conflicted in multi-thread env
	 * @throws InterruptedException
	 */
	@Test
	public void getSessionId() throws InterruptedException {
		final Set<String> countingResult = new ConcurrentSkipListSet<>();
		final CountDownLatch countDownLatch = new CountDownLatch(TASK_SIZE);
		final CyclicBarrier cyclicBarrier = new CyclicBarrier(100);
		for(int i = 0 ; i < TASK_SIZE; ++i){
			this.threadpool
				.submit(
					() -> {
						try {
							cyclicBarrier.await();
//							Thread.sleep(1000);
//							System.out.println("start");
						} catch (BrokenBarrierException | InterruptedException e) {
							e.printStackTrace();
						}
						countingResult.add(InMemoryFileSystem.getSession().getSessionId());
						countDownLatch.countDown();
					}
				);
		}
		countDownLatch.await(30, TimeUnit.SECONDS);
		assertEquals(TASK_SIZE, countingResult.size());
	}

	/**
	 * test if the sessionId might be conflicted in multi-thread env
	 * @throws InterruptedException
	 */
	@Test
	public void multiThreadOperation() throws InterruptedException {

		InMemoryFileSystem.getSession().rm("/", true);


		/*
		 * 100 thread add 1000000 file and 1000000 dirs simultaneous
		 * and then check whether the total count is correct
		 * notice the total dir is 1000000 + 1 (the root path /)
		 * */
		final CountDownLatch countDownLatch = new CountDownLatch(TASK_SIZE);
		final CyclicBarrier cyclicBarrier = new CyclicBarrier(100);
		for(int i = 0 ; i < TASK_SIZE; ++i){
			this.threadpool
				.submit(
					() -> {
						try {
							cyclicBarrier.await(10, TimeUnit.SECONDS);
//							Thread.sleep(1000);
//							System.out.println("start");
						} catch (BrokenBarrierException | InterruptedException | TimeoutException e) {
							e.printStackTrace();
						}
						//start do messy things
						InMemoryFileSystemSession session = InMemoryFileSystem.getSession();
						for(int j = 0; j < 10; ++j){
							String randomStr = UUID.randomUUID().toString();
							session.mkdir(randomStr);
							session.cd(randomStr);

							randomStr = UUID.randomUUID().toString();
							session.touch(randomStr);
						}
						countDownLatch.countDown();
					}
				);
		}
		countDownLatch.await(30, TimeUnit.SECONDS);

		Stats stats = InMemoryFileSystem.getInstance().getStats();
		assertEquals(2000001, stats.totalPath);
		assertEquals(1000000, stats.totalFile);
		assertEquals(1000001, stats.totalDir);

	}
}