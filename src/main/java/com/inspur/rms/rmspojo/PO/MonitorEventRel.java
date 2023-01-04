package com.inspur.rms.rmspojo.PO;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 监控点和事件关系表
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "monitor_event_rel")
public class MonitorEventRel implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 监控点uid
     */
    @TableField(value = "monitor_uid")
    private String monitorUid;

    /**
     * 事件
     */
    @TableField(value = "event")
    private String event;


    @TableField(value = "event_uid")
    private String eventUid;


    @TableField(value = "task_uid")
    private String taskUid;

    @TableField(value = "detail")
    private String detail;

    @TableField(value = "ablity_work_status")
    private String ablityWorkStatus;
    @TableField(value = "message")
    private String message;


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

    private static final long serialVersionUID = 1L;

    public static final String COL_ID = "id";

    public static final String COL_MONITOR_UID = "monitor_uid";

    public static final String COL_EVENT = "event";

    public static final String COL_DELETED = "deleted";

    public static final String COL_VERSION = "version";

    public static final String COL_CREATED_TIME = "created_time";

    public static final String COL_UPDATED_TIME = "updated_time";
}