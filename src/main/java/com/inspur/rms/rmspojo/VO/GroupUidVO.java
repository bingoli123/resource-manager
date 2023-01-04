package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2022/3/23 3:58 PM
 * @Copyright : 2022 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupUidVO implements Serializable {
    private static final long serialVersionUID = 9085512882336024541L;

    @JsonProperty(value = "GroupUID")
    private String groupUid;
}
