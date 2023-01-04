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
public class ResourceTreeMonitorVO implements Serializable {


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
     * 监控点uid
     */
    @JsonProperty(value = "MonitorUid")
    private String monitorUid;

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

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String online;

    @JsonProperty(value = "ParentName")
    private String parentName;

    @JsonProperty(value = "resource_type", access = JsonProperty.Access.WRITE_ONLY)
    private String resourceType;

    @JsonProperty(value = "businessUID", access = JsonProperty.Access.WRITE_ONLY)
    private String businessUid;

    @JsonProperty(value = "DeviceUid", access = JsonProperty.Access.WRITE_ONLY)
    private String deviceUid;
    @JsonProperty(value = "SubresourceID", access = JsonProperty.Access.WRITE_ONLY)
    private String subresourceId;

    @JsonProperty(value = "RelationResource")
    private RelationResourceVO relationResourceVO;

    /**
     * 设备离线原因
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String eventCause;
    @JsonProperty(value = "Online")
    private OnlineVO online2;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String message;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String ablityWorkStatus;

    @JsonProperty(value = "AblityConfigStatus")
    private ConfigStatusVO configStatusVO;

    @JsonProperty(value = "AblityWorkStatus")
    private WorkStatusVO workStatusVO;


}
