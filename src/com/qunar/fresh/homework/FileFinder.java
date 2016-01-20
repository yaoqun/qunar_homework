package com.qunar.fresh.homework;

import java.io.File;
import java.util.List;

/**
 * Created by Whiker on 2016/1/11.
 */
public class FileFinder {

	/**
	 * @param f
	 *   若f是文件, 且f以suffix结尾, 则f加入fileSet中
	 *   若f是目录, 则递归遍历该目录下的所有子目录和文件, 过滤出文件加入fileSet中
	 * @param suffix 后缀名, 需要包括'.'符号
	 * @param fileSet java文件对象集合
	 */
	public static void findBySuffix(final File f, final String suffix, List<File> fileSet) {
		if (f.isFile()) {
			if (f.canRead() && f.getName().endsWith(suffix)) {
				fileSet.add(f);
			}
		}
		else if (f.isDirectory()) {
			for (File subf : f.listFiles()) {
				findBySuffix(subf, suffix, fileSet);
			}
		}
	}

}
