package com.reddredd.backingyouup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Contacts extends Fragment {

    BottomNavigationView bottomNavigationView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_contacts, container, false);

        bottomNavigationView = myView.findViewById(R.id.top_nav_contacts);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        bottomNavigationView.setSelectedItemId(R.id.my_contacts);
        getFragmentManager().beginTransaction().replace(R.id.fragment_contacts, new MyContacts()).commit();



        return myView;
    }

    public BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId())
                    {
                        case R.id.call_logs:
                            selectedFragment = new CallLogs();
                            break;
                        case R.id.my_contacts:
                            selectedFragment = new MyContacts();
                            break;
                        case R.id.my_backup:
                            selectedFragment = new MyBackup();
                            break;
                    }

                    getFragmentManager().beginTransaction().replace(R.id.fragment_contacts, selectedFragment).commit();
                    return true;
                }
    };
}
