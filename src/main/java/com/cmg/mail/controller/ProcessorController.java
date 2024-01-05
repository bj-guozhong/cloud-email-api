package com.cmg.mail.controller;


import com.cmg.mail.controller.result.JsonResult;
import com.cmg.mail.services.ConfigService;
import com.cmg.mail.services.ProcessorService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;

@Controller
@RequestMapping("/email")
public class ProcessorController {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProcessorController.class);

    @Autowired
    private ProcessorService processorService;

    @Value("${mail.cctv.username}")
    private String username;

    @Value("${mail.cctv.password}")
    private String password;

    @Autowired
    private ConfigService configService;


    @ApiOperation(value = "删除某封信",httpMethod = "POST")
    @RequestMapping(value = "/deleteEmailById",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult deleteEmailById(HttpServletRequest request, HttpServletResponse response,
                                       @ApiParam(name = "emailId", value = "邮件ID集合", required = true)
                                           @RequestParam(name="emailId",required = true) String[] emailId,
                                      @ApiParam(name = "emailType", value = "邮件类型(收件箱INBOX，草稿箱Drafts，已发送Sent Items，垃圾箱)", required = true,allowableValues = "INBOX,Drafts,Sent Items,Trash")
                                          @RequestParam(name="emailType",required = true) String emailType

                                  ) {
        return processorService.deleteEmailById(username,password,emailId,emailType);
    }

    @ApiOperation(value = "移动某封信或某些信",httpMethod = "POST")
    @RequestMapping(value = "/moveEmailById",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult moveEmailById(HttpServletRequest request, HttpServletResponse response,
                                      @ApiParam(name = "emailId", value = "邮件ID集合", required = true)
                                        @RequestParam(name="emailId",required = true) String[] emailId,
                                      @ApiParam(name = "sourceBox", value = "邮件类型(收件箱INBOX,已发送Sent Items，已删除，垃圾邮件Trash,病毒文件夹Virus Items)", required = true,
                                              allowableValues = "INBOX,Sent Items,Trash,Virus")
                                        @RequestParam(name="sourceBox",required = true) String sourceBox,
                                      @ApiParam(name = "targetBox", value = "邮件类型(收件箱INBOX,已发送Sent Items，已删除，垃圾邮件Trash,病毒文件夹Virus Items)", required = true,
                                              allowableValues = "INBOX,Sent Items,Trash,Virus")
                                        @RequestParam(name="targetBox",required = true) String targetBox
                                    ) {
        return processorService.moveEmailById(username,password,emailId,sourceBox,targetBox);
    }

    @ApiOperation(value = "标记某封信已读|未读|红旗",httpMethod = "POST")
    @RequestMapping(value = "/flagEmailById",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult flagEmailById(HttpServletRequest request, HttpServletResponse response,
                                      @ApiParam(name = "emailId", value = "邮件ID集合", required = true)
                                      @RequestParam(name="emailId",required = true) String[] emailId,
                                      @ApiParam(name = "emailType", value = "邮件类型(收件箱INBOX，草稿箱Drafts，已发送Sent Items，垃圾箱Trash)", required = true,allowableValues = "INBOX,Drafts,Sent Items,Trash")
                                      @RequestParam(name="emailType",required = true) String emailType,
                                    @ApiParam(name = "flag", value = "标记类型,已读SEEN_TRUE，未读SEEN_FALSE,红旗FLAGGED_TRUE,取消红旗FLAGGED_FALSE,紧急URGENT、普通NORMAL、缓慢SLOW、", required = true,allowableValues = "TRUE,FALSE,FLAGGED_TRUE,FLAGGED_FALSE,URGENT,NORMAL,SLOW")
                                        @RequestParam(name="flag",required = true) String flag

    ) {
        return processorService.flagEmailById(username,password,emailId,emailType,flag);
    }

    @ApiOperation(value = "准备转发邮件-",httpMethod = "POST")
    @RequestMapping(value = "/forwarderEmail",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult forwarderEmail(HttpServletRequest request, HttpServletResponse response,
                                    @ApiParam(name = "emailId", value = "邮件ID集合", required = true)
                                        @RequestParam(name="emailId",required = true) String emailId,
                                    @ApiParam(name = "emailType", value = "邮件类型(收件箱INBOX，已发送Sent Items)", required = true,allowableValues = "INBOX,Sent Items")
                                        @RequestParam(name="emailType",required = true) String emailType
                                     ) {
        return processorService.forwarderEmail(username,password,emailId,emailType);
    }

    @ApiOperation(value = "准备转发邮件",httpMethod = "POST")
    @RequestMapping(value = "/replyEmail",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult replyEmail(HttpServletRequest request, HttpServletResponse response,
                                     @ApiParam(name = "emailId", value = "邮件ID集合", required = true)
                                     @RequestParam(name="emailId",required = true) String emailId,
                                     @ApiParam(name = "emailType", value = "邮件类型(收件箱INBOX，已发送Sent Items)", required = true,allowableValues = "INBOX,Sent Items")
                                     @RequestParam(name="emailType",required = true) String emailType
    ) {
        return processorService.replyEmail(username,password,emailId,emailType);
    }


}

