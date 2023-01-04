package com.inspur.rms.rmspojo.DTO;

/**
 * HTTP协议header签名Authorization对应属性类
 *
 * @author : gaomeirong
 * @date : 2019年4月1日 上午10:42:24
 * @Copyright : 2019 www.inspur.com Inc. All rights reserved.
 */
public class AuthorizationDTO {

    /**
     * 项目标识，默认为VSP
     */
    private String projectTag;

    /**
     * 账号名称
     */
    private String accountName;

    /**
     * 签名
     */
    private String signature;

    /**
     * 解析结果
     */
    private boolean parseSuccess;

    /**
     * 解析失败原因
     */
    private String errorMsg;


    public String getProjectTag() {
        return projectTag;
    }

    public void setProjectTag(String projectTag) {
        this.projectTag = projectTag;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public boolean isParseSuccess() {
        return parseSuccess;
    }

    public void setParseSuccess(boolean parseSuccess) {
        this.parseSuccess = parseSuccess;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }


}
