package com.qunar.fresh.main;

import com.qunar.fresh.homework.RmbRateFetcher;
import com.qunar.fresh.homework.ImportStatis;
import com.qunar.fresh.homework.MyLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Whiker on 2016/1/7.
 */
public class Main {
	public static void main(String[] args) throws Exception {
		homework0111();
	}

	private static void homework0111() {
		String srcDirName = "src/";
		int nTop = 10;
		List<String> classPaths = new ArrayList<>();

		ImportStatis importStat = new ImportStatis();
		boolean isSuccess = importStat.statis(srcDirName, nTop, classPaths);
		String result;
		if (isSuccess) {
			result = "\r\n被import的前10个类:\r\n";
			for (String classPath : classPaths) {
				result += "  " + classPath + "\r\n";
			}
		}
		else {
			result = "运行失败";
		}
		MyLogger.logInfo(result);
	}

	private static void homework0106() {
		RmbRateFetcher rmbRate = new RmbRateFetcher();
		boolean isSuccess = rmbRate.fetchToExcel(
				"2015-12-01", "2015-12-30", "rmb_rate.xls");
		MyLogger.logInfo("抓取" + (isSuccess ? "成功" : "失败"));
	}
}
