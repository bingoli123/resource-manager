package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2022/1/21 1:37 PM
 * @Copyright : 2022 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkStatusVO implements Serializable {

    @JsonProperty(value = "Code")
    private String code;

    /**
     * 设备离线原因
     */
    @JsonProperty(value = "Message")
    private String message;
}
