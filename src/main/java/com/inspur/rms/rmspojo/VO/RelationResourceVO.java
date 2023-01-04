package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/9/13 3:06 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelationResourceVO implements Serializable {
    private static final long serialVersionUID = -1000124526804930656L;

    @JsonProperty(value = "OwnerResourceName")
    private String ownerResourceName;
    @JsonProperty(value = "OwnerResourceUID")
    private String ownerResourceUid;
    @JsonProperty(value = "SubresourceName")
    private String subresourceName;
    @JsonProperty(value = "SubresourceUID")
    private String subresourceUid;

    @JsonProperty(value = "OwnerResourceType")
    private String ownerResourceType;

    //国标编码
    @JsonProperty(value = "DeviceID")
    private String deviceId;

    //端口
    @JsonProperty(value = "Port")
    private Long port;

    //ip地址
    @JsonProperty(value = "IpAddr")
    private String ipAddr;

    //设备协议
    @JsonProperty(value = "Protocol")
    private String protocol;
    /**
     * 媒体地址
     */
    @JsonProperty(value = "Url")
    private String url;
}
