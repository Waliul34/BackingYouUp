package com.reddredd.backingyouup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class MyContacts extends Fragment {

    View myView;
    HashSet<String> dupPhone = new HashSet<>();
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<SingleContact> contactList;
    Adapter_MyContacts adapter;
    Button backupInCloudBtn, btnCancel, btnClear;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    Dialog dialog;
    FloatingActionButton btnAdd, btnSearch;
    CardView cardView;
    EditText editTextSearch;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_my_contacts, container, false);
        getContacts();
        initRecyclerView();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Contacts").child(firebaseAuth.getCurrentUser().getUid());

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Uploading Your Contacts in the Cloud....");
        progressDialog.setCanceledOnTouchOutside(false);

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_signup);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        }
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        TextView msg = dialog.findViewById(R.id.message);
        msg.setText("Backup/Synchronization has been successfully done.");
        Button okay = dialog.findViewById(R.id.okayBtn);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        backupInCloudBtn = myView.findViewById(R.id.backupInCloud);
        backupInCloudBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getContext())
                        .setTitle("Do you want to backup/sync?")
                        .setMessage("All the contacts of your phone will be uploaded in the cloud and you can access those contacts from any device using this app.\n" +
                                "Note that contacts are synced.So, no phone numbers will be deleted.This operation just does synchronization.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                uploadInFirebase();

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            }
        });

        btnAdd = myView.findViewById(R.id.addContact);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                startActivity(intent);
            }
        });

        editTextSearch = myView.findViewById(R.id.search);
        cardView = myView.findViewById(R.id.cardViewSearch);
        btnSearch = myView.findViewById(R.id.searchContact);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardView.setVisibility(View.VISIBLE);
                editTextSearch.setHint("Search in " + contactList.size() + " Contacts");
                editTextSearch.setFocusableInTouchMode(true);
                editTextSearch.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(editTextSearch, InputMethodManager.SHOW_IMPLICIT);
                btnSearch.setVisibility(View.GONE);
                backupInCloudBtn.setVisibility(View.GONE);
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
                backupInCloudBtn.setVisibility(View.VISIBLE);
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
        List<SingleContact> filteredList = new ArrayList<>();
        for(SingleContact item : contactList) {
            if (item.getRecy_contact_name().toLowerCase().contains(text.toLowerCase()) || item.getRecy_contact_phone().contains(text))
                filteredList.add(item);
        }
        adapter.filterList(filteredList);
    }

    private void getContacts() {

        contactList = new ArrayList<>();
        Cursor contacts = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        while(contacts.moveToNext())
        {
            String name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneNumber = phoneNumber.replaceAll("[\\-\\s]", "");
            if(phoneNumber != null)
            {
                if(dupPhone.add(phoneNumber))
                {
                    contactList.add(new SingleContact(name, phoneNumber));
                }
            }
        }
        Comparator<SingleContact> cmpName = (SingleContact cnt1, SingleContact cnt2) -> cnt1.getRecy_contact_name().compareTo(cnt2.getRecy_contact_name());
        Collections.sort(contactList, cmpName);
        contacts.close();
    }

    private void initRecyclerView() {

        recyclerView = myView.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new Adapter_MyContacts(getActivity(), contactList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void uploadInFirebase() {
        progressDialog.show();
        uploadData();
        progressDialog.dismiss();
        dialog.show();

    }

    private void uploadData() {

        int size = contactList.size();
        SingleContact singleContact;
        for(int i = 0; i < size; i++)
        {
            singleContact = contactList.get(i);
            databaseReference.child(singleContact.getRecy_contact_phone()).setValue(singleContact.getRecy_contact_name());
        }

    }
}
