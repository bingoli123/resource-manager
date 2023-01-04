package com.inspur.rms.config;

import com.google.common.collect.Sets;
import com.inspur.rms.ResourceManagerApplication;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.KVCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author : lidongbin
 * @date : 2021/10/28 11:38 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Component
@Slf4j
@Order(value = Integer.MIN_VALUE + 1)
public class LogConfigListener implements CommandLineRunner {
    private final Consul consul;

    public static final String CONSUL_PROPERTY_GAYEWAY = "ivideo/resource-manager/" + ResourceManagerApplication.serviceUid + "/property";

    public LogConfigListener(Consul consul) {
        this.consul = consul;
    }


    @Override
    public void run(String... args) throws Exception {
        KeyValueClient keyValueClient = consul.keyValueClient();
        KVCache kvCache = KVCache.newCache(keyValueClient, CONSUL_PROPERTY_GAYEWAY, 2);
        kvCache.addListener(newValues -> {
            newValues.forEach((k, v) -> {
                //Yaml yaml = new Yaml();
                //final String s = v.getValueAsString().get();
                //Map<String, Object> newMap = yaml.load(s);
                //PropertyConfig propertyConfig = JSONObject.parseObject(JSONObject.toJSONString(newMap), PropertyConfig.class);
                //if (propertyConfig != null && propertyConfig.getLogConfig() != null && propertyConfig.getLogConfig().getLevel() != null) {
                //    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
                //    final Logger root = lc.getLogger("root");
                //    final Logger dtflys = lc.getLogger("com.dtflys");
                //    final Logger ibatis = lc.getLogger("org.apache.ibatis");
                //    final Logger inspur = lc.getLogger("com.inspur");
                //    root.setLevel(Level.toLevel(propertyConfig.getLogConfig().getLevel()));
                //    dtflys.setLevel(Level.toLevel(propertyConfig.getLogConfig().getLevel()));
                //    ibatis.setLevel(Level.toLevel(propertyConfig.getLogConfig().getLevel()));
                //    inspur.setLevel(Level.toLevel(propertyConfig.getLogConfig().getLevel()));
                //}

            });
        });
        log.info("开始监听LogConfig{} 。。。。", CONSUL_PROPERTY_GAYEWAY);
        kvCache.start();
    }

    public static void main(String[] args) {
        Set<String> old = Sets.newHashSet("1", "2", "3", "4");
        Set<String> news = Sets.newHashSet("3", "4", "5", "6");

        System.out.println(Sets.difference(old, news));


    }
}
