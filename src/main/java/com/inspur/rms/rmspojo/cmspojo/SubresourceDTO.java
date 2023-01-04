package com.inspur.rms.rmspojo.cmspojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/11/2 2:31 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubresourceDTO implements Serializable {
    private static final long serialVersionUID = 2852165548402629586L;

    @JsonProperty(value = "DeviceSubresource")
    private List<String> deviceSubresource;

    @JsonProperty(value = "MediaSubresource")
    private List<String> mediaSubresource;

    @JsonProperty(value = "EdgeSubresource")
    private List<String> edgeSubresource;

    @JsonProperty(value = "PlatformSubresource")
    private List<String> platformSubresource;
}
