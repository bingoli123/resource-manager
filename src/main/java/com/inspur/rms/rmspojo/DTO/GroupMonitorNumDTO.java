package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2022/7/21 08:45
 * @Copyright : 2022 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMonitorNumDTO implements Serializable {

    @NotEmpty(message = "分组树UID不能为空")
    @JsonProperty(value = "CatelogUID")
    private String catelogUid;

    /**
     * 用户选中的监控点 需要从结果中排除
     */
    @NotEmpty(message = "排除的监控点列表不能为空")
    @JsonProperty(value = "SourceMonitorUID")
    private String sourceMonitorUid;

    @JsonProperty(value = "MonitorUIDList")
    private List<String> monitorUidList;
    @JsonProperty(value = "GroupUIDList")
    private List<String> groupUidList;


}
