package com.wh.feign;

import com.wh.base.ResponseBase;
import com.wh.dto.TenantStateDto;
import com.wh.fallback.TenantClientFallback;
import com.wh.interceoter.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @ClassName PhpFeignClient
 * Description TODO
 * @Author 陈恩惠
 * @Date 2019/6/25 10:33
 **/

//然后再自己控制器里面调用此接口就完成了
@FeignClient(name = "tenant-service", fallback = TenantClientFallback.class, configuration = FeignRequestInterceptor.class)
public interface TenantFeignClient {

    /**
     * 调用租户列表 存入数据库
     *
     * @return
     */
    @GetMapping(value = "/api/v1/tenant/tenantList")
    ResponseBase getTenantList();

    /**
     * 调用租户列表 存入数据库
     *
     * @return
     */
    @GetMapping(value = "/api/v1/tenant/selTenantStatus")
    TenantStateDto selTenantStatus(@RequestParam("tenant") String tenant);

}
