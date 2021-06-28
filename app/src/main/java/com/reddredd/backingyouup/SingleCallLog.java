package com.reddredd.backingyouup;

public class SingleCallLog {

    private String recy_call_log_name, recy_call_log_phone, recy_call_log_duration, recy_call_log_day;

    SingleCallLog(String recy_call_log_name, String recy_call_log_phone, String recy_call_log_duration, String recy_call_log_day)
    {
        this.recy_call_log_day = recy_call_log_day;
        this.recy_call_log_duration = recy_call_log_duration;
        this.recy_call_log_name = recy_call_log_name;
        this.recy_call_log_phone = recy_call_log_phone;
    }

    public String getRecy_call_log_name() {
        return recy_call_log_name;
    }

    public String getRecy_call_log_phone() {
        return recy_call_log_phone;
    }

    public String getRecy_call_log_duration() {
        return recy_call_log_duration;
    }

    public String getRecy_call_log_day() {
        return recy_call_log_day;
    }
}
