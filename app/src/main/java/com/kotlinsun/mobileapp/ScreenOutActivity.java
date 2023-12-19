package com.kotlinsun.mobileapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class ScreenOutActivity extends AppCompatActivity {

    private Button LockBtn2;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_out);

        LockBtn2 =findViewById(R.id.LockBtn2);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(ScreenOutActivity.this, null);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }


        LockBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 알람 서비스 중지
                Intent serviceIntent = new Intent(ScreenOutActivity.this, BackgroundService.class);
                stopService(serviceIntent);

                SharedPreferences sharedPreferences= getSharedPreferences("alarmStat", MODE_PRIVATE);
                SharedPreferences.Editor editor= sharedPreferences.edit();
                editor.putInt("Stat",0); //수면 취소 및 종료
                editor.commit();

                finish();
            }
        });

    }
}