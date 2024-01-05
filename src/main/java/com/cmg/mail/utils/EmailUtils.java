package com.cmg.mail.utils;

import com.cmg.mail.bean.MailEnum;
import com.cmg.mail.controller.result.JsonResult;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class EmailUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailUtils.class);

	// TODO:针对邮件进行过滤处理
	public static List<Message> sortAndFilterMessage(String sortType,String filterName,Message[] messages) throws MessagingException {

		// 默认情况下按时间降序
		if(StringUtils.isEmpty(sortType) && StringUtils.isEmpty(filterName)){
			List<Message> messageList = Arrays.asList(messages);
			Collections.sort(messageList, (m1, m2) -> {
				try {
					return m2.getSentDate().compareTo(m1.getSentDate());
				} catch (MessagingException e) {
					e.printStackTrace();
					return 0;
				}
			});
			return messageList;
		}else{
			// 创建新的数组用于存储邮件
			List<Message> newMessageList = new ArrayList<>();

			// 过滤条件ALL,UNREAD,READ,FLAGGED,UNFLAGGED,URGENT,NORMAL,SLOW,WITH_ATT,NO_ATT,REPLIED,FORWARDED
			if(StringUtils.isNotEmpty(filterName) && StringUtils.isNotBlank(filterName)){
				for (Message message : messages) {
					if(filterName.equals("UNREAD")){
						if (!message.isSet(Flags.Flag.SEEN)) {
							newMessageList.add(message);
						}
					}else if(filterName.equals("READ")){
						if (message.isSet(Flags.Flag.SEEN)) {
							newMessageList.add(message);
						}
					}else if(filterName.equals("FLAGGED")){
						if (message.isSet(Flags.Flag.FLAGGED)) {
							newMessageList.add(message);
						}
					}else if(filterName.equals("UNFLAGGED")){
						if (!message.isSet(Flags.Flag.FLAGGED)) {
							newMessageList.add(message);
						}
					}else if(filterName.equals("URGENT")){
						if ( message.getHeader("X-Priority")[0].equals("Urgent")) {
							newMessageList.add(message);
						}
					}else if(filterName.equals("NORMAL")){
						if ( message.getHeader("X-Priority")[0].equals("Normal")) {
							newMessageList.add(message);
						}
					}else if(filterName.equals("SLOW")){
						if ( message.getHeader("X-Priority")[0].equals("Slow")) {
							newMessageList.add(message);
						}
					}else if(filterName.equals("WITH_ATT")){
						if (message.isMimeType(MailEnum.EMAIL_MULTIPART_INFO.getLabel())) {
							newMessageList.add(message);
						}
					}else if(filterName.equals("NO_ATT")){
						if (!message.isMimeType(MailEnum.EMAIL_MULTIPART_INFO.getLabel())) {
							newMessageList.add(message);
						}
					}else if(filterName.equals("REPLIED")){
						if (message.isSet(Flags.Flag.ANSWERED)) {
							newMessageList.add(message);
						}
					}else if(filterName.equals("FORWARDED")){
						// 暂时没有找到如何判断是否转发的标识。
					}
				}
			}else{
				newMessageList = Arrays.asList(messages);
			}

			// 排序条件 DATE_ASC,DATE_DESC,FROM_ASC,FROM_DESC,SUBJECT_ASC,SUBJECT_DESC,,MAIL_SIZE_ASC,MAIL_SIZE_DESC
			if(StringUtils.isNotEmpty(sortType) && StringUtils.isNotBlank(sortType)){
				if(sortType.equals("DATE_DESC")){
					Collections.sort(newMessageList, (m1, m2) -> {
						try {
							return m1.getSentDate().compareTo(m2.getSentDate());
						} catch (MessagingException e) {
							e.printStackTrace();
							return 0;
						}
					});
					return newMessageList;
				}
				if(sortType.equals("DATE_ASC")){
					Collections.sort(newMessageList, (m1, m2) -> {
						try {
							return m2.getSentDate().compareTo(m1.getSentDate());
						} catch (MessagingException e) {
							e.printStackTrace();
							return 0;
						}
					});
					return newMessageList;
				}
				// 发件人排序
				if(sortType.equals("FROM_ASC")){
					Arrays.sort(messages, Comparator.comparing(m -> {
						try {
							return getSenderEmailAddress((Message) m);
						} catch (MessagingException e) {
							e.printStackTrace();
							return null;
						}
					}));
				}
				if(sortType.equals("FROM_DESC")){
					Arrays.sort(messages, Comparator.comparing(m -> {
						try {
							return getSenderEmailAddress((Message) m);
						} catch (MessagingException e) {
							e.printStackTrace();
							return null;
						}
					}).reversed());
				}
				if(sortType.equals("SUBJECT_ASC")){
					Arrays.sort(messages, Comparator.comparing(m -> {
						try {
							return getSenderEmailSubject((Message) m);
						} catch (MessagingException e) {
							e.printStackTrace();
							return null;
						}
					}));
				}
				if(sortType.equals("SUBJECT_DESC")){
					Arrays.sort(messages, Comparator.comparing(m -> {
						try {
							return getSenderEmailSubject((Message) m);
						} catch (MessagingException e) {
							e.printStackTrace();
							return null;
						}
					}).reversed());
				}
				if(sortType.equals("MAIL_SIZE_ASC")){
					Arrays.sort(messages, Comparator.comparing(m -> {
						try {
							return getSenderEmailSize((Message) m);
						} catch (MessagingException e) {
							e.printStackTrace();
							return null;
						}
					}));
				}
				if(sortType.equals("MAIL_SIZE_DESC")){
					Arrays.sort(messages, Comparator.comparing(m -> {
						try {
							return getSenderEmailSize((Message) m);
						} catch (MessagingException e) {
							e.printStackTrace();
							return null;
						}
					}).reversed());
				}

			}
			return newMessageList;
		}
	}

	// 获取发件人邮箱地址
	private static String getSenderEmailAddress(Message message) throws MessagingException {
		Address[] fromAddresses = message.getFrom();
		if (fromAddresses.length > 0) {
			return ((InternetAddress) fromAddresses[0]).getAddress();
		}
		return "";
	}

	// 获取邮件主题
	private static String getSenderEmailSubject(Message message) throws MessagingException {
		if(StringUtils.isNotEmpty(message.getSubject()) && StringUtils.isNotBlank(message.getSubject())){
			return message.getSubject();
		}else{
			return "无主题";
		}
	}

	// 获取邮件大小
	private static Integer getSenderEmailSize(Message message) throws MessagingException {
		return message.getSize();
	}

	//根据相应参数返回对应的文件夹
	public static Folder getFolderByBoxType(String sourceBox,Store store){
		try {
			Folder commonFolder = null;
			if(sourceBox.equals(MailEnum.FOLDER_TYPE_SENT.getLabel())){
				commonFolder = store.getFolder(MailEnum.FOLDER_TYPE_SENT.getLabel());
			}else if(sourceBox.equals(MailEnum.FOLDER_TYPE_DRAFTS.getLabel())) {
				commonFolder = store.getFolder(MailEnum.FOLDER_TYPE_DRAFTS.getLabel());
			}else if(sourceBox.equals(MailEnum.FOLDER_TYPE_INBOX.getLabel())) {
				commonFolder = store.getFolder(MailEnum.FOLDER_TYPE_INBOX.getLabel());
			}else if(sourceBox.equals(MailEnum.FOLDER_TYPE_DELETE.getLabel())) {
				commonFolder = store.getFolder(MailEnum.FOLDER_TYPE_DELETE.getLabel());
			}else if(sourceBox.equals(MailEnum.FOLDER_TYPE_TRASH.getLabel())) {
				commonFolder = store.getFolder(MailEnum.FOLDER_TYPE_TRASH.getLabel());
			}else if(sourceBox.equals(MailEnum.FOLDER_TYPE_VIRUS.getLabel())) {
				commonFolder = store.getFolder(MailEnum.FOLDER_TYPE_VIRUS.getLabel());
			}else{
				return null;
			}
			return commonFolder;
		} catch (MessagingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
