package com.inspur.rms.api;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import com.inspur.rms.rmspojo.DTO.KongPluginCreateDTO;
import com.inspur.rms.rmspojo.DTO.KongPluginsDTO;
import com.inspur.rms.rmspojo.DTO.KongRoutesCreateDTO;
import com.inspur.rms.rmspojo.DTO.KongSerciceCreateDTO;
import org.springframework.stereotype.Component;

/**
 * @author : lidongbin
 * @date : 2021/9/27 9:44 上午
 * kong admin api
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Component
@BaseRequest(
        baseURL = "${kong}",
        contentType = "application/json"
)
public interface KongApi {

//    新增或修改service

    @PutRequest(
            url = "/services/${service_name}",
            dataType = "json"
    )
    ForestResponse<Object> createOrUpdateService(@JSONBody KongSerciceCreateDTO kongSerciceCreateDTO);

    //    新增或修改routers
    @PutRequest(
            url = "/services/${service_name}/routes/${routeName}",
            dataType = "json"
    )
    ForestResponse<Object> createOrUpdateRoute(@JSONBody KongRoutesCreateDTO kongRoutesCreateDTO, @Var("routeName") String routeName);


    //    绑定plugin
    @PostRequest(
            url = "/services/${serviceName}/plugins",
            dataType = "json"
    )
    ForestResponse<Object> createPlugin(@JSONBody KongPluginCreateDTO kongPluginCreateDTO, @Var("serviceName") String serviceName);

    @GetRequest(
            url = "/services/${serviceName}/plugins",
            dataType = "json"
    )
    ForestResponse<KongPluginsDTO> getPlugins(@Var("serviceName") String serviceName);

    //    绑定plugin
    @PutRequest(
            url = "/services/${serviceName}/plugins/${pluginId}",
            dataType = "json"
    )
    ForestResponse<Object> createOrUpdatePlugin(@JSONBody KongPluginCreateDTO kongPluginCreateDTO, @Var("serviceName") String serviceName, @Var("pluginId") String pluginId);
}
