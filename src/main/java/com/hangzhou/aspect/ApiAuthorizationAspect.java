package com.hangzhou.aspect;

import com.alibaba.fastjson.JSONObject;
import com.hangzhou.interceptor.RequestWrapper;
import com.hangzhou.utils.SignatureHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Api方法鉴权认证切面
 *  注意:如果项目中有过滤器链时应该注意在链中放行后是否对 inputstream 有过操作,否则会读不到 requestBody 中的数据
 * @Author linchenghui
 * @Date 2021/4/14
 */
@Aspect
@Component
@Slf4j
public class ApiAuthorizationAspect {

    @Around("@annotation(com.hangzhou.annotation.ApiAuthorization)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        System.out.println("切面检测到接口含有 @ApiAuthorization 注解");
        Map<String, String> headers = new HashMap<>();
        Map<String, String> requestBody = new HashMap<>();
        // 获取请求头参数
        String appID = request.getHeader("appID");
        String timestamp = request.getHeader("timestamp");
        String version = request.getHeader("version");
        String service = request.getHeader("service");
        String sign = request.getHeader("authorization");
        headers.put("appID",appID);
        headers.put("version",version);
        headers.put("timestamp",String.valueOf(System.currentTimeMillis()));
        headers.put("service",service);

        // 获取请求体参数
        if ("POST".equalsIgnoreCase(request.getMethod())){
            String jsonParam = new RequestWrapper(request).getBodyString();
            Map paramsMap = JSONObject.parseObject(jsonParam, HashMap.class);
            if (paramsMap != null && !paramsMap.isEmpty()){
                requestBody.putAll(paramsMap);
            }
        }

        // 组装调用放传来的参数进行校验
        requestBody.putAll(headers);

        if (sign == null || appID == null || timestamp == null){
            return "false";
        }

        // 时间戳与当前时间相差5秒则不通过
        long currentTime = System.currentTimeMillis();
        long oldTime = Long.parseLong(timestamp);
        if((currentTime - oldTime) > 5000){
            return "false";
        }

        // todo 校验appID
        if(!"appID".equals(appID)){
            return "false";
        }

        // todo 根据appID查询appKey
        boolean flag = SignatureHelper.verify(requestBody, "privateKey", sign);
        if(!flag){
            return "false";
        }
        // 放行
        Object proceed = point.proceed();
        return proceed.toString();
    }

}
