package com.cmg.mail.bean;

import java.util.Properties;

public enum MailEnum {

    PROTOCOL("imap"),
    PROTOCOL_SMTP("smtp"),

    FOLDER_TYPE_SENT("Sent Items"),
    FOLDER_TYPE_INBOX("INBOX"),
    FOLDER_TYPE_DRAFTS("Drafts"),

    PROPS_MAIL_IMAP_HOST("mail.imap.host"),
    PROPS_MAIL_IMAP_PORT("mail.imap.port"),
    PROPS_MAIL_IMAP_SSL("mail.imap.ssl"),
    PROPS_MAIL_IMAP_AUTH("mail.imap.auth"),
    PROPS_MAIL_IMAP_USER("mail.imap.user"),
    PROPS_MAIL_IMAP_PASS("mail.imap.pass"),
    PROPS_MAIL_IMAP_PROTOCOL("mail.store.protocol"),
    PROPS_MAIL_IMAP_PROTOCOL_VALUE("imap"),

    PROPS_MAIL_IMAP_SOCKET_SSL("javax.net.ssl.SSLSocketFactory"),
    PROPS_MAIL_IMAP_SOCKET_CLASS("mail.imap.socketFactory.class"),

    EMAIL_MULTIPART_INFO("multipart/*"),

    PROPS_MAIL_SMTP_HOST("mail.smtp.host"),
    PROPS_MAIL_SMTP_PORT("mail.smtp.port"),
    PROPS_MAIL_SMTP_AUTH("mail.smtp.auth"),
    PROPS_MAIL_SMTP_USER("mail.smtp.user"),
    PROPS_MAIL_SMTP_PASS("mail.smtp.pass"),
    PROPS_MAIL_SMTP_STARTTLS("mail.smtp.starttls.enable"),
    PROPS_MAIL_SMTP_SOCKET_SSL("javax.net.ssl.SSLSocketFactory"),
    PROPS_MAIL_SMTP_SOCKET_CLASS("mail.smtp.socketFactory.class"),
    PROPS_MAIL_SMTP_PROTOCOL("mail.transport.protocol"),
    PROPS_MAIL_SMTP_PROTOCOL_VALUE("smtp");


    private final String label;

    private MailEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
