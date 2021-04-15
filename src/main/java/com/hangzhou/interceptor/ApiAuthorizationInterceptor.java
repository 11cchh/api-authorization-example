package com.hangzhou.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.hangzhou.annotation.ApiAuthorization;
import com.hangzhou.utils.SignatureHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Api方法鉴权认证拦截器
 * @Author linchenghui
 * @Date 2021/4/13
 */
@Component
public class ApiAuthorizationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApiAuthorizationInterceptor.class);

    /**
     * 检测调用方法是否含有 @ApiAuthorization 注解
     * @param request request
     * @param response response
     * @param handler 调用方法
     * @return boolean
     * @throws Exception Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean haveAnnotation = handler.getClass().isAssignableFrom(HandlerMethod.class);
        if (haveAnnotation){
            // 得到执行控制器方法的注解检测是否含有 @ApiAuthorization 注解
            ApiAuthorization methodAnnotation = ((HandlerMethod) handler).getMethodAnnotation(ApiAuthorization.class);
            if (methodAnnotation != null){
                logger.info("拦截器检测到接口含有 @ApiAuthorization 注解");
                Map<String, String> headers = new HashMap<>();
                Map<String, String> requestBody = new HashMap<>();
                // 获取请求头参数
                String appID = request.getHeader("appID");
                String timestamp = request.getHeader("timestamp");
                String version = request.getHeader("version");
                String service = request.getHeader("service");
                headers.put("appID",appID);
                headers.put("version",version);
                headers.put("timestamp",timestamp);
                headers.put("service",service);

                if ("POST".equalsIgnoreCase(request.getMethod())){
                    String jsonParam = new RequestWrapper(request).getBodyString();
                    Map paramsMap = JSONObject.parseObject(jsonParam, HashMap.class);
                    if (paramsMap != null && !paramsMap.isEmpty()){
                        requestBody.putAll(paramsMap);
                    }
                }

                // 组装调用放传来的参数进行校验
                requestBody.putAll(headers);

                String sign = request.getHeader("authorization");

                if (sign == null || appID == null || timestamp == null){
                    returnResponse(response);
                    return false;
                }

                // 时间戳与当前时间相差5秒则不通过
                long currentTime = System.currentTimeMillis();
                long oldTime = Long.parseLong(timestamp);
                if((currentTime - oldTime) > 5000){
                    returnResponse(response);
                    return false;
                }

                // todo 校验appID
                if(!"appID".equals(appID)){
                    returnResponse(response);
                    return false;
                }

                // todo 根据appID查询appKey
                boolean flag = SignatureHelper.verify(requestBody, "privateKey", sign);
                if(!flag){
                    returnResponse(response);
                    return false;
                }
                return true;
            }
            return true;
        }
        returnResponse(response);
        return false;
    }

    /**
     * 返回结果信息
     * @param response response
     * @throws IOException IOException
     */
    private void returnResponse(HttpServletResponse response) throws IOException {
        // 重置 response
        response.reset();
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("rtnCode",1);
        jsonObject.put("rtnMsg","签名校验失败");
        writer.write(jsonObject.toString());
    }
}
