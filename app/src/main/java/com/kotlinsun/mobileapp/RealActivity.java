package com.kotlinsun.mobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;
import com.skydoves.balloon.TextForm;

public class RealActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore dbMain; //파이어스토어 전역변수
    private ConstraintLayout setBtn;
    private String UidDate = "";


    private PieChart pieChart;
    String name;    // 현재 접속한 사람의 이름을 저장할 변수
    String uid;     // 현재 사용자의 UID -> sleepdata 가져오기 위함
    TextView date;

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
           updatePieChart(sleepDuration,sleep,wake);
       }
       else {
           int sleep = Integer.parseInt(sleeptime);
           int wake  = Integer.parseInt(wakeuptime);
           updatePieChart(0,sleep,wake);
       }
    }


    private void updatePieChart(int sleepDuration,int sleep, int wake){
        PieChart pieChart = findViewById(R.id.pieChart);
        pieChart.clear();//그래프 정리

        if(sleepDuration > 0){
            // 현재 날짜를 가져오는 방법
            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();

            // SimpleDateFormat을 사용하여 날짜를 원하는 형식으로 포맷팅
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = dateFormat.format(currentDate);
            TextView DateToday = (TextView) findViewById(R.id.DateToday);
            DateToday.setText(formattedDate);

            // 그래프 설정

            List<PieEntry> entries = new ArrayList<>();

            entries.add(new PieEntry(sleepDuration, "수면 시간"));
            entries.add(new PieEntry(24 * 60 - sleepDuration, "활동 시간"));

            PieDataSet dataSet = new PieDataSet(entries,"");

            dataSet.setColors(new int[]{Color.rgb(107,191,128), Color.rgb(24,124,243)});
            dataSet.setValueTextSize(12f);
            dataSet.setDrawValues(false);

            PieData pieData = new PieData(dataSet);
            pieChart.setHoleRadius(20f); // 예시에서는 0.5f
            dataSet.setSliceSpace(5.05f);
            pieChart.setData(pieData);
            pieChart.getDescription().setEnabled(false);
            pieChart.invalidate();
            pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    if(e == null){
                        return ;
                    }
                    PieEntry entry = (PieEntry) e;
                    String css = entry.getLabel();
                    if(css.equals("활동 시간")){
                        elseshowballon(sleep,wake+1,pieChart);
                    }
                    if(css.equals("수면 시간")){
                        showballon(sleep,wake,pieChart);
                    }
                }

                @Override
                public void onNothingSelected() {

                }
            });
        }
        else{

        }
    }

    private  void showballon(int sleep, int wake, View pie){
        Context context = this;
        String start =String.valueOf(sleep/100);
        String end = String.valueOf(wake/100);
        Balloon balloon = new Balloon.Builder(context)
                .setText( start + "시 ~ " + end +"시")
                .setArrowSize(10)
                .setArrowOrientation(ArrowOrientation.BOTTOM)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setArrowPosition(0.1f)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(65)
                .setTextSize(15f)
                .setCornerRadius(4f)
                .setBackgroundColor(Color.rgb(255,255,255))
                .setAlpha(0.9f)
                .setTextColor(ContextCompat.getColor(context, R.color.black))
                .setTextIsHtml(true)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setArrowPosition(0.3f)
                .build();

        balloon.showAlignStart(pie,800,-300);
    }

    private void elseshowballon(int sleep, int wake, View pie){
        Context context = this;
        String start =String.valueOf(sleep/100);
        String end = String.valueOf(wake/100);
        Balloon balloon = new Balloon.Builder(context)
                .setText( end + "시 ~ " + start +"시")
                .setArrowSize(10)
                .setArrowOrientation(ArrowOrientation.BOTTOM)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setArrowPosition(0.9f)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(65)
                .setTextSize(15f)
                .setCornerRadius(4f)
                .setBackgroundColor(Color.rgb(255,255,255))
                .setAlpha(0.9f)
                .setTextColor(ContextCompat.getColor(context, R.color.black))
                .setTextIsHtml(true)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setArrowPosition(0.3f)
                .build();

        balloon.showAlignStart(pie,400,-300);
    }
}