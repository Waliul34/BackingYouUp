package com.reddredd.backingyouup;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter_CallLogs extends RecyclerView.Adapter<Adapter_CallLogs.ViewHolder> {

    private List<SingleCallLog> callLogsList;
    private Activity context;

    public Adapter_CallLogs(Activity context, List<SingleCallLog> callLogsList)
    {
        this.context = context;
        this.callLogsList = callLogsList;
    }


    @NonNull
    @Override
    public Adapter_CallLogs.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recy_item_call_logs, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter_CallLogs.ViewHolder holder, int position) {

        String name = callLogsList.get(position).getRecy_call_log_name();
        String phone = callLogsList.get(position).getRecy_call_log_phone();
        String duration = callLogsList.get(position).getRecy_call_log_duration();
        String day = callLogsList.get(position).getRecy_call_log_day();

        holder.setData(name, phone, duration, day);

        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent call = new Intent(Intent.ACTION_CALL);
                call.setData(Uri.parse("tel:" + phone)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(context, new String[] {Manifest.permission.CALL_PHONE}, 34);
                }
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                context.startActivity(call);
            }
        });

        holder.sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(context, new String[] {Manifest.permission.SEND_SMS}, 34);
                }
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                Intent sms = new Intent(Intent.ACTION_VIEW);
                sms.setData(Uri.parse("sms:" + phone));
                sms.putExtra("sms_body", "");
                context.startActivity(sms);
            }
        });

    }

    @Override
    public int getItemCount() {
        return callLogsList.size();
    }

    public void filterList(List<SingleCallLog> filteredList)
    {
        callLogsList = filteredList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView naam, phoone, dur, daay;
        private ImageButton call, sms;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            naam = itemView.findViewById(R.id.recy_call_logs_contact_name);
            phoone = itemView.findViewById(R.id.recy_call_logs_contact_phone);
            dur = itemView.findViewById(R.id.recy_call_logs_duration);
            daay = itemView.findViewById(R.id.recy_call_logs_day);
            call = itemView.findViewById(R.id.recy_call_logs_make_call);
            sms = itemView.findViewById(R.id.recy_call_logs_send_sms);
        }

        public void setData(String name, String phone, String duration, String day) {
            naam.setText(name);
            //if(duration.charAt(0) == 'M' && duration.charAt(1) == 'i')
            //   naam.setTextColor(Color.parseColor("#DC143C"));
            phoone.setText(phone);
            dur.setText(duration);
            daay.setText(day);
        }
    }
}
