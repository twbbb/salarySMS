package salary.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import salary.utils.AliyunSendSMS;
import salary.utils.XLSUtils;

public class SalarySMSUI extends JFrame
{
	static int parserxls = -1;
	JTextField days = new JTextField();
	JTextField walletText = new JTextField();

	JTextArea result = new JTextArea();
	JTextArea resultLogText = new JTextArea();
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();

	JButton sendSMS = new JButton();
	JButton start = new JButton();
	JButton clean = new JButton();
	JButton copy = new JButton();
	JButton copyToFile = new JButton();

	JScrollPane jScrollPane1 = new JScrollPane();
	JScrollPane jScrollPane2 = new JScrollPane();

	public static final String note = "操作步骤：" + "\r\n"
			+ "1.工资数据拷贝到工资sheet。注意，粘贴时候，选择粘贴值，不要把公式复制过来\r\n"
			+ "2.手机号码数据拷贝到手机号sheet。\r\n"
			+ "3.运行程序，点击解析xls按钮，生成待发送数据，检查待发送数据\r\n"
			+ "4.点击发送短信按钮，发送短信" + "\r\n";

	public SalarySMSUI()
	{
		try
		{
			jbInit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		SalarySMSUI frame = new SalarySMSUI();
		frame.setBounds(0, 0, 600, 570);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("工资短信发送工具");

	}

	private void jbInit() throws Exception
	{

		JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);
		JPanel combop = new JPanel();
		JPanel p1 = new JPanel();
		p1.setLayout(null);
		p1.setBounds(0, 0, 600, 480);

		tab.add(p1, "工资短信发送工具");

		tab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		Container container = this.getContentPane();

		container.add(tab, BorderLayout.CENTER);

		jLabel2.setText("工资模板xls路径:");
		jLabel2.setBounds(new Rectangle(6, 5, 100, 18));
		days.setText(XLSUtils.XLSPATH);
		days.setBounds(new Rectangle(110, 5, 350, 18));

		start.setText("解析xls");
		start.setBounds(new Rectangle(470, 5, 100, 18));

		jLabel1.setText("待发送数据路径:");
		jLabel1.setBounds(new Rectangle(6, 29, 100, 18));
		walletText.setBounds(new Rectangle(110, 29, 350, 18));
		walletText.setText(XLSUtils.RESULTPATH);
		sendSMS.setText("发送短信");
		sendSMS.setBounds(new Rectangle(470, 29, 100, 18));

		start.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				try
				{
					String resultStr = XLSUtils.readSalaryExcel(XLSUtils.XLSPATH);
					XLSUtils.writeToResultpath(resultStr);
					result.setText("xls解析成功，请检查待发送数据：" + XLSUtils.RESULTPATH);
					String resultStr1 = AliyunSendSMS.checkSendSMS();
					result.setText("xls解析成功，请检查待发送数据：" + XLSUtils.RESULTPATH+"\r\n"+resultStr1);
					parserxls = 1;
				}
				catch (Exception e1)
				{
					result.setText("xls解析失败：" + e1.toString() + "," + Arrays.toString(e1.getStackTrace()));
					e1.printStackTrace();
				}

			}

		});

		sendSMS.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (parserxls==-1)
				{
					result.setText("请先点击解析xls按钮!");
					return;
				}
				if (parserxls==0)
				{
					result.setText(result.getText()+"\r\n"+"短信已发送完成，请重新点击解析xls按钮生成待发送数据!");
					return;
				}
				String resultStr = "";
				try
				{
					resultStr = AliyunSendSMS.sendSMS();
					result.setText("发送完成:" + "\r\n" + resultStr);
					parserxls = 0;
					
				}
				catch (Exception e1)
				{
					result.setText("发送失败：" + e1.toString() + "," + Arrays.toString(e1.getStackTrace()));
					e1.printStackTrace();
					return;
				}
				
				try
				{
					XLSUtils.writeToFile(resultStr, System.getProperty("user.dir")  + File.separator + "发送结果"+ (new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date()))+".txt");
				}
				catch (Exception e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		});

		jLabel3.setText("结果:");
		jLabel3.setBounds(new Rectangle(6, 58, 50, 24));

		clean.setText("清除");
		clean.setBounds(new Rectangle(45, 61, 60, 20));
		clean.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				result.setText("");

			}

		});

		jScrollPane1.setBounds(new Rectangle(8, 88, 560, 400));

		p1.add(walletText, null);
		p1.add(sendSMS, null);
		p1.add(jLabel1, null);
		p1.add(jLabel2, null);
		p1.add(days, null);
		p1.add(start, null);
		p1.add(copy, null);
		p1.add(copyToFile, null);
		p1.add(clean, null);
		p1.add(jLabel3, null);
		p1.add(jScrollPane1, null);
		result.setText(note);
		jScrollPane1.getViewport().add(result, null);

		jScrollPane2.getViewport().add(resultLogText, null);
	}


}