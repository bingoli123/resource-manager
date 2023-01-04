package com.inspur.rms.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inspur.rms.rmspojo.PO.DictionaryData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictionaryDataMapper extends BaseMapper<DictionaryData> {
    int batchInsert(@Param("list") List<DictionaryData> list);
}