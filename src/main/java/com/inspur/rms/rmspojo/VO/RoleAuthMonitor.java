package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/9/10 10:35 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleAuthMonitor implements Serializable {
    private static final long serialVersionUID = 3191855774177715712L;

    @JsonProperty(value = "MonitorUID")
    private String monitorUid;

    //    是否有权限 0否 1是
    @JsonProperty(value = "Auth")
    private Boolean auth;
}
