package com.wh.interceoter;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName FeignRequestInterceptor
 * Description TODO
 * @Author 陈恩惠
 * @Date 2019/7/10 9:41
 * feign请求拦截器
 * 所有用feign发出的请求的拦截器，注意是feign作为客户端发出请求的，而不是服务端
 **/
@Configuration
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // requestTemplate.header("msClientId", "8888");
        }
    }
}
