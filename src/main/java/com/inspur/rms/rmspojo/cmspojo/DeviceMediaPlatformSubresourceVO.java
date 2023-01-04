package com.inspur.rms.rmspojo.cmspojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author : lidongbin
 * @date : 2021/11/2 2:41 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceMediaPlatformSubresourceVO implements Serializable {
    private static final long serialVersionUID = -341046527744302476L;
    @JsonProperty(value = "UID")
    private String uid;
    @JsonProperty(value = "Name")
    private String name;
    /**
     * 设备经度
     */
    @JsonProperty(value = "Latitude")
    private BigDecimal latitude;

    /**
     * 设备纬度
     */
    @JsonProperty(value = "Longitude")
    private BigDecimal longitude;
    /**
     * 地理高度
     */
    @JsonProperty(value = "Altitude")
    private BigDecimal altitude;

    /**
     * 行政区划
     */
    @JsonProperty(value = "PlaceCode")
    private String placeCode;

    /**
     * 详细地址
     */
    @JsonProperty(value = "FullAddress")
    private String fullAddress;
    /**
     * 原始能力集
     */
    @JsonProperty(value = "NativeCapabilities")
    private String nativeCapabilities;
}
