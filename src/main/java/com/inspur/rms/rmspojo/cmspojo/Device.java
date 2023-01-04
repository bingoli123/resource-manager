package com.inspur.rms.rmspojo.cmspojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 设备表;设备表
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "device")
public class Device implements Serializable {
    private static final long serialVersionUID = 8995646382311643094L;
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JsonProperty(value = "Id")
    private Long id;

    /**
     * 设备code
     */
    @TableField(value = "uid")
    @JsonProperty(value = "UID")
    private String uid;

    /**
     * 设备名称
     */
    @TableField(value = "name")
    @JsonProperty(value = "Name")
    private String name;

    /**
     * 设备描述
     */
    @TableField(value = "description")
    @JsonProperty(value = "Description")
    private String description;

    /**
     * 设备厂商
     */
    @TableField(value = "manufacturer")
    @JsonProperty(value = "Manufacturer")
    private String manufacturer;

    /**
     * 设备型号
     */
    @TableField(value = "model")
    @JsonProperty(value = "Model")
    private String model;

    /**
     * 设备类类别
     */
    @TableField(value = "`type`")
    @JsonProperty(value = "Type")
    private String type;

    /**
     * 详细信息
     */
    @TableField(value = "`more`")
    @JsonProperty(value = "More")
    private String more;

    /**
     * 连接协议（国标、onvif、海康、大华、宇视）
     */
    @TableField(value = "protocol")
    @JsonProperty(value = "Protocol")
    private String protocol;

    /**
     * 所属网域
     */
    @TableField(value = "net_domain")
    @JsonProperty(value = "NetDomain")
    private String netDomain;

    /**
     * 网络地址（onvif、海康、大华、宇视）
     */
    @TableField(value = "ip_addr")
    @JsonProperty(value = "IpAddr")
    private String ipAddr;

    /**
     * 端口
     */
    @TableField(value = "port")
    @JsonProperty(value = "Port")
    private Long port;

    /**
     * 域名
     */
    @TableField(value = "domain_name")
    @JsonProperty(value = "DomainName")
    private String domainName;

    /**
     * 协议版本
     */
    @TableField(value = "prorocol_version")
    @JsonProperty(value = "ProrocolVersion")
    private String prorocolVersion;

    /**
     * 原始能力集
     */
    @TableField(value = "native_capabilities")
    @JsonProperty(value = "NativeCapabilities")
    private String nativeCapabilities;

    /**
     * 用户名
     */
    @TableField(value = "`user`")
    @JsonProperty(value = "User")
    private String user;

    /**
     * 密码
     */
    @TableField(value = "`password`")
    @JsonProperty(value = "Password")
    private String password;

    /**
     * 设备类型 对应同步接口中的设备类别 DeviceType
     */
    @TableField(value = "device_class")
    @JsonProperty(value = "DeviceClass")
    private String deviceClass;

    /**
     * 国标编码（国标）
     */
    @TableField(value = "device_id")
    @JsonProperty(value = "DeviceID")
    private String deviceId;

    /**
     * ip版本
     */
    @TableField(value = "ip_version")
    @JsonProperty(value = "IpVersion")
    private String ipVersion;

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
     * 总通数量
     */
    @TableField(value = "sub_resource_num")
    @JsonProperty(value = "SubResourceNum")
    private Integer subResourceNum;

    /**
     * 已纳管数量
     */
    @TableField(value = "in_manage_num")
    @JsonProperty(value = "InManageNum")
    private Integer inManageNum;

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

    @TableField(exist = false)
    private String inmanageSql;

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
     * 录像状态
     */
    @TableField(value = "recording")
    @JsonProperty(value = "Recording")
    private String recording;


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
     * 网关
     */
    @TableField(value = "gateway")
    @JsonProperty(value = "Gateway")
    private String gateway;

    /**
     * 媒体模式
     */
    @TableField(value = "media_mode")
    @JsonProperty(value = "MediaMode")
    private String mediaMode;
    /**
     *
     */
    @TableField(value = "web_url")
    @JsonProperty(value = "WebURL")
    private String webUrl;

    /**
     *
     */
    @TableField(value = "net_address")
    @JsonProperty(value = "NetAddress")
    private String netAddress;

    //    纳管状态 查询条件
    @TableField(exist = false)
    @JsonProperty(value = "InManaged")
    private Integer inManaged;

    /**
     * 设备离线原因
     */
    @TableField(value = "event_cause")
    @JsonProperty(value = "EventCause")
    private String eventCause;
    /**
     * 是否鉴权 0-不鉴权 1-鉴权
     */
    @TableField(value = "auth")
    @JsonProperty(value = "Auth")
    private Boolean auth;


    public static final String COL_ID = "id";

    public static final String COL_DEVICE_UID = "device_uid";

    public static final String COL_DEVICE_NAME = "device_name";

    public static final String COL_DESCRIPTION = "description";

    public static final String COL_MANUFACTURER = "manufacturer";

    public static final String COL_MODEL = "model";

    public static final String COL_TYPE = "type";

    public static final String COL_MORE = "more";

    public static final String COL_PROTOCOL = "protocol";

    public static final String COL_NET_DOMAIN = "net_domain";

    public static final String COL_IP_ADDR = "ip_addr";

    public static final String COL_PORT = "port";

    public static final String COL_DOMAIN_NAME = "domain_name";

    public static final String COL_PROROCOL_VERSION = "prorocol_version";

    public static final String COL_USER = "user";

    public static final String COL_PASSWORD = "password";

    public static final String COL_DEVICE_CLASS = "device_class";

    public static final String COL_DEVICE_ID = "device_id";

    public static final String COL_ONLINE = "online";

    public static final String COL_WORK_STATUS = "work_status";

    public static final String COL_TOTAL_NUM = "total_num";

    public static final String COL_CASCADE_NUM = "cascade_num";

    public static final String COL_LATITUDE = "latitude";

    public static final String COL_LONGITUDE = "longitude";

    public static final String COL_ALTITUDE = "altitude";

    public static final String COL_PLACE_CODE = "place_code";

    public static final String COL_FULL_ADDRESS = "full_address";

    public static final String COL_RECORDING = "recording";

    public static final String COL_VERSION = "version";

    public static final String COL_CREATED_TIME = "created_time";

    public static final String COL_DELETED = "deleted";

    public static final String COL_UPDATED_TIME = "updated_time";
}