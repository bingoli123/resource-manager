package com.inspur.rms.config;

import com.inspur.ivideo.common.entity.consulentity.kong.Kong;
import com.inspur.ivideo.common.entity.consulentity.mysql.Mysql;
import com.inspur.ivideo.common.entity.consulentity.rabbitmq.Rabbitmq;
import com.inspur.ivideo.common.entity.consulentity.redis.Redis;
import com.inspur.ivideo.common.utils.JsonUtils;
import com.inspur.rms.ResourceManagerApplication;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.KVCache;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : lidongbin
 * @date : 2022/4/7 10:56 AM
 * @Copyright : 2022 www.inspur.com Inc. All rights reserved.
 */
@Component
@Slf4j
@Order(value = Integer.MAX_VALUE - 3)
public class MiddleareChangeListener implements CommandLineRunner {
    private final Consul consul;
    AtomicInteger start = new AtomicInteger(1);
    public static final String CONSUL_PROPERTY_GAYEWAY = "middleware/";

    public MiddleareChangeListener(Consul consul) {
        this.consul = consul;
    }

    @Value("${server.port}")
    private Integer serverPort;
    @Autowired
    private JsonUtils jsonUtils;

    @Override
    public void run(String... args) throws Exception {
        KeyValueClient keyValueClient = consul.keyValueClient();
        KVCache kvCache = KVCache.newCache(keyValueClient, CONSUL_PROPERTY_GAYEWAY, 2);
        Mysql mysqlProperty = ResourceManagerApplication.DOMAIN_SERVICE_MAP.getMysqlProperty();
        Redis redisProperty = ResourceManagerApplication.DOMAIN_SERVICE_MAP.getRedisProperty();
        Rabbitmq rabbitmqProperty = ResourceManagerApplication.DOMAIN_SERVICE_MAP.getRabbitmqProperty();
        Kong kongProperty = ResourceManagerApplication.DOMAIN_SERVICE_MAP.getKongProperty();
        kvCache.addListener(newValues -> {
            //判断mysql
            if (mysqlProperty != null && StringUtils.isNotBlank(mysqlProperty.getHost())) {
                com.orbitz.consul.model.kv.Value mysqlv = newValues.get("mysql");
                if (mysqlv == null) {
                    killService(serverPort);
                } else {
                    Mysql newMysqlProperty = parseMysql(mysqlv);
                    if (newMysqlProperty == null) {
                        killService(serverPort);
                    } else {
                        //判断mysql参数是否变化
                        if (mysqlProperty.getHost().equals(newMysqlProperty.getHost())
                                && mysqlProperty.getPassword().equals(newMysqlProperty.getPassword())
                                && mysqlProperty.getUsername().equals(newMysqlProperty.getUsername())
                                && mysqlProperty.getPort() == newMysqlProperty.getPort()) {
                        } else {
                            killService(serverPort);
                        }
                    }
                }
            }
            //判断redis
            if (redisProperty != null && StringUtils.isNotBlank(redisProperty.getAddress())) {
                com.orbitz.consul.model.kv.Value redisv = newValues.get("redis");
                if (redisv == null) {
                    log.info("redis配置发生变化，服务停止1");
                    killService(serverPort);
                } else {
                    Redis newRedisProperty = parseRedis(redisv);
                    if (newRedisProperty == null) {
                        log.info("redis配置发生变化，服务停止2");
                        killService(serverPort);
                    } else {
                        //判断mysql参数是否变化
                        if (redisProperty.getAddress().equals(newRedisProperty.getAddress())
                                && redisProperty.getPassword().equals(newRedisProperty.getPassword())) {
                        } else {
                            log.info("redis配置发生变化，服务停止3");
                            killService(serverPort);
                        }
                    }
                }
            }
            //判断rabbitmq
            if (rabbitmqProperty != null && StringUtils.isNotBlank(rabbitmqProperty.getHost())) {
                com.orbitz.consul.model.kv.Value rabbitmqv = newValues.get("rabbitmq");
                if (rabbitmqv == null) {
                    log.info("Rabbitmq配置发生变化，服务停止1");
                    killService(serverPort);
                } else {
                    Rabbitmq newRabbitmqProperty = parseRabbitmq(rabbitmqv);
                    if (newRabbitmqProperty == null) {
                        log.info("Rabbitmq配置发生变化，服务停止2");
                        killService(serverPort);
                    } else {
                        //判断mysql参数是否变化
                        if (rabbitmqProperty.getUsername().equals(newRabbitmqProperty.getUsername())
                                && rabbitmqProperty.getPassword().equals(newRabbitmqProperty.getPassword())
                                && rabbitmqProperty.getHost().equals(newRabbitmqProperty.getHost())
                                && rabbitmqProperty.getPort() == newRabbitmqProperty.getPort()) {
                        } else {
                            log.info("Rabbitmq配置发生变化，服务停止3");
                            killService(serverPort);
                        }
                    }
                }
            }
            //判断kong
            if (kongProperty != null && StringUtils.isNotBlank(kongProperty.getInternal().getHost())) {
                com.orbitz.consul.model.kv.Value kongqv = newValues.get("kong");
                if (kongqv == null) {
                    log.info("kongqv配置发生变化，服务停止1");
                    killService(serverPort);
                } else {
                    Kong newKongProperty = parseKong(kongqv);
                    if (newKongProperty == null) {
                        log.info("kongqv配置发生变化，服务停止2");
                        killService(serverPort);
                    } else {
                        //判断mysql参数是否变化
                        if (newKongProperty.getManage() != null && newKongProperty.getInternal() != null) {
                            if (kongProperty.getManage().getHost().equals(newKongProperty.getManage().getHost())
                                    && kongProperty.getManage().getPort().equals(newKongProperty.getManage().getPort())
                                    && kongProperty.getInternal().getHost().equals(newKongProperty.getInternal().getHost())
                                    && kongProperty.getInternal().getPort().equals(newKongProperty.getInternal().getPort())) {
                            } else {
                                log.info("kongqv配置发生变化，服务停止3");
                                killService(serverPort);
                            }
                        } else {
                            log.info("kongqv配置发生变化，服务停止4");
                            killService(serverPort);
                        }
                    }
                }
            }
        });
        log.info("开始监听LogConfig{} 。。。。", CONSUL_PROPERTY_GAYEWAY);
        kvCache.start();
    }

