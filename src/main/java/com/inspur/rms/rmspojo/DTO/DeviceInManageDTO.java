package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/1 10:23 上午
 * 设备纳管参数
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceInManageDTO implements Serializable {

    private static final long serialVersionUID = -4858501481490288250L;
    //导入到资源管理的目录uid
    @JsonProperty(value = "TargetUID")
    private String targetGroupUid;
    //    是否导入分组 true 导入 false不导入
    @JsonProperty(value = "ImportGroup")
    private boolean importGroup;
    //    纳管的设备uids
    @JsonProperty(value = "UIDList")
    private List<String> uidList;
}
