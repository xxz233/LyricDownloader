/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zhang.lyricdownloader.util;

import java.io.IOException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 *
 * @author Administrator
 */
public class HttpUtil {

    public static String doGet(String url) {
        String responseMsg = "";
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(url);
        //使用系统的默认的恢复策略  
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler());
        try {
            httpClient.executeMethod(getMethod);
            //读取内容  
            byte[] responseBody = getMethod.getResponseBody();
            //处理返回的内容  
            responseMsg = new String(responseBody);
            System.out.println(responseMsg);
        } catch (HttpException e) {
        } catch (IOException e) {
        }

        return responseMsg;
    }
}
