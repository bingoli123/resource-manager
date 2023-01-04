package com.inspur.rms.rmspojo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2022/3/9 3:02 PM
 * @Copyright : 2022 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMetaDataChange implements Serializable {

    private static final long serialVersionUID = 4842683547103758311L;

    @JsonProperty(value = "UID")
    private String groupUid;
    @JsonProperty(value = "Name")
    private String name;
    @JsonProperty(value = "OriginalPath")
    private String originalPath;
    @JsonProperty(value = "Path")
    private String path;
    @JsonProperty(value = "OriginalParentId")
    private Long originalParentId;
    @JsonProperty(value = "ParentId")
    private Long parentId;
}
