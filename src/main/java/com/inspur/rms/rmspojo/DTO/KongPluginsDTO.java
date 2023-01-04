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
public class KongPluginsDTO implements Serializable {

    private static final long serialVersionUID = 6662843877899794159L;
    @JsonProperty(value = "next")
    private String next;

    @JsonProperty(value = "data")
    private List<KongPluginsDataItem> data;

}