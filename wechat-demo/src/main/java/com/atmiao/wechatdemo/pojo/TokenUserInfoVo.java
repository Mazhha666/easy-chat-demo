package com.atmiao.wechatdemo.pojo;

import java.io.Serializable;

/**
 * @author miao
 * @version 1.0
 */
public class TokenUserInfoVo implements Serializable {
    private static final long  serialVersionUID = -6647650522565023501L;
    private  String userId;
    private String nickName;
    private Integer sex;
    private  Integer joinType;
    private String personalSignature;
    private String areaCode;
    private String token;
    private Boolean admin;
    private Integer contactStatus;
    public void copyFromUserInfo(UserInfo userInfo){
        //也可以反射提取methodName 来赋值
        this.userId = userInfo.getUserId();
        this.nickName = userInfo.getNickName();
        this.sex = userInfo.getSex();
        this.joinType = userInfo.getJoinType();
        this.personalSignature = userInfo.getPersonalSignature();
        this.areaCode = userInfo.getAreaCode();
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getJoinType() {
        return joinType;
    }

    public void setJoinType(Integer joinType) {
        this.joinType = joinType;
    }

    public String getPersonalSignature() {
        return personalSignature;
    }

    public void setPersonalSignature(String personalSignature) {
        this.personalSignature = personalSignature;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Integer getContactStatus() {
        return contactStatus;
    }

    public void setContactStatus(Integer contactStatus) {
        this.contactStatus = contactStatus;
    }
}
