package com.wh.entity.role;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wh.entity.ParentConfTable;

import java.io.Serializable;

/**
 * <p>
 * 角色表
 * </p>
 *
 * @author 陈恩惠
 * @since 2019-06-11
 */

public class WhUserRole extends ParentConfTable implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色id
     */
    @TableId(value = "rid", type = IdType.AUTO)
    private Long rid;

    /**
     * 角色名称
     */
    private String rName;

    /**
     * 角色标识
     */
    private String roleSign;

    /**
     * 接收的rids
     */
    @TableField(exist = false)
    private String rIds;


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getrIds() {
        return rIds;
    }

    public void setrIds(String rIds) {
        this.rIds = rIds;
    }

    public Long getRid() {
        return rid;
    }

    public void setRid(Long rid) {
        this.rid = rid;
    }

    public String getrName() {
        return rName;
    }

    public void setrName(String rName) {
        this.rName = rName;
    }

    public String getRoleSign() {
        return roleSign;
    }

    public void setRoleSign(String roleSign) {
        this.roleSign = roleSign;
    }
}
