/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zhang.lyricdownloader.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 *
 * @author zhang
 */
public class Transcoding {

    private BytesEncodingDetect encode = new BytesEncodingDetect();

    public Transcoding() {
    }

    /**
     * 编码转换
     *
     * @param toCharset 要转换的编码
     * @param path 要转换的文件路径
     * @return
     * @throws Exception
     */
    public String encoding(String toCharset, String path) throws Exception {
        File srcFile = new File(path);
        if(!srcFile.exists())
            return "文件不存在";
        int index = encode.detectEncoding(srcFile);
        String charset = BytesEncodingDetect.javaname[index];
        // 编码相同，无需转码
        if (charset.equalsIgnoreCase(toCharset)) {
            return "编码一样，无需转换";
        }

        InputStream in = new FileInputStream(path);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(in, charset));

        StringBuffer sb = new StringBuffer();
        String s1;
        while ((s1 = br.readLine()) != null) {
            String s = URLEncoder.encode(s1, toCharset);
            sb.append(s + "\r\n");//一行+回车
        }

        br.close();
        srcFile.delete();//删除原来文件
        //重新以新编码写入文件并返回值
        File newfile = new File(path);//重新建原来的文件
        newfile.createNewFile();
        OutputStream out = new FileOutputStream(newfile);
        OutputStreamWriter writer = new OutputStreamWriter(out, toCharset);
        BufferedWriter bw = new BufferedWriter(writer);
        bw.write(URLDecoder.decode(sb.toString(), toCharset));
        String result = URLDecoder.decode(sb.toString(), toCharset);
        bw.flush();//刷到文件中
        bw.close();
        return result;
    }
}
