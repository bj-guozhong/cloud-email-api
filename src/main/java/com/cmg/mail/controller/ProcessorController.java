package com.cmg.mail.controller;


import com.cmg.mail.controller.result.JsonResult;
import com.cmg.mail.services.ProcessorService;
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

    @ApiOperation(value = "标记某封信",httpMethod = "POST")
    @RequestMapping(value = "/flagEmailById",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult flagEmailById(HttpServletRequest request, HttpServletResponse response,
                                      @ApiParam(name = "emailId", value = "邮件ID集合", required = true)
                                      @RequestParam(name="emailId",required = true) String[] emailId,
                                      @ApiParam(name = "emailType", value = "邮件类型(收件箱INBOX，草稿箱Drafts，已发送Sent Items，垃圾箱)", required = true,allowableValues = "INBOX,Drafts,Sent Items,Trash")
                                      @RequestParam(name="emailType",required = true) String emailType,
                                    @ApiParam(name = "operaType", value = "标记类型", required = true,allowableValues = "SEEN,UNSEEN")
                                        @RequestParam(name="operaType",required = true) String operaType

    ) {
        return processorService.flagEmailById(username,password,emailId,emailType,operaType);
    }

}

