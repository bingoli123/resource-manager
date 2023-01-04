package com.inspur.rms.service;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspur.ivideo.common.entity.RabbitmqMessage;
import com.inspur.ivideo.common.utils.JsonUtils;
import com.inspur.rms.dao.MonitorEventRelMapper;
import com.inspur.rms.rmspojo.DTO.MonitorEventmqDTO;
import com.inspur.rms.rmspojo.PO.MonitorEventRel;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/12/20 10:09 AM
 * rabbitmq消息监听
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Service
@Slf4j
public class RabbitmqConsumerService {

    private final MonitorEventRelMapper monitorEventRelMapper;
    private final JsonUtils jsonUtils;
    private final ObjectMapper objectMapper;

    @Autowired
    public RabbitmqConsumerService(MonitorEventRelMapper monitorEventRelMapper, JsonUtils jsonUtils, ObjectMapper objectMapper) {
        this.monitorEventRelMapper = monitorEventRelMapper;
        this.jsonUtils = jsonUtils;
        this.objectMapper = objectMapper;
    }

    /**
     * 监听监控点和事件 任务的绑定关系
     *
     * @param message
     * @param channel
     * @throws Exception
     * @RabbitListener @QueueBinding @Queue @Exchange
     */
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "manage.monitor.status-changed", durable = "true"),
                    exchange = @Exchange(name = "manage.monitor.status-changed",
                            durable = "true",
                            type = ExchangeTypes.TOPIC,
                            ignoreDeclarationExceptions = "true"),
                    key = "#")}
    )
    @RabbitHandler
    public void onMessage(Message message, Channel channel) {
        MDC.put("traceId", UUID.randomUUID().toString());
        Long deliveryTag = (Long) message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
        String queue = (String) message.getHeaders().get(AmqpHeaders.CONSUMER_QUEUE);
        byte[] bs = (byte[]) message.getPayload();
        String mes = new String(bs);
        try {
            //根据不同queue的数据使用不同的处理方法 设备 平台 平台下设备 媒体
            RabbitmqMessage<List<MonitorEventmqDTO>> rabbitmqMessage = objectMapper.readValue(mes, new TypeReference<RabbitmqMessage<List<MonitorEventmqDTO>>>() {
            });
            String routingKey = (String) message.getHeaders().get(AmqpHeaders.RECEIVED_ROUTING_KEY);
            log.info("consumer rabbitmq message,message from queue:【{}】，routingKey:【{}】,payload is 【{}】", queue, routingKey, mes);
            List<MonitorEventmqDTO> data1 = rabbitmqMessage.getData();
            if (data1 != null && !data1.isEmpty()) {
                if ("VideoConfig".equals(rabbitmqMessage.getSubject())) {
                    //    存储
                    String eventUid = "Video";
                    for (MonitorEventmqDTO monitorEventmqDTO : data1) {
                        if (monitorEventmqDTO.getConfigStatus().equals(Boolean.TRUE)) {
                            //    判断是否存在 存在则修改 不存在则新增
                            List<MonitorEventRel> monitorEventRels = monitorEventRelMapper.selectList(Wrappers.<MonitorEventRel>lambdaQuery()
                                    .eq(MonitorEventRel::getMonitorUid, monitorEventmqDTO.getMonitorUid())
                                    .eq(MonitorEventRel::getEventUid, eventUid));
                            if (null != monitorEventRels && !monitorEventRels.isEmpty()) {
                                //    修改
                                MonitorEventRel monitorEventRel = monitorEventRels.get(0);
                                if (monitorEventmqDTO.getWorkStatusDTO() != null) {
                                    monitorEventRel.setAblityWorkStatus(String.valueOf(monitorEventmqDTO.getWorkStatusDTO().getCode()));
                                    monitorEventRel.setMessage(monitorEventmqDTO.getWorkStatusDTO().getMessage());
                                }
                                monitorEventRel.setDetail(jsonUtils.objectToJson(monitorEventmqDTO));
                                monitorEventRelMapper.updateById(monitorEventRel);
                            } else {
                                //    新增
                                MonitorEventRel monitorEventRel = MonitorEventRel.builder()
                                        .eventUid(eventUid)
                                        .monitorUid(monitorEventmqDTO.getMonitorUid())
                                        .build();
                                if (monitorEventmqDTO.getWorkStatusDTO() != null) {
                                    monitorEventRel.setAblityWorkStatus(String.valueOf(monitorEventmqDTO.getWorkStatusDTO().getCode()));
                                    monitorEventRel.setMessage(monitorEventmqDTO.getWorkStatusDTO().getMessage());
                                }
                                monitorEventRel.setDetail(jsonUtils.objectToJson(monitorEventmqDTO));
                                monitorEventRelMapper.insert(monitorEventRel);
                            }
                        } else {
                            //    删除配置的任务状态
                            monitorEventRelMapper.delete(Wrappers.<MonitorEventRel>lambdaQuery()
                                    .eq(MonitorEventRel::getMonitorUid, monitorEventmqDTO.getMonitorUid())
                                    .eq(MonitorEventRel::getEventUid, eventUid));
                        }
                    }
                } else {
                    //算法任务
                    for (MonitorEventmqDTO monitorEventmqDTO : data1) {
                        if (monitorEventmqDTO.getTaskStatus() == 1) {
                            //    启用事件
                            MonitorEventRel monitorEventRel = MonitorEventRel.builder()
                                    .eventUid(monitorEventmqDTO.getAlgorithmUid())
                                    .taskUid(monitorEventmqDTO.getTaskUid())
                                    .monitorUid(monitorEventmqDTO.getMonitorUid())
                                    .detail(jsonUtils.objectToJson(monitorEventmqDTO))
                                    .build();
                            monitorEventRelMapper.insert(monitorEventRel);
                        } else {
                            //    删除事件关系
                            if (StringUtils.isNotBlank(monitorEventmqDTO.getMonitorUid()) && StringUtils.isNotBlank(monitorEventmqDTO.getTaskUid())) {
                                monitorEventRelMapper.delete(Wrappers.<MonitorEventRel>lambdaQuery()
                                        .eq(MonitorEventRel::getMonitorUid, monitorEventmqDTO.getMonitorUid())
                                        .eq(MonitorEventRel::getEventUid, monitorEventmqDTO.getAlgorithmUid()));
                            }
                        }
                    }
                }
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("消息消费失败--{}", mes);
            log.error(e.toString());
            e.printStackTrace();
            try {
                //channel.basicNack(deliveryTag, false, true);
                channel.basicAck(deliveryTag, false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
