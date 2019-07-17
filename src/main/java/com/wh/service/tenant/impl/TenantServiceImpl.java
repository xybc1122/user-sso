package com.wh.service.tenant.impl;

import com.wh.dds.DynamicDataSource;
import com.wh.dds.DynamicDataSourceContextHolder;
import com.wh.exception.LsException;
import com.wh.service.redis.RedisService;
import com.wh.service.tenant.TenantService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName TenantServiceImpl
 * Description TODO
 * @Author 陈恩惠
 * @Date 2019/7/15 17:06
 **/
@Service
public class TenantServiceImpl implements TenantService {


    @Autowired
    private DynamicDataSource dynamicDataSource;

    @Autowired
    private RedisService redisService;

    @Override
    public void switchTenant(String tenant) {
        //切换租户 如果没有配置此租户
        if (!DynamicDataSourceContextHolder.setDataSourceKey(tenant)) {
            //1 查询redis 拿到租户key
            String stringKey = redisService.getStringKey(RedisService.redisTenantKey(tenant));
            if (StringUtils.isBlank(stringKey)) {
                throw new LsException("没有此租户,切换失败");
            }
            //2 创建一个数据源 并且添加一个租户
            dynamicDataSource.createDatasource(stringKey);

            //3 切换数据源
            DynamicDataSourceContextHolder.setDataSourceKey(tenant);
        }
        //不管
    }
}
