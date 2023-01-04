package com.inspur.rms.constant;

public enum MonitorMqExchange {

    //监控点元数据变更
    METADATACHANGED("manage.monitor.metadata-changed"),
    GROUP_METADATACHANGED("manage.group.metadata-changed");

    private String exchange;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    MonitorMqExchange(String exchange) {
        this.exchange = exchange;
    }
}
