package com.inspur.rms.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inspur.rms.rmspojo.PO.Dictionary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictionaryMapper extends BaseMapper<Dictionary> {
    int batchInsert(@Param("list") List<Dictionary> list);
}