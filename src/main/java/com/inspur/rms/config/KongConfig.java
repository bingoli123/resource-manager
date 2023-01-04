package com.inspur.rms.config;

import com.dtflys.forest.http.ForestResponse;
import com.google.common.collect.Lists;
import com.inspur.rms.api.KongApi;
import com.inspur.rms.rmspojo.DTO.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : lidongbin
 * @date : 2021/9/27 9:43 上午
 * 项目启动时向kong注册service和routers
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Component
@Slf4j
@Order(value = Integer.MAX_VALUE - 5)
public class KongConfig implements CommandLineRunner {

    @Autowired
    private KongApi kongApi;
    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${server.port}")
    private Integer serverPort;


    @Override
    public void run(String... args) throws Exception {
        //
        log.info("开始注册kong");
        KongSerciceCreateDTO kongSerciceCreateDTO = new KongSerciceCreateDTO();
        kongSerciceCreateDTO.setName(serviceName);
        kongSerciceCreateDTO.setRetries(5);
        kongSerciceCreateDTO.setProtocol("http");
        kongSerciceCreateDTO.setHost(serviceName + ".service.consul");
        kongSerciceCreateDTO.setPort(serverPort);
        kongSerciceCreateDTO.setPath("/" + serviceName);
        kongSerciceCreateDTO.setTags(Lists.newArrayList(serviceName, "3.0"));
        kongSerciceCreateDTO.setUrl(kongSerciceCreateDTO.getProtocol() + "://" + kongSerciceCreateDTO.getHost() + ":" + kongSerciceCreateDTO.getPort() + kongSerciceCreateDTO.getPath());
        ForestResponse<Object> res = kongApi.createOrUpdateService(kongSerciceCreateDTO);
        if (res.isSuccess()) {
            log.info("kong service注册完成");
            ArrayList<String> names = Lists.newArrayList(serviceName + "-inner-route", serviceName + "-outer-route");
            ArrayList<String> paths = Lists.newArrayList("/" + serviceName, "/ivideo/" + serviceName);
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                String path = paths.get(i);
                KongRoutesCreateDTO kongRoutesCreateDTO = new KongRoutesCreateDTO();
                kongRoutesCreateDTO.setName(name);
                kongRoutesCreateDTO.setHosts(Lists.newArrayList());
                kongRoutesCreateDTO.setTags(Lists.newArrayList("3.0"));
                kongRoutesCreateDTO.setPaths(Lists.newArrayList(path));
                kongRoutesCreateDTO.setProtocols(Lists.newArrayList("http"));
                ForestResponse<Object> routeres = kongApi.createOrUpdateRoute(kongRoutesCreateDTO, name);
                if (res.isSuccess()) {
                    log.info("route:" + kongRoutesCreateDTO.getName() + "创建成功");
                } else {
                    log.info("route:" + kongRoutesCreateDTO.getName() + "创建失败");
                    throw new Exception("kong route注册失败 ");
                }
            }
            //开始注册插件
            //查询出全部插件
            ForestResponse<KongPluginsDTO> plugins = kongApi.getPlugins(serviceName);
            if (plugins.isSuccess()) {
                KongPluginsDTO result = plugins.getResult();
                List<KongPluginsDataItem> data = result.getData();
                if (data == null || data.isEmpty()) {
                    //    直接注册插件
                    KongPluginCreateDTO kongPluginCreateDTO = new KongPluginCreateDTO();
                    kongPluginCreateDTO.setName("iVideoAuth");
                    kongPluginCreateDTO.setProtocols(Lists.newArrayList("http"));
                    kongPluginCreateDTO.setEnabled(true);
                    ForestResponse<Object> orUpdatePlugin = kongApi.createPlugin(kongPluginCreateDTO, serviceName);
                    if (orUpdatePlugin.isSuccess()) {
                        log.info("service插件创建成功");
                    }
                } else {
                    //    判断是否存在需要注册的插件
                    List<KongPluginsDataItem> collect = data.stream().filter(s -> "iVideoAuth".equals(s.getName())).collect(Collectors.toList());
                    if (collect.isEmpty()) {
                        //    需要注册插件
                        KongPluginCreateDTO kongPluginCreateDTO = new KongPluginCreateDTO();
                        kongPluginCreateDTO.setName("iVideoAuth");
                        kongPluginCreateDTO.setProtocols(Lists.newArrayList("http"));
                        kongPluginCreateDTO.setEnabled(true);
                        ForestResponse<Object> orUpdatePlugin = kongApi.createPlugin(kongPluginCreateDTO, serviceName);
                        if (orUpdatePlugin.isSuccess()) {
                            log.info("route插件创建成功");
                        }
                    } else {
                        //    更新插件
                        //    需要注册插件
                        KongPluginCreateDTO kongPluginCreateDTO = new KongPluginCreateDTO();
                        kongPluginCreateDTO.setName("iVideoAuth");
                        kongPluginCreateDTO.setProtocols(Lists.newArrayList("http"));
                        kongPluginCreateDTO.setEnabled(true);
                        ForestResponse<Object> orUpdatePlugin = kongApi.createOrUpdatePlugin(kongPluginCreateDTO, serviceName, collect.get(0).getId());
                        if (orUpdatePlugin.isSuccess()) {
                            log.info("service插件更新成功");
                        }
                    }
                }
            }
        } else {
            log.error("service注册失败");
            throw new Exception("kong service注册失败");
        }
    }
}
