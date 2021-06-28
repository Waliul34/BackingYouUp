package com.reddredd.backingyouup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class about_dev extends AppCompatActivity implements View.OnClickListener{

    ActionBar actionBar;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_BackingYouUp);
        setContentView(R.layout.activity_about_dev);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2E11AC")));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.about_dev_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        ImageButton facebookBtn = (ImageButton) findViewById(R.id.facebookBtn);
        facebookBtn.setOnClickListener(this);
        ImageButton codeforcesBtn = (ImageButton) findViewById(R.id.codeforcesBtn);
        codeforcesBtn.setOnClickListener(this);
        ImageButton gmailBtn = (ImageButton) findViewById(R.id.gmailBtn);
        gmailBtn.setOnClickListener(this);
        ImageButton githubBtn = (ImageButton) findViewById(R.id.githubBtn);
        githubBtn.setOnClickListener(this);


    }

    Uri uri;
    Intent intent;

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.facebookBtn:
                uri = Uri.parse("https://www.facebook.com/love.makes.life.easy");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.codeforcesBtn:
                uri = Uri.parse("https://codeforces.com/profile/.waliul.");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.githubBtn:
                uri = Uri.parse("https://github.com/waliul34");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.gmailBtn:
                uri = Uri.parse("mailto:00waliul00@gmail.com");
                intent = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(intent);
                break;
        }
    }
}
