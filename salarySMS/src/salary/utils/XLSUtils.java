package salary.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import salary.bean.SalaryBean;

/**
 * @Class XLSUtils.java
 * @Author 作者姓名:田文彬
 * @Version 1.0
 * @Date 创建时间：2017年8月30日 下午8:18:53
 * @Copyright Copyright by 智多星
 * @Direction 类说明
 */
public class XLSUtils
{
	static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
	static Map<String, CellStyle> styleMap = new HashMap<String, CellStyle>(); // 存储单元格样式的Map
	public static final String RESULTPATH = System.getProperty("user.dir")  + File.separator + "待发送数据.txt";
	public static final String XLSPATH = System.getProperty("user.dir")  + File.separator + "工资模板.xlsx";
	
	public static void main(String[] args) throws Exception
	{
//		System.out.println(System.getProperty("user.dir"));
		String result = readSalaryExcel(XLSPATH);
		writeToResultpath(result);
	}

	static int nameIndex = -1;
//	static int mobileIndex = -1;
	static int startIndex = -1;
	static int endIndex = -1;

	static String name = "姓名";
	static String mobile = "手机号";
	static String start = "底薪基数";
	static String end = "实发金额";

	public static void writeToResultpath(String result) throws Exception
	{
		
		writeToFile(result,RESULTPATH);
	}
	public static void writeToFile(String result,String resultPath) throws Exception
	{
		
		File file = new File(resultPath);
		PrintStream ps = new PrintStream(new FileOutputStream(file));
		ps.println(result);// 往文件里写入字符串
		ps.close();
	}

	public static Map readPhone(Workbook book,FormulaEvaluator formulaEvaluator) throws Exception
	{
		Map map = new HashMap();
		Sheet sheet = getSheetByNum(book, 2);
		int lastRowNum = sheet.getLastRowNum();

		List contentList = new ArrayList();
		for (int i = 1; i <= lastRowNum; i++)
		{

			List list = new ArrayList();
			rowToList(sheet, i, list, formulaEvaluator);

			contentList.add(list);
		}
		
		for (int i = 0; i < contentList.size(); i++)
		{
			List content = (List) contentList.get(i);
			if (content == null || content.size() == 0)
			{
				break;
			}
			
			String name = ((String) content.get(0)).trim();
			String mobile = ((String) content.get(1)).trim();
			if(!StringUtils.isEmpty(name)&&!StringUtils.isEmpty(mobile))
			{
				map.put(name, mobile);
			}
		}
		
		return map;
		
	}

	
	public static String readSalaryExcel(String filePath) throws Exception
	{
		StringBuffer resultSb = new StringBuffer();
		Workbook book = getExcelWorkbook(filePath);
		FormulaEvaluator formulaEvaluator = new XSSFFormulaEvaluator((XSSFWorkbook) book);
		Sheet sheet = getSheetByNum(book, 1);

		Map phoneMap = readPhone(book, formulaEvaluator);
		List headList = readSalaryExcelHead(sheet, formulaEvaluator);
		List contentList = readSalaryExcelContent(sheet, formulaEvaluator);

		for (int i = 0; i < contentList.size(); i++)
		{
			List content = (List) contentList.get(i);
			if (content == null || content.size() == 0)
			{
				break;
			}
			String name = (String) content.get(nameIndex);
			
			String mobile ="";
			if(phoneMap.containsKey(name))
			{
				mobile = (String) phoneMap.get(name);
			}

			if (name != null && !name.trim().isEmpty() )
			{
				resultSb.append(name + "||" + mobile + "||");

				StringBuffer sb = new StringBuffer();
				String preNameflag = "";
				Map nameMap = new LinkedHashMap();
				for (int j = startIndex; j < endIndex + 1 && j < content.size(); j++)
				{
					
					String str = (String) content.get(j);
					if (str != null && !str.trim().isEmpty() && string2Double(str, 0) != 0)
					{
						SalaryBean salaryBean = (SalaryBean) headList.get(j);
						String preName = salaryBean.getPreName();
						String namesb = salaryBean.getName();
						if (preName != null && !preName.trim().isEmpty())
						{
							if (nameMap.containsKey(preName))
							{
								String value = (String) nameMap.get(preName);
								if (value.contains(preName))
								{
									value = value.substring(0, value.length() - 1);
									nameMap.put(preName, value + "," + namesb + ":" + str + "}");
								}
								else
								{
									nameMap.put(preName, preName + "{" + value + "," + namesb + ":" + str + "}");
								}

							}
							else
							{
								if (namesb != null && !namesb.trim().isEmpty())
								{
									nameMap.put(preName, namesb + ":" + str);
								}
								else
								{
									nameMap.put(preName, preName + ":" + str);
								}

							}
						}
						else
						{
							nameMap.put(namesb, namesb + ":" + str);
						}

					}
				}

				Iterator<Map.Entry<Integer, String>> it = nameMap.entrySet().iterator();
				if (it.hasNext())
				{
					Map.Entry entry = it.next();
					resultSb.append(entry.getValue());
				}
				while (it.hasNext())
				{
					Map.Entry entry = it.next();
					resultSb.append(";").append(entry.getValue());
				}
				resultSb.append("\r\n");
			}

		}
		System.out.println(resultSb);
		return resultSb.toString();
	}

