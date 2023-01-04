package com.inspur.rms.rmspojo.pmspojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/7 4:33 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleSummaryVO implements Serializable {
    private static final long serialVersionUID = -1978104069032869980L;

    /**
     * 角色名称
     */
    @JsonProperty(value = "Name")
    private String roleName;

    @JsonProperty(value = "UID")
    private String roleUid;


    /**
     * 角色关联的组织树id
     */
    @JsonProperty(value = "CatelogUID")
    private String catelogUid;


    /**
     * 角色描述
     */
    @JsonProperty(value = "Description")
    private String roleDesc;

    @JsonProperty(value = "CatelogName")
    private String catelogName;

    @JsonProperty(value = "Default")
    private boolean systemDefault;

    //    资源权限
    @JsonProperty(value = "ResourcePermissions")
    private List<String> resourcePermissions;

    //    功能权限
    @JsonProperty(value = "FunctionPermissions")
    private List<String> functionPermissions;
}
