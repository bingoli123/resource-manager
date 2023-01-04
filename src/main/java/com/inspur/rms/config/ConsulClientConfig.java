package com.inspur.rms.config;

import com.orbitz.consul.Consul;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author : lidongbin
 * @date : 2021/10/28 11:08 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Slf4j
@Component
public class ConsulClientConfig {

    @Value("${spring.cloud.consul.host}")
    private String host;
    @Value("${spring.cloud.consul.port}")
    private Integer port;

    @Bean
    public Consul consul() throws MalformedURLException {
        URL http = new URL("http", host, port, "");
        Consul consul = Consul.builder()
                .withUrl(http)
                .build();
        return consul;
    }
}
