package com.kotlinsun.mobileapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore dbMain;

    private TextView btnLogout;

    private String UidDate = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        btnLogout = findViewById(R.id.btnLogout);

        firebaseAuth = FirebaseAuth.getInstance();
        // Firestore 인스턴스 가져오기
        dbMain = FirebaseFirestore.getInstance();

        updateLoginStatus();

        getUserData();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                Toast.makeText(SettingActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

                // 현재 액티비티에서 OtherActivity를 종료
                Intent intent = new Intent(SettingActivity.this, RealActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티를 종료

                Intent maingo = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(maingo);
                finish();
            }
        });

    }





    private void updateLoginStatus() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            // 로그인 중인 경우
            UidDate = firebaseAuth.getCurrentUser().getUid(); //사용자 uid 받기
        } else {
            // 로그아웃 상태인 경우
            Intent maingo = new Intent(this, MainActivity.class);
            startActivity(maingo);
            finish();
        }
    }


    private void getUserData() {
        // 원하는 UID
        String uid = UidDate;

        // "users" 컬렉션에서 해당 UID로 문서 가져오기
        DocumentReference docRef = dbMain.collection("users").document(uid);
        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // 문서가 존재하면 "name" 필드 값을 가져옴
                            String name = document.getString("name");
                            String email = document.getString("email");
                            System.out.println("Name: " + name + "Email : "+ email);
                            ((TextView) findViewById(R.id.infoNameTv)).setText("이름: " + name);
                            ((TextView) findViewById(R.id.infoEmailTv)).setText("이메일: " + email);
                        } else {
                            System.out.println("해당 UID로 된 문서가 없습니다.");
                        }
                    } else {
                        System.out.println("문서 가져오기 실패: " + task.getException());
                    }
                });
    }




}