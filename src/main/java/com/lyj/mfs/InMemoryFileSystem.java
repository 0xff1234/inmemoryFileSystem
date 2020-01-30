package com.lyj.mfs;

import static com.lyj.mfs.domain.InfoNode.FileType.DIRECTORY;
import static com.lyj.mfs.domain.InfoNode.FileType.FILE;
import static com.lyj.mfs.utils.Const.ROOT_PATH;

import com.google.common.base.MoreObjects;
import com.lyj.mfs.domain.InfoNode;
import com.lyj.mfs.domain.InfoNode.FileType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


class InMemoryFileSystem {

	private final static InMemoryFileSystem instance;
	/**
	 * initialize the root directory "/" of in memory file syttem's instance
	 */
	private final InfoNode root = new InfoNode(ROOT_PATH, FileType.DIRECTORY);

	/**
	 * if the file system is used by multiple user simultaneous, we should introduce the
	 * session, so that different user have different working directory
	 * like the usage of ssh client
	 * sessionsMap is used to store all session data
	 */
	private Map<String, InMemoryFileSystemSession> sessionsMap = new ConcurrentHashMap<>();


	/*
	  simply use eager initialization to implement singleton pattern
	  @return the singleton instane of file system
	 */
	static{
		try{
			instance = new InMemoryFileSystem();
		}catch(Exception e){
			throw new RuntimeException("Exception occured when creating InMemoryFileSystem instance");
		}
	}

	public static InMemoryFileSystem getInstance(){
		return instance;
	}

	/**
	 * @return a new session for visiting the in memory file system
	 */
	public static InMemoryFileSystemSession newSession(){
		InMemoryFileSystemSession session = new InMemoryFileSystemSession(instance);
		instance.sessionsMap.put(session.getSessionId(),session);
		return session;
	}

	private InMemoryFileSystem() {
	}

	public InfoNode getRoot() {
		return this.root;
	}

	public Stats getStats(){

		return getStats(this.root);
	}

	private Stats getStats(InfoNode node) {
		Stats status = new Stats();
		if(node == null){
			return status;
		}
		if(node.getFileType() == FILE){
			status.totalFile = 1;
			status.totalPath = 1;
			return status;
		}

		if(node.getFileType() == DIRECTORY){
			status.totalDir = 1;
			status.totalPath = 1;
			for(Map.Entry<String, InfoNode> entry : node.getChildren().entrySet()){
				InfoNode subNode = entry.getValue();
				status = status.plus(this.getStats(subNode));
			}
		}
		return status;

	}

	public class Stats {
		public long totalPath = 0;
		public long totalFile = 0;
		public long totalDir = 0;

		public Stats plus(Stats stats2){
			this.totalPath += stats2.totalPath;
			this.totalFile += stats2.totalFile;
			this.totalDir += stats2.totalDir;
			return this;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
				.add("totalPath", totalPath)
				.add("totalFile", totalFile)
				.add("totalDir", totalDir)
				.toString();
		}
	}
}
