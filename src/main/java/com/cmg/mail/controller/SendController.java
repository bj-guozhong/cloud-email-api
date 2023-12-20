package com.cmg.mail.controller;


import com.cmg.mail.controller.result.JsonResult;
import com.cmg.mail.services.SendService;
import com.cmg.mail.services.SentService;
import com.cmg.mail.utils.CommonUtils;
import com.cmg.mail.utils.FileUtils;
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
import java.io.File;

@Controller
@RequestMapping("/email")
public class SendController {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SendController.class);

    @Autowired
    private SendService sendService;

    @Value("${mail.cctv.username}")
    private String username;

    @Value("${mail.cctv.password}")
    private String password;

    @ApiOperation(value = "发送普通文本邮件",httpMethod = "POST")
    @RequestMapping(value = "/sendEmail",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult sendEmail(HttpServletRequest request, HttpServletResponse response,
                                @ApiParam(name = "from", value = "发件人", required = true)
                                  @RequestParam(name="from",required = true) String from,
                                @ApiParam(name = "to", value = "收件人", required = true)
                                    @RequestParam(name="to",required = true) String[] to,
                                @ApiParam(name = "subject", value = "邮件标题", required = true)
                                    @RequestParam(name="subject",required = true) String subject,
                                @ApiParam(name = "content", value = "邮件正文", required = true)
                                @RequestParam(name="content",required = false) String content
                                ) {
        if(to!=null && to.length>100){
            return JsonResult.error("对不起,服务嚣拒绝发送超过100人");
        }else{
            return sendService.sendEmail(username,password,from,to,subject,content);
        }

    }

    @ApiOperation(value = "发送带附件的邮件",httpMethod = "POST")
    @RequestMapping(value = "/sendEmailConAtt",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult sendEmailConAtt(HttpServletRequest request, HttpServletResponse response,
                                      @ApiParam(name = "from", value = "发件人", required = true)
                                          @RequestParam(name="from",required = true) String from,
                                      @ApiParam(name = "to", value = "收件人", required = true)
                                          @RequestParam(name="to",required = true) String[] to,
                                      @ApiParam(name = "subject", value = "邮件标题", required = true)
                                          @RequestParam(name="subject",required = true) String subject,
                                      @ApiParam(name = "content", value = "邮件正文", required = true)
                                          @RequestParam(name="content",required = false) String content,
                                      @ApiParam(name = "files", value = "上传附件(支持多个)", required = true)
                                          @RequestParam("files") MultipartFile[] files
    ) {
        if(to!=null && to.length>100){
            return JsonResult.error("对不起,服务嚣拒绝发送超过100人");
        }else{
            return sendService.sendEmailConAtt(username,password,from,to,subject,content,files);
        }
    }

    @ApiOperation(value = "上传附件(暂时不用此种异步方式)",httpMethod = "POST")
    @RequestMapping(value = "/mailUpload", method = RequestMethod.POST)
    public  @ResponseBody JsonResult mailUpload(@ApiParam(name = "file", value = "上传附件", required = true)
                                 @RequestParam("file") MultipartFile file,
                             HttpServletRequest request, HttpServletResponse response) {

        // 检查文件是否为空
        if (file.isEmpty()) {
            return JsonResult.error("请选择需要添加的附件.");
        }
        try {
            // 获取文件名
            String filename = file.getOriginalFilename();
            // 可选：根据需求进行文件名的处理，例如更改文件名、检查文件类型等
            // 执行文件上传操作，例如将文件保存到磁盘或存储到数据库等
            // 指定保存文件的目录
            String uploadDir = "D://temp";
            // 创建保存文件的目录（如果不存在）
            String attachmentIdentifier  = CommonUtils.generateUniqueIdentifier();
            // 创建文件对象
            File destFile = new File(uploadDir + File.separator + attachmentIdentifier + "_" +filename);
            // 保存文件
            file.transferTo(destFile);
            return JsonResult.success("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.error("上传附件发生异常");
        }
    }
}

