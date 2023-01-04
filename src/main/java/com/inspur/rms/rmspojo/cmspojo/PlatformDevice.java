package com.inspur.rms.rmspojo.cmspojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 平台设备表
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "platform_device")
public class PlatformDevice {
    /**
     * 主键
     */
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JsonProperty(value = "subResourceId")
    private Long id;

    /**
     * 平台code
     */
    @TableField(value = "owner_resource")
    @JsonProperty(value = "OwnerResource")
    private String ownerResource;

    /**
     * 节点code
     */
    @TableField(value = "group_uid")
    @JsonProperty(value = "GroupUID")
    private String groupUid;

    /**
     * 父节点code
     */
    @TableField(value = "parent_uid")
    @JsonProperty(value = "ParentUID")
    private String parentUid;

    /**
     * 路径
     */
    @TableField(value = "group_path")
    @JsonProperty(value = "GroupPath")
    private String groupPath;

    /**
     * 国标设备编码
     */
    @TableField(value = "device_id", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "ID")
    private String deviceId;

    /**
     * 节点名称
     */
    @TableField(value = "group_name", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "GroupName")
    private String groupName;

    /**
     * 节点类型（节点 通道）;1组织节点2监控点节点
     */
    @TableField(value = "group_type", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "GroupType")
    private String groupType;


    /**
     * 国标设备或监控点描述
     */
    @TableField(value = "description", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "Description")
    private String description;

    /**
     * 国标设备厂商
     */
    @TableField(value = "manufacturer", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "Manufacturer")
    private String manufacturer;

    /**
     * 国标设备型号
     */
    @TableField(value = "model", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "Model")
    private String model;

    /**
     * 设备类型
     */
    @TableField(value = "device_class", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "DeviceClass")
    private Integer deviceClass;

    /**
     * 国标设备类型
     */
    @TableField(value = "`type`", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "Type")
    private String type;

    /**
     * web地址
     */
    @TableField(value = "web_url", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "WebURL")
    private String webUrl;

    /**
     * 网络地址
     */
    @TableField(value = "net_address", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "NetAddress")
    private String netAddress;

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
     * 高度
     */
    @TableField(value = "altitude", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "Altitude")
    private BigDecimal altitude;

    /**
     * 行政区划
     */
    @TableField(value = "place_code", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "PlaceCode")
    private String placeCode;

    /**
     * 详细地址
     */
    @TableField(value = "full_address", updateStrategy = FieldStrategy.IGNORED)
    @JsonProperty(value = "FullAddress")
    private String fullAddress;

    /**
     * 在线状态
     */
    @TableField(value = "`online`")
    @JsonProperty(value = "Online")
    private String online;

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


    @TableField(value = "save", exist = false)
    @JsonProperty(value = "Save")
    private transient Integer save;

    /**
     * 原生能力集
     */
    @TableField(value = "capabilities")
    @JsonProperty(value = "Capabilities")
    private String capabilities;

    /**
     * 资源状态正常新增变更删除;正常、新增、变更、删除
     */
    @TableField(value = "resource_status")
    @JsonProperty(value = "ResourceStatus")
    private String resourceStatus;

    /**
     * 总设备数量
     */
    @TableField(value = "sub_resource_num")
    @JsonProperty(value = "SubResourceNum")
    private Integer subResourceNum;

    /**
     * 已纳管设备数量
     */
    @TableField(value = "in_manage_num")
    @JsonProperty(value = "InManageNum")
    private Integer inManageNum;

    @TableField(value = "parent_name", exist = false)
    @JsonProperty(value = "parent_name", access = JsonProperty.Access.WRITE_ONLY)
    private String parentName;

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

    public static final String COL_OWNER_RESOURCE = "owner_resource";

    public static final String COL_GROUP_UID = "group_uid";

    public static final String COL_PARENT_UID = "parent_uid";

    public static final String COL_GROUP_PATH = "group_path";

    public static final String COL_GROUP_NAME = "group_name";

    public static final String COL_GROUP_TYPE = "group_type";

    public static final String COL_DEVICE_ID = "device_id";

    public static final String COL_DESCRIPTION = "description";

    public static final String COL_MANUFACTURER = "manufacturer";

    public static final String COL_MODEL = "model";

    public static final String COL_DEVICE_CLASS = "device_class";

    public static final String COL_TYPE = "type";

    public static final String COL_WEB_URL = "web_url";

    public static final String COL_NET_ADDRESS = "net_address";

    public static final String COL_LATITUDE = "latitude";

    public static final String COL_LONGITUDE = "longitude";

    public static final String COL_ALTITUDE = "altitude";

    public static final String COL_PLACE_CODE = "place_code";

    public static final String COL_FULL_ADDRESS = "full_address";

    public static final String COL_ONLINE = "online";

    public static final String COL_WORK_STATE = "work_state";

    public static final String COL_RECORDING = "recording";

    public static final String COL_CAPABILITIES = "capabilities";

    public static final String COL_RESOURCE_STATUS = "resource_status";

    public static final String COL_SUB_RESOURCE_NUM = "sub_resource_num";

    public static final String COL_IN_MANAGE_NUM = "in_manage_num";

    public static final String COL_VERSION = "version";

    public static final String COL_CREATED_TIME = "created_time";

    public static final String COL_DELETED = "deleted";

    public static final String COL_UPDATED_TIME = "updated_time";
}