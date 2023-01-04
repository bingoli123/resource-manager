package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/15 6:22 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourcePermissionParamDTO implements Serializable {
    private static final long serialVersionUID = -2357293463086282636L;

    @NotBlank(message = "参数不能为空")
    @JsonProperty(value = "ResourcePermissionUID")
    private String resourcePermissionUid;
    @NotEmpty(message = "参数不能为空")
    @JsonProperty(value = "MonitorUIDList")
    private List<String> monitorUidList;
}
