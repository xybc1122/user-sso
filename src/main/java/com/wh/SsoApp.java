package com.wh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @ClassName UserApp
 * Description TODO
 * @Author 陈恩惠
 * @Date 2019/6/11 16:07
 **/
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@MapperScan("com.wh.mapper")
@ServletComponentScan
@EnableFeignClients  //开启 Feign调用
@EnableCircuitBreaker //开启熔断
public class SsoApp {

    public static void main(String[] args) {
        SpringApplication.run(SsoApp.class, args);
    }

}
