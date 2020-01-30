package com.lyj.mfs;

import static com.lyj.mfs.utils.Const.ROOT_PATH;

import com.lyj.mfs.domain.InfoNode;
import com.lyj.mfs.domain.InfoNode.FileType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: InmemoryFileSystem
 * @description: a in memory implementation for file system
 * @author: LYJ
 * @create: 2020-01-29 22:28
 **/

public class InMemoryFileSystem {

	private final static InMemoryFileSystem instance;
	/**
	 * initialize the root directory "/" of in memory file syttem's instance
	 */
	private final InfoNode root = new InfoNode(ROOT_PATH, FileType.DIRECTORY);

	/**
	 * if the file system is used by multiple user simultaneous, we should introduce the
	 * session concepts ,but that is a bit complex. So I just assume that every thread has
	 * only one session and use threadlocal to store working dir for different threads.
	 * Be careful while using threadpools, We should call cd("/") at begining.
	 */
	private Map<String, InMemoryFileSystemSession> sessionsMap = new ConcurrentHashMap<>();


	/**
	 * simply use eager initialization to implement singleton pattern
	 * @return the singleton instane of file system
	 */
	static{
		try{
			instance = new InMemoryFileSystem();
		}catch(Exception e){
			throw new RuntimeException("Exception occured when creating InMemoryFileSystem instance");
		}
	}

	public static InMemoryFileSystemSession getSession(){
		InMemoryFileSystemSession session = new InMemoryFileSystemSession(instance);
		instance.sessionsMap.put(session.getSessionId(),session);
		return session;
	}

	private InMemoryFileSystem() {
		//working dir is the same as root at begining
	}

	public InfoNode getRoot() {
		return root;
	}
}