	/**
	 * 
	 * @Title: string2Long
	 * @Description: String转long类型
	 * @param @param str
	 * @param @param defaultVal
	 * @param @return
	 * @return long
	 * @throws
	 */
	public static double string2Double(String str, double defaultVal)
	{
		double i;
		try
		{
			i = Double.parseDouble(str);
		}
		catch (NumberFormatException e)
		{
			i = defaultVal;
		}
		return i;
	}

	

	public static List readSalaryExcelContent(Sheet sheet, FormulaEvaluator formulaEvaluator) throws Exception
	{
		int lastRowNum = sheet.getLastRowNum();

		List resultList = new ArrayList();
		for (int i = 4; i <= lastRowNum; i++)
		{

			List list = new ArrayList();
			rowToList(sheet, i, list, formulaEvaluator);

			resultList.add(list);
		}
		return resultList;
	}

	public static List readSalaryExcelHead(Sheet sheet, FormulaEvaluator formulaEvaluator) throws Exception
	{

		int lastRowNum = sheet.getLastRowNum();

		List list1 = new ArrayList();
		List list2 = new ArrayList();
		List list3 = new ArrayList();
		rowToList(sheet, 1, list1, formulaEvaluator);
		for (int i = 0; i < list1.size(); i++)
		{
			String row1Str = (String) list1.get(i);
			if (row1Str != null && row1Str.contains(name))
			{
				nameIndex = i;
			}

//			if (row1Str != null && row1Str.contains(mobile))
//			{
//				mobileIndex = i;
//			}
		}

		rowToList(sheet, 2, list2, formulaEvaluator);
		rowToList(sheet, 3, list3, formulaEvaluator);
		int headflag = 0;
		int msgflag = 0;
		List salaryBeanList = new ArrayList();
		String preName = "";
		for (int i = 0; i < list3.size(); i++)
		{
			String row3Str = (String) list3.get(i);
			if (msgflag == 0 && (row3Str == null || row3Str.trim().isEmpty() || !row3Str.contains(start)))
			{
				SalaryBean salaryBean = new SalaryBean();
				salaryBean.setNum(i);
				salaryBeanList.add(salaryBean);
				continue;
			}
			if (startIndex == -1)
			{
				startIndex = i;
			}
			SalaryBean salaryBean = new SalaryBean();

			salaryBean.setNum(i);
			msgflag++;
			String head = (String) list2.get(i);
			if (head != null && !head.trim().isEmpty())
			{
				preName = head;
				headflag++;
			}
			if (preName != null && !preName.trim().isEmpty())
			{
				salaryBean.setPreName(preName);
			}
			salaryBean.setName(row3Str);
			salaryBeanList.add(salaryBean);
			if (head.contains(end))
			{
				endIndex = i + 1;
				break;
			}
		}

		return salaryBeanList;
	}

	private static void rowToList(Sheet sheet, int num, List list2, FormulaEvaluator formulaEvaluator) throws Exception
	{
		Row row;
		row = sheet.getRow(num);
		if (row != null)
		{
			int lastCellNum = row.getLastCellNum();
			Cell cell = null;
			for (int j = 0; j < lastCellNum; j++)
			{
				cell = row.getCell(j);
				if (cell != null)
				{
					String value = getValue(cell, formulaEvaluator);
					// System.out.println(value);
					list2.add(value);
				}
			}
		}
	}

