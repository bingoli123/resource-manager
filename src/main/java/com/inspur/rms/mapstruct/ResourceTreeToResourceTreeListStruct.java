package com.inspur.rms.mapstruct;

import com.inspur.ivideo.common.mapstruct.BaseMapperStruts;
import com.inspur.rms.rmspojo.PO.ResourceTree;
import com.inspur.rms.rmspojo.VO.ResourceTreeListVO;
import org.mapstruct.Mapper;

/**
 * @author : lidongbin
 * @date : 2021/9/9 11:23 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Mapper(componentModel = "spring")
public interface ResourceTreeToResourceTreeListStruct extends BaseMapperStruts<ResourceTree, ResourceTreeListVO> {
}
