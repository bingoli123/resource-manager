package com.inspur.rms.rmspojo.cmspojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 平台
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "platform")
public class Platform implements Serializable {
    private static final long serialVersionUID = -8067979134396009582L;
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JsonProperty(value = "ID")
    private Long id;

    /**
     * 设备code
     */
    @TableField(value = "`uid`")
    @JsonProperty(value = "UID")
    private String uid;

    /**
     * 平台名称
     */
    @TableField(value = "`name`")
    @JsonProperty(value = "Name")
    private String name;

    /**
     * 平台描述
     */
    @TableField(value = "description")
    @JsonProperty(value = "Description")
    private String description;

    /**
     * 平台厂商
     */
    @TableField(value = "manufacturer")
    @JsonProperty(value = "Manufacturer")
    private String manufacturer;

    /**
     * 平台型号
     */
    @TableField(value = "model")
    @JsonProperty(value = "Model")
    private String model;

    /**
     * 连接协议（国标连接、浪潮连接）
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
     * 国标编码
     */
    @TableField(value = "device_id")
    @JsonProperty(value = "DeviceID")
    private String deviceId;

    /**
     * 协议版本
     */
    @TableField(value = "proto_version")
    @JsonProperty(value = "ProtoVersion")
    private String protoVersion;

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
     * 编码规则（国标）
     */
    @TableField(value = "code_rule")
    @JsonProperty(value = "CodeRule")
    private Integer codeRule;


    /**
     * SIP域（国标）
     */
    @TableField(value = "realm")
    @JsonProperty(value = "Realm")
    private String realm;

    /**
     * 网关
     */
    @TableField(value = "gateway")
    @JsonProperty(value = "Gateway")
    private String gateway;

    /**
     * 媒体模式（国标）
     */
    @TableField(value = "media_mode")
    @JsonProperty(value = "MediaMode")
    private String mediaMode;

    /**
     * 连接模式（浪潮连接）
     */
    @TableField(value = "connect_mode")
    @JsonProperty(value = "ConnectMode")
    private Integer connectMode;

    /**
     * 网络地址（浪潮连接）
     */
    @TableField(value = "ip_addr")
    @JsonProperty(value = "IpAddr")
    private String ipAddr;

    /**
     * 域名（浪潮连接）
     */
    @TableField(value = "domain_name")
    @JsonProperty(value = "DomainName")
    private String domainName;

    /**
     * 端口
     */
    @TableField(value = "port")
    @JsonProperty(value = "Port")
    private Long port;

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
     * 同步状态
     */
    @TableField(value = "sync_status")
    @JsonProperty(value = "SyncStatus")
    private String syncStatus;

    /**
     * 录像状态
     */
    @TableField(value = "recording")
    @JsonProperty(value = "Recording")
    private String recording;

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

    /**
     * 是否鉴权 0-不鉴权 1-鉴权
     */
    @TableField(value = "auth")
    @JsonProperty(value = "Auth")
    private boolean auth;

    /**
     * 添加方式 1编码添加 2ip添加 3域名添加（浪潮连接）
     */
    @TableField(value = "add_method")
    @JsonProperty(value = "AddMethod")
    private Integer addMethod;


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
     * 平台上次同步时间
     */
    @TableField(value = "last_sync_time")
    @JsonProperty(value = "LastSyncTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastSyncTime;


    /**
     * 平台同步时分页的开始序号 如果同步中服务挂掉重启后会继续同步
     */
    @TableField(value = "start_index")
    @JsonProperty(value = "StartIndex")
    private String startIndex;

    /**
     * 平台同步时分页的开始序号 如果同步中服务挂掉重启后会继续同步
     */
    @TableField(value = "end_index")
    @JsonProperty(value = "EndIndex")
    private String endIndex;

    /**
     * 设备离线原因
     */
    @TableField(value = "event_cause")
    @JsonProperty(value = "EventCause")
    private String eventCause;

    public static final String COL_ID = "id";

    public static final String COL_UID = "uid";

    public static final String COL_NAME = "name";

    public static final String COL_DESCRIPTION = "description";

    public static final String COL_MANUFACTURER = "manufacturer";

    public static final String COL_MODEL = "model";

    public static final String COL_PROTOCOL = "protocol";

    public static final String COL_NET_DOMAIN = "net_domain";

    public static final String COL_PROTO_VERSION = "proto_version";

    public static final String COL_USER = "user";

    public static final String COL_PASSWORD = "password";

    public static final String COL_CODE_RULE = "code_rule";

    public static final String COL_DEVICE_ID = "device_id";

    public static final String COL_REALM = "realm";

    public static final String COL_GATEWAY = "gateway";

    public static final String COL_MEDIA_MODE = "media_mode";

    public static final String COL_CONNECT_MODE = "connect_mode";

    public static final String COL_IP_ADDR = "ip_addr";

    public static final String COL_DOMAIN_NAME = "domain_name";

    public static final String COL_PORT = "port";

    public static final String COL_ONLINE = "online";

    public static final String COL_WORK_STATE = "work_state";

    public static final String COL_SYNC_STATUS = "sync_status";

    public static final String COL_RECORDING = "recording";

    public static final String COL_LATITUDE = "latitude";

    public static final String COL_LONGITUDE = "longitude";

    public static final String COL_ALTITUDE = "altitude";

    public static final String COL_SUB_RESOURCE_NUM = "sub_resource_num";

    public static final String COL_IN_MANAGE_NUM = "in_manage_num";

    public static final String COL_PLACE_CODE = "place_code";

    public static final String COL_FULL_ADDRESS = "full_address";

    public static final String COL_VERSION = "version";

    public static final String COL_CREATED_TIME = "created_time";

    public static final String COL_DELETED = "deleted";

    public static final String COL_UPDATED_TIME = "updated_time";
}