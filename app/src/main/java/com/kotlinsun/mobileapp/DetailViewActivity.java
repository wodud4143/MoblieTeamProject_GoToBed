package com.kotlinsun.mobileapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.DayViewDecorator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class DetailViewActivity extends AppCompatActivity {

    String uid;     // 현재 사용자의 UID -> sleepdata 가져오기 위함
    private String UidDate = "";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore dbMain; //파이어스토어 전역변수
    private FirebaseFirestore db;


    TextView nameView;
    private MaterialCalendarView calendarView;
    TextView checkMark;
    private ArrayList<CalendarDay> calendarDayList = new ArrayList<>();
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        db = FirebaseFirestore.getInstance();
        calendarView = findViewById(R.id.calendarView);
        calendarView.setSelectedDate(CalendarDay.today());
        fetchData(); // 달력에 동그라미 체크
    }
    private void fetchData() {
        // 파이어스토어에서 데이터 가져오기
        db.collection("sleepdata")
                .document(uid)
                .collection("data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {


                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 각 문서의 이름을 리스트에 추가
                                String[] dateParts = document.getId().split("-");
                                int year = Integer.parseInt(dateParts[0]);
                                int month = Integer.parseInt(dateParts[1]);
                                int day = Integer.parseInt(dateParts[2]);

                                calendarDayList.add(CalendarDay.from(year,month,day));

                                Decorator decorator = new Decorator(calendarDayList,DetailViewActivity.this);
                                calendarView.addDecorator(decorator);
                            }

                        } else {


                        }
                    }
                });
    }



}



