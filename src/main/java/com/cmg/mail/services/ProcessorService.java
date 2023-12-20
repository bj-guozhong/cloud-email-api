package com.cmg.mail.services;

import com.cmg.mail.bean.MailEnum;
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

    //TODO:转发一封普通邮件，可带附件
    public JsonResult forwarderEmail(String username, String password, String emailId, String emailType, String[] to, String content, MultipartFile[] files){
        if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)){
            try {
                //IMAP收件服务嚣设置
                Properties props = configService.config(username, password);
                Session session = Session.getDefaultInstance(props, null);
                Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
                store.connect(configService.getImapHost(), username, password);

                //SMTP发件服务嚣设置
                Properties propsＳmtp = configService.configSmtp(username,password);
                // 创建会话对象
                Session sessionSmtp = Session.getInstance(propsＳmtp, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

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

                // 创建新邮件
                Message forwardMessage = new MimeMessage(sessionSmtp);

                // 设置新邮件的发件人、主题等信息
                forwardMessage.setFrom(new InternetAddress(username));
                forwardMessage.setSubject("Fwd: " + message.getSubject());

                //邮件内容部分---文本内容
                MimeBodyPart textPart = new MimeBodyPart(); //邮件中的文字部分

                // 创建包含附件的Multipart对象
                MimeMultipart multipart = new MimeMultipart();

                forwardMessage.setSentDate(message.getSentDate());
                StringBuffer sb = new StringBuffer();
                for (Address recipient : message.getAllRecipients()) {
                    sb.append(recipient.toString() + ";");
                }

                StringBuffer sbContent = new StringBuffer();
                sbContent.append(content);
                sbContent.append("\n\n\n\n\n\n---------- 原始邮件 ---------\n\n");
                sbContent.append("发件人: " + message.getFrom()[0] + "\n");
                sbContent.append("发送时间: " + CommonUtils.converDateFormat(message.getSentDate()) + "\n");
                sbContent.append("收件人：" + sb.toString() + "\n");
                sbContent.append("主题: " + message.getSubject() + "\n\n");
                sbContent.append(message.getContent().toString() + "\n");
                textPart.setText(sbContent.toString());

                for (MultipartFile file : files) {
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
                forwardMessage.setContent(multipart);

                for (String toSomeBody:to) {
                    forwardMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toSomeBody));
                    // 发送新邮件
                    Transport.send(forwardMessage);
                }

                // 关闭收件箱和会话
                draftsFolder.close(false);
                store.close();

                // 返回邮件列表
                return JsonResult.success(true);
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
    public JsonResult replyEmail(String username, String password, String emailId, String emailType, String[] to, String content, MultipartFile[] files) {
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            try {
                //IMAP收件服务嚣设置
                Properties props = configService.config(username, password);
                Session session = Session.getDefaultInstance(props, null);
                Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
                store.connect(configService.getImapHost(), username, password);

                //SMTP发件服务嚣设置
                Properties propsＳmtp = configService.configSmtp(username, password);
                // 创建会话对象
                Session sessionSmtp = Session.getInstance(propsＳmtp, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

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
                Message originalMessage = draftsFolder.getMessage(Integer.valueOf(emailId));
                // 创建回复邮件
                Message replyMessage = new MimeMessage(sessionSmtp);
                // 设置新邮件的发件人、主题等信息
                replyMessage.setFrom(new InternetAddress(username));
                replyMessage.setSubject("Re: " + originalMessage.getSubject());
                replyMessage.setSentDate(originalMessage.getSentDate());

                //邮件内容部分---文本内容
                MimeBodyPart textPart = new MimeBodyPart(); //邮件中的文字部分

                // 创建包含附件的Multipart对象
                MimeMultipart multipart = new MimeMultipart();

                // 添加多个收件人
                StringBuffer sb = new StringBuffer();
                for (Address recipient : originalMessage.getAllRecipients()) {
                    sb.append(recipient.toString() + ";");
                }

                // 回复邮件时带着原始邮件内容
                StringBuffer sbContent = new StringBuffer();
                sbContent.append(content);
                sbContent.append("\n\n\n\n\n\n---------- 原始邮件 ---------\n\n");
                sbContent.append("发件人: " + originalMessage.getFrom()[0] + "\n");
                sbContent.append("发送时间: " + CommonUtils.converDateFormat(originalMessage.getSentDate()) + "\n");
                sbContent.append("收件人：" + sb.toString() + "\n");
                sbContent.append("主题: " + originalMessage.getSubject() + "\n\n");
                sbContent.append(originalMessage.getContent().toString() + "\n");
                textPart.setText(sbContent.toString());

                for (MultipartFile file : files) {
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
                replyMessage.setContent(multipart);

                for (String toSomeBody:to) {
                    replyMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toSomeBody));
                    // 发送新邮件
                    Transport.send(replyMessage);
                }

                // 关闭收件箱和会话
                draftsFolder.close(false);
                store.close();

                // 返回邮件列表
                return JsonResult.success(true);

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
