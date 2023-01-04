package com.inspur.rms.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inspur.rms.rmspojo.PO.RoleResourceRel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleResourceRelMapper extends BaseMapper<RoleResourceRel> {
    int batchInsert(@Param("list") List<RoleResourceRel> list);
}