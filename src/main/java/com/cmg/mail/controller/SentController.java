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
                                           @RequestParam(name="pageSize",required = false) String pageSize
                                  ) {
        return sentService.sentEmailsReader(username,password,pageNumber,pageSize);
    }

}

