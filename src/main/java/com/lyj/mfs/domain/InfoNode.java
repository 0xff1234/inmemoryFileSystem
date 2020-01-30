package com.lyj.mfs.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The type Info node.
 *
 * @program: inmemoryFileSystem
 * @description: the class that describe infomation of one file or directory
 * @author: LYJ
 * @create: 2020 -01-29 22:22
 */
public class InfoNode {
	private final String path;
	private LocalDateTime createTime;
	private final FileType fileType;
	private Map<String, InfoNode> children;
	//not used
	private Byte[] content = null;

	/**
	 * if arguments are checked error, a RuntimeException will be throwed
	 *
	 * @param path     the path, can not be empty String or null
	 * @param fileType the file type,  directory or file
	 * @return the infoNode
	 * @exception throws RuntimeException if arguments checked error
	 */

	public InfoNode(String path, FileType fileType) {
		//check arguments
		Preconditions.checkArgument( path != null && !"".equals(path),
			"path can not be empty");
		Preconditions.checkNotNull(fileType, "fileType can not be null");

		this.path = path;
		this.fileType = fileType;
		this.createTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
		switch (fileType){
			case FILE:
				this.children = null;
				break;
			case DIRECTORY:
				this.children = new ConcurrentSkipListMap<>();
				break;
			default:
		}
	}

	public static enum FileType {
		DIRECTORY, FILE
	}

	public String getPath() {
		return path;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void updateCreateTime(){
		this.createTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
	}

	public Map<String, InfoNode> getChildren() {
		return children;
	}

	public boolean addChild(InfoNode newNode){
		this.children.put(newNode.getPath(), newNode);
		return true;
	}

	public boolean removeChild(String path){
		this.children.remove(path);
		return true;
	}


	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("path",path)
			.add("fileType", fileType)
			.toString();
	}
}
