package com.kotlinsun.mobileapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackgroundService extends Service {
    private static final String TAG = "BackgroundService";
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private String alarmTime; // 알람 시간을 저장할 변수

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");
        mediaPlayer = MediaPlayer.create(this, R.raw.sam); // "sam.mp3" 파일을 res/raw 폴더에 넣어주세요.
        mediaPlayer.setLooping(false);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");

        if (intent != null) {
            alarmTime = intent.getStringExtra("alarm_time");
        }

        // Foreground 서비스로 설정
        startForeground(1, createNotification());

        handler.postDelayed(checkTimeRunnable, 1000); // 1초마다 시간을 확인합니다.

        return START_STICKY;
    }



    private Runnable checkTimeRunnable = new Runnable() {
        @Override
        public void run() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date());

            try {
                Date currentTimeDate = sdf.parse(currentTime);
                Date alarmStartTime = sdf.parse(alarmTime + ":01");
                Date alarmEndTime = sdf.parse(alarmTime + ":59");

                if (currentTimeDate.after(alarmStartTime) && currentTimeDate.before(alarmEndTime)) {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        startMainActivity();
                    }
                } else {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            handler.postDelayed(this, 1000); // 1초마다 시간을 다시 확인합니다.
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service onDestroy");
        mediaPlayer.stop();
        mediaPlayer.release();
        handler.removeCallbacks(checkTimeRunnable);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, ScreenOutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id",
                    "알람 서비스",
                    NotificationManager.IMPORTANCE_NONE // 알림 없음
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        return new Notification.Builder(this, "channel_id").build();
    }


}

