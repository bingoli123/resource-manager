package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
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
public class MonitorNodeCopyDestinationDTO implements Serializable {
    private static final long serialVersionUID = -7201986443774368486L;


    //    复制到的目的分组树UID
    @NotBlank(message = "参数不能为空")
    @JsonProperty(value = "CateLogUID")
    private String catelogUid;

    //    复制到的目的分组节点uid
    @NotBlank(message = "父节点UID不能为空")
    @JsonProperty(value = "ParentGroupUID")
    private String parentGroupUid;
}
