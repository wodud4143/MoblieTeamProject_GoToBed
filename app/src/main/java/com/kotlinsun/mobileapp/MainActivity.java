package com.kotlinsun.mobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private View lottieLoad;
    private View loginButton;
    private View joinButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        lottieLoad = findViewById(R.id.lottieLoad);
        loginButton = findViewById(R.id.loginButton);
        joinButton = findViewById(R.id.JoinBtn);

        lottieLoad.setVisibility(View.GONE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                lottieLoad.setVisibility(View.VISIBLE);

                // Firebase Authentication을 사용하여 이메일/비밀번호로 로그인
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    // 로그인 성공
                                    loginButton.setEnabled(false);
                                    joinButton.setEnabled(false);
                                    Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                    // 로그인 상태 업데이트
                                    updateLoginStatus();
                                    lottieLoad.setVisibility(View.GONE);

                                    Intent go = new Intent(MainActivity.this, RealActivity.class);
                                    startActivity(go);
                                    finish();
                                } else {
                                    // 로그인 실패
                                    Toast.makeText(MainActivity.this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    lottieLoad.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });


        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent joingo = new Intent(MainActivity.this, JoinActivity.class);
                startActivity(joingo);
            }
        });

        // 로그인 상태 확인
        updateLoginStatus();

        if (firebaseAuth.getCurrentUser() != null) {
            // 로그인한 경우
            loginButton.setEnabled(false);
            joinButton.setEnabled(false);
            loginButton.setVisibility(View.GONE);
            joinButton.setVisibility(View.GONE);
            Intent go = new Intent(MainActivity.this, RealActivity.class);
            startActivity(go);
            finish();
            Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
        } else {
            // 로그인하지 않은 경우
            loginButton.setEnabled(true);
            joinButton.setEnabled(true);
            loginButton.setVisibility(View.VISIBLE);
            joinButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateLoginStatus() {
        if (firebaseAuth.getCurrentUser() != null) {
            // 로그인 중인 경우
            loginButton.setEnabled(false);
            loginButton.setVisibility(View.GONE);
        } else {
            // 로그아웃 상태인 경우
            loginButton.setEnabled(true);
            loginButton.setVisibility(View.VISIBLE);
        }
    }

    // 텍스트 필드 외 터치시 키보드 내리기
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null && imm.isActive()) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }

}