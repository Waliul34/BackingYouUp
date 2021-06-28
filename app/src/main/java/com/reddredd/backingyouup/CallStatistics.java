package com.reddredd.backingyouup;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

public class CallStatistics extends Fragment {

    View myView;
    BarChart barChart;
    ArrayList<BarEntry> barEntryArrayList;
    ArrayList<String> labelNames;
    List<SingleCallStatistics> callLogsList;
    HashSet<String> dupLog = new HashSet<>();

    AlertDialog alertDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_call_statistics, container, false);
        getCallLogs();
        showGraph();
        return myView;
    }

    public void getCallLogs()
    {
        Date today = new Date();
        Calendar cal =  new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date lastDate = cal.getTime();
        callLogsList = new ArrayList<>();
        Cursor callLogs = getContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
        int nam = callLogs.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int number = callLogs.getColumnIndex(CallLog.Calls.NUMBER);
        int type = callLogs.getColumnIndex(CallLog.Calls.TYPE);
        int date = callLogs.getColumnIndex(CallLog.Calls.DATE);
        int duration = callLogs.getColumnIndex(CallLog.Calls.DURATION);
        while(callLogs.moveToNext())
        {
            String name = callLogs.getString(nam);
            if(name == null)
                name = "Unknown";

            String phoneNumber = callLogs.getString(number);

            String callType = callLogs.getString(type);
            String dir = "";
            int dirCode = Integer.parseInt(callType);
            switch (dirCode)
            {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "Outgoing";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "Incoming";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    dir = "Missed";
                    break;
            }

            String callDate = callLogs.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = callLogs.getString(duration);
            int dur = Integer.parseInt(callDuration);
            if(lastDate.compareTo(callDayTime) > 0)
                continue;
            else
            {
                if(dupLog.add(phoneNumber))
                    callLogsList.add(new SingleCallStatistics(name, phoneNumber));
                int size = callLogsList.size();
                for(int i = 0; i < size; i++)
                {
                    if(callLogsList.get(i).statisticsPhone.equals(phoneNumber))
                    {
                        callLogsList.get(i).statisticsDuration += dur;
                        if(dir.equals("Outgoing"))
                            callLogsList.get(i).statisticsOutgoing++;
                        else if(dir.equals("Incoming"))
                            callLogsList.get(i).statisticsIncoming++;
                        else if(dir.equals("Missed"))
                            callLogsList.get(i).statisticsMissed++;
                    }
                }
            }

        }
        callLogs.close();

        Comparator<SingleCallStatistics> cmpDuration = (SingleCallStatistics cnt1, SingleCallStatistics cnt2) -> (cnt2.statisticsDuration > cnt1.statisticsDuration ? +1 : cnt2.statisticsDuration < cnt1.statisticsDuration ? -1 : 0);
        Collections.sort(callLogsList, cmpDuration);
    }

    public void showGraph()
    {
        barChart = myView.findViewById(R.id.barChart);
        barEntryArrayList = new ArrayList<>();
        labelNames = new ArrayList<>();

        int n = Math.min(5, callLogsList.size());

        for(int i = 0; i < n; i++)
        {
            String name = callLogsList.get(i).statisticsName;
            int dur = callLogsList.get(i).statisticsDuration;
            float duration = Float.valueOf(dur);
            duration /= 60;
            barEntryArrayList.add(new BarEntry(i, duration));
            labelNames.add(name);
        }

        BarDataSet barDataSet = new BarDataSet(barEntryArrayList, "Call Duration in Minutes(Fraction)");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        Description description = new Description();
        description.setText("Contact");
        barChart.setDescription(description);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelNames));
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labelNames.size());
        xAxis.setLabelRotationAngle(270);
        barChart.animateY(2000);
        barChart.invalidate();

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int x = barChart.getBarData().getDataSetForEntry(e).getEntryIndex((BarEntry)e);

                String nam, phn, out = "Outgoing : ", in = "Incoming : ", mis = "Missed    : ", dur = "Total Call Duration : ";
                nam = callLogsList.get(x).statisticsName;
                phn = callLogsList.get(x).statisticsPhone;
                out = out + NumberFormat.getNumberInstance().format(callLogsList.get(x).statisticsOutgoing) + " Times";
                in = in + NumberFormat.getNumberInstance().format(callLogsList.get(x).statisticsIncoming) + " Times";
                mis = mis + NumberFormat.getNumberInstance().format(callLogsList.get(x).statisticsMissed) + " Times";
                int hr, m, s, dr;
                dr = callLogsList.get(x).statisticsDuration;
                m = dr / 60;
                s = dr % 60;
                hr = m / 60;
                m = m % 60;
                if(hr != 0)
                    dur = dur + Integer.toString(hr) + "h ";
                dur = dur + Integer.toString(m) + "m ";
                dur = dur + Integer.toString(s) + "s";

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                View nview = LayoutInflater.from(getContext()).inflate(R.layout.call_statistics_single_bar_selected, null);
                TextView name = nview.findViewById(R.id.nameContact);
                TextView phone = nview.findViewById(R.id.phoneContact);
                TextView outgoing = nview.findViewById(R.id.outgoing);
                TextView incoming = nview.findViewById(R.id.incoming);
                TextView missed = nview.findViewById(R.id.missed);
                TextView duration = nview.findViewById(R.id.totalDuration);

                name.setText(nam);
                phone.setText(phn);
                outgoing.setText(out);
                incoming.setText(in);
                missed.setText(mis);
                duration.setText(dur);
                builder.setView(nview);
                alertDialog = builder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();

            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

}
