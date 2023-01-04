package com.inspur.rms.rmspojo.PO;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色资源关系表
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "role_resource_rel")
public class RoleResourceRel implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    @JsonProperty(value = "ID")
    private Long id;

    /**
     * 角色uid
     */
    @TableField(value = "role_uid")
    @JsonProperty(value = "roleUID")
    private String roleUid;

    /**
     * 节点路径
     */
    @TableField(value = "group_path")
    @JsonProperty(value = "GroupPath")
    private String groupPath;


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

    /**
     * 资源树resource_uid或者monitor_uid
     */
    @TableField(value = "group_uid")
    @JsonProperty(value = "GroupUID")
    private String groupUid;

    private static final long serialVersionUID = 1L;

    public static final String COL_ID = "id";

    public static final String COL_ROLE_UID = "role_uid";

    public static final String COL_GROUP_PATH = "group_path";

    public static final String COL_VERSION = "version";

    public static final String COL_CREATED_TIME = "created_time";

    public static final String COL_UPDATED_TIME = "updated_time";

    public static final String COL_DELETED = "deleted";

    public static final String COL_GROUP_UID = "group_uid";
}