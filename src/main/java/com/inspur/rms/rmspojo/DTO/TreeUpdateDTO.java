package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/8/13 4:48 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TreeUpdateDTO implements Serializable {

    private static final long serialVersionUID = -5093096568439807464L;

    /**
     * 树名称
     */
    @NotBlank(message = "分组树名称不能为空")
    @JsonProperty(value = "Name")
    private String treeName;

    /**
     * 树描述
     */
    @JsonProperty(value = "Description")
    private String treeDesc;

}