	private static String getValue(Cell cell, FormulaEvaluator formulaEvaluator) throws Exception
	{
		String value = "";
		String type_cn = null;
		String type_style = cell.getCellStyle().getDataFormatString().toUpperCase();
		String type_style_cn = getCellStyleByChinese(type_style);
		int type = cell.getCellType();

		switch (type)
		{
		case 0:
			if (DateUtil.isCellDateFormatted(cell))
			{
				type_cn = "NUMBER-DATE";
				Date date = cell.getDateCellValue();
				value = sFormat.format(date);
			}
			else
			{
				type_cn = "NUMBER";
				double tempValue = cell.getNumericCellValue();
				// BigDecimal b = new BigDecimal(tempValue);
				// double f1 = b.setScale(2,
				// BigDecimal.ROUND_HALF_UP).doubleValue();
				DecimalFormat df = new DecimalFormat("#.##");
				value = String.valueOf(df.format(tempValue));
			}
			break;
		case 1:
			type_cn = "STRING";
			value = cell.getStringCellValue();
			break;
		case 2:
			type_cn = "FORMULA";

			throw new Exception(
					"第" + (cell.getRowIndex() + 1) + "行，" + "第" + (cell.getColumnIndex() + 1) + "列数据包含公式，请检查");

		case 3:
			type_cn = "BLANK";
			value = cell.getStringCellValue();
			break;
		case 4:
			type_cn = "BOOLEAN";
			boolean tempValue = cell.getBooleanCellValue();
			value = String.valueOf(tempValue);
			break;
		case 5:
			type_cn = "ERROR";
			byte b = cell.getErrorCellValue();
			value = String.valueOf(b);

		default:
			break;
		}
		return value.replace("（元）", "").replace("\r", "").replace("\n", "");
	}

