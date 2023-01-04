package com.inspur.rms.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inspur.rms.rmspojo.PO.Monitor;
import com.inspur.rms.rmspojo.cmspojo.Device;
import com.inspur.rms.rmspojo.cmspojo.Media;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lidongbin
 */
@Mapper
public interface MonitorMapper extends BaseMapper<Monitor> {
    List<Monitor> findByAll(Monitor monitor);

    int batchInsert(@Param("list") List<Monitor> list);

    List<Device> queryDevice(@Param("protocol") String protocol);

    List<Media> queryMedia();
}
