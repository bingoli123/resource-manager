package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/8/31 9:27 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceTreeCatelogVO implements Serializable {


    private static final long serialVersionUID = -1340260568769687353L;

    /**
     * 节点code分组是分组uid监控点是监控点uid
     */
    @JsonProperty(value = "ID")
    private Long groupId;


    /**
     * 父节点code
     */
    @JsonProperty(value = "ParentID")
    private Long parentId;

    /**
     * 节点路径
     */
    @JsonProperty(value = "Path")
    private String groupPath;

    /**
     * 节点uid
     */
    @JsonProperty(value = "UID")
    private String groupUid;

    /**
     * 节点或监控点名称
     */
    @JsonProperty(value = "Name")
    private String groupName;

    /**
     * 节点类型分组或者监控点1分组2监控点
     */
    @JsonProperty(value = "Type")
    private String type;

    //目录下的监控点在线数
    @JsonProperty(value = "MonitorOnlineNum")
    private Integer monitorOnlineNum;

    //目录下的监控点总数
    @JsonProperty(value = "MonitorNum")
    private Integer monitorNum;


}