	/**
	 * 读excel
	 * 
	 * @param filePath
	 *            excel路径
	 */
	public static void readExcel(String filePath)
	{
		Workbook book = null;
		try
		{
			book = getExcelWorkbook(filePath);
			Sheet sheet = getSheetByNum(book, 1);
			System.out.println("sheet名称是：" + sheet.getSheetName());

			int lastRowNum = sheet.getLastRowNum();

			Row row = null;
			for (int i = 1; i <= lastRowNum; i++)
			{
				row = sheet.getRow(i);
				if (row != null)
				{
					System.out.println("正在读第" + (i + 1) + "行：");
					int lastCellNum = row.getLastCellNum();
					Cell cell = null;
					StringBuilder sb = null;
					for (int j = 0; j < lastCellNum; j++)
					{
						cell = row.getCell(j);
						if (cell != null)
						{
							sb = new StringBuilder("第" + (j + 1) + "列的单元格内容是：");
							String type_cn = null;
							String type_style = cell.getCellStyle().getDataFormatString().toUpperCase();
							String type_style_cn = getCellStyleByChinese(type_style);
							int type = cell.getCellType();
							String value = "";
							switch (type)
							{
							case 0:
								if (DateUtil.isCellDateFormatted(cell))
								{
									type_cn = "NUMBER-DATE";
									Date date = cell.getDateCellValue();
									value = sFormat.format(date);
								}
								else
								{
									type_cn = "NUMBER";
									double tempValue = cell.getNumericCellValue();
									value = String.valueOf(tempValue);
								}
								break;
							case 1:
								type_cn = "STRING";
								value = cell.getStringCellValue();
								break;
							case 2:
								type_cn = "FORMULA";
								value = cell.getCellFormula();
								break;
							case 3:
								type_cn = "BLANK";
								value = cell.getStringCellValue();
								break;
							case 4:
								type_cn = "BOOLEAN";
								boolean tempValue = cell.getBooleanCellValue();
								value = String.valueOf(tempValue);
								break;
							case 5:
								type_cn = "ERROR";
								byte b = cell.getErrorCellValue();
								value = String.valueOf(b);

							default:
								break;
							}
							sb.append(value + ",内容类型是：" + type_cn + ",单元格的格式是：" + type_style_cn);
							System.out.println(sb.toString());
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 根据单元格的格式 返回单元格的格式中文
	 * 
	 * @param type_style
	 * @return
	 */
	private static String getCellStyleByChinese(String type_style)
	{
		String cell_style_cn = "";
		if (type_style.contains("GENERAL"))
		{
			cell_style_cn = "常规";
		}
		else if (type_style.equals("_ * #,##0.00_ ;_ * \\-#,##0.00_ ;_ * \"-\"??_ ;_ @_ "))
		{
			cell_style_cn = "会计专用";
		}
		else if (type_style.equals("0"))
		{
			cell_style_cn = "整数";
		}
		else if (type_style.contains("YYYY/MM") || type_style.contains("YYYY\\-MM"))
		{
			cell_style_cn = "日期";
		}
		else if (type_style.equals("0.00%"))
		{
			cell_style_cn = "百分比";
		}
		else
		{
			cell_style_cn = "不符合规定格式类型:" + type_style;
			// cell_style_cn = type_style;
		}
		return cell_style_cn;
	}

	public static void testWrite(String xlsPath, List list)
	{
		testWrite(xlsPath, list, "sheet1");
	}

	/**
	 * 写内容到excel中
	 * 
	 * @throws IOException
	 */
	public static void testWrite(String xlsPath, List list, String sheets)
	{

		if (sheets == null || sheets.trim().isEmpty())
		{
			sheets = "sheet1";
		}

		// Sheet sheet1 = (Sheet) wb.createSheet(sheets);
		File file = new File(xlsPath);
		if (!file.exists())
		{
			Workbook wb = new XSSFWorkbook();
			// 创建sheet对象
			OutputStream outputStream = null;
			try
			{
				outputStream = new FileOutputStream(file);
				wb.write(outputStream);

			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				if (outputStream != null)
				{
					try
					{
						outputStream.flush();
						outputStream.close();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}

		FileOutputStream out = null;
		try
		{
			Workbook book = getExcelWorkbook(xlsPath);

			Sheet sheet = book.createSheet(sheets);

			Map<String, String> map = new HashMap<String, String>();
			// List<Map<String, String>> list = new LinkedList<Map<String,
			// String>>();
			// map.put("0", "4,INT");
			// map.put("6", "小红,GENERAL");
			// map.put("2", "18,INT");
			// map.put("3", "1990-03-10,DATE");
			// map.put("4", "0.056,PERCENT");
			// map.put("5", "4800,DOUBLE");
			// list.add(map);

			int startRow = 1;
			boolean result = writeToExcel(list, sheet, startRow);

			if (result)
			{
				out = new FileOutputStream(xlsPath);
				book.write(out);
				// System.out.println(xlsPath+"文件写入完成！");

			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (out != null)
				{
					out.flush();
					out.close();
				}

			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// /**
	// * 写内容到excel中
	// * @throws IOException
	// */
	// public static void testWrite(String srcFilePath,String tarFilePath){
	// FileOutputStream out = null;
	// try {
	// Workbook book = getExcelWorkbook(srcFilePath);
	// Sheet sheet = getSheetByNum(book,1);
	//
	// Map<String,String> map = new HashMap<String, String>();
	// List<Map<String,String>> list = new LinkedList<Map<String,String>>();
	// map.put("2", "4,INT");
	// map.put("3", "小红,GENERAL");
	// map.put("4", "18,INT");
	// map.put("5", "1990-03-10,DATE");
	// map.put("6", "0.056,PERCENT");
	// map.put("7", "4800,DOUBLE");
	// list.add(map);
	//
	// int startRow = 6;
	// boolean result = writeToExcel(list, sheet,startRow);
	// if(result){
	// out = new FileOutputStream(tarFilePath);
	// book.write(out);
	// System.out.println("文件写入完成！");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// out.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	/**
	 * 将传入的内容写入到excel中sheet里
	 * 
	 * @param list
	 */
	public static boolean writeToExcel(List list, Sheet sheet, int startRow)
	{
		boolean result = false;
		try
		{
			Map<Integer, Object> map = null;
			Row row = null;
			for (int i = 0; i < list.size(); i++)
			{
				map = (Map<Integer, Object>) list.get(i);
				row = sheet.getRow(startRow - 1);
				if (row == null)
				{
					row = sheet.createRow(startRow - 1);
				}
				startRow++;
				Cell cell = null;

				BigDecimal db = null;
				for (Map.Entry<Integer, Object> entry : map.entrySet())
				{

					int colNum = entry.getKey();

					Object value_type = entry.getValue();

					String style = "GENERAL";
					cell = row.getCell(colNum);
					if (cell == null)
					{
						cell = row.createCell(colNum);
					}
					if (value_type instanceof String)
					{
						cell.setCellValue(value_type.toString());
					}
					else
					{
						if (value_type instanceof Double)
						{

							cell.setCellValue((double) value_type);
							style = "DOUBLE";
						}

						if (value_type instanceof Integer)
						{

							cell.setCellValue((int) value_type);
							style = "INT";
						}

						else if (value_type instanceof Date)
						{
							cell.setCellValue((Date) value_type);
							style = "DATE";
						}
						cell.setCellStyle(styleMap.get(style));
					}
				}
			}
			result = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		return result;
	}

	/**
	 * 获取excel的Workbook
	 * 
	 * @throws IOException
	 */
	public static Workbook getExcelWorkbook(String filePath) throws IOException
	{
		Workbook book = null;
		File file = null;
		FileInputStream fis = null;

		try
		{
			file = new File(filePath);
			if (!file.exists())
			{
				 throw new RuntimeException("文件不存在");
			}
			else
			{
				fis = new FileInputStream(file);
				book = new XSSFWorkbook(fis);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}
		finally
		{
			if (fis != null)
			{
				fis.close();
			}
		}
		return book;
	}

	/**
	 * 根据索引 返回Sheet
	 * 
	 * @param number
	 */
	public static Sheet getSheetByNum(Workbook book, int number)
	{
		Sheet sheet = null;
		try
		{
			sheet = book.getSheetAt(number - 1);
			// if(sheet == null){
			// sheet = book.createSheet("Sheet"+number);
			// }
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}
		return sheet;
	}

	/**
	 * 根据索引 返回Sheet
	 * 
	 * @param number
	 */
	public static Sheet getLastSheet(Workbook book)
	{
		Sheet sheet = null;
		try
		{
			int num = book.getNumberOfSheets();
			sheet = book.getSheetAt(num - 1);
			// if(sheet == null){
			// sheet = book.createSheet("Sheet"+number);
			// }
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}
		return sheet;
	}

	/**
	 * 初始化格式Map
	 */

	public static void initStyleMap(Workbook book)
	{
		DataFormat hssfDF = book.createDataFormat();

		CellStyle doubleStyle = book.createCellStyle(); // 会计专用
		doubleStyle.setDataFormat(hssfDF.getFormat("_ * #,##0.0000_ ;_ * \\-#,##0.0000_ ;_ * \"-\"??_ ;_ @_ ")); // poi写入后为会计专用
		styleMap.put("DOUBLE", doubleStyle);

		CellStyle intStyle = book.createCellStyle(); // 会计专用
		intStyle.setDataFormat(hssfDF.getFormat("0")); // poi写入后为会计专用
		styleMap.put("INT", intStyle);

		CellStyle yyyyMMddStyle = book.createCellStyle();// 日期yyyyMMdd
		yyyyMMddStyle.setDataFormat(hssfDF.getFormat("yyyy-MM-dd HH:mm:ss"));
		// yyyyMMddStyle.setDataFormat(hssfDF.getFormat("yyyy-MM-dd"));
		styleMap.put("DATE", yyyyMMddStyle);

		CellStyle percentStyle = book.createCellStyle();// 百分比
		percentStyle.setDataFormat(hssfDF.getFormat("0.00%"));
		styleMap.put("PERCENT", percentStyle);
	}

	public static String toLetterString(int number)
	{
		if (number < 1)
		{//
			return null;
		}
		if (number < 27)
		{
			return String.valueOf((char) ('A' + number - 1));
		}
		if (number % 26 == 0)
		{
			return toLetterString(number / 26 - 1) + "Z";
		}
		return toLetterString(number / 26) + String.valueOf((char) ('A' + number % 26 - 1));
	}

}
