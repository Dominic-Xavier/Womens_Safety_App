package com.myapp.womenssafetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Button save;
    EditText emergency_Contact, message1, message2, message3;
    String emergencyContact, messageNO1, messageNO2, messageNO3;
    private static final int REQ_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emergency_Contact = findViewById(R.id.CallNumber);
        message1 = findViewById(R.id.contact1);
        message2 = findViewById(R.id.contact2);
        message3 = findViewById(R.id.contact3);
        save = findViewById(R.id.save);

        save.setOnClickListener((v)-> {
                emergencyContact = emergency_Contact.getText().toString();
                messageNO1 = message1.getText().toString();
                messageNO2 = message2.getText().toString();
                messageNO3 = message3.getText().toString();

                if(isValidPhoeNumber(emergencyContact.trim()) && isValidPhoeNumber(messageNO1.trim()) && isValidPhoeNumber(messageNO2.trim()) && isValidPhoeNumber(messageNO3.trim())){
                    Toast.makeText(this, "All Phone number is valid",Toast.LENGTH_SHORT).show();
                    if (!is_SamePhone_Number_Repeating(emergencyContact.trim(), messageNO1.trim(), messageNO2.trim(), messageNO3.trim())){
                        if(ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION) +
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) +
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.CALL_PHONE)+
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){

                            //When Permission Not granted
                            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this ,
                                    Manifest.permission.ACCESS_FINE_LOCATION) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                        Manifest.permission.ACCESS_COARSE_LOCATION) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                        Manifest.permission.CALL_PHONE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                        Manifest.permission.SEND_SMS)){
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("Grant Permissions")
                                        .setMessage("Please Grant permissions for all the thinngs")
                                        .setPositiveButton("Ok", (DialogInterface dialog, int which) -> {
                                                ActivityCompat.requestPermissions(MainActivity.this,
                                                        new String[]{
                                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                                                Manifest.permission.CALL_PHONE,
                                                                Manifest.permission.SEND_SMS
                                                        }, REQ_CODE);
                                        })
                                        .setNegativeButton("Cancel", null);
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        }
                        else {
                            //When permission is already granted
                            Toast.makeText(this,"Permission already Granted", Toast.LENGTH_SHORT).show();
                            Uri call = Uri.parse("tel:" + emergencyContact);
                            Intent surf = new Intent(Intent.ACTION_CALL, call);
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(this, "Call Phone permission is required...!",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            startActivity(surf);
                            ContextCompat.startForegroundService(MainActivity.this, new Intent(this, LocationService.class));
                        }
                    }
                    else
                        Toast.makeText(this, "Repeating phone numbers are not allowed",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(this, "Phone number is invalid",Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQ_CODE){
            if(grantResults.length>0
                && (grantResults[0]
                +grantResults[1]
                +grantResults[2]
                +grantResults[3] == PackageManager.PERMISSION_GRANTED)){
                    //Permission are granted
                    Toast.makeText(this, "Permissions are granted", Toast.LENGTH_SHORT).show();
            }
            else
                //Permission not granted
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidPhoeNumber(String phoneNumber) {
        Pattern p = Pattern.compile("[6-9][0-9]{9}");
        Matcher m = p.matcher(phoneNumber);
        return m.matches();
    }

    private boolean is_SamePhone_Number_Repeating(String emergencyContact, String messageNO1, String messageNO2, String messageNO3) {
        boolean numberpresent = false;
        String[] arr = new String[4];
        arr[0] = emergencyContact;
        arr[1] = messageNO1;
        arr[2] = messageNO2;
        arr[3] = messageNO3;
        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i].equals(arr[j])) {
                    numberpresent = true;
                    break;
                }
            }
        }
        return numberpresent;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        return super.dispatchGenericMotionEvent(ev);
    }
}