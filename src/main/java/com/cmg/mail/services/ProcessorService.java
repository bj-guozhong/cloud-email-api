package com.cmg.mail.services;

import com.cmg.mail.bean.MailEnum;
import com.cmg.mail.controller.result.JsonResult;
import jakarta.mail.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
            Folder deleteFolder = store.getFolder("已删除");
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
    public JsonResult flagEmailById(String username,String password,String[] emailId,String emailType,String isSeen){

        try {
            Properties props = configService.config(username, password);
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore(MailEnum.PROTOCOL.getLabel());
            store.connect(configService.getImapHost(), username, password);
            Folder draftsFolder = null;
            if(emailType.equals(MailEnum.FOLDER_TYPE_DRAFTS)){
                draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_DRAFTS.getLabel());
            }else if(emailType.equals(MailEnum.FOLDER_TYPE_INBOX)) {
                draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_INBOX.getLabel());
            }else if(emailType.equals(MailEnum.FOLDER_TYPE_SENT)) {
                draftsFolder = store.getFolder(MailEnum.FOLDER_TYPE_SENT.getLabel());
            }else{
                return JsonResult.error("对不起,邮箱类型参数不合法,请检查后重试!");
            }
            draftsFolder.open(Folder.READ_WRITE);
            if(emailId!=null && emailId.length>0){
                for(String  id:emailId){
                    // 根据邮件标识符获取指定邮件
                    Message message =draftsFolder.getMessage(Integer.valueOf(id));
                    if(isSeen.equals("true")){
                        // 将该邮件置为删除邮件
                        message.setFlag(Flags.Flag.SEEN, true);
                    }else{
                        message.setFlag(Flags.Flag.SEEN, false);
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

}
