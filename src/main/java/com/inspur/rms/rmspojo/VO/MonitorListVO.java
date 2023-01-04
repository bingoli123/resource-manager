package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/8/31 4:02 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorListVO implements Serializable {
    private static final long serialVersionUID = 5144285824425722510L;
    @JsonProperty(value = "Name")
    private String monitorName;
    @JsonProperty(value = "UID")
    private String monitorUid;

    @JsonProperty(value = "Online")
    private String online;

    @JsonProperty(value = "WorkState")
    private String workState;

}
