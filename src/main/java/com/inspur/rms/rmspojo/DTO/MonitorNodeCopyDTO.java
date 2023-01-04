package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/9/15 2:25 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorNodeCopyDTO implements Serializable {
    private static final long serialVersionUID = -7201986443774368486L;

    @JsonProperty(value = "Source")
    @Valid
    @NotNull(message = "参数不能为空")
    private MonitorNodeCopySourceDTO monitorNodeCopySourceDTO;

    @Valid
    @NotNull(message = "参数不能为空")
    @JsonProperty(value = "Destination")
    private MonitorNodeCopyDestinationDTO monitorNodeCopyDestinationDTO;
}
