package com.myapp.womenssafetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Button save;
    Intent intent;
    EditText emergency_Contact, message1, message2, message3;
    String emergencyContact, messageNO1, messageNO2, messageNO3;
    private static boolean permission_granted = false;
    private static final int REQ_CODE = 0;
    int i = 0;
    private static int counter = 0;
    private SensorManager sm;
    private float acelVal, acelLast, shake;
    private final static String log = "Main Activity";
    private Switch toggle_button;
    private LocationRequest locationRequest;

    Sqlite sqlite = new Sqlite(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emergency_Contact = findViewById(R.id.CallNumber);
        message1 = findViewById(R.id.contact1);
        message2 = findViewById(R.id.contact2);
        message3 = findViewById(R.id.contact3);
        save = findViewById(R.id.save);
        toggle_button = findViewById(R.id.toggle_buttons);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sensorEventListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        acelVal = SensorManager.GRAVITY_EARTH;
        acelLast = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;

        intent = new Intent(MainActivity.this, LocationService.class);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        toggle_button.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked == false) {
                stopService(intent);
                Toast.makeText(this, "Service stopped...!", Toast.LENGTH_SHORT).show();
            }
        });

        save.setOnClickListener((v) -> {
            emergencyContact = emergency_Contact.getText().toString();
            messageNO1 = message1.getText().toString();
            messageNO2 = message2.getText().toString();
            messageNO3 = message3.getText().toString();

            if (isValidPhoeNumber(emergencyContact.trim()) && isValidPhoeNumber(messageNO1.trim()) && isValidPhoeNumber(messageNO2.trim()) &&
                    isValidPhoeNumber(messageNO3.trim())) {
                if (!is_SamePhone_Number_Repeating(emergencyContact.trim(), messageNO1.trim(), messageNO2.trim(), messageNO3.trim())) {
                    sqlite.saveData(emergencyContact, messageNO1, messageNO2, messageNO3);
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) +
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.CALL_PHONE) +
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.SEND_SMS) +
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        //When Permission Not granted
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                        Manifest.permission.CALL_PHONE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                        Manifest.permission.SEND_SMS) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                        Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Grant Permissions")
                                    .setMessage("Please Grant all permissions")
                                    .setPositiveButton("Ok", (DialogInterface dialog, int which) -> {
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.CALL_PHONE,
                                                        Manifest.permission.SEND_SMS,
                                                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                                }, REQ_CODE);
                                    })
                                    .setNegativeButton("Cancel", null);
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.CALL_PHONE,
                                            Manifest.permission.SEND_SMS,
                                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                    }, REQ_CODE);
                        }
                    } else {
                            /*intent = new Intent(this, LocationService.class);
                            intent.putExtra("callNumber", emergencyContact);
                            ContextCompat.startForegroundService(MainActivity.this, intent);*/
                    }
                } else
                    Toast.makeText(this, "Repeating phone numbers are not allowed", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "Check all the Phone Numbers", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_CODE) {
            if (grantResults.length > 0
                    && (grantResults[0]
                    + grantResults[1]
                    + grantResults[2]
                    + grantResults[3] == PackageManager.PERMISSION_GRANTED)) {
                //Permission are granted
                //ContextCompat.startForegroundService(MainActivity.this, intent);
                permission_granted = true;
                Toast.makeText(this, "Permissions are granted", Toast.LENGTH_SHORT).show();

            } else
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

    private final SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            acelLast = acelVal;
            acelVal = (float) Math.sqrt((double) (x * x) + (y * y) + (z * z));
            float delta = acelVal - acelLast;
            shake = shake * 0.9f + delta;

            if (shake > 12) {
                counter++;
            }

            if (counter >= 3) {
                counter = 0;
                intent.putExtra("callNumber", emergencyContact);


                Log.i(log, "Dont Shake Your phone");
                if (isGPSEnabled()) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this, "Location permission is disabled", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                            .removeLocationUpdates(this);
                                    if(locationResult !=null && locationResult.getLocations().size() >0){
                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();

                                        Log.i(log, "latitude "+latitude + " longitude"+longitude);
                                        intent.putExtra("URL", getMapLink(latitude,longitude));
                                        toggle_button.setChecked(true);
                                        ContextCompat.startForegroundService(MainActivity.this, intent);
                                    }
                                }
                            }, Looper.getMainLooper());
                }
                else
                    turnOnGPS();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled;
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }

    void turnOnGPS() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            Toast.makeText(MainActivity.this, "Sorry....!  Your device has no location",Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }

    public String getMapLink(double sLatitude, double sLongitude) {
        String url = "https://maps.google.com/?q="+sLatitude+","+sLongitude;
        return url;
    }
}