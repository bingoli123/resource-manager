package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2022/2/17 8:59 AM
 * @Copyright : 2022 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorLabelItemVO implements Serializable {

    private static final long serialVersionUID = -7103795182429720860L;

    @JsonProperty(value = "Name")
    private String name;

    @JsonProperty(value = "Detail")
    private String detail;

}
