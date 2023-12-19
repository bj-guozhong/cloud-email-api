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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


@Service("downAttachmentService")
public class DownAttachmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownAttachmentService.class);

	@Autowired
	private ConfigService configService;

	//TODO:查看某封邮件详情
	public Object downAttachmentById(String emailId,String fileName,String username,String password){
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
							if(fileName.equals(bodyPart.getFileName())){
								// 将附件内容读取到字节数组
								ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
								bodyPart.getDataHandler().writeTo(outputStream);
								byte[] attachmentData = outputStream.toByteArray();
								// 设置响应头部信息
								HttpHeaders headers = new HttpHeaders();
								headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
								headers.setContentDispositionFormData("attachment", fileName);

								// 返回附件内容给前端
								return new ResponseEntity<>(attachmentData, headers, HttpStatus.OK);
							}
						}
					}
				}
				// 验证成功，返回成功消息
				return null;
			} catch (AuthenticationFailedException e) {
				e.printStackTrace();
				// 验证失败，返回错误消息
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				// 其他异常，返回错误消息
				return null;
			}
		}else{
			return null;
		}
	}


	/**
	 * 真正的保存附件到指定目录里
	 * @param fileName
	 * @param in
	 * @throws Exception
	 */
	private File saveFile(String fileName, InputStream in) throws Exception {

		String osName = System.getProperty("os.name");
		String storeDir = "D:\\temp";
		String separator = "";
		if (osName == null) {
			osName = "";
		}
		if (osName.toLowerCase().indexOf("win") != -1) {
			separator = "\\";
			if (storeDir == null || storeDir.equals("")) {
				storeDir = "D:\\temp";
			}
		} else {
			separator = "/";
			storeDir = "/tmp";
		}
		File storeFile = new File(storeDir + separator + fileName);
		LOGGER.info("附件的保存地址:　" + storeFile.toString());
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(storeFile));
			bis = new BufferedInputStream(in);
			int c;
			while ((c = bis.read()) != -1) {
				bos.write(c);
				bos.flush();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new Exception("文件保存失败!");
		} finally {
			bos.close();
			bis.close();
		}
		return storeFile;
	}
}
