package com.kotlinsun.mobileapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;

import java.util.Calendar;

public class JoinActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private View signupButton;
    private View checkDuplicateButton;

    private DatePicker datePicker;
    private TextView selectedDateTextView;

    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;

    private boolean isEmailValid = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signupButton = findViewById(R.id.signupButton);
        checkDuplicateButton = findViewById(R.id.checkDuplicateButton);

        datePicker = findViewById(R.id.datePicker);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);


        checkDuplicateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();

                firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(JoinActivity.this, new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            if (result != null && result.getSignInMethods().isEmpty()) {
                                // 중복되지 않는 이메일인 경우
                                Toast.makeText(JoinActivity.this, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show();
                                isEmailValid = true;
                                enableSignupButton();
                            } else {
                                // 중복된 이메일인 경우
                                Toast.makeText(JoinActivity.this, "중복된 이메일입니다.", Toast.LENGTH_SHORT).show();
                                isEmailValid = false;
                                disableSignupButton();
                            }
                        } else {
                            // 중복 체크 실패
                            Toast.makeText(JoinActivity.this, "중복 체크 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            isEmailValid = false;
                            disableSignupButton();
                        }
                    }
                });
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

                if (isEmailValid && password.equals(confirmPassword) && selectedYear != 0 && selectedMonth != 0 && selectedDay != 0) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(JoinActivity.this, new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                // 회원가입 성공
                                if (firebaseAuth.getCurrentUser() != null) {
                                    // Firestore에 사용자 데이터 추가
                                    String uid = firebaseAuth.getCurrentUser().getUid();
                                    User user = new User(name, email, selectedYear, selectedMonth, selectedDay);
                                    db.collection("users").document(uid).set(user).addOnSuccessListener(aVoid -> {
                                        Toast.makeText(JoinActivity.this, "회원가입 및 데이터 저장 완료", Toast.LENGTH_SHORT).show();
                                        clearInputFields();
                                        Intent mainIntent = new Intent(JoinActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(JoinActivity.this, "데이터 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } else {
                                // 회원가입 실패
                                Toast.makeText(JoinActivity.this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    // 이메일 유효성, 비밀번호 확인, 생년월일 선택 실패
                    Toast.makeText(JoinActivity.this, "이메일 유효성, 비밀번호 확인, 생년월일 선택 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });




        // DatePicker 값 변경 리스너 설정
        datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    // 선택한 날짜를 변수에 저장
                    selectedYear = year;
                    selectedMonth = monthOfYear + 1;
                    selectedDay = dayOfMonth;

                    // 선택한 날짜를 텍스트 뷰에 표시
                    String selectedDate = year + "년 " + (monthOfYear + 1) + "월 " + dayOfMonth + "일";
                    selectedDateTextView.setText("선택한 날짜: " + selectedDate);

                    // 생년월일 선택 여부에 따라 회원가입 버튼 활성화/비활성화
                    enableSignupButton();
                });







    }

    private void enableSignupButton() {
        if (isEmailValid && selectedYear != 0 && selectedMonth != 0 && selectedDay != 0) {
            signupButton.setEnabled(true);
        } else {
            signupButton.setEnabled(false);
        }
    }

    private void disableSignupButton() {
        signupButton.setEnabled(false);
    }

    private void clearInputFields() {
        nameEditText.getText().clear();
        emailEditText.getText().clear();
        passwordEditText.getText().clear();
        confirmPasswordEditText.getText().clear();
        selectedDateTextView.setText("선택한 날짜: ");
        datePicker.updateDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        selectedYear = 0;
        selectedMonth = 0;
        selectedDay = 0;
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

    public class User {
        private String userName;
        private String userEmail;
        private int userYearOfBirth;
        private int userMonthOfBirth;
        private int userDayOfBirth;

        public User() {
            // Default constructor required for Firestore
        }

        public User(String userName, String userEmail, int userYearOfBirth, int userMonthOfBirth, int userDayOfBirth) {
            this.userName = userName;
            this.userEmail = userEmail;
            this.userYearOfBirth = userYearOfBirth;
            this.userMonthOfBirth = userMonthOfBirth;
            this.userDayOfBirth = userDayOfBirth;
        }

        @PropertyName("name")
        public String getUserName() {
            return userName;
        }

        @PropertyName("email")
        public String getUserEmail() {
            return userEmail;
        }

        @PropertyName("year_of_birth")
        public int getUserYearOfBirth() {
            return userYearOfBirth;
        }

        @PropertyName("month_of_birth")
        public int getUserMonthOfBirth() {
            return userMonthOfBirth;
        }

        @PropertyName("day_of_birth")
        public int getUserDayOfBirth() {
            return userDayOfBirth;
        }
    }



}
