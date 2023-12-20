package com.kotlinsun.mobileapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScreenActivity extends AppCompatActivity {
    private WindowManager.LayoutParams params;

    private Handler handler;



    private Button cancleBtn;

    private TextView sleepwaketimeTv;
    private TextView SleepStat;

    private SimpleDateFormat sdf;

    private String sleeptimeData;
    private String waketimeData;


    private View overlayView;
    private boolean isOverlayVisible = false;



    private boolean isBackPressed = false; // 뒤로가기 버튼이 눌렸는지 여부를 저장하는 변수

    private boolean test = false;

    private Switch testSwitch; // 스위치 변수 추가




    @SuppressLint({"MissingInflatedId", "UnspecifiedImmutableFlag"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        cancleBtn = findViewById(R.id.cancleBtn);
        sleepwaketimeTv = findViewById(R.id.sleepwaketimeTv);




        params = getWindow().getAttributes();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //화면 자동꺼짐 방지



        if (Build.VERSION.SDK_INT >= 21) {
            // 21 버전 이상일 때
            // 상태바 색상 통일
            getWindow().setStatusBarColor(Color.parseColor("#000000"));
        }



        SharedPreferences sharedPreferences2= getSharedPreferences("sleepwaketime", MODE_PRIVATE);    // test 이름의 기본모드 설정, 만약 test key값이 있다면 해당 값을 불러옴.
        sleeptimeData = sharedPreferences2.getString("sleeptime","");
        waketimeData = sharedPreferences2.getString("waketime","");

        sleeptimeData = formatTime(sleeptimeData);
        waketimeData = formatTime(waketimeData);
        System.out.println(sleeptimeData);
        System.out.println(waketimeData);

        

        //알람 설정
        Intent serviceIntent = new Intent(ScreenActivity.this, BackgroundService.class);
        serviceIntent.putExtra("alarm_time", waketimeData);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        sleepwaketimeTv.setText(sleeptimeData + " ~ " + waketimeData);

        SleepStat = findViewById(R.id.SleepStat); // 여기에 적절한 TextView ID를 사용하세요.
        sdf = new SimpleDateFormat("HH:mm");
        handler = new Handler(Looper.getMainLooper());

        // Runnable을 사용하여 주기적으로 업데이트
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateSleepStatText();
                handler.postDelayed(this, 1000); // 1초마다 업데이트
            }
        };

        handler.post(updateRunnable); // Runnable 시작



        SharedPreferences sharedPreferences= getSharedPreferences("alarmStat", MODE_PRIVATE);    // test 이름의 기본모드 설정
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putInt("Stat",1); //수면 측정중
        editor.commit();














        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 알람 서비스 중지
                Intent serviceIntent = new Intent(ScreenActivity.this, BackgroundService.class);
                stopService(serviceIntent);

                SharedPreferences sharedPreferences= getSharedPreferences("alarmStat", MODE_PRIVATE);    // test 이름의 기본모드 설정
                SharedPreferences.Editor editor= sharedPreferences.edit();
                editor.putInt("Stat",0); //수면 측정중
                editor.commit();

                finish();


            }
        });


        checkOverlayPermission();
        overlayView = LayoutInflater.from(this).inflate(R.layout.activity_lock, null);
        Button LockBtn2 = overlayView.findViewById(R.id.LockBtn2);
        LockBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideOverlayView();
            }
        });

        hideOverlayView();


        // 스위치를 레이아웃에서 찾아와 변수에 할당
        testSwitch = findViewById(R.id.lockSwitch);

        // 스위치의 상태 변경 이벤트를 처리
        testSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 스위치 상태에 따라 test 변수 값을 변경
                test = isChecked;

                // test 변수에 따라 showOverlayView 또는 hideOverlayView 호출
                if (test) {
                    test = true;
                } else {
                    test = false;
                }
            }
        });


    }

    private void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 0);
        }
    }

    private void showOverlayView() {
        overlayView.setOnTouchListener(new View.OnTouchListener() {

            private float previousX = 0;
            private float previousY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        previousX = event.getRawX();
                        previousY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - previousX;
                        float deltaY = event.getRawY() - previousY;
                        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) v.getLayoutParams();
                        layoutParams.x += (int) deltaX;
                        layoutParams.y += (int) deltaY;
                        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                        windowManager.updateViewLayout(v, layoutParams);
                        previousX = event.getRawX();
                        previousY = event.getRawY();
                        return true;
                }
                return false;
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(overlayView, params);
        isOverlayVisible = true;
    }

    private void hideOverlayView() {
        if (overlayView.isAttachedToWindow()) {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeView(overlayView);
            isOverlayVisible = false;
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideOverlayView();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        hideOverlayView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed) {
            // 뒤로가기 버튼을 눌렀을 때가 아니라면 showOverlayView 호출

            if (test) {
                showOverlayView();
            }
            else hideOverlayView();
        }
    }





    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true; // 뒤로가기 버튼이 눌렸음을 표시
        hideOverlayView();
        finish();
    }



    private String formatTime(String time) {
        if (time.length() == 4) {
            // "HHmm" 형식인 경우
            String hour = time.substring(0, 2);
            String minute = time.substring(2, 4);
            return hour + ":" + minute;
        } else {

            return time;
        }
    }


    private void updateSleepStatText() {
        // SharedPreferences에서 가져온 잠자리 시간 및 깨어날 시간

        // 현재 시간 구하기
        String currentTimeString = sdf.format(new Date());

        // 시간 문자열을 Date 객체로 변환
        Date sleepTime, wakeTime, currentTime;
        try {
            sleepTime = sdf.parse(sleeptimeData);
            wakeTime = sdf.parse(waketimeData);
            currentTime = sdf.parse(currentTimeString);

            // 현재 시간과 잠자리 시간, 깨어날 시간 비교
            // 잠자리 시간이 깨어날 시간보다 뒤에 오면, 깨어날 시간을 다음 날로 설정
            if (sleepTime.after(wakeTime)) {
                Calendar c = Calendar.getInstance();
                c.setTime(wakeTime);
                c.add(Calendar.DATE, 1);
                wakeTime = c.getTime();
            }

// 현재 시간과 잠자리 시간, 깨어날 시간 비교
            if ((currentTime.after(sleepTime) || currentTime.equals(sleepTime)) && currentTime.before(wakeTime)) {
                // 현재 수면 중
                SleepStat.setText("수면 측정 중");
                cancleBtn.setText("수면 측정 취소");
            } else if (currentTime.after(wakeTime) || currentTime.equals(wakeTime)) {
                SleepStat.setText("수면 측정 완료");
                cancleBtn.setText("수면 측정 종료");

            } else {
                // 아직 수면 시간이 아님
                long timeDifference = sleepTime.getTime() - currentTime.getTime();
                long hours = timeDifference / (60 * 60 * 1000);
                long minutes = (timeDifference % (60 * 60 * 1000)) / (60 * 1000);

                String sleepStatText = String.format("잠자리까지 %d시간 %d분 남았습니다.", hours, minutes);
                SleepStat.setText(sleepStatText);
                cancleBtn.setText("수면 측정 취소");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
















}