package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/15 2:25 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorNodeCopySourceDTO implements Serializable {
    private static final long serialVersionUID = -7201986443774368486L;


    //    分组树UID
    @NotBlank(message = "参数不能为空")
    @JsonProperty(value = "CatelogUID")
    private String catelogUid;

    //    复制的分组节点uid
    @JsonProperty(value = "GroupUIDList")
    private List<String> groupUidList;
    //    复制的监控点节点UID
    @JsonProperty(value = "MonitorUIDList")
    private List<String> monitorUidList;
}
