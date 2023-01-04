package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2022/2/14 11:10 AM
 * @Copyright : 2022 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorOnlineGroupNameVO implements Serializable {
    private static final long serialVersionUID = 6310608131422740042L;

    @JsonProperty(value = "Online")
    private OnlineVO online;
    @JsonProperty(value = "UID")
    private String monitorUid;
    @JsonProperty(value = "GroupName")
    private String groupName;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String online2;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String message;


}
