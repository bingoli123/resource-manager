package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KongPluginsDataItem implements Serializable {

    private static final long serialVersionUID = -6924750734772817427L;
    @JsonProperty(value = "route")
    private Object route;

    @JsonProperty(value = "service")
    private Object service;

    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "created_at")
    private int createdAt;

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "protocols")
    private List<String> protocols;

    @JsonProperty(value = "config")
    private KongPluginsConfig config;

    @JsonProperty(value = "consumer")
    private Object consumer;

    @JsonProperty(value = "enabled")
    private boolean enabled;

    @JsonProperty(value = "tags")
    private List<String> tags;

}