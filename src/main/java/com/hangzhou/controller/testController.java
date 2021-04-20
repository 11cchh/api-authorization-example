package com.hangzhou.controller;

import com.alibaba.fastjson.JSONObject;
import com.hangzhou.annotation.ApiAuthorization;
import com.hangzhou.entity.Node;
import com.hangzhou.utils.HttpClientUtils;
import com.hangzhou.utils.SignatureHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author linchenghui
 * @Date 2021/4/13
 */
@RestController
@RequestMapping("/api")
public class testController {

    private static final Logger logger = LoggerFactory.getLogger(testController.class);

    private static final String appKey = "privateKey";

    /**
     * 测试 @ApiAuthorization 注解的路由能否被拦截并校验签名
     * @return String
     */
    @RequestMapping(value = "/interceptor",method = RequestMethod.POST)
    @ApiAuthorization
    @ResponseBody
    public String testInterceptor(HttpServletRequest request){
        return "hello interceptor";
    }

    /**
     * 校验签名
     * @param request request
     * @return String
     */
    @RequestMapping(value = "/verifySign",method = RequestMethod.POST)
    public String verifySign(HttpServletRequest request){
        Node node = new Node("cjw",new Node("pig",null));
        Map param = JSONObject.parseObject(JSONObject.toJSONString(node),Map.class);
        // 组装请求头
        Map<String, String> canonicalHeaders = SignatureHelper.getCanonicalHeaders("appID", "interceptor", "1.0");
        // 组装签名参数
        Map<String, String> allParams = SignatureHelper.getHeadersAndParams(canonicalHeaders,param);
        logger.info("allParams = {}",allParams);

        String sign = SignatureHelper.sign(allParams, appKey);
        logger.info("sign = {} ",sign);
        // 组装签名
        canonicalHeaders.put("authorization",sign);
        String response = HttpClientUtils.doPost("http://127.0.0.1:18080/api/interceptor", canonicalHeaders, param);
        System.out.println(response);
        return "hello world";
    }
}
