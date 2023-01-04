package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2022/3/9 3:02 PM
 * @Copyright : 2022 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorMetaDataChange implements Serializable {

    private static final long serialVersionUID = 4842683547103758311L;

    @JsonProperty(value = "UID")
    private String uid;
    @JsonProperty(value = "Name")
    private String name;
    @JsonProperty(value = "Latitude")
    private String latitude;
    @JsonProperty(value = "Longitude")
    private String longitude;
    @JsonProperty(value = "Altitude")
    private String altitude;
    @JsonProperty(value = "PlaceCode")
    private String placeCode;
    @JsonProperty(value = "FullAddress")
    private String fullAddress;
    @JsonProperty(value = "Description")
    private String description;
    @JsonProperty(value = "OldPath")
    private String oldpath;
    @JsonProperty(value = "NewPath")
    private String newpath;
    @JsonProperty(value = "ParentID")
    private Long parentId;
}
