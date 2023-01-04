package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/15 3:17 下午
 * 资源纳管参数
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceInManageDTO implements Serializable {
    private static final long serialVersionUID = -676989613582642804L;
    //    纳管到的基本分组树的节点UID
    @NotBlank(message = "参数不能为空")
    @JsonProperty(value = "ParentGroupUID")
    private String parentGroupUid;
    //    是否导入分组
    @JsonProperty(value = "ImportGroup")
    private boolean importGroup;

    //    设备或者媒体的UID
    @NotEmpty(message = "参数不能为空")
    @JsonProperty(value = "ResourceUIDList")
    private List<String> resourceUidList;

    //    资源类型 1设备 2媒体
    @NotBlank(message = "参数不能为空")
    @JsonProperty(value = "ResourceType")
    private String resourceType;
}
