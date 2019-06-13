package com.wh.dto;

/**
 * 用户返回前端视图层
 */
public class UserDto {

    private String uid;

    /**
     * 用户名字
     */
    private String name;

    /**
     * 是否首次登录修改密码(0不修改，1修改密码)
     */
    private Boolean isFirstLogin;

    /**
     * 最近登陆时间
     */
    private Long landingTime;

    /**
     * 头像图片url
     */
    private String imageUrl;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getFirstLogin() {
        return isFirstLogin;
    }

    public void setFirstLogin(Boolean firstLogin) {
        isFirstLogin = firstLogin;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
