package com.inspur.rms.service;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.inspur.ivideo.common.entity.RabbitmqMessage;
import com.inspur.ivideo.common.utils.JsonUtils;
import com.inspur.ivideo.rabbit.api.MessageType;
import com.inspur.ivideo.rabbit.producer.broker.ProducerClient;
import com.inspur.rms.constant.MonitorMqExchange;
import com.inspur.rms.constant.MonitorMqSubject;
import com.inspur.rms.rmspojo.DTO.GroupMetaDataChange;
import com.inspur.rms.rmspojo.DTO.MonitorMetaDataChange;
import com.inspur.rms.rmspojo.PO.Monitor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * <h1>
 * aaaaaaa
 * </h1>
 * <ol>
 *     <li>时间格式必须为 2022-08-08T11:11:11.000Z 格式 否则无法解析</li>
 *     <li>表中数据无法限制条数 只能设置保留数</li>
 *     <li>kuiper中sql语句 有时不生效</li>
 * </ol>
 *
 * @author : lidongbin
 * @date : 2022/1/25 4:50 PM
 * @Copyright : 2022 www.inspur.com Inc. All rights reserved.
 */
@Service
@Slf4j
public class RabbitMqSendService {

    private ProducerClient producerClient;
    private final JsonUtils jsonUtils;

    @Autowired
    public RabbitMqSendService(ProducerClient producerClient, JsonUtils jsonUtils) {
        this.producerClient = producerClient;
        this.jsonUtils = jsonUtils;
    }

    /**
     * 监控点新增 修改 删除发送相关mq 纳管 脱管 监控点属性修改
     */
    public void monitorMetadataChanged(List<Monitor> monitors, MonitorMqSubject monitorMqSubject) {
        log.info("start send rabbitmq msg");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        if (monitors == null || monitors.isEmpty()) {
            return;
        }
        for (Monitor monitor : monitors) {
            String formatDate = simpleDateFormat.format(new Date(System.currentTimeMillis()));
            String messageId = UUID.fastUUID().toString();
            MonitorMetaDataChange monitorMetaDataChange = new MonitorMetaDataChange();
            monitorMetaDataChange.setUid(monitor.getMonitorUid());
            monitorMetaDataChange.setName(monitor.getMonitorName());
            if (monitor.getLatitude() == null) {
                monitorMetaDataChange.setLatitude(null);
            } else {
                monitorMetaDataChange.setLatitude(String.valueOf(monitor.getLatitude()));
            }
            if (monitor.getLongitude() == null) {
                monitorMetaDataChange.setLongitude(null);
            } else {
                monitorMetaDataChange.setLongitude(String.valueOf(monitor.getLongitude()));
            }
            if (monitor.getAltitude() == null) {
                monitorMetaDataChange.setAltitude(null);
            } else {
                monitorMetaDataChange.setAltitude(String.valueOf(monitor.getAltitude()));
            }
            monitorMetaDataChange.setOldpath(monitor.getOldpath());
            monitorMetaDataChange.setNewpath(monitor.getNewpath());
            monitorMetaDataChange.setParentId(monitor.getParentId());
            monitorMetaDataChange.setPlaceCode(monitor.getPlaceCode());
            monitorMetaDataChange.setFullAddress(monitor.getFullAddress());
            monitorMetaDataChange.setDescription(monitor.getDescription());
            RabbitmqMessage<List<MonitorMetaDataChange>> rabbitmqMessage = RabbitmqMessage.<List<MonitorMetaDataChange>>builder()
                    .specversion("1.0")
                    .type("Manage:Monitor:MetadataChanged")
                    .id(messageId)
                    .source("monitor/" + monitor.getMonitorUid())
                    .subject(monitorMqSubject.getSubject())
                    .traceroute("resource-manager")
                    .time(formatDate)
                    .datacontenttype("application/json")
                    .dataschema("")
                    .data(Lists.newArrayList(monitorMetaDataChange))
                    .build();
            String s = jsonUtils.objectToJson(rabbitmqMessage);
            HashMap map = jsonUtils.jsonToObject(s, HashMap.class);
            com.inspur.ivideo.rabbit.api.Message message = new com.inspur.ivideo.rabbit.api.Message(messageId,
                    MonitorMqExchange.METADATACHANGED.getExchange(),
                    monitorMqSubject.getSubject(),
                    map, 0, MessageType.RAPID);
            producerClient.send(message);
            log.info(s);
            log.info("send msg............");
        }
    }

