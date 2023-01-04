package com.inspur.rms.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inspur.rms.rmspojo.PO.TreeList;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TreeListMapper extends BaseMapper<TreeList> {
    List<TreeList> findByAll(TreeList treeList);

    int batchInsert(@Param("list") List<TreeList> list);
}