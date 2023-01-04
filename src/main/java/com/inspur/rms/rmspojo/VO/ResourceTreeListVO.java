package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/9 9:55 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 * 查询资源树列表
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceTreeListVO implements Serializable {

    private static final long serialVersionUID = 1015561964626288915L;

    /**
     * 监控点节点id
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
     * 节点路径
     */
    @JsonProperty(value = "GroupPathName")
    private String groupPathName;

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

//    //    是否全选 0半选 1全选
//    @JsonProperty(value = "All")
//    private Integer all;

    //    是否是资源节点 0否1是
    @JsonProperty(value = "IsAuthNode", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean isAuthNode;


    //    是否还有下级节点 0否1是
    @JsonProperty(value = "HasNodes")
    private Boolean hasNodes;

    @JsonProperty(value = "CatelogUID")
    private String treeUid;

    /**
     * 监控点uid
     */
    @JsonProperty(value = "MonitorUID")
    private String monitorUid;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String online;

    @JsonProperty(value = "Online")
    private OnlineVO online2;

    @JsonProperty(value = "MonitorLabels")
    private List<MonitorLabelItemVO> monitorLabels;
}
