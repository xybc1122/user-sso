package com.wh.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wh.entity.ParentConfTable;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author 陈恩惠
 * @since 2019-06-11
 */
@TableName("wh_user_info")
public class UserInfo extends ParentConfTable implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId(value = "uid", type = IdType.AUTO)
    private Long uid;

    /**
     * 账号
     */
    @NotBlank(message = "is null")
    private String userName;

    /**
     * 用户密码
     */
    @NotBlank(message = "is null")
    private String pwd;

    /**
     * 租户有效时间/0代表始终有效
     */
    private Long tenantValidityPeriod;

    /**
     * 密码有效期 0为始终有效  非0密码到期会提示修改密码
     */
    private Long pwdValidityPeriod;

    /**
     * 账户状态，被锁定之类的，默认为0，表示正常
     */
    private Integer accountStatus;

    /**
     * 用户名字
     */
    private String name;

    /**
     * 是否首次登录修改密码(0不修改，1修改密码)
     */
    @TableField(value = "is_first_login")
    private Boolean firstLogin;

    /**
     * 最近登陆时间
     */
    private Long landingTime;


    /**
     * 头像图片url
     */
    private String imageUrl;

    /**
     * 租户标识
     */
    @NotBlank(message = "is null")
    private String tenant;
    /**
     * 租户id
     */
    private Integer tId;
    /**
     * 角色id 集合
     */
    @TableField(exist = false)
    private String rids;

    @TableField(exist = false)
    private String roleSign;

    /**
     * 记住我
     */
    @TableField(exist = false)
    private boolean rememberMe;

    public String getRoleSign() {
        return roleSign;
    }

    public void setRoleSign(String roleSign) {
        this.roleSign = roleSign;
    }

    public Integer gettId() {
        return tId;
    }

    public void settId(Integer tId) {
        this.tId = tId;
    }

    public UserInfo() {
    }

    public UserInfo(Long landingTime) {
        this.landingTime = landingTime;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getRids() {
        return rids;
    }

    public void setRids(String rids) {
        this.rids = rids;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public Long getTenantValidityPeriod() {
        return tenantValidityPeriod;
    }

    public void setTenantValidityPeriod(Long tenantValidityPeriod) {
        this.tenantValidityPeriod = tenantValidityPeriod;
    }

    public Long getPwdValidityPeriod() {
        return pwdValidityPeriod;
    }

    public void setPwdValidityPeriod(Long pwdValidityPeriod) {
        this.pwdValidityPeriod = pwdValidityPeriod;
    }

    public Integer getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(Integer accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(Boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    public Long getLandingTime() {
        return landingTime;
    }

    public void setLandingTime(Long landingTime) {
        this.landingTime = landingTime;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
