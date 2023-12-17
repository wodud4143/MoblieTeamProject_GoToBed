package com.kotlinsun.mobileapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.DayViewDecorator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.checkerframework.checker.units.qual.A;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DetailViewActivity extends AppCompatActivity {

    String uid;     // 현재 사용자의 UID -> sleepdata 가져오기 위함
    private String UidDate = "";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore dbMain; //파이어스토어 전역변수
    private FirebaseFirestore db;


    HorizontalBarChart BarChart;
    List<String> documentIds = new ArrayList<>();
    private MaterialCalendarView calendarView;

    private ArrayList<CalendarDay> calendarDayList = new ArrayList<>();

    TextView date2;
    TextView date3;
    TextView date4;

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

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                int year = date.getYear();
                int month = date.getMonth(); // 월은 0부터 시작하므로 1을 더해줍니다.
                int day = date.getDay();
                BarChart = findViewById(R.id.BarChart);
                String dateString = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
                System.out.println(dateString);
                if (documentIds.contains(dateString)) {
                    System.out.println(dateString);
                    BarChart.setVisibility(View.VISIBLE);
                    updateBar(dateString);
                } else {
                    BarChart.setVisibility(View.GONE);
                    BarChart.invalidate();
                }

            }
        });
    }

    private void updateBar(String dateString) {
        db.collection("sleepdata")
                .document(uid)
                .collection("data")
                .document(dateString)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            System.out.println(document);
                            if (document.exists()) {
                                // 문서가 존재하면 데이터를 가져와서 막대그래프 업데이트
                                Map<String, Object> data = document.getData();
                                updateBarChart(data);
                            } else {
                                // 문서가 존재하지 않으면 해당 날짜에 데이터가 없음을 알림
                                Toast.makeText(DetailViewActivity.this, "수면 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // 에러 처리
                            Toast.makeText(DetailViewActivity.this, "수면 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
                                documentIds.add(document.getId());
                                String[] dateParts = document.getId().split("-");
                                int year = Integer.parseInt(dateParts[0]);
                                int month = Integer.parseInt(dateParts[1]);
                                int day = Integer.parseInt(dateParts[2]);

                                calendarDayList.add(CalendarDay.from(year, month, day));

                                Decorator decorator = new Decorator(calendarDayList, DetailViewActivity.this);
                                calendarView.addDecorator(decorator);
                            }

                        }

                    }
                });
    }

    private void updateBarChart(Map<String, Object> data) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HHmm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH시 mm분", Locale.getDefault());

            String sleep = (String) data.get("sleep");
            String wake = (String) data.get("wake");
            String date = (String) data.get("date");

            String sleep_output = convertTimeFormat(sleep);
            String wake_output = convertTimeFormat(wake);

            date2 = findViewById(R.id.date2);
            date3 = findViewById(R.id.date3);
            date4 = findViewById(R.id.date4);

            date2.setText(date);
            date3.setText("수면 시간: "+sleep_output);
            date4.setText("기상 시간: "+wake_output);

            if (sleep != null && wake != null) {
                int sleepX = Integer.parseInt(sleep);
                int wakeX = Integer.parseInt(wake);

                int totalHours = 24;
                float sleepPercentage = (float) sleepX / (sleepX + wakeX);
                float wakePercentage = (float) wakeX / (sleepX + wakeX);

                List<BarEntry> entries = new ArrayList<>();
                entries.add(new BarEntry(1f, sleepPercentage*100, "수면 시간"));
                entries.add(new BarEntry(0f, wakePercentage*100, "활동 시간"));

                // BarDataSet 생성 및 설정
                BarDataSet dataSet = new BarDataSet(entries, "수면 및 활동 시간");
                dataSet.setColors(new int[]{Color.rgb(107, 191, 128), Color.rgb(24, 124, 243)});
                dataSet.setDrawIcons(false);
                dataSet.setDrawValues(false);

                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.5f);
                barData.setValueTextSize(10);

                BarChart.getDescription().setEnabled(false);
                BarChart.setTouchEnabled(false);
                BarChart.getLegend().setEnabled(false);
                BarChart.setExtraOffsets(10f, 0f, 40f, 0f);

                // X 축 설정
                XAxis xAxis = BarChart.getXAxis();
                xAxis.setDrawAxisLine(false);
                xAxis.setGranularity(1f);
                xAxis.setTextSize(15f);
                xAxis.setGridLineWidth(24f);
                xAxis.setGridColor(Color.parseColor("#80E5E5E5"));
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(new XAxisValueFormatter());

                // Y 축 설정
                YAxis axisLeft = BarChart.getAxisLeft();
                axisLeft.setDrawGridLines(false);
                axisLeft.setDrawAxisLine(false);
                axisLeft.setAxisMinimum(0f);
                axisLeft.setAxisMaximum(30f);
                axisLeft.setGranularity(3f);
                axisLeft.setDrawLabels(false);

                YAxis axisRight = BarChart.getAxisRight();
                axisRight.setTextSize(5f);
                axisRight.setDrawLabels(false);
                axisRight.setDrawGridLines(false);
                axisRight.setDrawAxisLine(false);

                BarChart.setData(barData);
                BarChart.invalidate();
            } else {
                BarChart.setVisibility(View.GONE);
                BarChart.invalidate();
            }
        } catch (Exception e) {
            Log.e("GraphError", "Error updating bar chart", e);
            throw new RuntimeException(e);
        }
    }

    public static String convertTimeFormat(String inputTime) {
        try {
            // 입력된 시간을 Date 객체로 파싱
            SimpleDateFormat inputFormat = new SimpleDateFormat("HHmm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH시 mm분", Locale.getDefault());
            Date date = inputFormat.parse(inputTime);

            // 변환된 형식으로 문자열 반환
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 변환 실패 시 원본 문자열 반환
        return inputTime;
    }
    private static class XAxisValueFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            // 여기에서 value는 막대의 위치를 나타냅니다. 적절한 라벨을 반환하도록 구현하세요.
            // 예를 들어, 막대의 위치가 0이면 "수면 시간", 1이면 "활동 시간" 등을 반환할 수 있습니다.
            if (value == 0f) {
                return "수면 시간";
            } else if (value == 1f) {
                return "활동 시간";
            }
            return super.getAxisLabel(value, axis); // 다른 경우에는 부모 메서드의 반환값 사용
        }
    }
}




