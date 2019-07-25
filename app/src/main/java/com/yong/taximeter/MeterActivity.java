package com.yong.taximeter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class MeterActivity extends AppCompatActivity implements LocationListener {
    int i = 0;

    int defaultCost = 3800;          // 기본요금
    int runningCost = 100;          // 주행요금
    int timeCost = 100;             // 시간요금 (시속 15km 이하)
    int defaultCostDistance = 2000;  // 기본요금 주행 거리
    int runningCostDistance = 132;  // 주행요금 추가 기준 거리
    int timeCostSecond = 31;       // 시간요금 추가 기준 시간
    int currentCost = defaultCost;    // 계산된 최종 요금

    int addNight = 20;                // 심야할증 비율
    int addOutCity = 20;              // 시외할증 비율

    boolean isNight = false;
    boolean isOutCity = false;

    double distanceForAdding = 0;
    double timeForAdding = 0;

    double sumDistance = 0;          // 총 이동거리
    double sumTime = 0;              // 총 이동시간

    private ImageView ivHorse;
    private LocationManager locationManager;
    private Location mLastlocation = null;
    private TextView tvCost, tvDistance, tvSpeed, tvTime, tvType;
    private ToggleButton isNightButton, isOutCityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter);

        ivHorse = findViewById(R.id.meter_image_horse);
        tvCost = findViewById(R.id.tvCost);
        tvDistance = findViewById(R.id.tvDistance);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvTime = findViewById(R.id.tvTime);
        tvType = findViewById(R.id.tvType);
        isNightButton = findViewById(R.id.isNight);
        isOutCityButton = findViewById(R.id.isOuterCity);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/digital_num.ttf");
        tvCost.setTypeface(typeFace);
        tvDistance.setTypeface(typeFace);
        tvSpeed.setTypeface(typeFace);
        tvTime.setTypeface(typeFace);
        tvType.setTypeface(typeFace);

        tvCost.setText(currentCost + "원");
        tvDistance.setText(sumDistance + "km");
        tvSpeed.setText("0.0km/s");
        tvTime.setText(sumTime + "초");
        tvType.setText("기본요금");

        isNightButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    Snackbar.make(getWindow().getDecorView().getRootView(), "최종 금액에 심야할증이 적용됩니다.", Snackbar.LENGTH_SHORT).show();
                }else{
                    Snackbar.make(getWindow().getDecorView().getRootView(), "심야할증 적용이 해제됩니다.", Snackbar.LENGTH_SHORT).show();
                }
                isNight = isChecked;
            }
        });
        isOutCityButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    Snackbar.make(getWindow().getDecorView().getRootView(), "최종 금액에 시외할증이 적용됩니다.", Snackbar.LENGTH_SHORT).show();
                }else{
                    Snackbar.make(getWindow().getDecorView().getRootView(), "시외할증 적용이 해제됩니다.", Snackbar.LENGTH_SHORT).show();
                }
                isOutCity = isChecked;
            }
        });

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }
 
    @Override
    public void onLocationChanged(Location location) {
        double deltaDistance = 0;
        double deltaTime = 0;
        double getSpeed = (Double.parseDouble(String.format(Locale.getDefault(), "%.3f", location.getSpeed()))) * 3600 / 1000;

        if(mLastlocation != null) {
            deltaDistance = getSpeed;
            deltaTime = (location.getTime() - mLastlocation.getTime()) / 1000.0;
            sumDistance += deltaDistance;
            sumTime += deltaTime;

            // 이동거리가 기본요금 거리 이상인지 확인
            if(sumDistance > defaultCostDistance){
                // 속도에 따라 거리요금 / 시간요금 선택 적용
                if(getSpeed < 15){
                    if(timeForAdding >= timeCostSecond){
                        currentCost += timeCost * Math.round(timeForAdding / timeCostSecond);
                        timeForAdding = 0;
                    }else{
                        timeForAdding += deltaTime;
                    }
                    tvType.setText("시간요금");
                }else{
                    if(distanceForAdding >= runningCostDistance){
                        currentCost += runningCost * Math.round(distanceForAdding / runningCostDistance);
                        distanceForAdding = 0;
                    }else{
                        distanceForAdding += deltaDistance;
                    }
                    tvType.setText("주행요금");
                }
            }else{
                tvType.setText("기본요금");
            }

            currentCost = currentCost / 100;
            tvCost.setText(String.format(Locale.getDefault(), "%d원", currentCost));
            tvDistance.setText(String.format(Locale.getDefault(), "%.1fkm", sumDistance/1000));
            tvSpeed.setText(String.format(Locale.getDefault(), "%.1fkm/s", getSpeed));
            tvTime.setText(String.format(Locale.getDefault(), "%.0f초", sumTime));

            runHorse(Math.round(getSpeed));
        }
        mLastlocation = location;
    }
 
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
 
    }
 
    @Override
    public void onProviderEnabled(String provider) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0, this);
    }
 
    @Override
    public void onProviderDisabled(String provider) {
 
    }

    public void startCount(View v){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0, this);
    }

    public void stopCount(View v){
        locationManager.removeUpdates(this);

        if(isOutCity){
            Log.i("OutCity", "TRUE");
            currentCost = currentCost * 120/100;
        }
        if(isNight){
            Log.i("Night", "TRUE");
            currentCost = currentCost * 120/100;
        }
        currentCost = (currentCost + 50) / 100 * 100;

        AlertDialog.Builder stopDialog = new AlertDialog.Builder(this);
        stopDialog.setTitle("운행이 종료되었습니다");
        stopDialog.setMessage("총 요금 : " + currentCost + "\n운행 시간 : " + sumTime + "\n이동 거리 : " + sumDistance);
        stopDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        stopDialog.show();
    }

    public void runHorse(long speed){
        AnimationDrawable animationDrawable = new AnimationDrawable();

        if(speed > 60){
            for(int i = 0; i < 7; i++){
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 47);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 47);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 48);
                animationDrawable.setOneShot(true);
            }
        }else if(speed > 40){
            for(int i = 0; i < 5; i++){
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 66);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 66);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 68);
                animationDrawable.setOneShot(true);
            }
        }else if(speed > 20){
            for(int i = 0; i < 3; i++){
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 111);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 111);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 111);
                animationDrawable.setOneShot(true);
            }
        }else if(speed > 0){
            for(int i = 0; i < 2; i++){
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_1), 166);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 166);
                animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_3), 168);
                animationDrawable.setOneShot(true);
            }
        }else{
            animationDrawable.addFrame(getResources().getDrawable(R.drawable.ic_horse_2), 1000);
            animationDrawable.setOneShot(true);
        }

        ivHorse.setBackground(animationDrawable);
        animationDrawable.start();
    }
}