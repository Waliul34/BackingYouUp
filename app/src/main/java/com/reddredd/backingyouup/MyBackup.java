package com.reddredd.backingyouup;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyBackup extends Fragment {

    View myView;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<SingleContact> contactList;
    Adapter_MyContacts adapter;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    //SwipeRefreshLayout swipeRefreshLayout;
    CardView cardView;
    EditText editTextSearch;
    FloatingActionButton btnSearch;
    Button btnCancel, btnClear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_my_backup, container, false);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Contacts").child(firebaseAuth.getCurrentUser().getUid());

        /*swipeRefreshLayout = myView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDataFromFirebase();

                swipeRefreshLayout.setRefreshing(false);
            }
        });*/

        getDataFromFirebase();

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
        List<SingleContact> filteredList = new ArrayList<>();
        for(SingleContact item : contactList) {
            if (item.getRecy_contact_name().toLowerCase().contains(text.toLowerCase()) || item.getRecy_contact_phone().contains(text))
                filteredList.add(item);
        }
        adapter.filterList(filteredList);
    }

    private void getDataFromFirebase() {

        recyclerView = myView.findViewById(R.id.recyclerViewMyBackup);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactList = new ArrayList<>();
        adapter = new Adapter_MyContacts(getActivity(), contactList);
        recyclerView.setAdapter(adapter);



        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Fetching the data from the Cloud....\nPlease make sure your internet connection is ON.");

        progressDialog.show();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    String name = dataSnapshot.getValue().toString();
                    String phone = dataSnapshot.getKey();
                    SingleContact singleContact = new SingleContact(name, phone);
                    contactList.add(singleContact);
                }

                Comparator<SingleContact> cmpName = (SingleContact cnt1, SingleContact cnt2) -> cnt1.getRecy_contact_name().compareTo(cnt2.getRecy_contact_name());
                Collections.sort(contactList, cmpName);

                adapter.notifyDataSetChanged();
                if(contactList.size() == 0)
                    Toast.makeText(getContext(), "No backup in the cloud.", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
