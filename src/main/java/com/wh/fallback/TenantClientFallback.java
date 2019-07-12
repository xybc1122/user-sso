package com.wh.fallback;

import com.wh.base.ResponseBase;
import com.wh.dto.TenantStateDto;
import com.wh.feign.TenantFeignClient;
import org.springframework.stereotype.Component;

/**
 * @ClassName TenantClientFallback
 * Description TODO
 * @Author 陈恩惠
 * @Date 2019/7/8 16:20
 **/
@Component
public class TenantClientFallback implements TenantFeignClient {
    @Override
    public ResponseBase getTenantList() {
        System.out.println("error");
        return null;
    }

    @Override
    public TenantStateDto selTenantStatus(String tenant) {
        System.out.println("error");
        return null;
    }
}