    public Mysql parseMysql(com.orbitz.consul.model.kv.Value mysqlv) {
        Yaml yaml = new Yaml();
        final String s = mysqlv.getValueAsString().get();
        if (StringUtils.isBlank(s)) {
            killService(serverPort);
        }
        Map<String, Object> newMap = yaml.load(s);
        Mysql newMysqlProperty = jsonUtils.jsonToObject(jsonUtils.objectToJson(newMap), Mysql.class);
        return newMysqlProperty;
    }

    public Redis parseRedis(com.orbitz.consul.model.kv.Value redisv) {
        Yaml yaml = new Yaml();
        final String s = redisv.getValueAsString().get();
        if (StringUtils.isBlank(s)) {
            killService(serverPort);
        }
        Map<String, Object> newMap = yaml.load(s);
        Redis newRedisProperty = jsonUtils.jsonToObject(jsonUtils.objectToJson(newMap), Redis.class);
        return newRedisProperty;
    }

    public Rabbitmq parseRabbitmq(com.orbitz.consul.model.kv.Value rabbitmqv) {
        Yaml yaml = new Yaml();
        final String s = rabbitmqv.getValueAsString().get();
        if (StringUtils.isBlank(s)) {
            killService(serverPort);
        }
        Map<String, Object> newMap = yaml.load(s);
        Rabbitmq newRabbitmqProperty = jsonUtils.jsonToObject(jsonUtils.objectToJson(newMap), Rabbitmq.class);
        return newRabbitmqProperty;
    }

    public Kong parseKong(com.orbitz.consul.model.kv.Value kongv) {
        Yaml yaml = new Yaml();
        final String s = kongv.getValueAsString().get();
        if (StringUtils.isBlank(s)) {
            killService(serverPort);
        }
        Map<String, Object> newMap = yaml.load(s);
        Kong newKongProperty = jsonUtils.jsonToObject(jsonUtils.objectToJson(newMap), Kong.class);
        return newKongProperty;
    }

    public static void killService(Integer serverPort) {
        OkHttpClient httpClient2 = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, "{}");
        Request request2 = new Request.Builder().url("http://localhost:" + serverPort + "/actuator/shutdown").post(requestBody).build();
        Response response2 = null;
        try {
            response2 = httpClient2.newCall(request2).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
