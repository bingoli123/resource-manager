package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2022/7/7 15:16
 * @Copyright : 2022 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkStatusDTO implements Serializable {
    private static final long serialVersionUID = 1162005363103050979L;
    @JsonProperty(value = "Code")
    private Integer code;
    @JsonProperty(value = "Message")
    private String message;

}
