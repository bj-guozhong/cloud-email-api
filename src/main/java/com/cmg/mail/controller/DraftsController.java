package com.cmg.mail.controller;


import com.cmg.mail.controller.result.JsonResult;
import com.cmg.mail.services.DraftsService;
import com.cmg.mail.services.SentService;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/email")
public class DraftsController {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DraftsController.class);

    @Autowired
    private DraftsService draftsService;

    @Value("${mail.cctv.username}")
    private String username;

    @Value("${mail.cctv.password}")
    private String password;


    @ApiOperation(value = "获取草稿箱列表",httpMethod = "POST")
    @RequestMapping(value = "/draftsEmailsReader",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult draftsEmailsReader(HttpServletRequest request, HttpServletResponse response,
                                         @ApiParam(name = "pageNumber", value = "每页多少条", required = false)
                                             @RequestParam(name="pageNumber",required = false) String pageNumber,
                                         @ApiParam(name = "pageSize", value = "当前页码", required = false)
                                             @RequestParam(name="pageSize",required = false) String pageSize
                                  ) {
        return draftsService.draftsEmailsReader(username,password,pageNumber,pageSize);
    }

    @ApiOperation(value = "保存普通文本邮件到草稿",httpMethod = "POST")
    @RequestMapping(value = "/saveToDrafts",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult saveToDrafts(HttpServletRequest request, HttpServletResponse response,
                                @ApiParam(name = "from", value = "发件人", required = true)
                                    @RequestParam(name="from",required = true) String from,
                                @ApiParam(name = "to", value = "收件人", required = true)
                                    @RequestParam(name="to",required = true) String to,
                                @ApiParam(name = "subject", value = "邮件标题", required = true)
                                    @RequestParam(name="subject",required = true) String subject,
                                @ApiParam(name = "content", value = "邮件正文", required = false)
                                    @RequestParam(name="content",required = false) String content,
                                @ApiParam(name = "emailId", value = "邮件ID", required = false)
                                    @RequestParam(name="emailId",required = false) String emailId
                                   ) {
        return draftsService.saveToDrafts(username,password,from,to,subject,content,emailId);
    }

    @ApiOperation(value = "保存带附件的邮件到草稿",httpMethod = "POST")
    @RequestMapping(value = "/saveFileEmailToDrafts",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult saveFileEmailToDrafts(HttpServletRequest request, HttpServletResponse response,
                                      @ApiParam(name = "from", value = "发件人", required = true)
                                        @RequestParam(name="from",required = true) String from,
                                      @ApiParam(name = "to", value = "收件人", required = true)
                                        @RequestParam(name="to",required = true) String to,
                                      @ApiParam(name = "subject", value = "邮件标题", required = true)
                                        @RequestParam(name="subject",required = true) String subject,
                                      @ApiParam(name = "content", value = "邮件正文", required = true)
                                        @RequestParam(name="content",required = false) String content,
                                      @ApiParam(name = "files", value = "上传附件(支持多个)", required = true)
                                        @RequestParam("files") MultipartFile[] files,
                                      @ApiParam(name = "emailId", value = "邮件ID", required = false)
                                        @RequestParam(name="emailId",required = false) String emailId
    ) {
        return draftsService.saveFileEmailToDrafts(username,password,from,to,subject,content,files,emailId);
    }

    @ApiOperation(value = "删除草稿箱中的附件",httpMethod = "POST,GET")
    @RequestMapping(value = "/deleteAttInDrafts",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public JsonResult deleteAttInDrafts(HttpServletRequest request, HttpServletResponse response,
                                     @ApiParam(name = "emailId", value = "邮件ID", required = true)
                                        @RequestParam(name="emailId",required = true) String emailId,
                                     @ApiParam(name = "fileName", value = "文件名", required = true)
                                        @RequestParam(name="fileName",required = true) String fileName
    ) throws IOException {
        return draftsService.deleteAttInDrafts(username,password,fileName,emailId);
    }

   @ApiOperation(value = "查看草稿箱中某封邮件详情",httpMethod = "POST")
    @RequestMapping(value = "/viewDraftsEmailById",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult viewDraftsEmailById(HttpServletRequest request, HttpServletResponse response,
                                    @ApiParam(name = "emailId", value = "邮件ID", required = true)
                                    @RequestParam(name="emailId",required = true) String emailId
    ) {
        return draftsService.viewDraftsEmailById(emailId,username,password);
    }
}

