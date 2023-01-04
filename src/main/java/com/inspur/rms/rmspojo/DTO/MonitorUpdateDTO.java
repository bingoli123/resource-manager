package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorUpdateDTO {

    @JsonProperty(value = "Name")
    @NotBlank(message = "监控点名称不能为空")
    private String monitorName;

    @JsonProperty(value = "Latitude")
    private BigDecimal latitude;

    @JsonProperty(value = "Longitude")
    private BigDecimal longitude;

    @JsonProperty(value = "Altitude")
    private BigDecimal altitude;
    /**
     * 使能能立集
     */
    @JsonProperty(value = "EnabledCapabilities")
    private List<String> enabledCapabilities;

    @JsonProperty(value = "FullAddress")
    private String fullAddress;

    @JsonProperty(value = "PlaceCode")
    private String placeCode;

    @JsonProperty(value = "Description")
    private String description;
}