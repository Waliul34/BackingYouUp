package com.reddredd.backingyouup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class signin_form extends AppCompatActivity {

    String txt_email = "", txt_password = "";
    EditText email, password;
    TextView forgotP;
    Button btn_login;
    ProgressDialog progressDialog;
    Dialog dialog, dialog_forg, dialog_succ, dialog_fail;
    private FirebaseAuth firebaseAuth;
    private int permissionCode = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_BackingYouUp);
        setContentView(R.layout.activity_signin_form);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sign In");

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(signin_form.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Working on it...");
        progressDialog.setCanceledOnTouchOutside(false);

        //DIALOG

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_box_all);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        }
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        Button okayB = dialog.findViewById(R.id.okayBtn);
        okayB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog_succ = new Dialog(this);
        dialog_succ.setContentView(R.layout.dialog_signup);
        dialog_succ.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView msg = dialog_succ.findViewById(R.id.message);
        msg.setText("An email has been sent. Please follow the link given in that email to reset password.");
        Button oka = dialog_succ.findViewById(R.id.okayBtn);
        oka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_succ.dismiss();
            }
        });

        dialog_forg = new Dialog(this);
        dialog_forg.setContentView(R.layout.dialog_box_all);
        dialog_forg.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_forg.setCancelable(false);

        TextView forgotP = findViewById(R.id.forgotPass);
        forgotP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView msg = dialog_forg.findViewById(R.id.message);
                msg.setText("Enter the email: ");
                Button cancelB = dialog_forg.findViewById(R.id.cancelBtn);
                Button okayB = dialog_forg.findViewById(R.id.okayBtn);
                dialog_forg.show();
                cancelB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_forg.dismiss();
                    }
                });

                okayB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText email = dialog_forg.findViewById(R.id.enterEmail);
                        txt_email = email.getText().toString().trim();
                        if(TextUtils.isEmpty(txt_email))
                        {
                            email.setError("Please Enter Email");
                        }
                        else if(!Patterns.EMAIL_ADDRESS.matcher(txt_email).matches())
                        {
                            email.setError("Invalid Email Format");
                        }
                        else
                        {
                            resetPassword();
                        }

                    }
                });

            }
        });

        dialog_fail = new Dialog(this);
        dialog_fail.setContentView(R.layout.dialog_box_all);
        dialog_fail.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Button okay_fail = dialog_fail.findViewById(R.id.okayBtn);
        okay_fail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_fail.dismiss();
            }
        });

        email = findViewById(R.id.log_email);
        password = findViewById(R.id.log_password);
        btn_login = findViewById(R.id.loginBtn);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void validateData()
    {
        txt_email = email.getText().toString().trim();
        txt_password = password.getText().toString().trim();

        if(TextUtils.isEmpty(txt_email))
        {
            email.setError("Please Enter Email");
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(txt_email).matches())
        {
            email.setError("Invalid Email Format");
        }
        else if(TextUtils.isEmpty(txt_password))
        {
            password.setError("Please Enter Password");
        }
        else if(txt_password.length() < 6)
        {
            password.setError("Password has to be at least 6 characters long");
        }
        else {
            firebaseLogin();
        }
    }

    private void firebaseLogin() {
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(txt_email, txt_password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if(firebaseAuth.getCurrentUser().isEmailVerified())
                        {
                            progressDialog.dismiss();
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                            if((ContextCompat.checkSelfPermission(signin_form.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(signin_form.this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED))
                            {
                                openHome();
                            }
                            else
                            {
                                checkPermissions();
                            }
                        }
                        else
                        {
                            progressDialog.dismiss();
                            TextView msg = dialog.findViewById(R.id.message);
                            msg.setText("Please verify your email adddress.");
                            EditText xemail = dialog.findViewById(R.id.enterEmail);
                            xemail.setVisibility(View.GONE);
                            Button cancelB = dialog.findViewById(R.id.cancelBtn);
                            cancelB.setVisibility(View.GONE);
                            dialog.show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        TextView msg = dialog.findViewById(R.id.message);
                        msg.setText(e.getMessage());
                        EditText xemail = dialog.findViewById(R.id.enterEmail);
                        xemail.setVisibility(View.GONE);
                        Button cancelB = dialog.findViewById(R.id.cancelBtn);
                        cancelB.setVisibility(View.GONE);
                        dialog.show();
                    }
                });
    }

    private void checkPermissions()
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG))
        {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed!!")
                    .setMessage("These permissions are needed to use this app. If you don't accept, this app will most likely to crash.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(signin_form.this, new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS}, permissionCode);
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
        else
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS}, permissionCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == permissionCode)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                openHome();
            }
            else
            {
                Toast.makeText(this, "Permissions DENIED! Please give permissions for the app to properly work.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openHome()
    {
        Intent intent = new Intent(signin_form.this, home.class);
        startActivity(intent);
        finishAffinity();
    }

    private void resetPassword()
    {
        progressDialog.show();
        firebaseAuth.sendPasswordResetEmail(txt_email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        dialog_forg.dismiss();
                        dialog_succ.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        dialog_forg.dismiss();
                        TextView msg = dialog_fail.findViewById(R.id.message);
                        msg.setText(e.getMessage());
                        EditText xemail = dialog_fail.findViewById(R.id.enterEmail);
                        xemail.setVisibility(View.GONE);
                        Button cancelB = dialog_fail.findViewById(R.id.cancelBtn);
                        cancelB.setVisibility(View.GONE);
                        dialog_fail.show();
                    }
                });
    }
}