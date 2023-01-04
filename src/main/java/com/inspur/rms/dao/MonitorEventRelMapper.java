package com.inspur.rms.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inspur.rms.rmspojo.PO.MonitorEventRel;
import com.inspur.rms.rmspojo.VO.MonitorEventRelGroupByUidVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MonitorEventRelMapper extends BaseMapper<MonitorEventRel> {
    int batchInsert(@Param("list") List<MonitorEventRel> list);

    List<MonitorEventRelGroupByUidVO> getMonitorEventByMonitorUid(@Param("monitorUids") List<String> monitorUids, @Param("algoUid") String algoUid);
}