package com.example.awesomebucket.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.awesomebucket.MyConstant;
import com.example.awesomebucket.MySharedPreferences;
import com.example.awesomebucket.R;
import com.example.awesomebucket.api.APIClient;
import com.example.awesomebucket.api.LoginAPIService;
import com.example.awesomebucket.dto.ErrorResultDto;
import com.example.awesomebucket.dto.ResultDto;
import com.example.awesomebucket.dto.UserDto;
import com.google.gson.Gson;

import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends Activity {

    Button loginBtn;
    TextView idET, pwET;

    Retrofit client = APIClient.getClient();
    LoginAPIService loginAPIService;

    // 커스텀 토스트 메시지
    View toastView;
    TextView toastTv;
    Toast toast;

    long pressedTime = 0; //'뒤로가기' 버튼 클릭했을 때의 시간

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        loginBtn = findViewById(R.id.loginBtn);
        idET = findViewById(R.id.idET);
        pwET = findViewById(R.id.pwET);

        // toast.xml을 View로 inflating하고 뷰 참조 후 뷰 객체 변수에 인플레이팅된 뷰를 할당
        toast = new Toast(getApplicationContext());
        toastView = (View) View.inflate(getApplicationContext(), R.layout.toast, null);
        toastTv = (TextView) toastView.findViewById(R.id.toastTv);

        //**************************** 로그인 버튼을 클릭했을 때 동작하는 이벤트 처리 ********************************
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = idET.getText().toString();
                String password = pwET.getText().toString();

                loginAPIService = client.create(LoginAPIService.class);
                Call<ResultDto> call = loginAPIService.login(new UserDto.LoginRequestDto(email, password));
                call.enqueue(new Callback<ResultDto>() {
                    @SneakyThrows
                    @Override
                    public void onResponse(Call<ResultDto> call, Response<ResultDto> response) {
                        ResultDto result = response.body();  // 응답 결과 바디

                        if (result != null && response.isSuccessful()) {
                            int resultStatus = result.getStatus();  // 응답 상태
                            String resultMessage = result.getMessage();  // 응답 메시지
                            Object resultData = result.getData();  // 응답 데이터

                            // 응답 데이터를 LoginResponseDto로 convert
                            String jsonResult = new Gson().toJson(resultData);
                            UserDto.LoginResponseDto loginResponseDto = new Gson().fromJson(jsonResult, UserDto.LoginResponseDto.class);

                            long id = loginResponseDto.getId();  // 로그인 한 회원 id

                            // User ID 저장
                            MySharedPreferences.setLoginUserId(getApplicationContext(), MyConstant.PREFERENCE_FILE_USER, "loginUserId", id);
                            Long loginUserId = MySharedPreferences.getLoginUserId(getApplicationContext(), MyConstant.PREFERENCE_FILE_USER, "loginUserId");

                            Log.i("Login", "SUCCESS. Hello, " + id);

                            // 메인 화면으로 이동
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            Log.i("Login", "FAIL");
                            Log.e("Response error", response.toString());

                            // 에러 바디를 ErrorResultDto로 convert
                            Converter<ResponseBody, ErrorResultDto> errorConverter = client.responseBodyConverter(ErrorResultDto.class, ErrorResultDto.class.getAnnotations());
                            ErrorResultDto error = errorConverter.convert(response.errorBody());

                            Log.e("ErrorResultDto", error.toString());

                            int errorStatus = error.getStatus();  // 에러 상태
                            String errorError = error.getError();  // 에러 이유
                            String errorMessage = error.getMessage();  // 에러 메시지

                            // 로그인 실패
                            if (errorMessage != null) {  // 개발자가 설정한 오류
                                PrintToast(errorMessage);  // 에러 메시지 출력
                            } else {  // 기타 오류
                                if (errorStatus >= 500) {  // 서버 오류
                                    PrintToast("Server Error");
                                } else if (errorStatus >= 400) {  // 클라이언트 오류
                                    PrintToast("Client Error");
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResultDto> call, Throwable t) {
                        Log.e("Throwable error", t.getMessage());
                    }
                });
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

    //**************************** 뒤로가기 버튼을 눌렀을 때의 이벤트 처리 ********************************
    @Override
    public void onBackPressed() {
        //마지막으로 누른 '뒤로가기' 버튼 클릭 시간이 이전의 '뒤로가기' 버튼 클릭 시간과의 차이가 2초보다 크면
        if (System.currentTimeMillis() > pressedTime + 2000) {
            //현재 시간을 pressedTime 에 저장
            pressedTime = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
        }

        //마지막 '뒤로가기' 버튼 클릭시간이 이전의 '뒤로가기' 버튼 클릭 시간과의 차이가 2초보다 작으면
        else {
            moveTaskToBack(true);  // 태스크를 백그라운드로 이동
            finishAndRemoveTask();  // 액티비티 종료 + 태스크 리스트에서 지우기
            android.os.Process.killProcess(android.os.Process.myPid());  // 앱 프로세스 종료
        }
    }
}
