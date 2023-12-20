package com.kotlinsun.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
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

public class CalCulateNow extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculate_now);
        final String[] getDate = new String[1];
        Intent Intentdata = getIntent();
        String uid = Intentdata.getStringExtra("uid");

        final String[] sleepToinetent = new String[1];
        final String[] wakeToinetent = {""};

        ArrayList<String> times = new ArrayList<>();// 스피너에 넣을 시간 리스트


        Spinner spinner = findViewById(R.id.spinner1);
        Button btn = findViewById(R.id.go);



        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String getTime = dateFormat.format(date);

        // 시 분 나누기
        String[] timeParts = getTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);



                String am_pm = "";

                    if (hour >= 12) { // 오후 선택
                        am_pm = "오후";
                        getDate[0] = am_pm + " " + (hour-12) + "시" + minute + "분";
                        //9시간
                        times.add(Wake_Car_nine(hour, minute, am_pm));

                        //7시간 30분
                        times.add(Wake_Car_seven(hour, minute, am_pm));

                        //6시간
                        times.add(Wake_Car_six(hour, minute, am_pm));

                        //4시간 30분
                        times.add(Wake_Car_four(hour, minute, am_pm));

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CalCulateNow.this, android.R.layout.simple_spinner_item, times);

                        spinner.setAdapter(adapter);

                    } else { //오전 선택
                        am_pm = "오전";

                        getDate[0] = am_pm + " " + (hour-12) + "시" + minute + "분";
                        //9시간
                        times.add(Wake_Car_nine(hour, minute, am_pm));

                        //7시간 30분
                        times.add(Wake_Car_seven(hour, minute, am_pm));

                        //6시간
                        times.add(Wake_Car_six(hour, minute, am_pm));

                        //4시간 30분
                        times.add(Wake_Car_four(hour, minute, am_pm));

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CalCulateNow.this, android.R.layout.simple_spinner_item, times);

                        spinner.setAdapter(adapter);
                    }








        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CalCulateNow.this, spinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();

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

                    sleepData.put("date", subDocumentId);
                    sleepData.put("sleep", convertTime(getDate[0].toString()));
                    sleepData.put("wake", convertTime(spinner.getSelectedItem().toString()));
                    sleepToinetent[0] = getDate[0].toString();
                    wakeToinetent[0] = spinner.getSelectedItem().toString();




                // 데이터를 Firestore에 추가
                db.collection(collectionName).document(documentId).collection(subCollectionName).document(subDocumentId).set(sleepData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 데이터 저장 성공 시 실행할 코드
                        Toast.makeText(getApplicationContext(), "기록이 저장 되었습니다.", Toast.LENGTH_SHORT).show();
                        // 홈으로 돌아감
                        Intent Startmain = new Intent(CalCulateNow.this, ScreenActivity.class);
                        Startmain.putExtra("sleeptime",sleepToinetent[0]);
                        Startmain.putExtra("waketime",wakeToinetent[0]);
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
