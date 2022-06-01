package com.example.awesomebucket.activity;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.awesomebucket.MyConstant;
import com.example.awesomebucket.R;
import com.example.awesomebucket.api.APIClient;
import com.example.awesomebucket.api.LoginApiService;
import com.example.awesomebucket.dto.ErrorResultDto;
import com.example.awesomebucket.dto.ResultDto;
import com.example.awesomebucket.dto.UserDto;
import com.example.awesomebucket.exception.NoInputDataException;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FindPwActivity extends AppCompatActivity {

    EditText emailET, codeET, newPasswordET;
    Button authBtn, sendCodeBtn, resetPasswordBtn;
    TextView newPasswordTV;

    Retrofit client = APIClient.getClient();
    LoginApiService loginApiService;

    // 커스텀 토스트 메시지
    View toastView;
    TextView toastTv;
    Toast toast;

    String authenticationCode;
    String email, inputCode, newPassword;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_pw);
        setTitle("Change Password");

        emailET = findViewById(R.id.emailET);
        codeET = findViewById(R.id.codeET);
        authBtn = findViewById(R.id.codeBtn);
        sendCodeBtn = findViewById(R.id.sendCodeBtn);
        newPasswordET = findViewById(R.id.newPasswordET);
        newPasswordTV = findViewById(R.id.newPasswordTV);
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn);

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

                    String checkEmailResult = checkEmail(email);// 이메일 패턴 체크
                    if (!checkEmailResult.equals("PASS")) {  // 이메일 패턴 체크 실패
                        PrintToast(checkEmailResult);
                        return;
                    }

                    loginApiService = client.create(LoginApiService.class);
                    Call<ResultDto> call = loginApiService.login(new UserDto.EmailAuthRequestDto("findPw", email));
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
                                Log.i("Send Email For Find Password", "SUCCESS");
                            } else {
                                try {
                                    Log.i("Send Email For Find Password", "FAIL");
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
                        newPasswordTV.setTextColor(Color.BLACK);
                        newPasswordET.setFocusable(true);
                        newPasswordET.setFocusableInTouchMode(true);
                    }

                } catch (NoInputDataException e) {
                    PrintToast(e.getMessage());
                }
            }
        });

        newPasswordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 새 비밀번호가 입력되었으면 비밀번호 재설정 버튼 활성화
                if (editable.toString().trim().getBytes().length >= 8) {
                    resetPasswordBtn.setClickable(true);
                    resetPasswordBtn.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.purple)));
                } else {
                    resetPasswordBtn.setClickable(false);
                    resetPasswordBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E1E1E1")));
                }
            }
        });

        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = emailET.getText().toString().trim();
                newPassword = newPasswordET.getText().toString();

                String checkPasswordResult = checkPassword(newPassword);// 비밀번호 패턴 체크

                // 비밀번호 패턴 체크 실패
                if (!checkPasswordResult.equals("PASS")) {
                    PrintToast(checkPasswordResult);
                    return;
                }

            }
        });

    }


    //**************************** 이메일 패턴 체크를 위한 checkEmail() 함수 정의 *******************************
    public String checkEmail(String email) {
        Matcher matcher;  // 정규식 검사 객체

        // 정규식 체크
        matcher = Pattern.compile(MyConstant.emailPattern).matcher(email);
        if (!matcher.matches())
            return "이메일 형식이 올바르지 않습니다";

        return "PASS";
    }


    //**************************** 비밀번호 패턴 체크를 위한 checkPassword() 함수 정의 *******************************
    public String checkPassword(String password) {
        Matcher matcher;  // 정규식 검사 객체

        // 공백 체크
        matcher = Pattern.compile(MyConstant.blankPattern).matcher(password);
        if (matcher.find())
            return "비밀번호는 공백을 포함할 수 없습니다";

        // 정규식 체크
        matcher = Pattern.compile(MyConstant.pwPattern).matcher(password);
        if (!matcher.matches())
            return "비밀번호는 영문, 숫자를 포함해야합니다";

        return "PASS";
    }


    //**************************** 커스텀 토스트 메세지 출력을 위한 PrintToast() 함수 정의 ********************************
    public void PrintToast(String msg) {
        // toast.xml이 msg 내용의 토스트 메세지로 나오도록 설정
        toastTv.setText(msg);
        toast.setView(toastView);
        toast.show();
    }
}
