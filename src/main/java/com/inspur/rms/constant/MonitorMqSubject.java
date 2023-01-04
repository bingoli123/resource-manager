package com.inspur.rms.constant;

public enum MonitorMqSubject {

    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete");

    private String subject;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    MonitorMqSubject(String subject) {
        this.subject = subject;
    }
}
