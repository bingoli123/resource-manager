package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/10 8:42 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchParamDTO2 implements Serializable {
    private static final long serialVersionUID = 218534318043449589L;

    @NotEmpty(message = "uidlist不能为空")
    @JsonProperty(value = "UIDList")
    private List<String> uidList;

    @NotNull(message = "状态不能为空")
    @JsonProperty(value = "Status")
    private Integer status;
}
