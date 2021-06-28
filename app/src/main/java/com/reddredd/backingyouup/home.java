package com.reddredd.backingyouup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer;

    long backPressedTime;
    private Toast backToast;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("User");
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null)
        {
            Intent intent = new Intent(home.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            setNameEmail();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if(savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Contacts()).commit();
            navigationView.setCheckedItem(R.id.nav_contacts);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_apps:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Apps()).commit();
                break;
            case R.id.nav_contacts:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Contacts()).commit();
                break;
            case R.id.nav_message:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Messages()).commit();
                break;
            case R.id.nav_my_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyProfile()).commit();
                break;
            case R.id.nav_call_statistics:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CallStatistics()).commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setNameEmail() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                View headerView = navigationView.getHeaderView(0);
                String name = "";
                try{
                    name = snapshot.child(firebaseAuth.getCurrentUser().getUid()).child("fullName").getValue().toString();
                }
                catch (Exception e){
                    firebaseAuth.signOut();
                    Intent intent = new Intent(home.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                String email = snapshot.child(firebaseAuth.getCurrentUser().getUid()).child("email").getValue().toString();
                String url_image = snapshot.child(firebaseAuth.getCurrentUser().getUid()).child("imageUrl").getValue().toString();
                ImageView image = headerView.findViewById(R.id.nav_pro_pic);
                TextView s_name = headerView.findViewById(R.id.nav_name);
                TextView s_email = headerView.findViewById(R.id.nav_email);
                s_name.setText("" + name);
                s_email.setText("" + email);
                try
                {
                    Picasso.get().load(url_image).into(image);
                }
                catch (Exception e)
                {
                    Picasso.get().load(R.drawable.default_profile_pic).into(image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(home.this, "Something's wrong.Please reopen the app.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            if(backPressedTime + 2000 > System.currentTimeMillis())
            {
                backToast.cancel();
                super.onBackPressed();
                return;
            }
            else
            {
                backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime = System.currentTimeMillis();
        }
    }
}