    /**
     * 监控点分组删除
     */
    public void groupMetaDataChange(List<GroupMetaDataChange> groupMetaDataChanges, MonitorMqSubject monitorMqSubject) {
        log.info("start send rabbitmq msg");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        if (groupMetaDataChanges == null || groupMetaDataChanges.isEmpty()) {
            return;
        }
        for (GroupMetaDataChange groupMetaDataChange : groupMetaDataChanges) {
            String formatDate = simpleDateFormat.format(new Date(System.currentTimeMillis()));
            String messageId = UUID.fastUUID().toString();
            RabbitmqMessage<List<GroupMetaDataChange>> rabbitmqMessage = RabbitmqMessage.<List<GroupMetaDataChange>>builder()
                    .specversion("1.0")
                    .type("Manage:Group:MetadataChanged")
                    .id(messageId)
                    .source("group/" + groupMetaDataChange.getGroupUid())
                    .subject(monitorMqSubject.getSubject())
                    .traceroute("resource-manager")
                    .time(formatDate)
                    .datacontenttype("application/json")
                    .dataschema("")
                    .data(Lists.newArrayList(groupMetaDataChange))
                    .build();
            String s = jsonUtils.objectToJson(rabbitmqMessage);
            HashMap map = jsonUtils.jsonToObject(s, HashMap.class);
            com.inspur.ivideo.rabbit.api.Message message = new com.inspur.ivideo.rabbit.api.Message(messageId,
                    MonitorMqExchange.GROUP_METADATACHANGED.getExchange(),
                    monitorMqSubject.getSubject(),
                    map, 0, MessageType.RAPID);
            producerClient.send(message);
            log.info(s);
            log.info("send msg............");
        }
    }

