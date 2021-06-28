package com.reddredd.backingyouup;

public class SingleCallStatistics {
    public String statisticsName, statisticsPhone;
    public int statisticsDuration, statisticsOutgoing, statisticsIncoming, statisticsMissed;

    SingleCallStatistics()
    {
        statisticsName = "";
        statisticsPhone = "";
        statisticsDuration = 0;
        statisticsIncoming = 0;
        statisticsOutgoing = 0;
        statisticsMissed = 0;
    }
    SingleCallStatistics(String name, String phone)
    {
        statisticsName =name;
        statisticsPhone = phone;
        statisticsDuration = 0;
        statisticsIncoming = 0;
        statisticsOutgoing = 0;
        statisticsMissed = 0;
    }
}
