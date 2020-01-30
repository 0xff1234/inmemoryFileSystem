package com.lyj.mfs.utils;

import static com.lyj.mfs.utils.Const.DELIMITER;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: inmemoryFileSystem
 * @description:
 * @author: LYJ
 * @create: 2020-01-30 11:22
 **/

public class StringUtils {
	private StringUtils(){}
	private static Splitter splitter = Splitter.on(DELIMITER).
		trimResults().omitEmptyStrings();

	public static List<String> splitPath(String pathStr){
		ArrayList<String> paths = new ArrayList<>();
		Iterable<String> splited = splitter.split(pathStr);
		for(String p : splited){
			paths.add(p);
		}
		return paths;
	}

	public static String extractFileName(String pathStr){
		List<String> pathList = splitPath(pathStr);
		String lastFileName = pathList.get(pathList.size() - 1);
		return lastFileName;
	}

	public static String extractParentDir(String pathStr){
		List<String> pathList = splitPath(pathStr);
		String lastFileName = pathList.get(pathList.size() - 1);
		int lastPosition = pathStr.lastIndexOf(lastFileName);
		return pathStr.substring(0, lastPosition).trim();
	}

	public static void checkIsFilePath(String path){
		Preconditions.checkArgument(path != null && !"".equals(path) && !path.endsWith(DELIMITER), "file path can not be empty or a directory");
	}

	public static void checkIsDirPath(String path){
		Preconditions.checkArgument(path != null , "dir path can not be null ");
	}

}
