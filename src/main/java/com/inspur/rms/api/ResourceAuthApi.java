package com.inspur.rms.api;


import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.GetRequest;
import com.dtflys.forest.annotation.Query;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.http.ForestResponse;
import com.inspur.ivideo.common.entity.ValueResult;
import com.inspur.rms.rmspojo.pmspojo.RoleSummaryVO;
import com.inspur.rms.rmspojo.pmspojo.UserSummaryVO;
import org.springframework.stereotype.Component;

@Component
@BaseRequest(
        baseURL = "${inner_api}",
        contentType = "application/json"
)
public interface ResourceAuthApi {

    //    查询用户详情
    @GetRequest(
            url = "/permission-manager/v3/account/${UserName}",
            dataType = "json"
    )
    ForestResponse<UserSummaryVO> getAccounrSummary(@Var("UserName") String accountName);

    @GetRequest(
            url = "/permission-manager/v3/roles/${UID}",
            dataType = "json"
    )
    ForestResponse<RoleSummaryVO> getRoleSummary(@Var("UID") String roleUid);

    @GetRequest(
            url = "/permission-manager/v3/roles/catelog/rel",
            dataType = "json"
    )
    ForestResponse<ValueResult> catelogUidRoleRel(@Query("CatelogUid") String roleUid);
}
