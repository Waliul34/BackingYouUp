package com.reddredd.backingyouup;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallLogs extends Fragment {
    View myView;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<SingleCallLog> callLogsList;
    Adapter_CallLogs adapter;
    CardView cardView;
    EditText editTextSearch;
    FloatingActionButton btnSearch;
    Button btnCancel, btnClear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_call_logs, container, false);
        getCallLogs();
        initRecyclerView();

        editTextSearch = myView.findViewById(R.id.search);
        cardView = myView.findViewById(R.id.cardViewSearch);
        btnSearch = myView.findViewById(R.id.searchContact);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardView.setVisibility(View.VISIBLE);
                editTextSearch.setHint("Search in " + callLogsList.size() + " Call Logs");
                editTextSearch.setFocusableInTouchMode(true);
                editTextSearch.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(editTextSearch, InputMethodManager.SHOW_IMPLICIT);
                btnSearch.setVisibility(View.GONE);
            }
        });

        btnClear = myView.findViewById(R.id.clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearch.setText("");
            }
        });
        btnCancel = myView.findViewById(R.id.cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardView.setVisibility(View.GONE);
                editTextSearch.setText("");
                InputMethodManager inputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(myView.getWindowToken(), 0);
                btnSearch.setVisibility(View.VISIBLE);
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        return myView;
    }

    private void filter(String text)
    {
        List<SingleCallLog> filteredList = new ArrayList<>();
        for(SingleCallLog item : callLogsList) {
            if (item.getRecy_call_log_name().toLowerCase().contains(text.toLowerCase()) || item.getRecy_call_log_phone().contains(text))
                filteredList.add(item);
        }
        adapter.filterList(filteredList);
    }

    private void getCallLogs()
    {
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
            String callDate = callLogs.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = callLogs.getString(duration);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd hh:mm:ss a");
            callDate = simpleDateFormat.format(callDayTime);

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
            String callTypeDuration = "";
            int dur = Integer.parseInt(callDuration);
            if(dir == "Missed")
                callTypeDuration = dir;
            else
                callTypeDuration = dir + ": " + (dur / 60) + "m " + (dur % 60) + "s";

            callLogsList.add(new SingleCallLog(name, phoneNumber, callTypeDuration, callDate));
        }
        callLogs.close();
    }

    private void initRecyclerView()
    {
        recyclerView = myView.findViewById(R.id.recyclerViewCallLogs);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new Adapter_CallLogs(getActivity(), callLogsList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
