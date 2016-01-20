package com.qunar.fresh.homework;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Whiker on 2016/1/7.
 */
public class RmbRateFetcher {

	private SimpleDateFormat mDateFormat = new SimpleDateFormat("YYYY-MM-dd");

	// 抓取人民币汇率, 并保存到excel文件中
	// 返回是否抓取成功
	public boolean fetchToExcel(String fromDate, String toDate, String excelFilename) {
		try {
			String query = getUrlQueryString(fromDate, toDate);
			String html = getQueryResult(query);

			List<String> currencies = new ArrayList<>();  // 币种
			List<String> dates = new ArrayList<>();       // 日期
			List<double[]> rmbRate = new ArrayList<>();   // 汇率
			parseHtml(html, currencies, dates, rmbRate);

			saveResultToExcel(currencies, dates, rmbRate, excelFilename);

			return true;
		}
		catch (Exception e) {
			MyLogger.logError(e.toString());
			return false;
		}
	}

	// 根据起始日期和结束日期获取url查询字符串
	private String getUrlQueryString(String from, String to) throws Exception {
		Date fromDate = mDateFormat.parse(from);
		Date toDate = mDateFormat.parse(to);

		checkDateLegal(fromDate, toDate);

		String url = "http://www.safe.gov.cn/AppStructured/view/project!RMBQuery.action" +
				"?projectBean.startDate=" + from +
				"&projectBean.endDate=" + to;

		return url;
	}

	// 检查两个日期的合法性
	private void checkDateLegal(Date from, Date to) throws Exception {
		if (from.after(to))
			throw new Exception("起始日期晚于结束日期");
		if (to.after(new Date()))
			throw new Exception("结束日期早于当前日期");
	}

	// 获取HTTP的查询结果
	private String getQueryResult(String query) throws IOException {
		URL url = new URL(query);

		URLConnection urlConn = url.openConnection();
		urlConn.connect();
		InputStream in = urlConn.getInputStream();

		StringBuffer strbuf = new StringBuffer();
		byte[] buf = new byte[1024];
		for (int nRead; (nRead = in.read(buf)) > 0; ) {
			strbuf.append(new String(buf, 0, nRead));
		}
		return strbuf.toString();
	}

	// 解析页面内容
	private void parseHtml(String html, List<String> currencies,
						   List<String> dates, List<double[]> rmbRate) {
		// 去掉空白字符
		html = html.replaceAll("\\s", "").replaceAll("&nbsp;", "");

		// 获取币种
		Pattern currencyPattern = Pattern.compile("<optionvalue=\"([^\"]*)\"");
		Matcher m = currencyPattern.matcher(html);
		while (m.find()) {
			currencies.add(m.group(1));
		}

		// 获取日期
		String datePatternStr = "<tdwidth=\"8%\"align=\"center\">(\\d{4}-\\d{2}-\\d{2})</td>";
		m = Pattern.compile(datePatternStr).matcher(html);
		while (m.find()) {
			dates.add(m.group(1));
		}

		// 获取汇率
		String[] rows = html.split(datePatternStr);
		int nRows = rows.length-1;
		int nCols = currencies.size();
		String ratePatternStr = "<tdwidth=\"8%\"align=\"center\">([\\d\\.]*)</td>";
		for (int i = 1; i <= nRows; i++) {
			double[] rate = new double[nCols];
			m = Pattern.compile(ratePatternStr).matcher(rows[i]);
			for (int j = 0; j < nCols && m.find(); j++) {
				rate[j] = Double.parseDouble(m.group(1));
			}
			rmbRate.add(rate);
		}
	}

	// 把结果保存到excel文件
	private void saveResultToExcel(List<String> currencies, List<String> dates,
								   List<double[]> rmbRate, String excelFilename)
			throws Exception {
		FileOutputStream out = null;
		WritableWorkbook wbook = null;

		try {
			out = new FileOutputStream(excelFilename);
			wbook = Workbook.createWorkbook(out);
			WritableSheet sheet = wbook.createSheet("sheet-1", 0);

			// 表的第一行, 列名称
			Label label = new Label(0, 0, "日期");
			sheet.addCell(label);
			for (int i = 0; i < currencies.size(); i++) {
				label = new Label(i+1, 0, currencies.get(i));
				sheet.addCell(label);
			}

			// 表的数据
			for (int i = 0; i < dates.size(); i++) {
				label = new Label(0, i+1, dates.get(i));
				sheet.addCell(label);
				double[] rate = rmbRate.get(i);
				for (int j = 0; j < rate.length; j++) {
					label = new Label(j+1, i+1, String.valueOf(rate[j]));
					sheet.addCell(label);
				}
			}

			wbook.write();

		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				if (wbook != null)
					wbook.close();
				if (out != null)
					out.close();
			}
			catch (Exception e) {
				throw e;
			}
		}
	}
}
