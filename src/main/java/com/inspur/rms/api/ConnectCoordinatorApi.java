package com.inspur.rms.api;

import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.PostRequest;
import com.dtflys.forest.http.ForestResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 连接协调服务接口
 *
 * @author : lidongbin
 * @ClassName : Api
 * @Description :
 * @date : ada 9:44 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 * <p>
 * /**
 * *@DataVariable
 * @var *@JSONBody
 * @query * @header
 * *@Body("key1")
 * *
 * *
 */
@Component
@BaseRequest(
        baseURL = "${inner_api}",
        contentType = "application/json",
        timeout = -1
)

public interface ConnectCoordinatorApi {

    /**
     * 取消纳管之后将取消纳管的设备发送给协调
     */
    @PostRequest(
            url = "/connection-coordinator/v3/subresource/manage/batch/delete",
            timeout = -1,
            dataType = "json"
    )
    ForestResponse<String> subresource(@JSONBody Map<String, Object> param) throws Exception;
}