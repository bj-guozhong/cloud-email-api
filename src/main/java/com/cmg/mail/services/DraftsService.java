package com.cmg.mail.services;

import com.cmg.mail.bean.Attachment;
import com.cmg.mail.bean.MailEnum;
import com.cmg.mail.bean.MailObject;
import com.cmg.mail.controller.result.JsonResult;
import com.cmg.mail.utils.CommonUtils;
import com.cmg.mail.utils.EmailUtils;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service("draftsService")
public class DraftsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DraftsService.class);

	@Autowired
	private ConfigService configService;

	//TODO:获取草稿箱邮件列表
	public JsonResult draftsEmailsReader(String username,String password,String pageNumber,String pageSize,String sortType,String filterName){
		if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)){
			try {
				Properties props = configService.config(username,password);
				Session session = Session.getDefaultInstance(props, null);
				Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
				store.connect(configService.getImapHost(), username, password);
				// 打开草稿箱
				Folder drafts = store.getFolder(MailEnum.FOLDER_TYPE_DRAFTS.getLabel());
				drafts.open(Folder.READ_ONLY);
				// 获取草稿箱中的所有邮件，并按照日期降序排序
				Message[] messages = drafts.getMessages();

				// 过滤邮件 20240103 add
				List<Message> messageList = EmailUtils.sortAndFilterMessage(sortType,filterName,messages);

				// 获取邮件列表
				List<Message> currentPageMessages;
				if(StringUtils.isNotEmpty(pageNumber) && StringUtils.isNotBlank(pageNumber) && StringUtils.isNotEmpty(pageSize)){
					// 获取邮件数量
					int totalMessages = drafts.getMessageCount();
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
					//LOGGER.info("======================get email id is:"+message.getHeader("Message-ID")[0]);
					//LOGGER.info("======================get email id is:"+message.getm);
					String[] messageIDs = message.getHeader("Message-ID");
					if (messageIDs != null && messageIDs.length > 0) {
						String messageID = messageIDs[0];
						System.out.println("邮件唯一标识：" + messageID);
					}

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
				// 关闭草稿箱和存储
				drafts.close(false);
				store.close();
				// 返回邮件列表
				return JsonResult.success(mailObjectArrayList);
			} catch (Exception e) {
				e.printStackTrace();
				// 处理异常，返回错误消息
				return JsonResult.error("获取草稿箱列表发生异常,请联系管理员");
			}
		}else{
			return JsonResult.error("用户名或密码为空!");
		}
	}

	//TODO：保存普通邮件到草稿
	public JsonResult saveToDrafts(String username, String password, String from, String to, String subject, String content,String emailId) {
		if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
			try {
				Properties props = configService.config(username, password);
				Session session = Session.getDefaultInstance(props, null);
				Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
				store.connect(configService.getImapHost(), username, password);
				// 打开草稿箱
				Folder draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_DRAFTS.getLabel());
				draftsFolder.open(Folder.READ_WRITE);

				// 创建一封新的 MimeMessage
				MimeMessage draftMessage = new MimeMessage(session);
				if(StringUtils.isNotEmpty(emailId) && StringUtils.isNotBlank(emailId)){
					// 根据邮件标识符获取指定邮件
					Message message =draftsFolder.getMessage(Integer.valueOf(emailId));
					// 删除原始草稿邮件
					message.setFlag(Flags.Flag.DELETED, true);
					// 执行 expunge 操作可能会有一些性能开销，并且标记为删除的邮件将被永久删除
					draftsFolder.expunge();
					// 修改时设置为已读
					draftMessage.setFlag(Flags.Flag.SEEN,true);
				}
				draftMessage.setFrom(new InternetAddress(from));
				draftMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
				draftMessage.setSubject(subject);
				draftMessage.setText(content);
				// 保存草稿
				draftsFolder.appendMessages(new Message[]{draftMessage});
				// 关闭连接
				draftsFolder.close(true);
				store.close();
				return JsonResult.success(true);
			}catch(Exception e){
				e.printStackTrace();
				// 处理异常，返回错误消息
				return JsonResult.error("保存到草稿箱发生异常,请联系管理员");
			}
		}else{
			return JsonResult.error("登录信息已过期,请重新登录!");
		}
	}
	//TODO:保存带附件的邮件到草稿箱
	public JsonResult saveFileEmailToDrafts(String username, String password, String from, String to, String subject, String content, MultipartFile[] files,String emailId) {
		if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
			try {
				Properties props = configService.config(username, password);
				Session session = Session.getDefaultInstance(props, null);
				Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
				store.connect(configService.getImapHost(), username, password);
				// 打开草稿箱
				Folder draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_DRAFTS.getLabel());
				draftsFolder.open(Folder.READ_WRITE);

				// 创建一封新的 MimeMessage
				MimeMessage draftMessage = new MimeMessage(session);
				draftMessage.setFrom(new InternetAddress(from));
				draftMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
				draftMessage.setSubject(subject);

				if(StringUtils.isNotEmpty(emailId) && StringUtils.isNotBlank(emailId)){
					// 根据邮件标识符获取指定邮件
					Message message =draftsFolder.getMessage(Integer.valueOf(emailId));
					// 删除原始草稿邮件
					message.setFlag(Flags.Flag.DELETED, true);
					// 执行 expunge 操作可能会有一些性能开销，并且标记为删除的邮件将被永久删除
					draftsFolder.expunge();
					// 修改时设置为已读
					draftMessage.setFlag(Flags.Flag.SEEN,true);
				}

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
				draftMessage.setContent(multipart);
				// 保存草稿
				draftsFolder.appendMessages(new Message[]{draftMessage});

				// 关闭连接
				draftsFolder.close(true);
				store.close();
				return JsonResult.success(true);
			}catch(Exception e){
				e.printStackTrace();
				// 处理异常，返回错误消息
				return JsonResult.error("保存到草稿箱发生异常,请联系管理员");
			}
		}else{
			return JsonResult.error("登录信息已过期,请重新登录!");
		}
	}

	//TODO:删除草稿箱中某封信中的附件
	public JsonResult deleteAttInDrafts(String username, String password,String fileName,String emailId){
		if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
			try {
				Properties props = configService.config(username, password);
				Session session = Session.getDefaultInstance(props, null);
				Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
				store.connect(configService.getImapHost(), username, password);
				// 打开草稿箱
				Folder draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_DRAFTS.getLabel());
				draftsFolder.open(Folder.READ_WRITE);

				// 根据邮件标识符获取指定邮件
				Message message =draftsFolder.getMessage(Integer.valueOf(emailId));
				MimeMessage mimeMessage = new MimeMessage((MimeMessage) message);

				// 将邮件转换为 Multipart 对象
				Multipart multipart = (Multipart) mimeMessage.getContent();
				// 遍历每个邮件部分
				for (int i = 0; i < multipart.getCount(); i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					// 检查邮件部分是否是附件
					if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
						if(fileName.equals(bodyPart.getFileName())){
							//删除附件
							multipart.removeBodyPart(bodyPart);
							LOGGER.info("delete att success.");
							break;
						}
					}
				}
				// 保存修改后的邮件内容
				mimeMessage.saveChanges();;
				// 删除原始草稿邮件
				message.setFlag(Flags.Flag.DELETED, true);
				// 执行 expunge 操作可能会有一些性能开销，并且标记为删除的邮件将被永久删除
				draftsFolder.expunge();
				//　保存草稿
				draftsFolder.appendMessages(new Message[]{mimeMessage});
				// 关闭连接
				draftsFolder.close(true);
				store.close();
				return JsonResult.success(true);
			}catch(Exception e){
				e.printStackTrace();
				// 处理异常，返回错误消息
				return JsonResult.error("删除草稿箱中附件发生异常,请联系管理员");
			}
		}else{
			return JsonResult.error("登录信息已过期,请重新登录!");
		}
	}

	//TODO:查看草稿箱中某封信
	public JsonResult viewDraftsEmailById(String emailId,String username,String password){
		if(StringUtils.isNotEmpty(emailId) && StringUtils.isNotEmpty(emailId)){
			try {
				// 创建会话对象
				Properties props = configService.config(username,password);
				Session session = Session.getDefaultInstance(props, null);
				// 连接到邮件服务器
				Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
				store.connect(configService.getImapHost(), username, password);
				// 获取收件箱
				Folder inbox = store.getFolder(MailEnum.FOLDER_TYPE_DRAFTS.getLabel());
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
				return JsonResult.error("查看草稿箱中某邮件详情异常,请联系管理员!");
			} catch (Exception e) {
				e.printStackTrace();
				// 其他异常，返回错误消息
				return JsonResult.error("查看草稿箱中某邮件详情异常!");
			}
		}else{
			return JsonResult.error("参数有误!");
		}
	}
}
