package com.wh.service.role.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wh.entity.role.WhUserRole;
import com.wh.mapper.WhUserRoleMapper;
import com.wh.service.role.IWhUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author 陈恩惠
 * @since 2019-06-11
 */
@Service
public class WhUserRoleServiceImpl extends ServiceImpl<WhUserRoleMapper, WhUserRole> implements IWhUserRoleService {
    @Autowired
    private WhUserRoleMapper roleMapper;

    @Override
    public WhUserRole serviceSelRids(Long uid) {

        return roleMapper.selRids(uid);
    }
}
