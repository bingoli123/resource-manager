package com.inspur.rms.mapstruct;

import com.inspur.ivideo.common.mapstruct.BaseMapperStruts;
import com.inspur.rms.rmspojo.DTO.ResourceTreeUpdateDTO;
import com.inspur.rms.rmspojo.PO.ResourceTree;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResourceTreeToResourceTreeUpdateDtoStruct extends BaseMapperStruts<ResourceTree, ResourceTreeUpdateDTO> {
}
