package com.inspur.rms.rmspojo.PO;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

/**
 * 基本资源树
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "resource_tree")
public class ResourceTree {
    /**
     * 节点code分组是分组uid监控点是监控点uid
     */
    @TableId(value = "group_id", type = IdType.AUTO)
    @JsonProperty(value = "ID")
    private Long groupId;

    /**
     * 树uid
     */
    @TableField(value = "tree_uid")
    @JsonProperty(value = "TreeUID")
    private String treeUid;

    /**
     * 父节点code
     */
    @TableField(value = "parent_id")
    @JsonProperty(value = "ParentId")
    private Long parentId;

    /**
     * 节点路径
     */
    @TableField(value = "group_path")
    @JsonProperty(value = "Path")
    private String groupPath;

    /**
     * 节点uid
     */
    @TableField(value = "group_uid")
    @JsonProperty(value = "UID")
    private String groupUid;

    /**
     * 节点或监控点名称
     */
    @TableField(value = "group_name")
    @JsonProperty(value = "Name")
    private String groupName;

    /**
     * 节点或监控点名称
     */
    @TableField(value = "business_uid")
    @JsonProperty(value = "BussinessUID")
    private String businessUid;

    /**
     * 节点或监控点名称
     */
//    @TableField(value = "group_desc")
//    @JsonProperty(value = "Desc")
//    private String groupDesc;

    /**
     * 节点类型分组或者监控点1分组2监控点
     */
    @TableField(value = "`type`")
    @JsonProperty(value = "Type")
    private String type;

    @TableField(value = "description")
    @JsonProperty(value = "Description")
    private String description;

    @TableField(value = "monitor_uid")
    @JsonProperty(value = "monitorUID")
    private String monitorUid;
    /**
     * 乐观锁
     */
    @TableField(value = "version", fill = FieldFill.INSERT)
    @JsonProperty(value = "Version")
    @Version
    private Integer version;

    /**
     * 纳管目录的批次号
     */
    @TableField(value = "batch_no")
    @JsonProperty(value = "BatchNo")
    private String batchNo;
    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @JsonProperty(value = "CreatedTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    /**
     * 删除状态
     */
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    @JsonProperty(value = "Deleted")
    private Integer deleted;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    @JsonProperty(value = "UpdatedTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedTime;

    /**
     * 设备离线原因
     */
    @TableField(value = "event_cause")
    @JsonProperty(value = "EventCause")
    private String eventCause;

    @TableField(exist = false)
    @JsonProperty(value = "Online")
    private String online;

    public static final String COL_GROUP_ID = "group_id";

    public static final String COL_TREE_ID = "tree_id";

    public static final String COL_PARENT_ID = "parent_id";

    public static final String COL_GROUP_PATH = "group_path";

    public static final String COL_GROUP_NAME = "group_name";

    public static final String COL_TYPE = "type";

    public static final String COL_VERSION = "version";

    public static final String COL_CREATED_TIME = "created_time";

    public static final String COL_DELETED = "deleted";

    public static final String COL_UPDATED_TIME = "updated_time";
}