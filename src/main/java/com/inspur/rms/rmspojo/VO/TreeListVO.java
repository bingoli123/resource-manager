package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/8/13 10:45 上午
 * 查询虚拟资源树返回
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TreeListVO implements Serializable {
    private static final long serialVersionUID = 3970351836989605081L;


    @JsonProperty(value = "UID")
    private String treeUid;
    @JsonProperty(value = "Name")
    private String treeName;
    @JsonProperty(value = "Status")
    private boolean treeStatus;
    @JsonProperty(value = "Type")
    private String treeType;
}
