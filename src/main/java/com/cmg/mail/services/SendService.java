package com.cmg.mail.services;


import com.cmg.mail.bean.MailEnum;
import com.cmg.mail.bean.MailObject;
import com.cmg.mail.controller.result.JsonResult;
import com.cmg.mail.utils.CommonUtils;
import com.sun.mail.util.MailSSLSocketFactory;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;


@Service("sendService")
public class SendService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendService.class);

	@Autowired
	private ConfigService configService;

	//TODO:发送普通邮件
	public JsonResult sendEmail(String username,String password,String from,String to,String subject,String content){
		if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)){
			try {
				Properties props = configService.configSmtp(username,password);
				// 创建会话对象
				Session session = Session.getInstance(props, new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(from, password);
					}
				});
				// 创建邮件
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress(from));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
				message.setSubject(subject);
				message.setText(content);
				// 发送邮件
				Transport.send(message);
				//store.close();
				// 返回邮件列表
				return JsonResult.success(true);
			} catch (Exception e) {
				e.printStackTrace();
				// 处理异常，返回错误消息
				return JsonResult.error("获取已发送列表发生异常,请联系管理员");
			}
		}else{
			return JsonResult.error("用户名或密码为空!");
		}
	}

	//TODO:发送带附件的邮件
	public JsonResult sendEmailConAtt(String username, String password, String from, String to, String subject, String content, MultipartFile[] files){
		if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)){
			try {
				Properties props = configService.configSmtp(username,password);
				// 创建会话对象
				Session session = Session.getInstance(props, new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(from, password);
					}
				});
				// 生成唯一标识
				//String attachmentIdentifier  = CommonUtils.generateUniqueIdentifier();
				// 通过session得到transport对象
				Transport ts = session.getTransport();
				// 连接邮件服务器：邮箱类型，帐号，授权码代替密码（更安全）
				ts.connect(configService.getSmtpHost(), username, password);
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress(from));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
				message.setSubject(subject);

				//邮件内容部分---文本内容
				MimeBodyPart textPart  = new MimeBodyPart(); //邮件中的文字部分
				textPart.setText(content);
				// 创建包含附件的Multipart对象
				MimeMultipart multipart = new MimeMultipart();

				for(MultipartFile file:files){
					// 将文本内容部分添加到Multipart对象中
					multipart.addBodyPart(textPart);
					// 创建附件部分
					MimeBodyPart attachmentPart = new MimeBodyPart();
					// 将MultipartFile文件对象转换为DataSource对象
					DataSource source = new FileDataSource(CommonUtils.convertMultipartFileToFile(file));
					// 设置附件数据处理器
					attachmentPart.setDataHandler(new DataHandler(source));
					// 设置附件文件名
					attachmentPart.setFileName(file.getOriginalFilename());
					// 将附件部分添加到Multipart对象中
					multipart.addBodyPart(attachmentPart);
				}

				// 将Multipart对象设置为邮件的内容
				message.setContent(multipart);
				// 发送邮件
				// 发送邮件
				Transport.send(message);

				// 返回邮件列表
				return JsonResult.success(true);
			} catch (Exception e) {
				e.printStackTrace();
				// 处理异常，返回错误消息
				return JsonResult.error("发送带附件的邮件发生异常,请联系管理员");
			}
		}else{
			return JsonResult.error("用户名或密码为空!");
		}
	}
}
