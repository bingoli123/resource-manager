package com.inspur.rms.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dtflys.forest.http.ForestResponse;
import com.google.common.collect.Lists;
import com.inspur.ivideo.common.entity.DataItemDTO;
import com.inspur.ivideo.common.entity.DictionaryDTO;
import com.inspur.ivideo.common.entity.GroupsItemDTO;
import com.inspur.rms.api.PublicDictionaryApi;
import com.inspur.rms.dao.DictionaryDataMapper;
import com.inspur.rms.dao.DictionaryMapper;
import com.inspur.rms.rmspojo.PO.Dictionary;
import com.inspur.rms.rmspojo.PO.DictionaryData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author : lidongbin
 * @date : 2021/9/11 5:14 下午
 * 字典表同步
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Component
@Slf4j
@Order(value = Integer.MAX_VALUE - 1)
public class DictionaryAsyncConfig implements CommandLineRunner {

    @Value("${spring.application.name}")
    private String serviceName;

    private final DictionaryMapper dictionaryMapper;

    private final PublicDictionaryApi publicDictionaryApi;

    @Autowired
    private DictionaryDataMapper dictionaryDataMapper;

    public DictionaryAsyncConfig(DictionaryMapper dictionaryMapper, PublicDictionaryApi publicDictionaryApi) {
        this.dictionaryMapper = dictionaryMapper;
        this.publicDictionaryApi = publicDictionaryApi;
    }


    @Override
    public void run(String... args) throws Exception {
        log.info("start sync public dictionary......");
//        1、查询出字典表全部数据
        List<Dictionary> dictionaries = dictionaryMapper.selectList(Wrappers.<Dictionary>lambdaQuery().eq(Dictionary::getOwner, serviceName));
        if (dictionaries == null || dictionaries.isEmpty()) {
            return;
        }
        List<String> groupName = dictionaries.stream().map(Dictionary::getGroupValue).collect(Collectors.toList());
        List<DictionaryData> dictionaryData = dictionaryDataMapper.selectList(Wrappers.<DictionaryData>lambdaQuery().in(DictionaryData::getGroupValue, groupName));
        Map<String, List<DictionaryData>> listMap = Optional.ofNullable(dictionaryData).map(List::stream).orElseGet(Stream::empty).collect(Collectors.groupingBy(DictionaryData::getGroupValue));
        DictionaryDTO dictionaryDTO = new DictionaryDTO();
        List<GroupsItemDTO> groupsItemDTOList = Lists.newArrayList();
        for (Dictionary dictionary : dictionaries) {
            List<DictionaryData> dictionaryDataList = listMap.get(dictionary.getGroupValue());
            GroupsItemDTO groupsItemDTO = GroupsItemDTO.builder()
                    .key(dictionary.getGroupValue())
                    .name(dictionary.getGroupName())
                    .struct(dictionary.getStruct())
                    .owner(dictionary.getOwner())
                    .version(dictionary.getVersion())
                    .scope(dictionary.getScope())
                    .build();
            if (null == dictionaryDataList || dictionaryDataList.isEmpty()) {
                groupsItemDTO.setData(new ArrayList<>());
            } else {
                List<DataItemDTO> dataItemDTOS = Lists.newArrayList();
                for (DictionaryData data : dictionaryDataList) {
                    DataItemDTO dataItemDTO = DataItemDTO.builder()
                            .key(data.getKey())
                            .label(data.getLabel())
                            .build();
                    dataItemDTOS.add(dataItemDTO);
                }
                groupsItemDTO.setData(dataItemDTOS);
            }
            groupsItemDTOList.add(groupsItemDTO);
        }
        dictionaryDTO.setGroups(groupsItemDTOList);
//        3、发送请求
        long i = 1;
        for (; ; ) {
            ForestResponse<Object> dictionarys = publicDictionaryApi.dictionarys(dictionaryDTO);
            if (!dictionarys.isSuccess()) {
                log.error("dictionary sync fail。。。。");
                //    如果发送失败则每次休眠增加1s
                Thread.sleep(i * 1000);
                i = i + 1;
            } else {
                log.info("dictionary sync success。。。。");
                break;
            }
        }

    }
}
