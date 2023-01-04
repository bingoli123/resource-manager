package com.inspur.rms.rmspojo.cmspojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/8/24 11:15 上午
 * 取消纳管 连接管理操作
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSubResourceNumDTO implements Serializable {
    private static final long serialVersionUID = -8273445941053186397L;

    //    需要删除的uid  subresource表中的sub_resource_uid，或者platform_device表中的groupUid
    @JsonProperty(value = "SubResourceUid")
    private String subResourceUid;
    //    ResourceTypeEnum
    @JsonProperty(value = "ResourceType")
    private String resourceType;


}
