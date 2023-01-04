package com.inspur.rms.mapstruct;

import com.inspur.ivideo.common.mapstruct.BaseMapperStruts;
import com.inspur.rms.rmspojo.PO.Monitor;
import com.inspur.rms.rmspojo.VO.MonitorOnlineVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MonitorToMonitorOnlineVoStruct extends BaseMapperStruts<Monitor, MonitorOnlineVO> {
}
