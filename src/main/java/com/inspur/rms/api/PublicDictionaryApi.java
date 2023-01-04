package com.inspur.rms.api;


import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.PostRequest;
import com.dtflys.forest.http.ForestResponse;
import com.inspur.ivideo.common.entity.DictionaryDTO;
import org.springframework.stereotype.Component;

@Component
@BaseRequest(
        baseURL = "${inner_api}",
        contentType = "application/json"
)
public interface PublicDictionaryApi {

    @PostRequest(
            url = "/dictionary/v3/groups/batch/update",
            dataType = "json"
    )
    ForestResponse<Object> dictionarys(@JSONBody DictionaryDTO dictionaryDTO);
}
