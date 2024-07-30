package com.atmiao.wechatdemo.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author miao
 * @version 1.0
 */
public class StreamChangeUtil {
    public static byte[] streamToByteArray(InputStream is) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len;
        while((len = is.read(b)) != -1) {
            bos.write(b,0,len);
        }
        byte[] array = bos.toByteArray();
        bos.close();
        return  array;
    }
    public static String streamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line + "\r\n");
        }
        return stringBuilder.toString();
    }
}
