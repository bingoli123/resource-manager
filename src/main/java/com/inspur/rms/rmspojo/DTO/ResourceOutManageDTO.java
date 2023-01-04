package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
public class ResourceOutManageDTO implements Serializable {
    private static final long serialVersionUID = -676989613582642804L;

    //    设备或者媒体的UID
    @NotEmpty(message = "参数不能为空")
    @JsonProperty(value = "ResourceUIDList")
    private List<String> resourceUidList;

    //    资源类型 1设备 2媒体
    @NotNull(message = "参数不能为空")
    @JsonProperty(value = "ResourceType")
    private String resourceType;
}
