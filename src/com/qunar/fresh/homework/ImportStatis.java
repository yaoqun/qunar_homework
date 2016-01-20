package com.qunar.fresh.homework;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by Whiker on 2016/1/11.
 */
public class ImportStatis {

	/**
	 * 根据Java语法有:
	 *   1. import的"包名.类名"必须是完整路径,不能用相对路径,因此可以唯一标识类
	 *   2. import必须出现在类定义前, 因此可以不用读取整个java文件
	 * @param srcDirName 源代码目录名
	 * @param nTop 前nTop个
	 * @param classPaths 前nTop个类路径
	 * @return 函数是否运行成功
	 */
	public boolean statis(String srcDirName, int nTop, List<String> classPaths) {
		File srcDir = checkSrcDir(srcDirName);
		if (srcDir == null)
			return false;

		HashMap<String, Integer> hashMap = new HashMap<>();

		List<File> javaFiles = new ArrayList<>();
		FileFinder.findBySuffix(srcDir, ".java", javaFiles);

		for (File javaFile : javaFiles) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(javaFile);
				List<String> importClasses = new ArrayList<>();
				JavaFileInfo javaFileInfo = new JavaFileInfo();
				javaFileInfo.getImportClass(in, importClasses);
				for (String classPath : importClasses) {
					if (hashMap.containsKey(classPath)) {
						hashMap.put(classPath, hashMap.get(classPath)+1);
					}
					else {
						hashMap.put(classPath, 1);
					}
				}
			}
			catch (IOException e) {
				MyLogger.logError(e.toString());
				return false;
			}
			finally {
				if (in != null) {
					try {
						in.close();
					}
					catch (IOException e) {
						MyLogger.logError(e.toString());
						return false;
					}
				}
			}
		}

		PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
				nTop,
				new Comparator<Map.Entry<String, Integer>>() {
					@Override
					public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
						return o1.getValue() - o2.getValue();
					}
				}
		);
		for (Map.Entry<String, Integer> e : hashMap.entrySet()) {
			pq.add(e);
			if (pq.size() > nTop) {
				pq.remove();
			}
		}
		for (Object e : pq.toArray()) {
			Map.Entry<String, Integer> c = (Map.Entry<String, Integer>)e;
			classPaths.add(c.getKey() + ", num: " + c.getValue().toString());
		}
		return true;
	}

	/**
	 * 检查源代码目录是否存在
	 * @param srcDirName 源代码目录名
	 * @return 目录的文件对象
	 */
	private File checkSrcDir(String srcDirName) {
		if (srcDirName == null) {
			MyLogger.logError("目录名是null");
			return null;
		}

		File srcDir = new File(srcDirName);
		if ( !(srcDir.exists() && srcDir.isDirectory()) ) {
			MyLogger.logError(srcDirName + "不存在或不是目录");
			return null;
		}
		return srcDir;
	}

}
