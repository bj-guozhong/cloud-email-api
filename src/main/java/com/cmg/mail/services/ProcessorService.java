package com.cmg.mail.services;

import com.cmg.mail.bean.Attachment;
import com.cmg.mail.bean.MailEnum;
import com.cmg.mail.bean.MailObject;
import com.cmg.mail.controller.result.JsonResult;
import com.cmg.mail.utils.CommonUtils;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

@Service("processorService")
public class ProcessorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorService.class);

    @Autowired
    private ConfigService configService;

    //TODO:删除某封信
    public JsonResult deleteEmailById(String username,String password,String[] emailId,String emailType){

        try {
            // 配置邮箱服务
            Properties props = configService.config(username, password);
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
            store.connect(configService.getImapHost(), username, password);
            Folder draftsFolder = null;
            Folder deleteFolder = store.getFolder(MailEnum.ALREADY_DELETE_FLAG.getLabel());
            deleteFolder.open(Folder.READ_WRITE);

            // 判断邮件是收件箱、发件箱、草稿箱中的邮件？
            if(emailType.equals(MailEnum.FOLDER_TYPE_DRAFTS.getLabel())){
                draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_DRAFTS.getLabel());
            }else if(emailType.equals(MailEnum.FOLDER_TYPE_INBOX.getLabel())) {
                draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_INBOX.getLabel());
            }else if(emailType.equals(MailEnum.FOLDER_TYPE_SENT.getLabel())) {
                draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_SENT.getLabel());
            }else{
                return JsonResult.error("对不起,邮件类型参数不合法,请检查后重试!");
            }

            // 打开对应的邮箱
            draftsFolder.open(Folder.READ_WRITE);
            if(emailId!=null && emailId.length>0){
                for(String  id:emailId){
                    // 根据邮件标识符获取指定邮件
                    Message message =draftsFolder.getMessage(Integer.valueOf(id));
                    // 将该邮件置为删除邮件
                    message.setFlag(Flags.Flag.DELETED, true);
                    // 将已删除邮件拷到已删除文件夹下
                    draftsFolder.copyMessages(new Message[]{message}, deleteFolder);
                }
            }else{
                return JsonResult.error("请选择要删除哪一封邮件!");
            }
            // 关闭连接
            draftsFolder.close(true);
            store.close();

        } catch (MessagingException e) {
            e.printStackTrace();
            return JsonResult.error("删除邮件时发生异常,请联系管理员!");
        }
        return JsonResult.success(true);
    }

    //TODO:标记某封信
    public JsonResult flagEmailById(String username,String password,String[] emailId,String emailType,String flag){

        try {
            Properties props = configService.config(username, password);
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
            store.connect(configService.getImapHost(), username, password);
            Folder draftsFolder = null;
            if(emailType.equals(MailEnum.FOLDER_TYPE_DRAFTS.getLabel())){
                draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_DRAFTS.getLabel());
            }else if(emailType.equals(MailEnum.FOLDER_TYPE_INBOX.getLabel())) {
                draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_INBOX.getLabel());
            }else if(emailType.equals(MailEnum.FOLDER_TYPE_SENT.getLabel())) {
                draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_SENT.getLabel());
            }else{
                return JsonResult.error("对不起,邮箱类型参数不合法,请检查后重试!");
            }
            draftsFolder.open(Folder.READ_WRITE);
            if(emailId!=null && emailId.length>0){
                for(String  id:emailId){
                    // 根据邮件标识符获取指定邮件
                    Message message =draftsFolder.getMessage(Integer.valueOf(id));
                    if(flag.equals("TRUE")){
                        // 将该邮件置为已读未读
                        message.setFlag(Flags.Flag.SEEN, true);
                    }
                    if(flag.equals("FALSE")){
                        message.setFlag(Flags.Flag.SEEN, false);
                    }
                    if(flag.equals("FLAGGED_TRUE")){
                        // 将邮件标记为红旗
                        message.setFlag(Flags.Flag.FLAGGED, true);
                    }
                    if(flag.equals("FLAGGED_FALSE")){
                        message.setFlag(Flags.Flag.FLAGGED, false);
                    }
                }

            }else{
                // 关闭连接
                draftsFolder.close(true);
                store.close();
                return JsonResult.error("请选择要删除哪一封邮件!");
            }
            // 关闭连接
            draftsFolder.close(true);
            store.close();

        } catch (MessagingException e) {
            e.printStackTrace();
            return JsonResult.error("删除邮件时发生异常,请联系管理员!");
        }
        return JsonResult.success(true);
    }

    //TODO:准备转发邮件
    public JsonResult forwarderEmail(String username, String password, String emailId, String emailType){
        if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)){
            try {
                //IMAP收件服务嚣设置
                Properties props = configService.config(username, password);
                Session session = Session.getDefaultInstance(props, null);
                Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
                store.connect(configService.getImapHost(), username, password);

                Folder draftsFolder = null;
                if(emailType.equals(MailEnum.FOLDER_TYPE_INBOX.getLabel())) {
                    draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_INBOX.getLabel());
                }else if(emailType.equals(MailEnum.FOLDER_TYPE_SENT.getLabel())) {
                    draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_SENT.getLabel());
                }else{
                    return JsonResult.error("对不起,邮箱类型参数不合法,请检查后重试!");
                }
                draftsFolder.open(Folder.READ_ONLY);

                // 根据邮件标识符获取指定邮件
                Message message =draftsFolder.getMessage(Integer.valueOf(emailId));

                MailObject mailObject = new MailObject();

                Address[] fromAddresses = message.getFrom();
                for (Address fromAddress : fromAddresses) {
                    String decodedFrom = CommonUtils.decodeFromAddress(fromAddress.toString());
                    System.out.println("Decoded From: " + decodedFrom);
                    mailObject.setFrom(decodedFrom);
                }
                mailObject.setSubject("Fwd: " + message.getSubject());

                mailObject.setDate(CommonUtils.converDateFormat(message.getSentDate()));
                StringBuffer sb = new StringBuffer();
                for (Address recipient : message.getAllRecipients()) {
                    String decodedFrom = CommonUtils.decodeFromAddress(recipient.toString());
                    sb.append(decodedFrom+ ";");
                }
                StringBuffer sbContent = new StringBuffer();
                sbContent.append("\n\n\n\n\n\n---------- 原始邮件 ---------\n\n");
                sbContent.append("发件人: " + message.getFrom()[0] + "\n");
                sbContent.append("发送时间: " + CommonUtils.converDateFormat(message.getSentDate()) + "\n");
                sbContent.append("收件人：" + sb.toString() + "\n");
                sbContent.append("主题: " + message.getSubject() + "\n\n");
                //sbContent.append(message.getContent().toString() + "\n");

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
                            //mailObject.setBody(bodyPart.getContent().toString());
                            sbContent.append(bodyPart.getContent().toString());
                        }
                    }
                    //将附件封装
                    mailObject.setAttachmentList(attachments);
                }
                mailObject.setContent(sbContent.toString());
                // 关闭收件箱和会话
                draftsFolder.close(true);
                store.close();

                // 返回邮件列表
                return JsonResult.success(mailObject);
            } catch (Exception e) {
                e.printStackTrace();
                // 处理异常，返回错误消息
                return JsonResult.error("转发普通邮件发生异常,请联系管理员");
            }
        }else{
            return JsonResult.error("用户名或密码为空!");
        }
    }



    //TODO:回复一封邮件
    public JsonResult replyEmail(String username, String password, String emailId, String emailType) {
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            try {
                //IMAP收件服务嚣设置
                Properties props = configService.config(username, password);
                Session session = Session.getDefaultInstance(props, null);
                Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
                store.connect(configService.getImapHost(), username, password);

                // 判断是收件箱还是发件箱？
                Folder draftsFolder = null;
                if (emailType.equals(MailEnum.FOLDER_TYPE_INBOX.getLabel())) {
                    draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_INBOX.getLabel());
                } else if (emailType.equals(MailEnum.FOLDER_TYPE_SENT.getLabel())) {
                    draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_SENT.getLabel());
                } else {
                    return JsonResult.error("对不起,邮箱类型参数不合法,请检查后重试!");
                }
                draftsFolder.open(Folder.READ_ONLY);

                // 根据邮件标识符获取指定邮件
                Message message =draftsFolder.getMessage(Integer.valueOf(emailId));

                MailObject mailObject = new MailObject();
                mailObject.setFrom(username);
                mailObject.setTo(message.getFrom());
                mailObject.setSubject("Fwd: " + message.getSubject());

                mailObject.setDate(CommonUtils.converDateFormat(message.getSentDate()));
                StringBuffer sb = new StringBuffer();
                for (Address recipient : message.getAllRecipients()) {
                    String decodedFrom = CommonUtils.decodeFromAddress(recipient.toString());
                    sb.append(decodedFrom+ ";");
                }

                StringBuffer sbContent = new StringBuffer();
                sbContent.append("\n\n\n\n\n\n---------- 原始邮件 ---------\n\n");
                sbContent.append("发件人: " + message.getFrom()[0] + "\n");
                sbContent.append("发送时间: " + CommonUtils.converDateFormat(message.getSentDate()) + "\n");
                sbContent.append("收件人：" + sb.toString() + "\n");
                sbContent.append("主题: " + message.getSubject() + "\n\n");
                sbContent.append(message.getContent().toString() + "\n");
                mailObject.setContent(sbContent.toString());

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

                // 关闭收件箱和会话
                draftsFolder.close(true);
                store.close();

                // 返回邮件列表
                return JsonResult.success(mailObject);

            }catch (Exception e) {
                e.printStackTrace();
                // 处理异常，返回错误消息
                return JsonResult.error("回复邮件发生异常,请联系管理员");
            }
        }else{
            return JsonResult.error("用户名或密码为空!");
        }
    }




}
