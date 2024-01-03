package com.cmg.mail.services;

import com.cmg.mail.bean.MailEnum;
import com.cmg.mail.bean.MailObject;
import com.cmg.mail.controller.result.JsonResult;
import com.cmg.mail.utils.CommonUtils;
import com.cmg.mail.utils.EmailUtils;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service("inboxService")
public class InboxService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InboxService.class);

	@Autowired
	private ConfigService configService;

	//TODO:认证
	public JsonResult auth(String username, String password){
		if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)){
			try {
				Properties props = configService.config(username,password);
				Session session = Session.getDefaultInstance(props);
				Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
				//LOGGER.info("======================auth:"+props.getProperty(MailEnum.PROPS_MAIL_IMAP_AUTH.getLabel()));
				if (Boolean.valueOf(props.getProperty(MailEnum.PROPS_MAIL_IMAP_AUTH.getLabel()))) {
					// 需要认证
					store.connect(props.getProperty(MailEnum.PROPS_MAIL_IMAP_HOST.getLabel()), Integer.valueOf(props.getProperty(MailEnum.PROPS_MAIL_IMAP_PORT.getLabel())),
							props.getProperty(MailEnum.PROPS_MAIL_IMAP_USER.getLabel()), props.getProperty(MailEnum.PROPS_MAIL_IMAP_PASS.getLabel()));
				} else {
					store.connect();
				}

				// 验证成功，返回成功消息
				return JsonResult.success("认证成功!");
			} catch (AuthenticationFailedException e) {
				e.printStackTrace();
				// 验证失败，返回错误消息
				return JsonResult.error("认证失败!");
			} catch (Exception e) {
				e.printStackTrace();
				// 其他异常，返回错误消息
				return JsonResult.error("认证失败!");
			}
		}else{
			return JsonResult.error("用户名或密码为空!");
		}
	}

	//TODO:获取邮件列表
	public JsonResult fetchEmails(String username,String password,String pageNumber,String pageSize,String sortType,String filterName){
		if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)){
			try {
				Properties props = configService.config(username,password);
				Session session = Session.getDefaultInstance(props, null);
				Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
				store.connect(configService.getImapHost(), username, password);
				// 打开收件箱
				Folder inbox = store.getFolder(MailEnum.FOLDER_TYPE_INBOX.getLabel());
				inbox.open(Folder.READ_ONLY);
				// 获取收件箱中的所有邮件，并按照日期降序排序
				Message[] messages = inbox.getMessages();

				// 过滤邮件 20240103 add
				List<Message> messageList = EmailUtils.sortAndFilterMessage(sortType,filterName,messages);

				// 获取邮件列表
				List<Message> currentPageMessages;
				if(StringUtils.isNotEmpty(pageNumber) && StringUtils.isNotBlank(pageNumber) && StringUtils.isNotEmpty(pageSize)){
					// 获取邮件数量
					int totalMessages = inbox.getMessageCount();
					// 根据每页邮件数量和页数计算起始邮件和结束邮件的索引
					int startIndex = (Integer.valueOf(pageNumber) - 1) * Integer.valueOf(pageSize);
					int endIndex = Math.min(startIndex + Integer.valueOf(pageSize), totalMessages);
					// 获取当前页的邮件列表
					currentPageMessages = messageList.subList(startIndex, endIndex);
				}else{
					currentPageMessages = messageList;
				}
				// 构建 JSON 数组来存储邮件
				ArrayList<MailObject> mailObjectArrayList = new ArrayList<>();
				for (int i = 0; i < currentPageMessages.size();i++){
					Message message = currentPageMessages.get(i);

					// 构建 JSON 对象来存储邮件信息
					MailObject mailObject = new MailObject();
					mailObject.setEid(String.valueOf(message.getMessageNumber()));
					mailObject.setSubject(message.getSubject());
					mailObject.setFrom(String.valueOf(message.getFrom()[0]));
					mailObject.setTo(message.getAllRecipients());
					mailObject.setDate(CommonUtils.converDateFormat(message.getSentDate()));
					mailObject.setBody(message.getContent().toString());
					// 将邮件信息添加到数组中
					mailObjectArrayList.add(mailObject);
				}
				// 关闭收件箱和存储
				inbox.close(false);
				store.close();
				// 返回收件箱列表
				return JsonResult.success(mailObjectArrayList);
			} catch (Exception e) {
				e.printStackTrace();
				// 处理异常，返回错误消息
				return JsonResult.error("获取收件箱列表发生异常,请联系管理员");
			}
		}else{
			return JsonResult.error("用户名或密码为空!");
		}
	}

}
