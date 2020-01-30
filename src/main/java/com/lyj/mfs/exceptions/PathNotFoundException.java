package com.lyj.mfs.exceptions;

/**
 * @program: inmemoryFileSystem
 * @description:
 * @author: LYJ
 * @create: 2020-01-30 11:47
 **/

public class PathNotFoundException extends Exception {

	public PathNotFoundException() {
		super();
	}

	public PathNotFoundException(String message) {
		super(message);
	}

	public PathNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
