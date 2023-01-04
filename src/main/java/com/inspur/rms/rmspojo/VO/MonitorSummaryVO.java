package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/3 3:55 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorSummaryVO implements Serializable {
    private static final long serialVersionUID = -6104276153794694903L;
    /**
     * 节点名称
     */
    @JsonProperty(value = "Name")
    private String monitorName;

    @JsonProperty(value = "UID")
    private String monitorUid;

    /**
     * 纬度
     */
    @JsonProperty(value = "Latitude")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @JsonProperty(value = "Longitude")
    private BigDecimal longitude;

    /**
     * 地理高度
     */
    @JsonProperty(value = "Altitude")
    private BigDecimal altitude;

    /**
     * 使能能立集
     */
    @JsonProperty(value = "EnabledCapabilities")
    private List<String> enabledCapabilities;

    /**
     * 原始能力集
     */
    @JsonProperty(value = "NativeCapabilities")
    private List<String> nativeCapabilities;

    /**
     * 业务id通道或者平台边缘下设备uid subresourceuid platformdeviceuid
     */
    @JsonProperty(value = "BusinessUID")
    private String businessUid;

    @JsonProperty(value = "ResourceType")
    private String resourceType;


    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String online;
    @JsonProperty(value = "Online")
    private OnlineVO online2;

    @JsonProperty(value = "WorkState")
    private String workState;

    @JsonProperty(value = "Recording")
    private String recording;

    @JsonProperty(value = "FullAddress")
    private String fullAddress;

    @JsonProperty(value = "PlaceCode")
    private String placeCode;

    @JsonProperty(value = "RelationResource")
    private RelationResourceVO relationResourceVO;

    @JsonProperty(value = "Description")
    private String description;


}
