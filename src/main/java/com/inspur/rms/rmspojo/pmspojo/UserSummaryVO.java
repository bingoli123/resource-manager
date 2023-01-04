package com.inspur.rms.rmspojo.pmspojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryVO implements Serializable {

    private static final long serialVersionUID = -8436670016804010893L;


    @JsonProperty(value = "Name")
    private String userName;

    @JsonProperty(value = "UID")
    private String userUid;

    @JsonProperty(value = "RealName")
    private String realName;

    @JsonProperty(value = "OrganizationName")
    private String organizationName;

    @JsonProperty(value = "OrganizationUID")
    private String organizationUid;

    @JsonProperty(value = "RoleName")
    private String roleName;

    @JsonProperty(value = "RoleUID")
    private String roleUid;

    @JsonProperty(value = "Email")
    private String email;

    @JsonProperty(value = "Level")
    private Integer userLevel;

    @JsonProperty(value = "ExpireTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    @JsonProperty(value = "IDNumber")
    private String idNumber;

    @JsonProperty(value = "Phone")
    private String phone;

    @JsonProperty(value = "Status")
    private boolean userStatus;

    @JsonProperty(value = "OnlineCount")
    private int onlineCount;

    @JsonProperty(value = "LastLoginTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    @JsonProperty(value = "LoginCount")
    private Integer loginCount;


    @JsonProperty(value = "Default")
    private boolean systemDefault;

    @JsonProperty(value = "Description")
    private String userDesc;

    @JsonProperty(value = "MultiLogin")
    private boolean multiLogin;

}