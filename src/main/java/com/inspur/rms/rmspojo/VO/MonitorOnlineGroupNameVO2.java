package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

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
public class MonitorOnlineGroupNameVO2 implements Serializable {
    private static final long serialVersionUID = 6310608131422740042L;
    //access write_only 转成json的时候会过滤掉
    @JsonProperty(value = "Online", access = JsonProperty.Access.WRITE_ONLY)
    private OnlineVO online;
    @JsonProperty(value = "UID")
    private String monitorUid;
    @JsonProperty(value = "GroupName")
    private String groupName;
    @JsonProperty(value = "Online2")
    @JsonIgnore
    private String online2;
    @JsonProperty(value = "Message")
    //@JsonIgnore
    private String message;
    @JsonProperty(value = "Date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;


}
