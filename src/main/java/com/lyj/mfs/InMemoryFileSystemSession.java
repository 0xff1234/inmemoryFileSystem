package com.lyj.mfs;

import static com.lyj.mfs.domain.InfoNode.FileType.DIRECTORY;
import static com.lyj.mfs.domain.InfoNode.FileType.FILE;
import static com.lyj.mfs.utils.Const.ROOT_PATH;

import com.lyj.mfs.domain.AbsolutePath;
import com.lyj.mfs.domain.InfoNode;
import com.lyj.mfs.exceptions.PathNotFoundException;
import com.lyj.mfs.utils.StringUtils;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @program: inmemoryFileSystem
 * @description: the domain object represent each client session
 * @author: LYJ
 * @create: 2020-01-30 12:01
 **/

public class InMemoryFileSystemSession implements FileSystem {

	/**
		* a uuid to distinguish sessions
	 */
	private final String sessionId;

	/**
	 * current working directory, will be modified when we call cd()
	 */
	private AbsolutePath workingDir;

	/**
	 * the time that this session was created, can be used to do session timeout, not implement yet
	 */
	private final LocalDateTime createTime;

	/**
	 * the singleton in-memory file system, shared by every session
	 */
	private final InMemoryFileSystem mfsInstance;


	public InMemoryFileSystemSession(InMemoryFileSystem instance) {
		this.mfsInstance = instance;
		this.sessionId = UUID.randomUUID().toString();
		this.createTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
		this.workingDir = new AbsolutePath(mfsInstance.getRoot());
	}

	public String getSessionId() {
		return sessionId;
	}


	@Override
	public Iterable<String> ls() {
		List<String> ret = new ArrayList<>();

		if (workingDirhasBeenRemoved()) {
			/*
			check if workingDir or it's upper dir has been remove by somebody
			if yes, just return empty list
		    */
			return ret;
		}

		InfoNode lowestNode = workingDir.getLowestNode();
		ret = lowestNode.getChildren().entrySet()
			.stream().map(entry -> entry.getValue().getPath())
			.collect(Collectors.toList());

		return ret;
	}


	@Override
	public boolean mkdir(String path) {
		StringUtils.checkIsPath(path);

		try {
			//find and create dir recursively
			AbsolutePath absDir = this.findAbsDir(path, true);
			return true;
		} catch (PathNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String touch(String path) {
		StringUtils.checkIsFilePath(path);
		String parentDir = StringUtils.extractParentDir(path);
		String fileName = StringUtils.extractFileName(path);
		AbsolutePath absParentDir = null;

		/*
		* 1. find the parent dir path
		* */
		try {
			absParentDir = this.findAbsDir(parentDir, true);
		} catch (PathNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		InfoNode lowestNode = absParentDir.getLowestNode();
		InfoNode fileNode = lowestNode.getChildren().get(fileName);
		/*
		* check the filename is a file or a directory or is not exist
		* */
		if (fileNode != null && fileNode.getFileType() == FILE) {
			// file exist, only need to update the createtime
			fileNode.updateCreateTime();
		} else if (fileNode != null && fileNode.getFileType() == DIRECTORY) {
			//same name directory exist, can not touch new file return null
			System.out.println("found a directory with same name");
			return null;
		} else {
			// file or directory is not exist, so we can create a new file
			fileNode = new InfoNode(fileName, FILE);
			lowestNode.addChild(fileNode);
		}

		//absosult path drill down to the file node
		absParentDir.appendRelativePath(fileNode);
		return absParentDir.toPathStr();

	}

	@Override
	public String cd(String path) {
		StringUtils.checkIsPath(path);

		try {
			this.workingDir = this.findAbsDir(path, false);
			return this.workingDir.toPathStr();
		} catch (PathNotFoundException e) {
			e.printStackTrace();
		}
		return workingDir.toPathStr();
	}

	@Override
	public String pwd() {
		return workingDir.toPathStr();
	}

	@Override
	public boolean rm(String path, boolean recursive) {
		StringUtils.checkIsPath(path);

		if( ROOT_PATH.equals(path)){
			if(recursive){
				this.mfsInstance.getRoot().getChildren().clear();
				return true;
			}else{
				return false;
			}
		}

		String parentDir = StringUtils.extractParentDir(path);
		String fileName = StringUtils.extractFileName(path);

		AbsolutePath absParentDir = null;
		try {
			absParentDir = this.findAbsDir(parentDir, false);
		} catch (PathNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		InfoNode lowestNode = absParentDir.getLowestNode();
		InfoNode fileNode = lowestNode.getChildren().get(fileName);

		if (fileNode == null){
			System.out.println("file not exist");
			return false;
		}else if (fileNode.getFileType() == DIRECTORY && !recursive){
			System.out.println("directory can not be removed");
			return false;
		}else{
			// file or (directory && recursive)
			lowestNode.removeChild(fileName);
		}

		return true;
	}

	/**
	 * drill down to the deepest path recursively,and auto determine start from root
	 * or from current working dir by the path argument
	 *
	 * @param path a string of path to be find, can be a relative path or a absolute path
	 * @param createIfNotExist if true, create new dir when dir is not exist
	 * @return the absolute path we found or created
	 * @throws PathNotFoundException throwed when no suitable path had found
	 */
	private AbsolutePath findAbsDir(String path, boolean createIfNotExist) throws PathNotFoundException {
		StringUtils.checkIsPath(path);

		/*
		 * dertimine the initial diretory to look up
		 * */
		AbsolutePath absPath = null;
		if (path.startsWith(ROOT_PATH)) {
			absPath = new AbsolutePath(mfsInstance.getRoot());
		} else {
			absPath = new AbsolutePath(this.workingDir);
		}

		Iterable<String> dirs = StringUtils.splitPath(path);

		/*
			drill down to the lowest dir
		*/
		InfoNode lowestDir = absPath.getLowestNode();
		for (String dir : dirs) {
			/*
			deal with the case of current dir
			* */
			if (".".equals(dir)) {
				continue;
			}

			/*
			deal with the case of upper dir
			* */
			if ("..".equals(dir)) {
				absPath.removeLowest();
				lowestDir = absPath.getLowestNode();
				continue;
			}

			/*
			 check if current dir areadly have the next dir,
			 if yes, we drill down to the next dir and append the found dir to absPath,
			 if not, we can create it when in mkdir mode or throw a PathNotFoundException
			*/

			InfoNode nextDir = lowestDir.getChildren().get(dir);
			if (nextDir != null && nextDir.getFileType() == DIRECTORY) {
				//sub dir already exist, do nothing

			} else if (nextDir == null && createIfNotExist) {
				//there is no file with the same name, so create a new dir
				nextDir = new InfoNode(dir, DIRECTORY);
				lowestDir.addChild(nextDir);
			} else {
				// other cases
				throw new PathNotFoundException("can not find path:" + dir + " in " + absPath.toPathStr());
			}
			absPath.appendRelativePath(nextDir);
			lowestDir = nextDir;
		}

		return absPath;
	}

	private boolean workingDirhasBeenRemoved() {
		/*
		* if we can retrive the current working dir from root, then it has not been removed
		* */
		try{
			this.findAbsDir(this.workingDir.toPathStr(), false);
			return false;
		}catch (PathNotFoundException e) {
			e.printStackTrace();
		}
		return true;
	}
}
