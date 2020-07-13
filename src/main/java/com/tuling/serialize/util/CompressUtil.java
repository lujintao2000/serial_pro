package com.tuling.serialize.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 压缩工具类
 * @author lujintao
 * @date 2020-07-13
 */
public class CompressUtil {
    /**
     * 采用GZIP压缩算法对指定的字节数组进行压缩
     * @param content
     * @return  压缩后的字节数组
     */
    public static byte[] compress(byte[] content) {
        if (content == null || content.length == 0) {
            return new byte[]{};
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(content);
            gzip.close();
        } catch ( Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    /**
     * 采用GZIP算法对指定字节数组进行解压
     * @param bytes
     * @return  解压后的字节数组
     */
    public static byte[] uncompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new byte[]{};
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream unGzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = unGzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

}
