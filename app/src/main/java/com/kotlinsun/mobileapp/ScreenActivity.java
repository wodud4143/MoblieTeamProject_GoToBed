package com.kotlinsun.mobileapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScreenActivity extends AppCompatActivity {
    private WindowManager.LayoutParams params;
    private float origin;

    private Handler handler;

    private View overlayView;
    private boolean isOverlayVisible = false;

    private Button test2;



    private boolean isBackPressed = false; // 뒤로가기 버튼이 눌렸는지 여부를 저장하는 변수



    @SuppressLint({"MissingInflatedId", "UnspecifiedImmutableFlag"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        test2 = findViewById(R.id.test2);




        params = getWindow().getAttributes();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //화면 자동꺼짐 방지



        if (Build.VERSION.SDK_INT >= 21) {
            // 21 버전 이상일 때
            // 상태바 색상 통일
            getWindow().setStatusBarColor(Color.parseColor("#000000"));
        }

        //알람 설정
        Intent serviceIntent = new Intent(ScreenActivity.this, BackgroundService.class);
        serviceIntent.putExtra("alarm_time", "06:17");
        startService(serviceIntent);

        SharedPreferences sharedPreferences= getSharedPreferences("alarmStat", MODE_PRIVATE);    // test 이름의 기본모드 설정
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putInt("Stat",1); //수면 측정중
        editor.commit();







        // 화면 밝기 제어 (가장 어둡게)
        origin = params.screenBrightness;
        params.screenBrightness = 0.5f;
        getWindow().setAttributes(params);





// 인텐트에서 데이터 가져오기
        Intent intent = getIntent();
        String selectedTime = intent.getStringExtra("selectedTime");
        String currentDate = intent.getStringExtra("currentDate");
        System.out.println(selectedTime);
        System.out.println(currentDate);

        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 알람 서비스 중지
                Intent serviceIntent = new Intent(ScreenActivity.this, BackgroundService.class);
                stopService(serviceIntent);

                SharedPreferences sharedPreferences= getSharedPreferences("alarmStat", MODE_PRIVATE);    // test 이름의 기본모드 설정
                SharedPreferences.Editor editor= sharedPreferences.edit();
                editor.putInt("Stat",0); //수면 취소 및 종료
                editor.commit();

                finish();
            }
        });



    }


    private void doFullScreen() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE|
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                        View.SYSTEM_UI_FLAG_FULLSCREEN);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed) {
            // 뒤로가기 버튼을 눌렀을 때가 아니라면 showOverlayView 호출
        }
    }


    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true; // 뒤로가기 버튼이 눌렸음을 표시
        finish();
    }



















}