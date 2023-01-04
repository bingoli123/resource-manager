package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/8/31 10:22 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceTreeUpdateDTO implements Serializable {
    private static final long serialVersionUID = -4688818217738485715L;

    /**
     * 节点或监控点名称
     */
    @JsonProperty(value = "Name")
    @NotBlank(message = "目录名称不能为空")
    private String groupName;

    /**
     * 节点或监控点名称
     */
    @JsonProperty(value = "Description")
    private String description;
}
