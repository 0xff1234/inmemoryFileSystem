package com.lyj.mfs.domain;

import static com.lyj.mfs.utils.Const.DELIMITER;
import static com.lyj.mfs.utils.Const.ROOT_PATH;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: inmemoryFileSystem
 * @description: the class that represent a absolute path
 * @author: LYJ
 * @create: 2020-01-30 07:21
 **/

public class AbsolutePath {
	private final List<InfoNode> paths;
	public AbsolutePath(InfoNode root){
		this.paths = new ArrayList<>();
		paths.add(root);
	}

	/**
	 * Clone form another AbsolutePath object
	 * @param otherAbsPath another AbsolutePath
	 */
	public AbsolutePath(AbsolutePath otherAbsPath){
		this.paths = new ArrayList<>(otherAbsPath.immutablePaths());
	}


	/**
	 * helper method to get the last element of a List
	 * @return the deepest layer directoy
	 */
	public InfoNode getLowestNode(){
		return this.paths.get(this.paths.size() - 1);
	}

	public synchronized boolean appendRelativePath(InfoNode node){
		paths.add(node);
		return true;
	}

	/**
	 * @return false if there no directory to remove, because
	 * we must perserve the root path
	 */
	public synchronized boolean removeLowest(){
		if(paths.size() > 1){
			//help gc
			paths.set(paths.size() - 1 , null);
			paths.remove(paths.size() - 1);
			return true;
		}
		return false;
	}

	public List<InfoNode> immutablePaths(){
		return ImmutableList.copyOf(this.paths);
	}

	/**
	 * @return the absolute path str of this object
	 */
	public String toPathStr(){
		return ROOT_PATH +
			Joiner
			.on(DELIMITER)
			.join(paths.stream().
				map(p -> p.getPath()).skip(1)
				.collect(Collectors.toList())
			);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("paths", paths)
			.toString();
	}
}
