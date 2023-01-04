package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/9/24 3:58 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorOnlineVO implements Serializable {
    private static final long serialVersionUID = 6594972286062868726L;

    @JsonProperty(value = "UID")
    private String monitorUid;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String online;
    @JsonProperty(value = "Online")
    private OnlineVO online2;
}
