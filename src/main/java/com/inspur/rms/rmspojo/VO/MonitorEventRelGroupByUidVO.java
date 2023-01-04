package com.inspur.rms.rmspojo.VO;

import com.inspur.rms.rmspojo.PO.MonitorEventRel;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/12/20 10:43 AM
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorEventRelGroupByUidVO implements Serializable {

    private static final long serialVersionUID = -6417868184636326902L;
    private String monitorUid;


    private List<MonitorEventRel> events;
}
