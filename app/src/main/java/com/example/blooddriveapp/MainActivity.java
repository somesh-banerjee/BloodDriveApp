package com.example.blooddriveapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnlogout, btnUpdateInfo;
    private TextView fname,tvsex,tvdob,tvbg;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnlogout = findViewById(R.id.btnlogout);
        btnUpdateInfo = findViewById(R.id.btnUpdateInfo);
        fname = findViewById(R.id.fname);
        tvdob = findViewById(R.id.tvdob);
        tvbg = findViewById(R.id.tvbg);
        tvsex = findViewById(R.id.tvsex);
        fStore = FirebaseFirestore.getInstance();
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent intent = new Intent(MainActivity.this, phoneRegistration.class);
            startActivity(intent);
            return;
        }
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fStore.collection("users").document(userID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            fname.setText(documentSnapshot.getString("FullName"));
                            tvdob.setText(documentSnapshot.getString("DOB"));
                            tvsex.setText(documentSnapshot.getString("Sex"));
                            tvbg.setText(documentSnapshot.getString("BloodGroup"));
                        }else{
                            Intent i = new Intent(MainActivity.this, UserInfoUpdate.class);
                            i.putExtra("userID", userID);
                            startActivity(i);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this,phoneRegistration.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        btnUpdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,UserInfoUpdate.class);
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent intent = new Intent(MainActivity.this, phoneRegistration.class);
            startActivity(intent);
        }
    }
}