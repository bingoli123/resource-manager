package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author : lidongbin
 * @date : 2022/1/26 9:04 AM
 * @Copyright : 2022 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMonitorCoordinateDTO implements Serializable {

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

    @JsonProperty(value = "UID")
    private String monitorUid;
}
