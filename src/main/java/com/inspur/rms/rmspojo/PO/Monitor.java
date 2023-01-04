package com.inspur.rms.rmspojo.PO;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 监控点表
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "monitor")
public class Monitor {
    @TableId(value = "monitor_id", type = IdType.AUTO)
    @JsonProperty(value = "ID")
    private Long monitorId;

    /**
     * 节点名称
     */
    @TableField(value = "monitor_name")
    @JsonProperty(value = "Name")
    private String monitorName;

    @TableField(value = "monitor_uid")
    @JsonProperty(value = "UID")
    private String monitorUid;

    /**
     * 纬度
     */
    @TableField(value = "latitude", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "Latitude")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @TableField(value = "longitude", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "Longitude")
    private BigDecimal longitude;

    /**
     * 地理高度
     */
    @TableField(value = "altitude", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "Altitude")
    private BigDecimal altitude;

    /**
     * 使能能立集
     */
    @TableField(value = "enabled_capabilities")
    @JsonProperty(value = "EnabledCapabilities")
    private String enabledCapabilities;

    /**
     * 原始能力集
     */
    @TableField(value = "native_capabilities")
    @JsonProperty(value = "NativeCapabilities")
    private String nativeCapabilities;


    /**
     * 业务id通道或者平台边缘下设备uid
     */
    @TableField(value = "business_uid")
    @JsonProperty(value = "businessUID")
    private String businessUid;

    @TableField(value = "resource_type")
    @JsonProperty(value = "ResourceType")
    private String resourceType;

    @TableField(value = "`online`")
    @JsonProperty(value = "Online")
    private String online;

    @TableField(value = "work_state")
    @JsonProperty(value = "WorkState")
    private String workState;

    @TableField(value = "recording")
    @JsonProperty(value = "Recording")
    private String recording;

    @TableField(value = "full_address", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "FullAddress")
    private String fullAddress;

    @TableField(value = "place_code", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "PlaceCode")
    private String placeCode;

    @TableField(value = "Description")
    @JsonProperty(value = "Description")
    private String description;

    /**
     * 设备离线原因
     */
    @TableField(value = "event_cause")
    @JsonProperty(value = "EventCause")
    private String eventCause;

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
     * 监控点名称是否修改过
     */
    @TableField(value = "monitor_name_update")
    private Boolean monitorNameUpdate;

    @TableField(value = "latitude_update")
    private Boolean latitudeUpdate;

    @TableField(value = "longitude_update")
    private Boolean longitudeUpdate;

    @TableField(value = "altitude_update")
    private Boolean altitudeUpdate;

    @TableField(value = "place_code_update")
    private Boolean placeCodeUpdate;

    @TableField(value = "full_address_update")
    private Boolean fullAddressUpdate;

    @TableField(exist = false)
    private String oldpath;

    @TableField(exist = false)
    private String newpath;
    @TableField(exist = false)
    private Long parentId;

    public static final String COL_MONITOR_ID = "monitor_id";

    public static final String COL_MOITOR_NAME = "moitor_name";

    public static final String COL_MONITOR_UID = "monitor_uid";

    public static final String COL_LATITUDE = "latitude";

    public static final String COL_LONGITUDE = "longitude";

    public static final String COL_ALTITUDE = "altitude";

    public static final String COL_ENABLED_CAPABILITIES = "enabled_capabilities";

    public static final String COL_NATIVE_CAPABILITIES = "native_capabilities";

    public static final String COL_VERSION = "version";

    public static final String COL_CREATED_TIME = "created_time";

    public static final String COL_UPDATED_TIME = "updated_time";

    public static final String COL_DELETED = "deleted";

    public static final String COL_BUSINESS_UID = "business_uid";

    public static final String COL_RESOURCE_TYPE = "resource_type";

    public static final String COL_ONLINE = "online";

    public static final String COL_WORK_STATE = "work_state";

    public static final String COL_RECORDING = "recording";
}