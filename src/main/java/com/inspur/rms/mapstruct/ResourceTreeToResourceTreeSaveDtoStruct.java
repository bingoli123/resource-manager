package com.inspur.rms.mapstruct;

import com.inspur.ivideo.common.mapstruct.BaseMapperStruts;
import com.inspur.rms.rmspojo.DTO.ResourceTreeSaveDTO;
import com.inspur.rms.rmspojo.PO.ResourceTree;
import org.mapstruct.Mapper;

/**
 * @author : lidongbin
 * @date : 2021/9/17 9:07 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Mapper(componentModel = "spring")
public interface ResourceTreeToResourceTreeSaveDtoStruct extends BaseMapperStruts<ResourceTree, ResourceTreeSaveDTO> {
}
