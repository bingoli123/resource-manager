package com.inspur.rms.rmspojo.PO;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 树表
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "tree_list")
public class TreeList implements Serializable {
    private static final long serialVersionUID = 2691495859337254825L;
    /**
     * 树uid
     */
    @TableId(value = "tree_id", type = IdType.AUTO)
    @JsonProperty(value = "ID")
    private Long treeId;

    /**
     * 树名称
     */
    @TableField(value = "tree_name")
    @JsonProperty(value = "Name")
    private String treeName;

    /**
     * 树名称
     */
    @TableField(value = "tree_uid")
    @JsonProperty(value = "UID")
    private String treeUid;

    /**
     * 树类型0基本资源树1虚拟资源树
     */
    @TableField(value = "tree_type")
    @JsonProperty(value = "Type")
    private String treeType;

    /**
     * 树描述
     */
    @TableField(value = "tree_desc")
    @JsonProperty(value = "Description")
    private String treeDesc;

    @TableField(value = "tree_status")
    @JsonProperty(value = "Status")
    private boolean treeStatus;

    /**
     * 乐观锁
     */
    @TableField(value = "version", fill = FieldFill.INSERT)
    @JsonProperty(value = "Version")
    @Version
    private Integer version;

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

    public static final String COL_TREE_ID = "tree_id";

    public static final String COL_TREE_NAME = "tree_name";

    public static final String COL_TREE_TYPE = "tree_type";

    public static final String COL_TREE_DESC = "tree_desc";

    public static final String COL_VERSION = "version";

    public static final String COL_CREATED_TIME = "created_time";

    public static final String COL_UPDATED_TIME = "updated_time";

    public static final String COL_DELETED = "deleted";

    public static final String COL_TREE_STATUS = "tree_status";

    public static final String COL_TREE_UID = "tree_uid";
}