    //@Scheduled(fixedDelay = 1000)
    public void sendMsg() throws Exception {
        //{"test1":1,"test2":2,"test3":3,"date":"2022-08-03T16:02:05.000Z","date2":1523619204809}
        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                try {
                    DateTime randomDateTime = randomDateTime("2022-08-01T00:00:00.000Z", "2022-08-07T23:59:59.000Z");
                    SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    List uuids = Lists.newArrayList(
                            "093b43f3c49a4699bb43f3c49ae699ea",
                            "e785f92226f94b2185f92226f9eb219f",
                            "831d46a6f57a46779d46a6f57aa67739",
                            "328eefb2292c497a8eefb2292c697a23",
                            "f5985c52dcb2470b985c52dcb2970b1b",
                            "ff55f26b8e18400f95f26b8e18900f0b",
                            "ed8118a8828e48f08118a8828e88f0ad",
                            "e520473a8a2247e5a0473a8a2247e562",
                            "d5776538d11240bfb76538d112a0bf86",
                            "26a64399a3724f77a64399a3726f7768",
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", ""),
                            java.util.UUID.randomUUID().toString().replace("-", "")
                    );
                    Random random = new Random();
                    Date parse = s.parse(randomDateTime.toString());
                    String format = s.format(parse) + "Z";
                    HashMap map = new HashMap();
                    map.put("test1", RandomUtil.randomInt(20));
                    map.put("test2", RandomUtil.randomInt(20));
                    map.put("test3", RandomUtil.randomInt(20));
                    map.put("date", format);
                    map.put("monitor_uid", uuids.get(random.nextInt(uuids.size())));
                    map.put("date2", parse.getTime());
                    String messageId = UUID.fastUUID().toString();
                    com.inspur.ivideo.rabbit.api.Message message = new com.inspur.ivideo.rabbit.api.Message(messageId,
                            "kuiper-test",
                            "#",
                            map, 0, MessageType.RAPID);
                    log.info("发送消息：{}", new ObjectMapper().writeValueAsString(message));
                    producerClient.send(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "线程1").start();

        //new Thread(() -> {
        //    for (int i = 0; i < 1000; i++) {
        //        try {
        //            DateTime randomDateTime = randomDateTime("2022-08-01T00:00:00.000Z", "2022-08-07T23:59:59.000Z");
        //            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        //            Date parse = s.parse(randomDateTime.toString());
        //            String format = s.format(parse) + "Z";
        //            HashMap map = new HashMap();
        //            map.put("test1", RandomUtil.randomInt());
        //            map.put("test2", RandomUtil.randomInt());
        //            map.put("test3", RandomUtil.randomInt());
        //            map.put("date", format);
        //            map.put("date2", parse.getTime());
        //            String messageId = UUID.fastUUID().toString();
        //            com.inspur.ivideo.rabbit.api.Message message = new com.inspur.ivideo.rabbit.api.Message(messageId,
        //                    "kuiper-test",
        //                    "#",
        //                    map, 0, MessageType.RAPID);
        //            log.info("发送消息：{}", new ObjectMapper().writeValueAsString(message));
        //            producerClient.send(message);
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
        //    }
        //}, "线程2").start();
    }

    public static void main(String[] args) throws ParseException {
        DateTime randomDateTime = randomDateTime("2018-11-27T10:00:00.000Z", "2018-11-28T12:00:00.000Z");
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        Date parse = s.parse(randomDateTime.toString());
        String format = s.format(parse) + "Z";
        System.out.println(format);
    }

    public static DateTime randomDateTime(String beginStr, String endStr) {
        try {
            DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            DateTime beginTime = DateTime.parse(beginStr, format);
            DateTime endTime = DateTime.parse(endStr, format);

            if (beginTime.getMillis() > endTime.getMillis()) {
                return null;
            }

            long randDateTime = random(beginTime.getMillis(), endTime.getMillis());
            return new DateTime(randDateTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static long random(long begin, long end) {
        long rand = begin + (long) (Math.random() * (end - begin));
        if (rand == begin || rand == end) {
            return random(begin, end);
        }
        return rand;
    }

    //public static void main(String[] args) {
    //    String a = "select *,format_time(cast(mystream2.date,\"datetime\"),\"EEE\") as aaaa,format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\") as bbbb\n" +
    //            "from mystream2 left  JOIN mytable on mystream2.monitor_uid=mytable.monitor_uid\n" +
    //            "where isNull(mytable.monitor_name)=FALSE and (\n" +
    //            "(format_time(cast(mystream2.date,\"datetime\"),\"EEE\")=\"Mon\" and ((format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")>=\"11:11:00\" and format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")<\"13:11:00\") or (format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")>=\"18:11:00\" and format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")<\"22:11:00\"))) or\n" +
    //            "(format_time(cast(mystream2.date,\"datetime\"),\"EEE\")=\"Tue\" and ((format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")>=\"11:11:00\" and format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")<\"15:11:00\"))) or\n" +
    //            "(format_time(cast(mystream2.date,\"datetime\"),\"EEE\")=\"Wed\" and ((format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")>=\"11:11:00\" and format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")<\"16:11:00\"))) or\n" +
    //            "(format_time(cast(mystream2.date,\"datetime\"),\"EEE\")=\"Thu\" and ((format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")>=\"11:11:00\" and format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")<\"17:11:00\"))) or\n" +
    //            "(format_time(cast(mystream2.date,\"datetime\"),\"EEE\")=\"Fri\" and ((format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")>=\"11:11:00\" and format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")<\"18:11:00\"))) or\n" +
    //            "(format_time(cast(mystream2.date,\"datetime\"),\"EEE\")=\"Sat\" and ((format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")>=\"11:11:00\" and format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")<\"19:11:00\"))) or\n" +
    //            "(format_time(cast(mystream2.date,\"datetime\"),\"EEE\")=\"Sun\" and ((format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")>=\"11:11:00\" and format_time(cast(mystream2.date,\"datetime\"),\"HH:mm:ss\")<\"20:11:00\")))\n" +
    //            ");";
    //}

}
