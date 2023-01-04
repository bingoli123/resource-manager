package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KongPluginsConfig implements Serializable {

    private static final long serialVersionUID = 5135979971698411007L;
    @JsonProperty(value = "hour")
    private int hour;

    @JsonProperty(value = "minute")
    private int minute;

}