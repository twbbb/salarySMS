package salary.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudTopic;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.model.BatchSmsAttributes;
import com.aliyun.mns.model.MessageAttributes;
import com.aliyun.mns.model.RawTopicMessage;
import com.aliyun.mns.model.TopicMessage;

/**
 * @Class SMSChannelUtils.java
 * @Author 作者姓名:田文彬
 * @Version 1.0
 * @Date 创建时间：2017年3月29日 下午5:31:37
 * @Copyright Copyright by 智多星
 * @Direction 类说明
 */
public class SMSChannelUtils
{

	// 存放短信发送通道信息
	private static Map smsMap = new ConcurrentHashMap();
	public static final String SENDSTATE = "sendState";
	public static final String RESPONSEDATA = "responseData";



	/**
	 * @throws Exception
	 * 
	 * @Title: sendAliyunMsg
	 * @Description: 发送阿里云短信
	 * @param @param accessKey
	 * @param @param accessSecret
	 * @param @param signName
	 * @param @param templateCode
	 * @param @param mobile
	 * @param @param paramMap
	 * @return void
	 * @throws
	 */
	public static Map sendAliyunMsg(String accessKey, String accessSecret, String signName, String templateCode,
			String mobile, Map paramMap, String mnsEndpoint, String topicStr)
	{
		Map resultMap = new HashMap();
		/**
		 * Step 1. 获取主题引用
		 */
		CloudAccount account = new CloudAccount(accessKey, accessSecret, mnsEndpoint);
		MNSClient client = account.getMNSClient();
		CloudTopic topic = client.getTopicRef(topicStr);
		/**
		 * Step 2. 设置SMS消息体（必须）
		 *
		 * 注：目前暂时不支持消息内容为空，需要指定消息内容，不为空即可。
		 */
		RawTopicMessage msg = new RawTopicMessage();
		msg.setMessageBody("sms-message");
		/**
		 * Step 3. 生成SMS消息属性
		 */
		MessageAttributes messageAttributes = new MessageAttributes();
		BatchSmsAttributes batchSmsAttributes = new BatchSmsAttributes();
		// 3.1 设置发送短信的签名（SMSSignName）
		batchSmsAttributes.setFreeSignName(signName);
		// 3.2 设置发送短信使用的模板（SMSTempateCode）
		batchSmsAttributes.setTemplateCode(templateCode);
		// 3.3 设置发送短信所使用的模板中参数对应的值（在短信模板中定义的，没有可以不用设置）
		BatchSmsAttributes.SmsReceiverParams smsReceiverParams = new BatchSmsAttributes.SmsReceiverParams();
		
		if (paramMap != null && paramMap.size() > 0)
		{
			Iterator it = paramMap.entrySet().iterator();
			
			while (it.hasNext())
			{
				Map.Entry entry = (Map.Entry) it.next();
				smsReceiverParams.setParam((String) entry.getKey(), (String) entry.getValue());
			}
		}
		
		// 3.4 增加接收短信的号码
		batchSmsAttributes.addSmsReceiver(mobile, smsReceiverParams);
		
		messageAttributes.setBatchSmsAttributes(batchSmsAttributes);
		try
		{
			/**
			 * Step 4. 发布SMS消息
			 */
			TopicMessage ret = topic.publishMessage(msg, messageAttributes);
			resultMap.put(SENDSTATE, "Y");
			resultMap.put(RESPONSEDATA, ret.getMessageId());
			System.out.println("MessageId: " + ret.getMessageId());
			System.out.println("MessageMD5: " + ret.getMessageBodyMD5());
		}
		catch (Exception e)
		{
			
//			logger.error(e.toString() + "," + Arrays.toString(e.getStackTrace()));
			resultMap.put(SENDSTATE, "N");
			resultMap.put(RESPONSEDATA, e.toString());
			
		}
		client.close();
		return resultMap;
	}

	
}
