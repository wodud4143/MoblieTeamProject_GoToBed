package com.kotlinsun.mobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RealActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore dbMain; //파이어스토어 전역변수
    private ConstraintLayout setBtn;
    private String UidDate = "";

    private PieChart pieChart;
    String name;    // 현재 접속한 사람의 이름을 저장할 변수
    String uid;     // 현재 사용자의 UID -> sleepdata 가져오기 위함

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real);

        firebaseAuth = FirebaseAuth.getInstance();
        // Firestore 인스턴스 가져오기
        dbMain = FirebaseFirestore.getInstance();

        setBtn = findViewById(R.id.setBtn);

        updateLoginStatus();

        getUserName();

        fetchSleepData();

        //설정 버튼
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent SettingIntent = new Intent(RealActivity.this, SettingActivity.class);
                startActivity(SettingIntent);
            }
        });








    }

    private void updateLoginStatus() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            // 로그인 중인 경우
            UidDate = firebaseAuth.getCurrentUser().getUid(); //사용자 uid 받기
            System.out.println(uid);//디버깅용
        } else {
            // 로그아웃 상태인 경우
            ((TextView) findViewById(R.id.loginStatusTextView2)).setText("로그인 상태: 로그아웃");
            Intent maingo = new Intent(this, MainActivity.class);
            startActivity(maingo);
            finish();
        }
    }


    private void getUserName() {
        // 원하는 UID
        uid = UidDate;
        // "users" 컬렉션에서 해당 UID로 문서 가져오기
        DocumentReference docRef = dbMain.collection("users").document(uid);
        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // 문서가 존재하면 "name" 필드 값을 가져옴
                            name = document.getString("name");
                            System.out.println("Name: " + name);
                            ((TextView) findViewById(R.id.loginStatusTextView2)).setText(name + "님 안녕하세요");
                        } else {
                            System.out.println("해당 UID로 된 문서가 없습니다.");
                        }
                    } else {
                        System.out.println("문서 가져오기 실패: " + task.getException());
                    }
                });
    }

    private void fetchSleepData() {
        System.out.println(uid);
        dbMain.collection("sleepdata")
                .document(uid)//현재 사용자의 UID
                .collection("data")
                .document("2023-12-13")//이 부분에 가장 날짜가 최근인 문서를 넣어야함
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            //성공적으로 업무를 수행하면 어제의 수면 시간 데이터를 가져온다
                            String sleeptime = document.getString("sleep");
                            String wakeuptime = document.getString("wake");

                            System.out.println(sleeptime + " " + wakeuptime);//디버깅용
                            try {
                                displayPieChart(sleeptime,wakeuptime);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        else{
                            Log.d("Firestore", "Error getting documents: ", task.getException());

                        }
                    }
                });
    }

    private int calculateSleepDuration(int sleepTime, int wakeTime) {
        // 시간 데이터가 "HHmm" 형식으로 주어진다고 가정
        int sleepHour = sleepTime / 100;
        int sleepMinute = sleepTime % 100;

        int wakeHour = wakeTime / 100;
        int wakeMinute = wakeTime % 100;

        // 수면 시작 시간과 기상 시간을 분 단위로 변환
        int sleepTotalMinutes = sleepHour * 60 + sleepMinute;
        int wakeTotalMinutes = wakeHour * 60 + wakeMinute;

        // 수면 시간을 계산
        int sleepDurationMinutes;
        if (wakeTotalMinutes > sleepTotalMinutes) {
            sleepDurationMinutes = wakeTotalMinutes - sleepTotalMinutes;
        } else {
            // 다음 날로 넘어간 경우 (예: 취침 시간이 2200, 기상 시간이 0900)
            sleepDurationMinutes = (24 * 60 - sleepTotalMinutes) + wakeTotalMinutes;
        }

        return sleepDurationMinutes;
    }

    private void displayPieChart(String sleeptime, String wakeuptime) throws ParseException {

       if(sleeptime != null && wakeuptime != null){
           int sleep = Integer.parseInt(sleeptime);
           int wake  = Integer.parseInt(wakeuptime);
           int sleepDuration = calculateSleepDuration(sleep, wake);
           updatePieChart(sleepDuration);
       }
       else {
           updatePieChart(0);
       }

    }

    private void updatePieChart(int sleepDuration){
        PieChart pieChart = findViewById(R.id.pieChart);
        pieChart.clear();//그래프 정리
        if(sleepDuration > 0){
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(sleepDuration, "수면 시간"));
            entries.add(new PieEntry(24 * 60 - sleepDuration, "나머지 시간"));

            PieDataSet dataSet = new PieDataSet(entries, "어제의 수면 시간");
            dataSet.setColors(new int[]{Color.BLUE, Color.GRAY});
            dataSet.setValueTextSize(12f);

            PieData pieData = new PieData(dataSet);

            pieChart.setData(pieData);
            pieChart.setCenterText(String.format("%.1f시간", sleepDuration / 60.0));
            pieChart.getDescription().setEnabled(false);
            pieChart.invalidate();
        }
    }



}