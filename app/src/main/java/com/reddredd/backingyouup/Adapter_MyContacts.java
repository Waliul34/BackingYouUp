package com.reddredd.backingyouup;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class Adapter_MyContacts extends RecyclerView.Adapter<Adapter_MyContacts.ViewHolder> {

    private Activity context;
    private List<SingleContact> contactsList;

    public Adapter_MyContacts(Activity context, List<SingleContact> contactsList)
    {
        this.context = context;
        this.contactsList = contactsList;
    }



    @NonNull
    @Override
    public Adapter_MyContacts.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recy_item_design, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter_MyContacts.ViewHolder holder, int position) {

        String name = contactsList.get(position).getRecy_contact_name();
        String phone = contactsList.get(position).getRecy_contact_phone();
        holder.setData(name, phone);

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
        return contactsList.size();
    }

    public void filterList(List<SingleContact> filteredList)
    {
        contactsList = filteredList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView naam, phoone;
        private ImageButton call, sms;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            naam = itemView.findViewById(R.id.recy_contact_name);
            phoone = itemView.findViewById(R.id.recy_contact_phone);
            call = itemView.findViewById(R.id.recy_make_call);
            sms = itemView.findViewById(R.id.recy_send_sms);

        }

        public void setData(String name, String phone) {
            naam.setText(name);
            phoone.setText(phone);
        }
    }
}
