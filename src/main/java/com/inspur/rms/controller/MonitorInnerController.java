package com.inspur.rms.controller;

import com.google.common.base.Preconditions;
import com.inspur.rms.rmspojo.VO.MonitorSummaryVO;
import com.inspur.rms.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author : lidongbin
 * @date : 2021/10/26 4:40 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Slf4j
@RestController
@RequestMapping("/resource-manager/v3/inner")
public class MonitorInnerController {

    private final MonitorService monitorService;

    @Autowired
    public MonitorInnerController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    /**
     * 监控点详情
     */
    @GetMapping("/monitors/{UID}")
    public ResponseEntity<MonitorSummaryVO> monitorSummary(@PathVariable("UID") String monitorUid) throws Exception {
        Preconditions.checkNotNull(monitorUid);
        MonitorSummaryVO monitorSummaryVO = monitorService.monitorSummary(monitorUid);
        return ResponseEntity.ok(monitorSummaryVO);
    }

    /**
     * 修改已纳管的监控点的在线状态
     */
    @GetMapping("/monitors/{UID}/online/{onlineStatus}")
    public void monitorOnline(@PathVariable("UID") String subresourceUid,
                              @PathVariable("onlineStatus") String onlineStatus,
                              @RequestParam(value = "EventCause", required = false) String eventCause,
                              @RequestParam(value = "MessageTime", required = false) Long messageTime) throws Exception {
        if (StringUtils.isNotBlank(subresourceUid) && null != onlineStatus) {
            monitorService.updateMonitorOnline(subresourceUid, onlineStatus, eventCause);
        }
    }
}
