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
 * @create: 2020-01-30 09:01
 **/

public class InMemoryFileSystemSession implements FileSystem {

	private final String sessionId;
	private AbsolutePath workingDir;
	private final LocalDateTime createTime;
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
		/*
		check if workingDir and it's upper dir has been remove
		 */
		if (hasBeenRemoved()) {
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
		StringUtils.checkIsDirPath(path);

		try {
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
		try {
			absParentDir = this.findAbsDir(parentDir, true);
		} catch (PathNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		InfoNode lowestNode = absParentDir.getLowestNode();
		InfoNode fileNode = lowestNode.getChildren().get(fileName);
		if (fileNode != null && fileNode.getFileType() == FILE) {
			// file exist, update the createtime
			fileNode.updateCreateTime();
		} else if (fileNode != null && fileNode.getFileType() == DIRECTORY) {
			// directory exist, return null
			return null;
		} else {
			// file or directory is not exist, so create a new file
			fileNode = new InfoNode(fileName, FILE);
			lowestNode.addChild(fileNode);
		}

		//absosult path drill down to the file node
		absParentDir.append(fileNode);
		return absParentDir.toString();

	}

	@Override
	public String cd(String path) {
		StringUtils.checkIsDirPath(path);

		try {
			this.workingDir = this.findAbsDir(path, false);
			return this.workingDir.toString();
		} catch (PathNotFoundException e) {
			e.printStackTrace();
		}
		return ROOT_PATH;
	}

	@Override
	public String pwd() {
		return workingDir.toString();
	}

	@Override
	public boolean rm(String path, boolean recursive) {
		StringUtils.checkIsFilePath(path);
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
			// file or (directory + recursive)
			fileNode.removeChild(fileName);
		}

		return true;
	}

	private AbsolutePath findAbsDir(String path, boolean createIfNotExist) throws PathNotFoundException {
		StringUtils.checkIsDirPath(path);

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
				throw new PathNotFoundException("can not find path:" + dir + " in " + absPath.toString());
			}
			absPath.append(nextDir);
			lowestDir = nextDir;
		}

		return absPath;
	}

	private boolean hasBeenRemoved() {
		try{
			this.findAbsDir(this.workingDir.toString(), false);
			return false;
		}catch (PathNotFoundException e) {
			e.printStackTrace();
		}
		return true;
	}
}
