package com.kotlinsun.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class CalculateActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculate_sleep);
        final String[] getDate = new String[1];
        Intent Intentdata = getIntent();
        String uid = Intentdata.getStringExtra("uid");

        ArrayList<String> times = new ArrayList<>();// 스피너에 넣을 시간 리스트

        TimePicker timePicker = findViewById(R.id.time_picker);
        Spinner spinner = findViewById(R.id.spinner1);
        Button btn = findViewById(R.id.go);
        Switch switch1 = findViewById(R.id.switch1);
        TextView waketext1 = findViewById(R.id.waketext1);
        TextView waketext2 = findViewById(R.id.waketext2);
        TextView waketext3 = findViewById(R.id.waketext3);
        TextView waketext4 = findViewById(R.id.waketext4);
        final boolean[] Trigger = {false};

        //타임피커 리스너
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                times.clear();
                Toast.makeText(CalculateActivity.this, " " + hourOfDay + " " + minute + " ", Toast.LENGTH_SHORT).show();
                String am_pm = "";
                if(!Trigger[0]){ // 스위치 꺼졌을 때
                    if (hourOfDay >= 12) { // 오후 선택
                        am_pm = "오후";
                        getDate[0] = am_pm + " " + (hourOfDay-12) + "시" + minute + "분";
                        //9시간
                        times.add(Wake_Car_nine(hourOfDay, minute, am_pm));

                        //7시간 30분
                        times.add(Wake_Car_seven(hourOfDay, minute, am_pm));

                        //6시간
                        times.add(Wake_Car_six(hourOfDay, minute, am_pm));

                        //4시간 30분
                        times.add(Wake_Car_four(hourOfDay, minute, am_pm));

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CalculateActivity.this, android.R.layout.simple_spinner_item, times);

                        spinner.setAdapter(adapter);

                    } else { //오전 선택
                        am_pm = "오전";

                        getDate[0] = am_pm + " " + (hourOfDay-12) + "시" + minute + "분";
                        //9시간
                        times.add(Wake_Car_nine(hourOfDay, minute, am_pm));

                        //7시간 30분
                        times.add(Wake_Car_seven(hourOfDay, minute, am_pm));

                        //6시간
                        times.add(Wake_Car_six(hourOfDay, minute, am_pm));

                        //4시간 30분
                        times.add(Wake_Car_four(hourOfDay, minute, am_pm));

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CalculateActivity.this, android.R.layout.simple_spinner_item, times);

                        spinner.setAdapter(adapter);
                    }
                }else // 스위치 켜졌을 때
                {
                    if (hourOfDay >= 12) { // 오후 선택
                        am_pm = "오후";
                        getDate[0] = am_pm + " " + (hourOfDay-12) + "시" + minute + "분";
                        //9시간
                        times.add(Sleep_Car_nine(hourOfDay, minute, am_pm));

                        //7시간 30분
                        times.add(Sleep_Car_seven(hourOfDay, minute, am_pm));

                        //6시간
                        times.add(Sleep_Car_six(hourOfDay, minute, am_pm));

                        //4시간 30분
                        times.add(Sleep_Car_four(hourOfDay, minute, am_pm));

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CalculateActivity.this, android.R.layout.simple_spinner_item, times);

                        spinner.setAdapter(adapter);

                    } else { //오전 선택
                        am_pm = "오전";

                        getDate[0] = am_pm + " " + (hourOfDay-12) + "시" + minute + "분";
                        //9시간
                        times.add(Sleep_Car_nine(hourOfDay, minute, am_pm));

                        //7시간 30분
                        times.add(Sleep_Car_seven(hourOfDay, minute, am_pm));

                        //6시간
                        times.add(Sleep_Car_six(hourOfDay, minute, am_pm));

                        //4시간 30분
                        times.add(Sleep_Car_four(hourOfDay, minute, am_pm));

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CalculateActivity.this, android.R.layout.simple_spinner_item, times);

                        spinner.setAdapter(adapter);
                    }

                }

            }
        });

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    waketext1.setVisibility(View.VISIBLE);
                    waketext2.setVisibility(View.VISIBLE);
                    waketext3.setVisibility(View.GONE);
                    waketext4.setVisibility(View.GONE);
                    Trigger[0] = true;

                }else{
                    waketext1.setVisibility(View.GONE);
                    waketext2.setVisibility(View.GONE);
                    waketext3.setVisibility(View.VISIBLE);
                    waketext4.setVisibility(View.VISIBLE);
                    Trigger[0] = false;
                }
            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CalculateActivity.this, spinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();

                //현재 날짜 저장
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String getTime = dateFormat.format(date);

                // Firestore 인스턴스 가져오기
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // 데이터를 저장할 컬렉션, 문서 및 필드 이름 정의
                String collectionName = "sleepdata";
                String documentId = uid;
                String subCollectionName = "data";
                String subDocumentId = getTime.toString();

                // 데이터 추가할 맵 생성
                Map<String, Object> sleepData = new HashMap<>();
                if(!Trigger[0]){
                    sleepData.put("date", subDocumentId);
                    sleepData.put("sleep", convertTime(getDate[0].toString()));
                    sleepData.put("wake", convertTime(spinner.getSelectedItem().toString()));
                }else{
                    sleepData.put("date", subDocumentId);
                    sleepData.put("sleep", convertTime(spinner.getSelectedItem().toString()));
                    sleepData.put("wake", convertTime(getDate[0].toString()));
                }



                // 데이터를 Firestore에 추가
                db.collection(collectionName).document(documentId).collection(subCollectionName).document(subDocumentId).set(sleepData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 데이터 저장 성공 시 실행할 코드
                        Toast.makeText(getApplicationContext(), "기록이 저장 되었습니다.", Toast.LENGTH_SHORT).show();
                        // 홈으로 돌아감
                        Intent Startmain = new Intent(CalculateActivity.this, RealActivity.class);
                        startActivity(Startmain);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 데이터 저장 실패 시 실행할 코드
                        Toast.makeText(getApplicationContext(), "데이터 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    String Wake_Car_nine(int hourOfDay, int minute, String am_pm) {
        hourOfDay += 9;
        minute += 15;

        if (minute >= 60) {
            minute -= 60;
            hourOfDay++;
        }
        if (hourOfDay >= 24) {
            hourOfDay -= 24;
            am_pm = "오전";
        } else if (hourOfDay >= 12) {
            hourOfDay -= 12;
            am_pm = "오후";

        }
        if(hourOfDay == 0){
            hourOfDay = 12;
        }
        return am_pm + " " + hourOfDay + "시" + minute + "분";
    }

    String Wake_Car_seven(int hourOfDay, int minute, String am_pm) {
        hourOfDay += 7;
        minute += 45;

        if (minute >= 60) {
            minute -= 60;
            hourOfDay++;
        }
        if (hourOfDay >= 24) {
            hourOfDay -= 24;
            am_pm = "오전";
        } else if (hourOfDay >= 12) {
            hourOfDay -= 12;
        }
        return am_pm + " " + hourOfDay + "시" + minute + "분";
    }

    String Wake_Car_six(int hourOfDay, int minute, String am_pm) {
        hourOfDay += 6;
        minute += 15;

        if (minute >= 60) {
            minute -= 60;
            hourOfDay++;
        }
        if (hourOfDay >= 24) {
            hourOfDay -= 24;
            am_pm = "오전";
        } else if (hourOfDay >= 12) {
            hourOfDay -= 12;
        }
        return am_pm + " " + hourOfDay + "시" + minute + "분";
    }

    String Wake_Car_four(int hourOfDay, int minute, String am_pm) {
        hourOfDay += 4;
        minute += 45;

        if (minute >= 60) {
            minute -= 60;
            hourOfDay++;
        }
        if (hourOfDay >= 24) {
            hourOfDay -= 24;
            am_pm = "오전";
        } else if (hourOfDay >= 12) {
            hourOfDay -= 12;
        }
        return am_pm + " " + hourOfDay + "시" + minute + "분";
    }

    String convertTime(String timeStr) {

        boolean isAM = timeStr.contains("오전");


        String[] parts = timeStr.split("[시분]");
        int hour = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
        int minute = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));

        // 오전 12시는 00시로, 오후 12시는 그대로 12시로 처리합니다.
        if (isAM && hour == 12) {
            hour = 0;
        } else if (!isAM && hour < 12) {
            hour += 12;
        }


        return String.format("%02d%02d", hour, minute);
    }

    String Sleep_Car_nine(int hourOfDay, int minute, String am_pm) {
        hourOfDay += 15;
        minute -= 15;

        if (minute < 0) {
            minute += 60;
            hourOfDay--;
        }
        if (hourOfDay >= 24) {
            hourOfDay -= 24;
            if(hourOfDay>=12) {
                am_pm = "오후";
            }else{
                am_pm = "오전";
            }

        } else if (hourOfDay >= 12) {
            hourOfDay -= 12;
            am_pm = "오후";
        }
        return am_pm + " " + hourOfDay + "시" + minute + "분";
    }

    String Sleep_Car_seven(int hourOfDay, int minute, String am_pm) {
        hourOfDay += 17;
        minute -= 45;

        if (minute < 0) {
            minute += 60;
            hourOfDay--;
        }
        if (hourOfDay >= 24) {
            hourOfDay -= 24;
            if(hourOfDay>=12) {
                am_pm = "오후";
            }else{
                am_pm = "오전";
            }

        } else if (hourOfDay >= 12) {
            hourOfDay -= 12;
            am_pm = "오후";
        }
        return am_pm + " " + hourOfDay + "시" + minute + "분";
    }

    String Sleep_Car_six(int hourOfDay, int minute, String am_pm) {
        hourOfDay += 18;
        minute -= 15;

        if (minute < 0) {
            minute += 60;
            hourOfDay--;
        }
        if (hourOfDay >= 24) {
            hourOfDay -= 24;
            if(hourOfDay>=12) {
                am_pm = "오후";
            }else{
                am_pm = "오전";

            }

        } else if (hourOfDay >= 12) {
            hourOfDay -= 12;
            am_pm = "오후";
        }
        return am_pm + " " + hourOfDay + "시" + minute + "분";
    }

    String Sleep_Car_four(int hourOfDay, int minute, String am_pm) {
        hourOfDay += 20;
        minute -= 45;

        if (minute < 0) {
            minute += 60;
            hourOfDay--;
        }
        if (hourOfDay >= 24) {
            hourOfDay -= 24;
            if(hourOfDay>=12) {
                am_pm = "오후";
            }else{
                am_pm = "오전";
            }

        } else if (hourOfDay >= 12) {
            hourOfDay -= 12;
            am_pm = "오후";
        }
        return am_pm + " " + hourOfDay + "시" + minute + "분";
    }
}