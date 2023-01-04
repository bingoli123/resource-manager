package com.inspur.rms.rmspojo.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/8/13 4:40 下午
 * 树详情返回参数
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TreeSummaryVO implements Serializable {

    private static final long serialVersionUID = 1341493889499896654L;
    /**
     * 树uid
     */
    @JsonProperty(value = "UID")
    private String treeUid;

    /**
     * 树名称
     */
    @JsonProperty(value = "Name")
    private String treeName;

    /**
     * 树类型0基本资源树1虚拟资源树
     */
    @JsonProperty(value = "Type")
    private String treeType;

    /**
     * 树描述
     */
    @JsonProperty(value = "Description")
    private String treeDesc;

    @JsonProperty(value = "Status")
    private boolean treeStatus;
}
