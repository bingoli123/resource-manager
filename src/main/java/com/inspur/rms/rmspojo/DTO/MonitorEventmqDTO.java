package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/12/21 5:42 PM
 * 监控点绑定事件 mq消息消费实体类
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorEventmqDTO implements Serializable {
    private static final long serialVersionUID = -3380402549081609145L;

    @JsonProperty(value = "AlgorithmUID")
    private String algorithmUid;

    @JsonProperty(value = "TaskUID")
    private String taskUid;

    @JsonProperty(value = "MonitorUid")
    private String monitorUid;

    //启用 1 删除 0
    @JsonProperty(value = "TaskStatus")
    private Integer taskStatus;

    //存储任务相关字段
    @JsonProperty(value = "ConfigStatus")
    private Boolean configStatus;

    @JsonProperty(value = "WorkStatus")
    private WorkStatusDTO workStatusDTO;
}
