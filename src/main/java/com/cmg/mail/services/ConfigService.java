package com.cmg.mail.services;

import com.cmg.mail.bean.MailEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Properties;

@Service("configService")
public class ConfigService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);

	@Value("${imap.server.address}")
	private String host;
	@Value("${imap.server.port}")
	private String port;
	@Value("${imaps}")
	private String ssl;
	@Value("${smtp.auth}")
	private String auth;

	@Value("${smtp.server.address}")
	private String smtpHost;
	@Value("${smtp.server.port}")
	private String smtpPort;
	@Value("${smtp.auth}")
	private String smtpAuth;
	@Value("${smtps}")
	private String smtpStarttls;

	public Properties config(String username,String password){
		// imap配置，可保存到properties文件，读取
		Properties props = new Properties();
		props.setProperty(MailEnum.PROPS_MAIL_IMAP_HOST.getLabel(), host);
		props.setProperty(MailEnum.PROPS_MAIL_IMAP_PORT.getLabel(), port);
		props.setProperty(MailEnum.PROPS_MAIL_IMAP_SSL.getLabel(), ssl);
		// 需要认证
		props.setProperty(MailEnum.PROPS_MAIL_IMAP_AUTH.getLabel(), auth);
		props.setProperty(MailEnum.PROPS_MAIL_IMAP_USER.getLabel(), username);
		props.setProperty(MailEnum.PROPS_MAIL_IMAP_PASS.getLabel(), password);
		// 使用ssl
		props.put(MailEnum.PROPS_MAIL_IMAP_SOCKET_CLASS.getLabel(), MailEnum.PROPS_MAIL_IMAP_SOCKET_SSL.getLabel());
		props.setProperty(MailEnum.PROPS_MAIL_IMAP_PROTOCOL.getLabel(), MailEnum.PROPS_MAIL_IMAP_PROTOCOL_VALUE.getLabel());
		return props;
	}

	public Properties configSmtp(String username,String password){
		// smtp配置，可保存到properties文件，读取
		Properties props = new Properties();
		props.setProperty(MailEnum.PROPS_MAIL_SMTP_HOST.getLabel(), smtpHost);
		props.setProperty(MailEnum.PROPS_MAIL_SMTP_PORT.getLabel(), smtpPort);
		// 需要认证
		props.setProperty(MailEnum.PROPS_MAIL_SMTP_AUTH.getLabel(), smtpAuth);
		props.setProperty(MailEnum.PROPS_MAIL_SMTP_STARTTLS.getLabel(), smtpStarttls);
		// 使用ssl
		props.put(MailEnum.PROPS_MAIL_SMTP_SOCKET_CLASS.getLabel(), MailEnum.PROPS_MAIL_SMTP_SOCKET_SSL.getLabel());
		props.setProperty(MailEnum.PROPS_MAIL_SMTP_PROTOCOL.getLabel(), MailEnum.PROPS_MAIL_SMTP_PROTOCOL_VALUE.getLabel());
		return props;
	}

	public String getImapHost(){
		return host;
	}

	public String getSmtpHost(){
		return smtpHost;
	}

}
