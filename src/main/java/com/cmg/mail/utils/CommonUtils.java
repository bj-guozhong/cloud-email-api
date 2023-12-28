package com.cmg.mail.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class CommonUtils {


    public static String generateUniqueIdentifier() {
        // 在这里生成并返回一个唯一的标识符，例如使用UUID
        return java.util.UUID.randomUUID().toString();
    }

    public static File convertMultipartFileToFile(MultipartFile file) throws IOException {
        // 创建临时文件
        File convertedFile = File.createTempFile("temp", null);
        // 将 MultipartFile 文件对象写入临时文件
        file.transferTo(convertedFile);
        return convertedFile;
    }

    public static String converDateFormat(Date inputDate){
        // 定义目标时间字符串的格式
        SimpleDateFormat targetDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 将 Date 对象格式化为目标时间字符串
        return targetDateFormat.format(inputDate);
    }

    //base64转码
    public static String decodeFromAddress(String encodedString) throws UnsupportedEncodingException {
        // Extract the encoded part between "?B?" and "?="
        int startIndex = encodedString.indexOf("?B?") + 3;
        int endIndex = encodedString.lastIndexOf("?=");

        if (startIndex != -1 && endIndex != -1) {
            String encodedPart = encodedString.substring(startIndex, endIndex);
            byte[] decodedBytes = Base64.getDecoder().decode(encodedPart);
            String decodedString = new String(decodedBytes, "UTF-8");

            // Combine the decoded part with the rest of the string
            String restOfString = encodedString.substring(endIndex + 2).trim(); // Skip "?=" and trim
            //return decodedString + " " + restOfString;
            return "\"" + decodedString + "\"" + restOfString + "";
        } else {
            // No encoding found, return the original string
            return encodedString;
        }
    }
}
