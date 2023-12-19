package com.cmg.mail.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSON;
import com.cmg.mail.services.ConfigService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @Autowired
    private ConfigService demoService;

    @RequestMapping(value = "/index",method = RequestMethod.GET)
    @ResponseBody
    public String index(HttpServletRequest request, HttpServletResponse response) {
        return "hello spring boot index";
    }

    @RequestMapping(value = "/hello",method = RequestMethod.GET)
    @ResponseBody
    public String hello(HttpServletRequest request, HttpServletResponse response) {
        return "hello world";
    }

    @RequestMapping(value = "/findUserByName",method = RequestMethod.GET)
    @ResponseBody
    public String findUser(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam(name="username",required = false) String username) {

        if(StringUtils.isNotEmpty(username)){
            return JSON.toJSONString("result");
        }else{
            return "sorry,please input username.";
        }
    }
}

