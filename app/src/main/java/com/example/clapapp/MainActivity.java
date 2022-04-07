package com.example.clapapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity  implements SensorEventListener {

    SensorManager sensorManager;
    Sensor proximitySensor;
    int clapCount=0;
    private static final int CAMERA_REQUEST_CODE = 100;

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if(proximitySensor==null) {
            Toast.makeText(getApplicationContext(), "This device doesn't have proximity sensor", Toast.LENGTH_SHORT).show();
        } else if (!checkCameraHardware(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "This device doesn't have a camera", Toast.LENGTH_SHORT).show();
        } else {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
        TextView v=findViewById(R.id.clap_count);
        v.setText("0");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    protected void onPause()    {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onStop(){
        super.onStop();
        setLight(false);
    }

    protected void onDestroy(){
        super.onDestroy();
        setLight(false);
    }
    protected void onResume()   {
        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private void setLight(boolean status) {
        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = camManager.getCameraIdList()[0]; // Usually front camera is at 0 position and back camera is 1.
            camManager.setTorchMode(cameraId, status);
        } catch (Exception e) {
            Log.e("Torch", String.format("Exception while genabling torch: %s", e) );
        }
    }
    private void turnLightOn() {
        setLight(true);
    }

    private void turnLightOff() {
        setLight(false);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView clap_count = findViewById(R.id.clap_count);
        System.out.println("Sensor event "+event.sensor.getName()+" "+ event.values[0]);
        if (event.values[0] == 0) {
            //increment Clap
            clapCount++;
            clap_count.setText(String.format("%s",  clapCount ));
            turnLightOn();
        }
        else{
            //turn off torch
            turnLightOff();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor == proximitySensor) {
            switch (accuracy) {
                case 0:
                    Toast.makeText(getApplicationContext(), "Unreliable", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), "Low Accuracy", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), "Medium Accuracy", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(), "High Accuracy", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}