package salary.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Class 	AliyunSendSMS.java
 * @Author 	作者姓名:田文彬
 * @Version	1.0
 * @Date	创建时间：2017年3月30日 下午12:31:07
 * @Copyright Copyright by 智多星
 * @Direction 类说明
 */
public class AliyunSendSMS
{

	public static String sendSMS() throws Exception
	{
		StringBuffer resultSb = new StringBuffer();
		String accessKey = "";
		String accessSecret = "";

		File file = new File(XLSUtils.RESULTPATH);
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null)
			{
				try
				{
					if (!line.trim().isEmpty())
					{
						String[] lines = line.split("\\|\\|");
						if (lines.length == 3 && !lines[0].trim().isEmpty() && !lines[1].trim().isEmpty()
								&& !lines[2].trim().isEmpty() && validateMobile(lines[1].trim()))
						{
							Map paramMap = new HashMap();
							paramMap.put("code", lines[0].trim());
							paramMap.put("product", ":" + lines[2].trim());
							Map resultMap = SMSChannelUtils.sendAliyunMsg(accessKey, accessSecret, "大商帮",
									"SMS_112095005", lines[1].trim(), paramMap,
									"https://1665999861283009.mns.cn-hangzhou.aliyuncs.com/", "sms.topic-cn-hangzhou");
							String sendState = (String) resultMap.get(SMSChannelUtils.SENDSTATE);
							String responseData = (String) resultMap.get(SMSChannelUtils.RESPONSEDATA);

							if ("Y".equals(sendState))
							{
								resultSb.append("发送成功:" + line + "，消息id：").append(responseData).append("\r\n");
							}
							else
							{
								resultSb.append("发送失败:" + line + "，").append(responseData).append("\r\n");
							}
						}
						else
						{
							resultSb.append("数据不全，发送失败:" + line).append("\r\n");
						}

					}
				}
				catch (Exception e)
				{
					resultSb.append("发送出现异常，发送失败:" + line)
							.append(e.toString() + "," + Arrays.toString(e.getStackTrace())).append("\r\n");
				}

				line = br.readLine();
			}

		}
		finally
		{
			if (br != null)
			{
				br.close();
			}

		}

		return resultSb.toString();

	}

	public static String checkSendSMS() throws Exception
	{
		StringBuffer resultSb = new StringBuffer();

		File file = new File(XLSUtils.RESULTPATH);
		int i=1;
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null)
			{
				if (!line.trim().isEmpty())
				{
					String[] lines = line.split("\\|\\|");
					if (lines.length == 3 && !lines[0].trim().isEmpty() && !lines[1].trim().isEmpty()
							&& !lines[2].trim().isEmpty() && validateMobile(lines[1].trim()))
					{

					}
					else
					{
						resultSb.append("第"+i+"行数据不全：" + line).append("\r\n");
					}

				}
				i++;
				line = br.readLine();
			}

		}
		finally
		{
			if (br != null)
			{
				br.close();
			}

		}

		return resultSb.toString();

	}

	/**
	 * 
	 * @Title: validateMobile
	 * @Description: 检查手机号是否合法
	 * @param @param mobile
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean validateMobile(String mobile)
	{
		boolean flag = false;
		if (mobile != null)
		{
			Matcher m = null;
			Pattern p = Pattern.compile("^[1][0-9]{10}$");
			m = p.matcher(mobile);
			flag = m.matches();
		}

		return flag;

	}

}
