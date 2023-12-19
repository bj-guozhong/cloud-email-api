package com.cmg.mail.services;

import com.cmg.mail.bean.Attachment;
import com.cmg.mail.bean.MailEnum;
import com.cmg.mail.bean.MailObject;
import com.cmg.mail.controller.result.JsonResult;
import jakarta.mail.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service("inboxDetailService")
public class InboxDetailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InboxDetailService.class);

	@Autowired
	private ConfigService configService;

	//TODO:查看某封邮件详情
	public JsonResult viewInboxEmailById(String emailId,String username,String password){
		if(StringUtils.isNotEmpty(emailId) && StringUtils.isNotEmpty(emailId)){
			try {
				// 创建会话对象
				Properties props = configService.config(username,password);
				Session session = Session.getDefaultInstance(props, null);
				// 连接到邮件服务器
				Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
				store.connect(configService.getImapHost(), username, password);
				// 获取收件箱
				Folder inbox = store.getFolder(MailEnum.FOLDER_TYPE_INBOX.getLabel());
				inbox.open(Folder.READ_ONLY);
				// 根据邮件标识符获取指定邮件
				Message message = inbox.getMessage(Integer.valueOf(emailId));
				MailObject mailObject = new MailObject();
				mailObject.setEid(String.valueOf(message.getMessageNumber()));
				mailObject.setSubject(message.getSubject());
				mailObject.setFrom(String.valueOf(message.getFrom()[0]));
				mailObject.setTo(message.getAllRecipients());
				mailObject.setDate(message.getSentDate().toString());
				//mailObject.setBody(message.getContent().toString());
				// 检查邮件是否有附件
				if (message.isMimeType(MailEnum.EMAIL_MULTIPART_INFO.getLabel())) {
					// 将邮件转换为 Multipart 对象
					Multipart multipart = (Multipart) message.getContent();
					// 创建一个列表来存储附件信息
					List<Attachment> attachments = new ArrayList<>();
					// 遍历每个邮件部分
					for (int i = 0; i < multipart.getCount(); i++) {
						BodyPart bodyPart = multipart.getBodyPart(i);
						// 检查邮件部分是否是附件
						if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
							// 获取附件文件名
							String fileName = bodyPart.getFileName();
							// 创建一个附件对象，并存储附件的文件名和内容类型
							Attachment attachment = new Attachment(fileName, bodyPart.getContentType());
							// 将附件对象添加到附件列表中
							attachments.add(attachment);
						}else{
							mailObject.setBody(bodyPart.getContent().toString());
						}
					}
					//将附件封装
					mailObject.setAttachmentList(attachments);
				}
				// 验证成功，返回成功消息
				return JsonResult.success(mailObject);
			} catch (AuthenticationFailedException e) {
				e.printStackTrace();
				// 验证失败，返回错误消息
				return JsonResult.error("查看邮件详情异常,请联系管理员!");
			} catch (Exception e) {
				e.printStackTrace();
				// 其他异常，返回错误消息
				return JsonResult.error("查看邮件详情异常!");
			}
		}else{
			return JsonResult.error("参数有误!");
		}
	}
}
