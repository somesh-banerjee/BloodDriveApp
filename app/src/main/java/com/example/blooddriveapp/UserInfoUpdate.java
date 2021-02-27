package com.example.blooddriveapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserInfoUpdate extends AppCompatActivity {

    private EditText fullname, DOB;
    private TextView locdetect;
    private Spinner Bgrp, sex;
    private Button btnUpdateInfo, btnlocdetect;
    private FirebaseFirestore fStore;
    private ProgressBar pgbar;
    private static final String[] sexcats = {"Select","Male", "Female", "Other"};
    private static final String[] bgcats = {"Select","A+", "B+", "AB+", "O+", "A-", "B-", "AB-", "O-"};
    private static final int REQUEST_PERMISSION_CODE = 1000;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Double Lat, Log;
    private DatePickerDialog.OnDateSetListener setListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_update);

        if (!checkPermissionFromDevice())
            requestPermission();

        fullname = findViewById(R.id.editTextTextPersonName);
        DOB = findViewById(R.id.editTextDate);
        Bgrp = findViewById(R.id.spinner2);
        sex = findViewById(R.id.spinner);
        btnUpdateInfo = findViewById(R.id.btnupdateinfo);
        fStore = FirebaseFirestore.getInstance();
        locdetect = findViewById(R.id.locdetect);
        btnlocdetect = findViewById(R.id.btnlocdetect);
        pgbar = findViewById(R.id.progressBar);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        sex.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, sexcats));
        Bgrp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, bgcats));

        DOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        UserInfoUpdate.this,
                        android.R.style.Theme,
                        setListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        setListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = dayOfMonth + "/" + month + "/" + year;
                DOB.setText(date);
            }
        };

        btnlocdetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkPermissionFromDevice())
                    requestPermission();
                else {
                    /*int off = 0;
                    try {
                        off = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
                    } catch (Settings.SettingNotFoundException e) {
                        e.printStackTrace();
                    }
                    if(off==0){
                        Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(onGPS);
                    }*/
                    pgbar.setVisibility(View.VISIBLE);
                    getLocation();
                }
            }
        });

        btnUpdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sfullname, sDOB, sSex, sBgrp;
                sfullname = fullname.getText().toString();
                sDOB = DOB.getText().toString();
                sBgrp = bgcats[Bgrp.getSelectedItemPosition()];
                sSex = sexcats[sex.getSelectedItemPosition()];

                if (TextUtils.isEmpty(sfullname)) {
                    fullname.setError("Field is required");
                    return;
                }
                if (TextUtils.isEmpty(sDOB)) {
                    DOB.setError("Field is required");
                    return;
                }
                if(sBgrp == "Select"){
                    Toast.makeText(UserInfoUpdate.this,"Select Blood Group", Toast.LENGTH_LONG).show();
                    return;
                }
                if(sSex == "Select"){
                    Toast.makeText(UserInfoUpdate.this,"Select Sex", Toast.LENGTH_LONG).show();
                    return;
                }

                String userID = getIntent().getStringExtra("userID");

                DocumentReference documentReference = fStore.collection("users")
                        .document(userID);
                Map<String, Object> user = new HashMap<>();
                user.put("FullName", sfullname);
                user.put("DOB", sDOB);
                user.put("Sex", sSex);
                user.put("BloodGroup", sBgrp);
                user.put("Latitude", Lat);
                user.put("Longitude", Log);
                documentReference.set(user);

                Intent intent = new Intent(UserInfoUpdate.this, MainActivity.class);
                startActivity(intent);

            }
        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, REQUEST_PERMISSION_CODE);
    }

    private boolean checkPermissionFromDevice() {
        int loc1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int loc2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return loc1 == PackageManager.PERMISSION_GRANTED && loc2 == PackageManager.PERMISSION_GRANTED;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            pgbar.setVisibility(View.INVISIBLE);
            requestPermission();
            return;
        }
        pgbar.setVisibility(View.VISIBLE);
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {

                        Geocoder geocoder = new Geocoder(UserInfoUpdate.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        locdetect.setText(Html.fromHtml(addresses.get(0).getLocality()));
                        Lat = addresses.get(0).getLatitude();
                        Log = addresses.get(0).getLongitude();
                        pgbar.setVisibility(View.INVISIBLE);
                        btnUpdateInfo.setEnabled(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}