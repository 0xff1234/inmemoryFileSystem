package com.lyj.mfs.domain;

import static com.lyj.mfs.utils.Const.DELIMITER;

import com.google.common.base.Joiner;
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

	public AbsolutePath(AbsolutePath otherAbsPath){
		this.paths = new ArrayList<>(otherAbsPath.immutablePaths());
	}

	public InfoNode getLowestNode(){
		return this.paths.get(this.paths.size() - 1);
	}

	public synchronized boolean append(InfoNode node){
		paths.add(node);
		return true;
	}

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

	@Override
	public String toString(){
		return this.paths.get(0).getPath() +
			Joiner
			.on(DELIMITER)
			.join(paths.stream().
				map(p -> p.getPath()).skip(1)
				.collect(Collectors.toList())
			);
	}
}
