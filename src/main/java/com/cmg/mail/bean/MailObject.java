package com.cmg.mail.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

import jakarta.mail.*;

@Setter
@Getter
public class MailObject  {
    enum EmailFormat {
        TEXT, HTML
    }

    /**
     * 邮件ID
     */
    private String eid;
    /**
     * 发件人
     */
    private String from;

    /**
     * 收件人
     */
    private Address[] to;

    /**
     * 抄送人
     */
    private Address[] cc;

    /**
     * 密送人
     */
    private Address[] bcc;

    /**
     * 邮件标题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String body;
    private String content;

    /**
     * 邮件格式
     */
    private EmailFormat format;

    /**
     * 附件
     */
    private List<Attachment> attachmentList;

    private String date;
}
