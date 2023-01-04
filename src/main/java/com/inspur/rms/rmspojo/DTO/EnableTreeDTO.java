package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/9/13 2:09 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnableTreeDTO implements Serializable {
    private static final long serialVersionUID = 358895450536702037L;

    //    true启用 False禁用
    @NotNull(message = "启用禁用状态不能为空")
    @JsonProperty(value = "Value")
    private Boolean value;
}
