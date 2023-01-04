package com.inspur.rms.rmspojo.cmspojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 通道或国标设备或视频源
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "subresource")
public class Subresource implements Serializable {
    private static final long serialVersionUID = -6366145646228925535L;
    /**
     * 通道id
     */
    @TableId(value = "sub_resource_id", type = IdType.AUTO)
    @JsonProperty(value = "SubResourceId")
    private Long subResourceId;

    /**
     * uid
     */
    @TableField(value = "sub_resource_uid")
    @JsonProperty(value = "UID")
    private String uid;

    /**
     * 视频源令牌或国标设备编码
     */
    @TableField(value = "id")
    @JsonProperty(value = "ID")
    private String id;

    /**
     * 通道名称
     */
    @TableField(value = "name")
    @JsonProperty(value = "Name")
    private String name;

    /**
     * 通道描述
     */
    @TableField(value = "description")
    @JsonProperty(value = "Description")
    private String description;

    /**
     * 通道类型
     */
    @TableField(value = "`type`")
    @JsonProperty(value = "Type")
    private String type;

    /**
     * 所属设备code
     */
    @TableField(value = "owner_resource")
    @JsonProperty(value = "OwnerResource")
    private String ownerResource;

    /**
     * 详细信息
     */
    @TableField(value = "`more`")
    @JsonProperty(value = "More")
    private String more;

    /**
     * 格式
     */
    @TableField(value = "format")
    @JsonProperty(value = "Format")
    private Integer format;

    /**
     * 信息
     */
    @TableField(value = "info")
    @JsonProperty(value = "Info")
    private String info;

    /**
     * 设备经度
     */
    @TableField(value = "latitude")
    @JsonProperty(value = "Latitude")
    private BigDecimal latitude;

    /**
     * 设备纬度
     */
    @TableField(value = "longitude")
    @JsonProperty(value = "Longitude")
    private BigDecimal longitude;

    /**
     * 地理高度
     */
    @TableField(value = "altitude")
    @JsonProperty(value = "Altitude")
    private BigDecimal altitude;

    /**
     * 行政区划
     */
    @TableField(value = "place_code")
    @JsonProperty(value = "PlaceCode")
    private String placeCode;

    /**
     * 详细地址
     */
    @TableField(value = "full_address")
    @JsonProperty(value = "FullAddress")
    private String fullAddress;

    /**
     * 在线状态
     */
    @TableField(value = "`online`")
    @JsonProperty(value = "Online")
    private String online;

    /**
     * 原始能力集
     */
    @TableField(value = "native_capabilities")
    @JsonProperty(value = "NativeCapabilities")
    private String nativeCapabilities;

    /**
     * 工作状态
     */
    @TableField(value = "work_state")
    @JsonProperty(value = "WorkState")
    private String workState;

    /**
     * 录像状态
     */
    @TableField(value = "recording")
    @JsonProperty(value = "Recording")
    private String recording;

    /**
     * 资源状态正常、新增、变更、删除
     */
    @TableField(value = "resource_status")
    @JsonProperty(value = "ResourceStatus")
    private String resourceStatus;

    /**
     * 纳管状态0未纳管1已纳管
     */
    @TableField(value = "in_managed")
    @JsonProperty(value = "InManaged")
    private Integer inManaged;


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
     * 设备离线原因
     */
    @TableField(value = "event_cause")
    @JsonProperty(value = "EventCause")
    private String eventCause;

    public static final String COL_SUB_RESOURCE_ID = "sub_resource_id";

    public static final String COL_SUB_RESOURCE_UID = "UID";

    public static final String COL_ID = "id";

    public static final String COL_CHANNEL_NAME = "name";

    public static final String COL_CHANNEL_DESCRIPTION = "description";

    public static final String COL_TYPE = "type";

    public static final String COL_OWNER_RESOURCE = "owner_resource";

    public static final String COL_MORE = "more";

    public static final String COL_FORMAT = "format";

    public static final String COL_INFO = "info";

    public static final String COL_LATITUDE = "latitude";

    public static final String COL_LONGITUDE = "longitude";

    public static final String COL_PLACE_CODE = "place_code";

    public static final String COL_FULL_ADDRESS = "full_address";

    public static final String COL_ONLINE = "online";

    public static final String COL_WORK_STATUS = "work_status";

    public static final String COL_RECORDING = "recording";

    public static final String COL_RESOURCE_STATUS = "resource_status";

    public static final String COL_INMANAGE_STATUS = "inmanage_status";

    public static final String COL_VERSION = "version";

    public static final String COL_CREATED_TIME = "created_time";

    public static final String COL_DELETED = "deleted";

    public static final String COL_UPDATED_TIME = "updated_time";
}