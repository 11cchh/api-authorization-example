package com.hangzhou.utils;

import com.alibaba.fastjson.JSONObject;
import com.hangzhou.entity.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 签名工具类
 * @Author linchenghui
 * @Date 2021/4/13
 */
public class SignatureHelper {

    private static final Logger logger = LoggerFactory.getLogger(SignatureHelper.class);

    /**
     * MD5私钥
     */
    private static final String privateKey = "privateKey";

    /**
     * 将条件字段进行排序并生成签名
     * @param params 参数
     * @param privateKey MD5私钥
     * @return 签名
     */
    public static String sign(Map<String,String> params, String privateKey){
        Properties properties = new Properties();

        Iterator<String> iterator = params.keySet().iterator();
        while (iterator.hasNext()){
            String name = iterator.next();
            Object value = params.get(name);
            properties.setProperty(name,value == null ? "":String.valueOf(value));
        }

        String content = getSignatureContent(properties);
        logger.info("sign content = {}",content);
        String sign;
        sign = signMD5(content,privateKey);
        return sign;
    }

    /**
     * 校验签名
     * @param params 服务端端传来的参数
     * @param privateKey 服务器端从库中查出的key
     * @param sign 客户端传入的签名
     * @return boolean
     */
    public static boolean verify(Map<String,String> params, String privateKey, String sign){
        String mySign = sign(params, privateKey);
        logger.info("服务器端签名 = {},调用方签名 = {}", mySign, sign);
        return mySign.equals(sign);
    }

    /**
     * 将参数按照ascii码进行排序
     * @param properties 参数
     * @return 排序后的参数
     */
    private static String getSignatureContent(Properties properties) {
        StringBuilder content = new StringBuilder();
        List keys = new ArrayList(properties.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            String value = (String) properties.get(key);
            content.append(i == 0 ? "":"&").append(key).append("=").append(value);
        }
        return content.toString();
    }

    /**
     * 加密
     * @param content 参数字段
     * @param privateKey MD5私钥
     * @return 加密后的字段
     */
    private static String signMD5(String content, String privateKey) {
        if (privateKey == null){
            return null;
        }
        String signBefore = content + privateKey;
        logger.debug("signMD5 sign before = {}",signBefore);
        return MD5Encrypt.md5(signBefore);
    }

    /**
     * 将请求头参数和业务参数进行组装
     * @param canonicalHeaders 请求头参数
     * @param param 业务参数
     * @return 组装签名所需参数
     */
    public static Map<String, String> getHeadersAndParams(Map<String, String> canonicalHeaders, Map param) {
        HashMap<String, String> allParams = new HashMap<>();
        allParams.putAll(canonicalHeaders);
        allParams.putAll(param);
        return allParams;
    }

    /**
     * 组装合法的请求头参数
     * @param appID 服务端提供参数
     * @param service 路由
     * @param version 接口版本
     * @return 请求头参数
     */
    public static Map<String,String> getCanonicalHeaders(String appID,String service,String version){
        HashMap<String, String> headers = new HashMap<>(10);
        headers.put("appID",appID);
        headers.put("timestamp",String.valueOf(System.currentTimeMillis()));
        headers.put("service",service);
        headers.put("version",version);
        return headers;
    }

    public static void main(String[] args) {
        // 请求头参数
        Map<String, String> canonicalHeaders = getCanonicalHeaders("appID", "interceptor", "1.0");
        // 模拟业务参数
        Node node = new Node("cjw",new Node("pig",null));
        Map params = JSONObject.parseObject(JSONObject.toJSONString(node),Map.class);
        // 构造签名参数
        Map<String, String> headersAndParams = getHeadersAndParams(canonicalHeaders, params);
        // 加密
        String sign = sign(headersAndParams, privateKey);
        System.out.println(sign);
    }
}
