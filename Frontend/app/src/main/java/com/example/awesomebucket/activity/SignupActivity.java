package com.example.awesomebucket.activity;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.awesomebucket.R;
import com.example.awesomebucket.api.APIClient;
import com.example.awesomebucket.api.LoginApiService;
import com.example.awesomebucket.dto.ErrorResultDto;
import com.example.awesomebucket.dto.ResultDto;
import com.example.awesomebucket.dto.UserDto;
import com.example.awesomebucket.exception.NoInputDataException;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignupActivity extends AppCompatActivity {

    EditText emailET, codeET, passwordET, nameET;
    Button authBtn, sendCodeBtn, joinBtn;
    TextView passwordTV, nameTv;

    Retrofit client = APIClient.getClient();
    LoginApiService loginApiService;

    // 커스텀 토스트 메시지
    View toastView;
    TextView toastTv;
    Toast toast;

    String authenticationCode;
    String email, inputCode;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        setTitle("Signup");

        emailET = findViewById(R.id.emailET);
        codeET = findViewById(R.id.codeET);
        authBtn = findViewById(R.id.codeBtn);
        sendCodeBtn = findViewById(R.id.sendCodeBtn);
        passwordET = findViewById(R.id.passwordET);
        nameET = findViewById(R.id.nameET);
        passwordTV = findViewById(R.id.passwordTV);
        nameTv = findViewById(R.id.nameTv);
        joinBtn = findViewById(R.id.joinBtn);

        // toast.xml을 View로 inflating하고 뷰 참조 후 뷰 객체 변수에 인플레이팅된 뷰를 할당
        toast = new Toast(getApplicationContext());
        toastView = (View) View.inflate(getApplicationContext(), R.layout.toast, null);
        toastTv = (TextView) toastView.findViewById(R.id.toastTv);

        //**************************** 인증번호 발송 버튼을 클릭했을 때 동작하는 이벤트 처리 ********************************
        sendCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    email = emailET.getText().toString().trim();

                    if (email.getBytes().length <= 0)
                        throw new NoInputDataException("이메일을 입력하세요");

                    loginApiService = client.create(LoginApiService.class);
                    Call<ResultDto> call = loginApiService.login(new UserDto.EmailAuthRequestDto("join", email));
                    call.enqueue(new Callback<ResultDto>() {
                        @Override
                        public void onResponse(Call<ResultDto> call, Response<ResultDto> response) {
                            ResultDto result = response.body();  // 응답 결과 바디

                            if (result != null && response.isSuccessful()) {
                                Object resultData = result.getData();  // 응답 데이터

                                // 응답 데이터를 emailAuthResponseDto로 convert
                                UserDto.EmailAuthResponseDto emailAuthResponseDto = new Gson().fromJson(new Gson().toJson(resultData), UserDto.EmailAuthResponseDto.class);

                                authenticationCode = emailAuthResponseDto.getAuthenticationCode();  // 인증 번호

                                PrintToast("입력한 이메일로 인증번호가 발송되었습니다.");
                                Log.i("Send Email For Signup", "SUCCESS");
                            } else {
                                try {
                                    Log.i("Send Email For Signup", "FAIL");
                                    Log.e("Response error", response.toString());

                                    // 에러 바디를 ErrorResultDto로 convert
                                    Converter<ResponseBody, ErrorResultDto> errorConverter = client.responseBodyConverter(ErrorResultDto.class, ErrorResultDto.class.getAnnotations());
                                    ErrorResultDto error = errorConverter.convert(response.errorBody());

                                    Log.e("ErrorResultDto", error.toString());

                                    String errorMessage = error.getMessage();  // 에러 메시지

                                    // 인증 메일 전송 실패
                                    if (errorMessage != null) {  // 개발자가 설정한 오류
                                        PrintToast(errorMessage);  // 에러 메시지 출력
                                    } else {
                                        PrintToast("인증번호 발송 실패. 잠시후 다시 시도해주세요.");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResultDto> call, Throwable t) {
                            Log.e("Throwable error", t.getMessage());
                            PrintToast("인증번호 발송 실패. 잠시후 다시 시도해주세요.");
                        }
                    });
                } catch (NoInputDataException e) {
                    PrintToast(e.getMessage());
                }
            }
        });

        //**************************** 인증 버튼을 클릭했을 때 동작하는 이벤트 처리 ********************************
        authBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    inputCode = codeET.getText().toString().trim();

                    if (inputCode.getBytes().length <= 0)
                        throw new NoInputDataException("인증번호를 입력하세요");

                    if (!(inputCode.equals(authenticationCode))) {
                        PrintToast("인증번호가 올바르지 않습니다");
                    } else {
                        PrintToast("인증이 완료되었습니다");

                        // 입력, 버튼 활성화 및 비활성화
                        emailET.setFocusable(false);
                        codeET.setFocusable(false);
                        sendCodeBtn.setClickable(false);
                        sendCodeBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E1E1E1")));
                        authBtn.setClickable(false);
                        authBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E1E1E1")));
                        passwordTV.setTextColor(Color.BLACK);
                        nameTv.setTextColor(Color.BLACK);
                        passwordET.setFocusable(true);
                        nameET.setFocusable(true);
                        passwordET.setFocusableInTouchMode(true);
                        nameET.setFocusableInTouchMode(true);
                    }

                } catch (NoInputDataException e) {
                    PrintToast(e.getMessage());
                }
            }
        });

    }


    //**************************** 커스텀 토스트 메세지 출력을 위한 PrintToast() 함수 정의 ********************************
    public void PrintToast(String msg) {
        // toast.xml이 msg 내용의 토스트 메세지로 나오도록 설정
        toastTv.setText(msg);
        toast.setView(toastView);
        toast.show();
    }
}
