package com.qunar.fresh.homework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by Whiker on 2016/1/11.
 */
public class JavaFileInfo {

	private boolean mIsMultiLineComment = false;  // 前一行代码是否包含未结束的"/*"

	/**
	 * 获取java源码中被import的类完整路径
	 * 例如, 源码里有 import java.lang.String; import java.lang.Object;
	 * 得到 {"java.lang.String", "java.lang.Object"}
	 * @param in java源代码输入流
	 * @param importClasses 结果
	 */
	public void getImportClass(InputStream in, List<String> importClasses)
			throws IOException {
		if (in == null) {
			MyLogger.logError("输入流是null");
			return;
		}

		String code = getCodeWithoutComment(in);
		if (code == null)
			return;
		code = code.replaceAll("\r\n", "");

		// import语句出现在类定义外, 因此无需查找"{"后面的字符串
		// 在"{"前面不会有双引号包括的import
		int limit = code.indexOf('{');
		if (limit < 0)
			limit = code.length();

		for (int i = 0; i < limit; ) {
			i = code.indexOf("import", i);
			if (i >= 0 && i < limit) {
				i += 6;
				int j = code.indexOf(';', i);
				if (j >= 0) {
					String classPath = code.substring(i, j).replaceAll("\\s", "");
					importClasses.add(classPath);
					i = j;
				} else {
					MyLogger.logError("语法错误: import没有以;结尾");
					break;
				}
			} else
				break;
		}
	}

	/**
	 * 删除Java源代码中的注释
	 */
	public String getCodeWithoutComment(InputStream in) throws IOException {
		if (in == null) {
			MyLogger.logError("输入流是null");
			return null;
		}

		StringBuilder str = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		mIsMultiLineComment = false;
		String line;
		while ( (line = reader.readLine()) != null ) {
			line = getValidPartOfCodeline(line);
			if (line == null)
				continue;
			str.append(line);
			str.append("\r\n");
		}

		return str.toString();
	}

	/**
	 * 不会把占多行的一条语句合并成一行返回
	 * @param codeline 一行代码
	 * @return 该行代码的有效部分
	 */
	private String getValidPartOfCodeline(String codeline) {
		if (mIsMultiLineComment) {
			int i = codeline.indexOf("*/");
			if (i >= 0) {  // 多行注释结束了
				mIsMultiLineComment = false;
				i += 2;
				if (i < codeline.length())
					return getValidPartOfCodeline(codeline.substring(i));
			}
			return null;  // 多行注释未结束, 或"*/"后没有字符
		}

		int i = 0;
		while (i < codeline.length()) {
			char ch = codeline.charAt(i);
			if (ch == '\'') {
				if (i+2 >= codeline.length()) {
					MyLogger.logError("源代码语法错误: " + codeline);
					return null;
				}
				if (codeline.charAt(i+1) == '\\') {
					if (i+3 >= codeline.length() || codeline.charAt(i+3) != '\'') {
						MyLogger.logError("源代码语法错误: " + codeline);
						return null;
					}
					i += 4;
				}
				else if (codeline.charAt(i+2) != '\'') {
					MyLogger.logError("源代码语法错误: " + codeline);
					return null;
				}
				else
					i += 3;
			}
			else if (ch == '"') {
				while (true) {
					i = codeline.indexOf('"', i+1);
					if (i > 0) {
						if (codeline.charAt(i-1) == '\\')
							continue;
						else
							break;
					}
					else {  // 语法错误, 未形成双引号匹配对
						MyLogger.logError("源代码语法错误: " + codeline);
						return null;
					}
				}
			}
			else if (ch == '/') {
				if (++i < codeline.length()) {
					ch = codeline.charAt(i);
					if (ch == '/') {  // "//"型注释
						codeline = codeline.substring(0, i-1);
						break;
					}
					if (ch == '*') {  // "/*"型注释
						int p = codeline.indexOf("*/", i+1);
						if (p > 0) {
							codeline = codeline.substring(0, i-1) +
									codeline.substring(p+2, codeline.length());
							--i;
							continue;
						}
						else {
							mIsMultiLineComment = true;
							codeline = codeline.substring(0, i-1);
							break;
						}
					}
				}
			}
			++i;
		}

		codeline = codeline.trim();
		if (codeline.length() <= 0)
			return null;
		return codeline;
	}
}
