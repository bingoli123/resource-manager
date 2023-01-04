package com.inspur.rms.rmspojo.cmspojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 媒体流表
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "media")
public class Media implements Serializable {
    private static final long serialVersionUID = 287770535411921334L;
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
     * 连接协议
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
     * 媒体地址
     */
    @TableField(value = "url")
    @JsonProperty(value = "Url")
    private String url;

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


    /**
     * 纳管状态0未纳管1已纳管
     */
    @TableField(value = "in_managed")
    @JsonProperty(value = "InManaged")
    private Integer inManaged;

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

    public static final String COL_ID = "id";

    public static final String COL_MEDIA_UID = "media_uid";

    public static final String COL_MEDIA_NAME = "media_name";

    public static final String COL_DESCRIPTION = "description";

    public static final String COL_PROTOCOL = "protocol";

    public static final String COL_NET_DOMAIN = "net_domain";

    public static final String COL_URL = "url";

    public static final String COL_ONLINE = "online";

    public static final String COL_WORK_STATUS = "work_status";

    public static final String COL_RECORDING = "recording";

    public static final String COL_CASCADE_NUM = "cascade_num";

    public static final String COL_LATITUDE = "latitude";

    public static final String COL_LONGITUDE = "longitude";

    public static final String COL_ALTITUDE = "altitude";

    public static final String COL_PLACE_CODE = "place_code";

    public static final String COL_FULL_ADDRESS = "full_address";

    public static final String COL_CREATED_TIME = "created_time";

    public static final String COL_DELETED = "deleted";

    public static final String COL_UPDATED_TIME = "updated_time";

    public static final String COL_VERSION = "version";
}