package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/15 3:17 下午
 * 子资源纳管参数
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubresourceOutManageDTO implements Serializable {
    private static final long serialVersionUID = -676989613582642804L;


    //    设备下通道或者平台下监控点UID
    @NotEmpty(message = "参数不能为空")
    @JsonProperty(value = "SubresourceUIDList")
    private List<String> subresourceUidList;

    //    资源类型 1设备 3边缘 4平台
    @NotNull(message = "参数不能为空")
    @JsonProperty(value = "ResourceType")
    private String resourceType;

    //    资源uid 平台或者设备UID
    @NotBlank(message = "参数不能为空")
    @JsonProperty(value = "ResourceUID")
    private String resourceUid;
}
