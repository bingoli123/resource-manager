package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/1 10:23 上午
 * 设备纳管参数
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformFoldInManageDTO implements Serializable {

    private static final long serialVersionUID = -4858501481490288250L;
    //导入到资源管理的目录uid
    @NotBlank(message = "参数不能为空")
    @JsonProperty(value = "ParentGroupUID")
    private String targetGroupUid;
    //    平台目录的uid
    @JsonProperty(value = "UIDList")
    private List<String> uidList;

    //    平台节点uid
    @NotBlank(message = "参数不能为空")
    @JsonProperty(value = "GroupUID")
    private String groupUid;

    //    资源类型
    @NotNull(message = "参数不能为空")
    @JsonProperty(value = "ResourceType")
    private String resourceType;

    //    所属平台yid
    @NotNull(message = "参数不能为空")
    @JsonProperty(value = "ResourceUID")
    private String resourceUid;

    @NotNull(message = "参数不能为空")
    @JsonProperty(value = "ImportGroup")
    private boolean importGroup;

}
