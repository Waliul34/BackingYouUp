package com.reddredd.backingyouup;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class MyProfile extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference, dRef, ref;
    StorageReference storageReference;
    Dialog dialog_de;
    private Uri imageUri;
    ImageView image;
    ProgressBar progressBar;
    StorageTask uploadTask;
    String nam, ema, img;

    private static final int IMAGE_REQUEST = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View myView = inflater.inflate(R.layout.activity_profile, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("User");
        storageReference = FirebaseStorage.getInstance().getReference("User");

        dRef = FirebaseDatabase.getInstance().getReference("User").child(firebaseUser.getUid());
        ref = FirebaseDatabase.getInstance().getReference("Contacts").child(firebaseUser.getUid());
        checkUser();

        image = getActivity().findViewById(R.id.defaultProfilePic);

        Button signOu = myView.findViewById(R.id.signoutBtn);
        signOu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        dialog_de = new Dialog(myView.getContext());
        dialog_de.setContentView(R.layout.dialog_box_all);
        dialog_de.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Button okay = dialog_de.findViewById(R.id.okayBtn);
        Button cancel = dialog_de.findViewById(R.id.cancelBtn);
        okay.setText("Yes");
        cancel.setText("No");
        EditText ed = dialog_de.findViewById(R.id.enterEmail);
        ed.setHint("Type -> YES");
        TextView msg = dialog_de.findViewById(R.id.message);
        msg.setText("Deleting your account will delete all your data from the cloud.You can't recover them.\nARE YOU SURE?\nIf you are, type YES in the box below.");

        Button changePhoto = myView.findViewById(R.id.changePhoto);
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        Button deleteBtn = myView.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_de.show();
                String ys = "YES";
                okay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String usr = ed.getText().toString().trim();
                        if(ys.equals(usr))
                        {
                            firebaseAuth.signOut();
                            dialog_de.dismiss();
                            firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(myView.getContext(), "Account has been deleted.", Toast.LENGTH_LONG).show();
                                        ref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                dRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        startActivity(new Intent(getActivity(), MainActivity.class));
                                                        getActivity().finishAffinity();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        else
                        {
                            dialog_de.dismiss();
                            Toast.makeText(myView.getContext(), "Thank you for not deleting.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_de.dismiss();
                        Toast.makeText(getActivity(), "Thank you for not deleting.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        return myView;
    }

    public String getFileExtension(Uri imageUri)
    {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    void openFileChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data!=null && data.getData()!=null)
        {
            image = getActivity().findViewById(R.id.defaultProfilePic);
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(image);
            if(uploadTask != null && uploadTask.isInProgress())
            {
                Toast.makeText(getActivity(), "Uploading is in progress.", Toast.LENGTH_SHORT).show();
            }
            else {
                saveData();
            }
        }
    }

    public void saveData()
    {
        progressBar = getActivity().findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        StorageReference ref = storageReference.child(firebaseAuth.getCurrentUser().getUid() + "." + getFileExtension(imageUri));
        ref.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressBar.setVisibility(View.GONE);
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        img = downloadUri.toString();
                        databaseReference.child(firebaseAuth.getCurrentUser().getUid()).child("imageUrl").setValue(img);
                        Toast.makeText(getContext(), "Profile Picture changed successfully.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null)
        {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
        else
        {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = "";
                    try{
                        name = snapshot.child(firebaseUser.getUid()).child("fullName").getValue().toString();
                    }
                    catch (Exception e){
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    String email = snapshot.child(firebaseUser.getUid()).child("email").getValue().toString();
                    String url_image = snapshot.child(firebaseUser.getUid()).child("imageUrl").getValue().toString();
                    nam = name;
                    ema = email;
                    TextView s_name = getActivity().findViewById(R.id.nameShow);
                    TextView s_email = getActivity().findViewById(R.id.emailShow);
                    image = getActivity().findViewById(R.id.defaultProfilePic);
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
                    Toast.makeText(getActivity(), "Something's wrong.Please reopen the app.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
