package com.cmg.mail.services;

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

@Service("sentService")
public class SentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SentService.class);

	@Autowired
	private ConfigService configService;

	//TODO:获取已发送邮件列表
	public JsonResult sentEmailsReader(String username,String password,String pageNumber,String pageSize){
		if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)){
			try {
				Properties props = configService.config(username,password);
				Session session = Session.getDefaultInstance(props, null);
				Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
				store.connect(configService.getImapHost(), username, password);
				// 打开收件箱
				//Folder folder[ ]=store.getDefaultFolder().list();
				Folder sent = store.getFolder(MailEnum.FOLDER_TYPE_SENT.getLabel());
				sent.open(Folder.READ_ONLY);

				// 获取收件箱中的所有邮件，并按照日期降序排序
				Message[] messages = sent.getMessages();
				List<Message> messageList = Arrays.asList(messages);
				Collections.sort(messageList, (m1, m2) -> {
					try {
						return m2.getSentDate().compareTo(m1.getSentDate());
					} catch (MessagingException e) {
						e.printStackTrace();
						return 0;
					}
				});

				// 获取邮件列表
				List<Message> currentPageMessages;
				if(StringUtils.isNotEmpty(pageNumber) && StringUtils.isNotBlank(pageNumber) && StringUtils.isNotEmpty(pageSize)){
					// 获取邮件数量
					int totalMessages = sent.getMessageCount();
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
				for (int i = 0; i < currentPageMessages.size(); i++) {
					Message message = currentPageMessages.get(i);
					// 构建 JSON 对象来存储邮件信息
					MailObject mailObject = new MailObject();
					mailObject.setEid(String.valueOf(message.getMessageNumber()));
					mailObject.setSubject(message.getSubject());
					mailObject.setFrom(String.valueOf(message.getFrom()[0]));
					mailObject.setTo(message.getAllRecipients());
					mailObject.setDate(message.getSentDate().toString());
					mailObject.setBody(message.getContent().toString());
					// 将邮件信息添加到数组中
					mailObjectArrayList.add(mailObject);
				}
				// 关闭收件箱和存储
				sent.close(false);
				store.close();
				// 返回邮件列表
				return JsonResult.success(mailObjectArrayList);
			} catch (Exception e) {
				e.printStackTrace();
				// 处理异常，返回错误消息
				return JsonResult.error("获取已发送列表发生异常,请联系管理员");
			}
		}else{
			return JsonResult.error("用户名或密码为空!");
		}
	}

}
