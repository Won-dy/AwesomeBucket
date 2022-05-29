package com.example.awesomebucket.activity;

import static com.example.awesomebucket.MyConstant.NO_ID;
import static com.example.awesomebucket.MyConstant.PREFERENCE_FILE_USER;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.awesomebucket.MySharedPreferences;
import com.example.awesomebucket.R;
import com.example.awesomebucket.api.APIClient;
import com.example.awesomebucket.api.CategoryApiService;
import com.example.awesomebucket.dto.CategoryDto;
import com.example.awesomebucket.dto.ErrorResultDto;
import com.example.awesomebucket.dto.ResultDto;
import com.example.awesomebucket.exception.NoInputDataException;
import com.example.awesomebucket.exception.UnauthorizedAccessException;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CategoryActivity extends AppCompatActivity {

    // 변수 선언
    ListView listV;
    CtgrListVAdapter ctgrListVAdapter;
    View toastView;
    TextView toastTv;
    Toast toast;

    Retrofit client = APIClient.getClient();
    CategoryApiService categoryApiService;

    Context context;

    Long loginUserId;
    String categoryName;
    long pressedTime = 0; //'뒤로가기' 버튼 클릭했을 때의 시간
    InputMethodManager imm;  // 키보드 숨기기 위해 객체 선언

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category);
        setTitle("Category Manager");

        // findViewById() 메소드를 사용하여 category.xml에 정의된 뷰를 inflation(객체화)하여 뷰 참조
        // 뷰 객체 변수에 인플레이팅된 뷰를 할당
        listV = findViewById(R.id.listV);
        final EditText ctgrAddET = findViewById(R.id.ctgrAddEt);
        Button ctgrAddBtn = findViewById(R.id.ctgrAddBtn);

        // toast.xml을 View로서 inflating하고 뷰 참조 후 뷰 객체 변수에 인플레이팅된 뷰를 할당
        toast = new Toast(CategoryActivity.this);
        toastView = (View) View.inflate(CategoryActivity.this, R.layout.toast, null);
        toastTv = (TextView) toastView.findViewById(R.id.toastTv);

        // 로그인 한 User의 기본키 조회
        loginUserId = MySharedPreferences.getLoginUserId(getApplicationContext(), PREFERENCE_FILE_USER, "loginUserId");
        context = this;  // 컨텍스트 얻기

        // 입력받는 방법을 관리하는 Manager 객체를 요청하여 InputMethodmanager에 반환
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        loadCategory();  // 카테고리 불러오기위해 loadCategory() 함수 호출

        // 카테고리 추가 버튼을 클릭했을 때 동작하는 이벤트 처리
        ctgrAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    validateLoginState();  // 로그인 상태 확인

                    categoryName = ctgrAddET.getText().toString().trim();

                    if (categoryName.getBytes().length <= 0)
                        throw new NoInputDataException("카테고리를 입력하세요");  // 카테고리에 빈칸을 입력한 경우 예외 처리

                    categoryApiService = client.create(CategoryApiService.class);
                    Call<ResultDto> call = categoryApiService.createCategory(new CategoryDto.CreateUpdateRequestDto(loginUserId, categoryName));
                    call.enqueue(new Callback<ResultDto>() {
                        @Override
                        public void onResponse(Call<ResultDto> call, Response<ResultDto> response) {
                            ResultDto result = response.body();  // 응답 결과 바디

                            if (result != null && response.isSuccessful()) {
                                Object resultData = result.getData();  // 응답 데이터

                                // 응답 데이터를 IdResponseDto로 convert
                                CategoryDto.IdResponseDto idResponseDto = new Gson().fromJson(new Gson().toJson(resultData), CategoryDto.IdResponseDto.class);
                                long id = idResponseDto.getId();  // 등록한 카테고리 id

                                Log.i("ADD CATEGORY", "SUCCESS");
                                PrintToast("카테고리 추가 : " + categoryName);

                                loadCategory();  // 카테고리 불러오기

                                ctgrAddET.setText("");  // 입력했던 내용을 비우고
                                ctgrAddET.clearFocus();  // 초점을 없앤다
                                imm.hideSoftInputFromWindow(ctgrAddET.getWindowToken(), 0);  // 키보드 숨기기

                            } else {
                                try {
                                    Log.i("ADD CATEGORY", "FAIL");
                                    Log.e("Response error", response.toString());

                                    // 에러 바디를 ErrorResultDto로 convert
                                    Converter<ResponseBody, ErrorResultDto> errorConverter = client.responseBodyConverter(ErrorResultDto.class, ErrorResultDto.class.getAnnotations());
                                    ErrorResultDto error = errorConverter.convert(response.errorBody());

                                    Log.e("ErrorResultDto", error.toString());

                                    int errorStatus = error.getStatus();  // 에러 상태
                                    String errorMessage = error.getMessage();  // 에러 메시지

                                    // 로그인 실패
                                    if (errorMessage != null) {  // 개발자가 설정한 오류
                                        PrintToast(errorMessage);  // 에러 메시지 출력
                                        if (errorStatus == 401) {  // 인증되지 않은 사용자가 접근
                                            logout();
                                        }
                                    } else {  // 기타 오류
                                        if (errorStatus >= 500) {  // 서버 오류
                                            PrintToast("Server Error");
                                        } else if (errorStatus >= 400) {  // 클라이언트 오류
                                            PrintToast("Client Error");
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResultDto> call, Throwable t) {
                            Log.e("Throwable error", t.getMessage());
                        }
                    });
                } catch (UnauthorizedAccessException e) {  // 인증되지 않은 사용자가 접근할 때 발생하는 예외
                    PrintToast(e.getMessage());  // 에러 메시지 출력
                    logout();
                } catch (NoInputDataException nide) {
                    PrintToast(nide.getMessage());
                }
            }
        });

    }

    //**************************** 카테고리를 불러오기 위한 loadCategory() 함수 정의 ********************************
    public void loadCategory() {

        // 카테고리 불러오기
        try {
            validateLoginState();  // 로그인 상태 확인

            ctgrListVAdapter = new CtgrListVAdapter(context);  // CtgrListVAdapter 생성
            categoryApiService = client.create(CategoryApiService.class);
            Call<ResultDto> call = categoryApiService.getCategories(loginUserId);
            call.enqueue(new Callback<ResultDto>() {
                @Override
                public void onResponse(Call<ResultDto> call, Response<ResultDto> response) {

                    ResultDto result = response.body();  // 응답 결과 바디

                    if (result != null && response.isSuccessful()) {
                        ArrayList resultData = (ArrayList) result.getData();// 응답 데이터

                        // 응답 데이터를 FindResponseDto로 convert
                        ArrayList<CategoryDto.FindResponseDto> categories = new ArrayList<>();
                        for (Object resultDatum : resultData)
                            categories.add(new Gson().fromJson(new Gson().toJson(resultDatum), CategoryDto.FindResponseDto.class));

                        // 카테고리 리스트에 값 넣기
                        for (CategoryDto.FindResponseDto category : categories)
                            ctgrListVAdapter.addItem(category.getId(), category.isDefault(), category.getName());

                        Log.i("Load Category", "SUCCESS");

                    } else {  // 카테고리 로드 실패
                        Log.i("Load Category", "FAIL");
                        Log.e("Response error", response.toString());

                        try {
                            // 에러 바디를 ErrorResultDto로 convert
                            Converter<ResponseBody, ErrorResultDto> errorConverter = client.responseBodyConverter(ErrorResultDto.class, ErrorResultDto.class.getAnnotations());
                            ErrorResultDto error = null;
                            error = errorConverter.convert(response.errorBody());

                            Log.e("ErrorResultDto", error.toString());

                            int errorStatus = error.getStatus();  // 에러 상태
                            String errorMessage = error.getMessage();  // 에러 메시지

                            if (errorMessage != null) {  // 개발자가 설정한 오류
                                PrintToast(errorMessage);  // 에러 메시지 출력
                                if (errorStatus == 401) {  // 인증되지 않은 사용자가 접근
                                    logout();
                                }
                            } else {  // 기타 오류
                                if (errorStatus >= 500) {  // 서버 오류
                                    PrintToast("Server Error");
                                } else if (errorStatus >= 400) {  // 클라이언트 오류
                                    PrintToast("Client Error");
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    listV.setAdapter(ctgrListVAdapter);  // ListView 객체에 CtgrListVAdapter 적용
                }

                @Override
                public void onFailure(Call<ResultDto> call, Throwable t) {
                    Log.e("Throwable error", t.getMessage());
                }
            });
        } catch (UnauthorizedAccessException e) {  // 인증되지 않은 사용자가 접근할 때 발생하는 예외
            PrintToast(e.getMessage());  // 에러 메시지 출력
            logout();
        }
    }


    //**************************** 로그인되어있나 확인하는 validateLoginState() 함수 정의 ********************************
    public void validateLoginState() throws UnauthorizedAccessException {
        if (loginUserId == NO_ID)
            throw new UnauthorizedAccessException("로그인이 필요합니다");
    }


    //**************************** 로그아웃을 위한 logout() 함수 정의 ********************************
    public void logout() {
        // SharedPreferences에서 로그인한 User ID 제거
        MySharedPreferences.removeOne(getApplicationContext(), PREFERENCE_FILE_USER, "loginUserId");

        // 명시적 인텐트를 사용하여 LoginActivity 호출
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
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
