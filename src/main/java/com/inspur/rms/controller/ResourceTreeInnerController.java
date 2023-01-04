package com.inspur.rms.controller;

import com.inspur.rms.service.ResourceTreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/28 1:56 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Slf4j
@RestController
@RequestMapping("/resource-manager/v3/inner")
public class ResourceTreeInnerController {


    private final ResourceTreeService resourceTreeService;

    @Autowired
    public ResourceTreeInnerController(ResourceTreeService resourceTreeService) {
        this.resourceTreeService = resourceTreeService;
    }

    @PostMapping("/monitors/batch/delete")
    public Integer updateDeviceManage(@RequestBody List<String> businessUids) throws Exception {
        return resourceTreeService.updateDeviceManage(businessUids);
    }

}
