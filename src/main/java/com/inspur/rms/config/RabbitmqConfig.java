package com.inspur.rms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;

/**
 * @author : lidongbin
 * @date : 2021/10/16 2:46 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
//@Configuration
public class RabbitmqConfig {

    // 声明队列
    @Bean
    public Queue queue() {
        return new Queue("manage.monitor.metadata-changed", true); // true表示持久化该队列
    }

    // 声明交互器
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("manage.monitor.metadata-changed");
    }

    // 绑定
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(topicExchange()).with("#");
    }
}
