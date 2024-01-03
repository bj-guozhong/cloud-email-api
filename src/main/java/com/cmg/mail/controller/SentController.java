package com.cmg.mail.controller;


import com.cmg.mail.controller.result.JsonResult;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/email")
public class SentController {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SentController.class);

    @Autowired
    private SentService sentService;

    @Value("${mail.cctv.username}")
    private String username;

    @Value("${mail.cctv.password}")
    private String password;

    @ApiOperation(value = "获取已发送邮箱列表",httpMethod = "POST")
    @RequestMapping(value = "/sentEmailsReader",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult sentEmailsReader(HttpServletRequest request, HttpServletResponse response,
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
        return sentService.sentEmailsReader(username,password,pageNumber,pageSize,sortType,filterName);
    }

}

