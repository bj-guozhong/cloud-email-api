package com.cmg.mail.controller;

import com.cmg.mail.controller.result.JsonResult;
import com.cmg.mail.services.DownAttachmentService;
import com.cmg.mail.services.InboxDetailService;
import com.cmg.mail.services.InboxService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/email")
public class InboxController {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(InboxController.class);

    @Autowired
    private InboxService inboxService;

    @Autowired
    private InboxDetailService inboxDetailService;

    @Autowired
    private DownAttachmentService downAttachmentService;

    @Value("${mail.cctv.username}")
    private String username;

    @Value("${mail.cctv.password}")
    private String password;

    @ApiOperation(value = "邮件认证",httpMethod = "POST")
    @RequestMapping(value = "/checkCredentials",method = RequestMethod.POST)
    public @ResponseBody
    JsonResult checkCredentials(HttpServletRequest request, HttpServletResponse response,
                                @ApiParam(name = "usernameInput", value = "用户名", required = true)
                                        @RequestParam(name="usernameInput",required = true) String usernameInput,
                                @ApiParam(name = "passwordInput", value = "密码", required = true)
                                        @RequestParam(name="passwordInput",required = true) String passwordInput
                           ) {
        return inboxService.auth(usernameInput,passwordInput);
    }

    @ApiOperation(value = "获取收件箱列表",httpMethod = "POST")
    @RequestMapping(value = "/fetchEmails",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult fetchEmails(HttpServletRequest request, HttpServletResponse response,
                                  /*@ApiParam(name = "username", value = "用户名", required = true)
                                        @RequestParam(name="username",required = true) String username,
                                  @ApiParam(name = "password", value = "密码", required = true)
                                        @RequestParam(name="password",required = true) String password,*/
                                  @ApiParam(name = "pageNumber", value = "每页多少条", required = false)
                                        @RequestParam(name="pageNumber",required = false) String pageNumber,
                                  @ApiParam(name = "pageSize", value = "当前页码", required = false)
                                        @RequestParam(name="pageSize",required = false) String pageSize,
                                  @ApiParam(name = "sortType", value = "排序类型(时间升序DATE_ASC、时间降序DATE_DESC、发件人升序FROM_ASC、发件人降序FROM_DESC、主题升序SUBJECT_ASC、主题降序SUBJECT_DESC、邮件大小升序MAIL_SIZE_ASC、邮件大小降序MAIL_SIZE_DESC)", required = false,
                                          allowableValues = "DATE_ASC,DATE_DESC,FROM_ASC,FROM_DESC,SUBJECT_ASC,SUBJECT_DESC,,MAIL_SIZE_ASC,MAIL_SIZE_DESC")
                                      @RequestParam(name="sortType",required = false) String sortType,
                                  @ApiParam(name = "filterName", value = "筛选条件(全部ALL、未读UNREAD、已读READ、已标记FLAGGED、UNFLAGGED、紧急URGENT、普通NORMAL、缓慢SLOW、包含附件WITH_ATT、不含附件NO_ATT、已回复REPLIED、已转发FORWARDED)", required = false,
                                          allowableValues = "ALL,UNREAD,READ,FLAGGED,UNFLAGGED,URGENT,NORMAL,SLOW,WITH_ATT,NO_ATT,REPLIED,FORWARDED")
                                      @RequestParam(name="filterName",required = false) String filterName
                                  ) {
        return inboxService.fetchEmails(username,password,pageNumber,pageSize,sortType,filterName);
    }

    @ApiOperation(value = "查看收件箱中某封邮件详情",httpMethod = "POST")
    @RequestMapping(value = "/showEmailById",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult showEmailById(HttpServletRequest request, HttpServletResponse response,
                                    /*@ApiParam(name = "username", value = "用户名", required = true)
                                        @RequestParam(name="username",required = true) String username,
                                    @ApiParam(name = "password", value = "密码", required = true)
                                        @RequestParam(name="password",required = true) String password,*/
                                    @ApiParam(name = "emailId", value = "邮件ID", required = true)
                                        @RequestParam(name="emailId",required = true) String emailId
    ) {
        return inboxDetailService.viewInboxEmailById(emailId,username,password);
    }

    @ApiOperation(value = "下载附件",httpMethod = "POST,GET")
    @RequestMapping(value = "/downAttachmentById",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Object downAttachmentById(HttpServletRequest request, HttpServletResponse response,
                                     /*@ApiParam(name = "username", value = "用户名", required = true)
                                        @RequestParam(name="username",required = true) String username,
                                     @ApiParam(name = "password", value = "密码", required = true)
                                        @RequestParam(name="password",required = true) String password,*/
                                     @ApiParam(name = "emailId", value = "邮件ID", required = true)
                                        @RequestParam(name="emailId",required = true) String emailId,
                                     @ApiParam(name = "fileName", value = "文件名", required = true)
                                        @RequestParam(name="fileName",required = true) String fileName
                                         ) throws IOException {

        return downAttachmentService.downAttachmentById(emailId,fileName,username,password);
    }

}

