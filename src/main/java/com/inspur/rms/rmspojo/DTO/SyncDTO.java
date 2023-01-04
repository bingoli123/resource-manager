package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

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
public class SyncDTO implements Serializable {
    private static final long serialVersionUID = 218534318043449589L;

    @JsonProperty(value = "IsForce")
    private Boolean isForce;
}
