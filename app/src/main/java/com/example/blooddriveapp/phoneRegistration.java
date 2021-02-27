package com.example.blooddriveapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class phoneRegistration extends AppCompatActivity {

    private EditText editTextPhone;
    private Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_registration);

        editTextPhone = findViewById(R.id.editTextPhone);
        spinner = findViewById(R.id.countrycode);

        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));


        findViewById(R.id.btnVerifyPhone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = editTextPhone.getText().toString().trim();
                String ISD = CountryData.countryISDCodes[spinner.getSelectedItemPosition()];

                if(mobile.isEmpty() || mobile.length() < 10){
                    editTextPhone.setError("Enter a valid mobile");
                    editTextPhone.requestFocus();
                    return;
                }

                String mobileNO = "+" + ISD + mobile;

                Intent intent = new Intent(phoneRegistration.this, VerifyPhoneActivity.class);
                intent.putExtra("mobile", mobileNO);
                startActivity(intent);
            }
        });
    }
}