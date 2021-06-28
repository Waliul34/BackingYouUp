package com.reddredd.backingyouup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class signup_form extends AppCompatActivity {
    EditText fullName, email, password, confirmPassword;
    Button createAcBtn;
    String txtEmail, txtFullName, txtPassword, txtConfirmPassword;
    ProgressDialog progressDialog;
    Dialog dialog, dialog_failed;
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_BackingYouUp);
        setContentView(R.layout.activity_signup_form);

        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fullName = (EditText)findViewById(R.id.fullName);
        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
        confirmPassword = (EditText)findViewById(R.id.confirmPassword);
        createAcBtn = findViewById(R.id.createAccountBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        progressDialog = new ProgressDialog(signup_form.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Creating Your Account...");
        progressDialog.setCanceledOnTouchOutside(false);

        //DIALOG

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_signup);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        }
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        Button okay = dialog.findViewById(R.id.okayBtn);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();

            }
        });

        dialog_failed = new Dialog(this);
        dialog_failed.setContentView(R.layout.dialog_signup_failed);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            dialog_failed.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        }
        dialog_failed.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_failed.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        Button f_okay = dialog_failed.findViewById(R.id.f_okayBtn);
        f_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_failed.dismiss();
            }
        });

        //DIALOG FINISHED

        createAcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                txtEmail = email.getText().toString().trim();
                txtFullName = fullName.getText().toString().trim();
                txtPassword = password.getText().toString().trim();
                txtConfirmPassword = confirmPassword.getText().toString().trim();

                if(TextUtils.isEmpty(txtFullName))
                {
                    fullName.setError("Please Enter Full Name");
                }
                else if(TextUtils.isEmpty(txtEmail))
                {
                    email.setError("Please Enter Email");
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches())
                {
                    email.setError("Invalid Email Format");
                }
                else if(TextUtils.isEmpty(txtPassword))
                {
                    password.setError("Please Enter Password");
                }
                else if(txtPassword.length() < 6)
                {
                    password.setError("Password has to be at least 6 characters long");
                }
                else if(TextUtils.isEmpty(txtConfirmPassword))
                {
                    confirmPassword.setError("Please Confirm Password");
                }
                else if(!txtPassword.equals(txtConfirmPassword))
                {
                    confirmPassword.setError("Please Enter The Same Password");
                }
                else
                {
                    firebaseSignUp();
                }
            }

            private void firebaseSignUp() {
                progressDialog.show();
                firebaseAuth.createUserWithEmailAndPassword(txtEmail, txtPassword)
                        .addOnSuccessListener(signup_form.this, new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                user inf = new user(txtFullName, txtEmail, "");
                                databaseReference.child("User").child(firebaseAuth.getCurrentUser().getUid()).setValue(inf);
                                firebaseAuth.getCurrentUser().sendEmailVerification()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                dialog.show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                TextView msg = dialog_failed.findViewById(R.id.message);
                                                msg.setText(""+e.getMessage());
                                                dialog_failed.show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                TextView msg = dialog_failed.findViewById(R.id.message);
                                msg.setText(""+e.getMessage());
                                dialog_failed.show();
                            }
                        });

            }
        });

    }


}
