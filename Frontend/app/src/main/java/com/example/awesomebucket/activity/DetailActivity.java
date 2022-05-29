package com.example.awesomebucket.activity;

import static com.example.awesomebucket.MyConstant.NO_ID;
import static com.example.awesomebucket.MyConstant.PREFERENCE_FILE_USER;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.awesomebucket.MyConstant;
import com.example.awesomebucket.MyDBHelper;
import com.example.awesomebucket.MySharedPreferences;
import com.example.awesomebucket.R;
import com.example.awesomebucket.api.APIClient;
import com.example.awesomebucket.api.BucketListApiService;
import com.example.awesomebucket.dto.BucketListDto;
import com.example.awesomebucket.dto.ErrorResultDto;
import com.example.awesomebucket.dto.ResultDto;
import com.example.awesomebucket.exception.UnauthorizedAccessException;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DetailActivity extends AppCompatActivity {

    // 변수 선언
    Retrofit client = APIClient.getClient();
    BucketListApiService bucketListApiService;

    TextView bName, d_Day, crtDate, cName, achvRt, tgdtTv, achvdtTv, memoTv;
    ProgressBar pBar;
    RatingBar rBar;
    View toastView;
    TextView toastTv;
    Toast toast;

    Long loginUserId;
    long bucketListId;
    String category_name, title, target_date, achievement_date, registered_date, memo;
    Long categoryId;
    int achievement_rate;
    float importance;
    long pressedTime = 0; //'뒤로가기' 버튼 클릭했을 때의 시간
    long start, end; // 함수 실행시간 측정을 위한 변수

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        setTitle("Bucket Detail");

        // findViewById() 메소드를 사용하여 detail.xml에 정의된 뷰를 inflation(객체화)하여 뷰 참조
        // 뷰 객체 변수에 인플레이팅된 뷰를 할당
        bName = findViewById(R.id.bName);
        d_Day = findViewById(R.id.d_Day);
        crtDate = findViewById(R.id.crtDate);
        cName = findViewById(R.id.cName);
        achvRt = findViewById(R.id.achvRt);
        pBar = findViewById(R.id.pBar);
        rBar = findViewById(R.id.rBar);
        tgdtTv = findViewById(R.id.tgdtTv);
        achvdtTv = findViewById(R.id.achvdtTv);
        memoTv = findViewById(R.id.memoTv);

        // toast.xml을 View로서 inflating하고 뷰 참조 후 뷰 객체 변수에 인플레이팅된 뷰를 할당
        toast = new Toast(DetailActivity.this);
        toastView = (View) View.inflate(DetailActivity.this, R.layout.toast, null);
        toastTv = (TextView) toastView.findViewById(R.id.toastTv);

        // MainActivity로 부터 intent를 통해 데이터 값을 전달 받음
        Intent inIntent = getIntent();
        bucketListId = inIntent.getLongExtra("bucketListId", MyConstant.NO_ID);  // 버킷리스트 ID 값 받기

        // 로그인 한 User의 기본키 조회
        loginUserId = MySharedPreferences.getLoginUserId(getApplicationContext(), PREFERENCE_FILE_USER, "loginUserId");

        if (bucketListId == NO_ID) {
            PrintToast("버킷리스트 조회 실패");
            Intent intent = new Intent(DetailActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        start = System.currentTimeMillis();
        try {
            validateLoginState();  // 로그인 상태 확인

            bucketListApiService = client.create(BucketListApiService.class);
            Call<ResultDto> call = bucketListApiService.getBucketListDetail(loginUserId, bucketListId);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Response<ResultDto> response = call.execute();
                        ResultDto result = response.body();

                        if (result != null && response.isSuccessful()) {
                            Object resultData = result.getData();  // 응답 데이터

                            // 응답 데이터를 findDetailResponseDto로 convert
                            BucketListDto.FindDetailResponseDto findDetailResponseDto = new Gson().fromJson(new Gson().toJson(resultData), BucketListDto.FindDetailResponseDto.class);

                            categoryId = findDetailResponseDto.getId();
                            title = findDetailResponseDto.getTitle();
                            registered_date = findDetailResponseDto.getRegisteredDate();
                            importance = findDetailResponseDto.getImportance();
                            achievement_rate = findDetailResponseDto.getAchievementRate();
                            achievement_date = findDetailResponseDto.getAchievementDate();
                            target_date = findDetailResponseDto.getTargetDate();
                            memo = findDetailResponseDto.getMemo();
                            category_name = findDetailResponseDto.getCategoryName();

                            Log.i("Load BucketList Detail", "SUCCESS");
                        } else {
                            Log.i("Load BucketList Detail", "FAIL");
                            Log.e("Response error", response.toString());

                            // 에러 바디를 ErrorResultDto로 convert
                            Converter<ResponseBody, ErrorResultDto> errorConverter = client.responseBodyConverter(ErrorResultDto.class, ErrorResultDto.class.getAnnotations());
                            ErrorResultDto error = errorConverter.convert(response.errorBody());

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
                        }
                    } catch (IOException e) {
                        Log.e("Throwable error", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }).start();
            end = System.currentTimeMillis();
            Thread.sleep((end - start) * 100);

        } catch (UnauthorizedAccessException e) {  // 인증되지 않은 사용자가 접근할 때 발생하는 예외
            PrintToast(e.getMessage());  // 에러 메시지 출력
            logout();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 값 등록
        bName.setText(title);
        d_Day.setText(inIntent.getStringExtra("dDay"));  // 디데이 값 받기
        crtDate.setText("등록일 " + registered_date);
        rBar.setRating(importance);
        cName.setText("[" + category_name + "]");
        pBar.setProgress(achievement_rate);
        achvRt.setText(achievement_rate + "%");
        tgdtTv.setText("목표일 [" + target_date + "]");
        memoTv.setText(memo);
        memoTv.setBackgroundColor(Color.WHITE);

        // 달성일이 null이면 위젯 없애기
        if (TextUtils.isEmpty(achievement_date))
            achvdtTv.setVisibility(View.GONE);
        else
            achvdtTv.setText("달성일 [" + achievement_date + "]");

        // 달성 했으면 버킷리스트 명 흐리게 표시
        if (achievement_rate == 100) {
            bName.setTextColor(Color.argb(255, 255, 215, 0));

            Drawable progressDrawable = getResources().getDrawable(R.drawable.progressbar_custom_2);
            progressDrawable.setBounds(pBar.getProgressDrawable().getBounds());
            pBar.setProgressDrawable(progressDrawable);
        }

    }

    //**************************** 버킷 수정, 삭제 이미지 버튼을 메뉴 액션바에 등록 ********************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // menu_edit_delete_bucket.xml에서 메뉴 내용을 읽어와 인플레이션하여 메뉴 등록
        // 메뉴 xml 파일을 클래스와 바인딩하여 인플레이션할 수 있도록해주는 클래스의 싱글톤 이용
        getMenuInflater().inflate(R.menu.menu_edit_delete_bucket, menu);
        return true;
    }

    //**************************** 메뉴의 item(항목)을 선택했을 때 동작하는 이벤트 처리 ********************************
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {  // 선택된 메뉴 판별 및 처리
            case R.id.editB:  // 수정 버튼 선택
                // 명시적 인텐트를 사용하여 AddActivity 호출
                Intent intent = new Intent(DetailActivity.this, AddActivity.class);
                // Hash 구조(키, 값)로 데이터를 저장하여 값 전달
                intent.putExtra("flag", 2);
                intent.putExtra("title", title);
                intent.putExtra("category_name", category_name);
                startActivity(intent);
                finish();
                return true;

            case R.id.deleteB:  // 삭제 버튼 선택
                // AlertDialog.Builder 클래스로 대화상자 생성
                AlertDialog.Builder deleteDlg = new AlertDialog.Builder(DetailActivity.this);
                deleteDlg.setMessage("해당 버킷리스트를 삭제하시겠습니까?");  // 내용 입력
                // 확인 버튼을 클릭했을 때 동작하는 이벤트 처리
                deleteDlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            validateLoginState();  // 로그인 상태 확인

                            bucketListApiService = client.create(BucketListApiService.class);
                            Call<ResultDto> call = bucketListApiService.deleteBucketList(loginUserId, bucketListId);
                            call.enqueue(new Callback<ResultDto>() {
                                @Override
                                public void onResponse(Call<ResultDto> call, Response<ResultDto> response) {
                                    ResultDto result = response.body();  // 응답 결과 바디

                                    if (result != null && response.isSuccessful()) {
                                        Log.i("Delete BucketList", "SUCCESS");
                                        PrintToast("버킷리스트가 삭제되었습니다.");  // PrintToast() 함수 호출하여 토스트 메세지 출력

                                        // 명시적 인텐트를 사용하여 MainActivity 호출
                                        Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {  // 버킷리스트 삭제 실패
                                        Log.i("Delete BucketList", "FAIL");
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
                });
                // 취소 버튼을 클릭했을 때 동작하는 이벤트 처리
                deleteDlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                deleteDlg.show();  // 대화상자 화면 출력
                return true;
        }
        return false;
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
