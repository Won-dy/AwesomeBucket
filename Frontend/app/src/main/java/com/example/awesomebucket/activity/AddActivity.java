package com.example.awesomebucket.activity;

import static com.example.awesomebucket.MyConstant.NO_LOGIN_USER_ID;
import static com.example.awesomebucket.MyConstant.PREFERENCE_FILE_USER;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.awesomebucket.MyDBHelper;
import com.example.awesomebucket.MySharedPreferences;
import com.example.awesomebucket.R;
import com.example.awesomebucket.api.APIClient;
import com.example.awesomebucket.api.CategoryApiService;
import com.example.awesomebucket.dto.CategoryDto;
import com.example.awesomebucket.dto.ErrorResultDto;
import com.example.awesomebucket.dto.ResultDto;
import com.example.awesomebucket.exception.UnauthorizedAccessException;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

class EmptyCtgrException extends Exception {
}  // 값을 입력하지 않은 경우 사용자 정의 예외 처리

class DuplicateCtgrException extends Exception {
}  // 입력 값이 이미 존재하는 경우 사용자 정의 예외 처리

class ValidityException extends Exception {
}  // 달성률 입력 값이 0~100 범위를 벗어나는 경우 사용자 정의 예외 처리

public class AddActivity extends AppCompatActivity {

    // 변수 선언
    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;

    View ctgrDialogView, toastView;
    TextView toastTv, achvdTv;
    Toast toast;
    EditText cNameEt, ctgrEt, nameEt, memoEt, achvEt, tgdEt, achvdEt;
    Button sltCtgrBtn;

    Retrofit client = APIClient.getClient();
    CategoryApiService categoryApiService;

    Long loginUserId;
    int y, m, d;
    int flag;
    int addSuccess = 0, editSuccess = 0;
    int category_number, achievement_rate, bucket_number;
    float importance;
    String title, memo, target_date, completion_date, ctgr_name, bName, category_name, sltCtgr, beforeEdit;
    long pressedTime = 0; //'뒤로가기' 버튼 클릭했을 때의 시간

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);
        setTitle("My Bucket");

        // findViewById() 메소드를 사용하여 add.xml에 정의된 뷰를 inflation(객체화)하여 뷰 참조
        // 뷰 객체 변수에 인플레이팅된 뷰를 할당
        final RatingBar rB = findViewById(R.id.rB);
        cNameEt = findViewById(R.id.cNameEt);
        sltCtgrBtn = findViewById(R.id.sltCtgrBtn);
        nameEt = findViewById(R.id.nameEt);
        memoEt = findViewById(R.id.memoEt);
        achvEt = findViewById(R.id.achvEt);
        tgdEt = findViewById(R.id.tgdEt);
        achvdEt = findViewById(R.id.achvdEt);
        achvdTv = findViewById(R.id.achvdTv);
        achvdEt = findViewById(R.id.achvdEt);
        ImageButton tgdBtn = findViewById(R.id.tgdBtn);
        ImageButton achvdBtn = findViewById(R.id.achvdBtn);
        ImageButton cAddBtn = findViewById(R.id.cAddBtn);
        ImageButton cpltBtn = findViewById(R.id.cpltBtn);

        // toast.xml을 View로서 inflating하고 뷰 참조 후 뷰 객체 변수에 인플레이팅된 뷰를 할당
        toast = new Toast(AddActivity.this);
        toastView = (View) View.inflate(AddActivity.this, R.layout.toast, null);
        toastTv = (TextView) toastView.findViewById(R.id.toastTv);

        // 로그인 한 User의 기본키 조회
        loginUserId = MySharedPreferences.getLoginUserId(getApplicationContext(), PREFERENCE_FILE_USER, "loginUserId");

        myDBHelper = new MyDBHelper(this);  // DB와 Table 생성 (MyDBHelper.java의 생성자, onCreate() 호출)

        // intent를 통해 데이터 값을 전달 받음
        Intent inIntent = getIntent();
        flag = inIntent.getIntExtra("flag", 0);  // 추가 버튼을 클릭하여 화면에 연결된 것인지 수정 버튼을 클릭하여 화면에 연결된 것인지를 판별
        bName = inIntent.getStringExtra("title");
        category_name = inIntent.getStringExtra("category_name");
        beforeEdit = bName;

        //**************************** 달성률이 100인 경우 달성일 입력 폼 띄우기 ********************************

        achvEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("100")) {
                    achvdTv.setVisibility(View.VISIBLE);
                    achvdEt.setVisibility(View.VISIBLE);
                    achvdBtn.setVisibility(View.VISIBLE);
                } else {
                    achvdTv.setVisibility(View.GONE);
                    achvdEt.setText(null);
                    achvdEt.setVisibility(View.GONE);
                    achvdBtn.setVisibility(View.GONE);
                }
            }
        });


        //**************************** 추가 버튼을 클릭하여 화면에 연결된 경우 ********************************
        if (flag == 1) {
            // 추가에서 연결
        }
        //**************************** 수정 버튼을 클릭하여 화면에 연결된 경우 DB에 저장된 값들을 각 입력란에 띄움 ********************************
        if (flag == 2) {

            sqlDB = myDBHelper.getReadableDatabase();  // 읽기용 DB 열기

            Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
            // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
            cursor = sqlDB.rawQuery("SELECT * FROM bucket WHERE title = '" + bName + "';", null);

            while (cursor.moveToNext()) {  // 현재 커서의 다음 행으로 이동할 수 있을 때 까지 반복하여 데이터 operating
                category_number = cursor.getInt(2);
                importance = cursor.getInt(3);
                achievement_rate = cursor.getInt(4);
                target_date = cursor.getString(5);
                completion_date = cursor.getString(6);
                memo = cursor.getString(8);
            }
            cursor.close();  // cursor 닫기
            sqlDB.close();  // DB 닫기

            // DB에 저장된 값들을 각 입력란에 띄움
            cNameEt.setText(category_name);
            nameEt.setText(bName);
            memoEt.setText(memo);
            rB.setRating(importance);
            achvEt.setText(String.valueOf(achievement_rate));
            tgdEt.setText(target_date);
            achvdEt.setText(completion_date);
        }

        //**************************** 카테고리 선택 버튼을 클릭했을 때 동작하는 이벤트 처리 ********************************
        sltCtgrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AlertDialog.Builder 클래스로 대화상자 생성
                AlertDialog.Builder sltCtgrDlg = new AlertDialog.Builder(AddActivity.this);
                sltCtgrDlg.setTitle("카테고리 선택");  // 제목 설정
                sltCtgrDlg.setIcon(R.drawable.ctgr_select_dlg);  // 아이콘 설정

                // ArrayAdapter 객체 생성
                final ArrayAdapter<String> cAdapter = new ArrayAdapter<String>(AddActivity.this, android.R.layout.select_dialog_singlechoice);

                // 카테고리 불러오기
                try {
                    validateLoginState();  // 로그인 상태 확인

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

                                // ArrayAdapter에 데이터 넣기
                                for (CategoryDto.FindResponseDto category : categories)
                                    cAdapter.add(category.getName());

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

                // 취소 버튼을 클릭했을 때 동작하는 이벤트 처리
                sltCtgrDlg.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();  // 대화상자 종료
                    }
                });

                // ArrayAdapter 객체를 builder에 setAdapter 함수의 인수로 넘겨 adapter 적용
                sltCtgrDlg.setAdapter(cAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sltCtgr = cAdapter.getItem(which);  // 선택한 항목의 값 가져오기
                        cNameEt.setText(sltCtgr);
                    }
                });
                sltCtgrDlg.show();  // 대화상자 화면 출력
            }
        });

        //**************************** RatingBar 값이 바뀌었을 때 동작하는 이벤트 처리 ********************************
        rB.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                importance = rating;
            }
        });

        //**************************** 카테고리 추가 버튼을 클릭했을 때 동작하는 이벤트 처리 ********************************
        cAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AlertDialog.Builder 클래스로 대화상자 생성
                AlertDialog.Builder cAddDlg = new AlertDialog.Builder(AddActivity.this);
                cAddDlg.setTitle("카테고리 추가");  // 제목 설정
                cAddDlg.setIcon(R.drawable.ctgr_add_dlg);  // 아이콘 설정

                // category_add.xml을 View로서 inflating하고 뷰 참조 후 뷰 객체 변수에 인플레이팅된 뷰를 할당
                ctgrDialogView = View.inflate(AddActivity.this, R.layout.category_add, null);
                cAddDlg.setView(ctgrDialogView);  // 대화상자에 ctgrDialogView 설정

                // 추가 버튼을 클릭했을 때 동작하는 이벤트 처리
                cAddDlg.setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            // 카테고리의 추가
                            ctgrEt = ctgrDialogView.findViewById(R.id.ctgrEt);
                            ctgr_name = ctgrEt.getText().toString();

                            if (ctgr_name.equals("")) {
                                throw new EmptyCtgrException();  // 카테고리에 빈칸을 입력한 경우 예외 처리
                            }
                            sqlDB = myDBHelper.getWritableDatabase();  // 읽고 쓰기용 DB 열기

                            Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
                            // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
                            cursor = sqlDB.rawQuery("SELECT category_name FROM category WHERE category_name = '" + ctgr_name + "';", null);

                            if (cursor.getCount() > 0) {  // 결과 값이 있는 경우
                                cursor.close();  // cursor 닫기
                                throw new DuplicateCtgrException();  // 입력한 카테고리가 이미 존재하는 경우 예외 처리
                            }
                            cursor.close();  // cursor 닫기

                            sqlDB.execSQL("INSERT INTO category(category_name) VALUES ( '" + ctgr_name + "' );");  // 카테고리 Table에 카테고리 이름 값 삽입

                            sqlDB.close();  // DB 닫기

                            PrintToast("카테고리 추가 : " + ctgr_name);  // PrintToast() 함수 호출하여 토스트 메세지 출력
                        } catch (EmptyCtgrException ee) {
                            PrintToast("카테고리를 입력하세요.");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                        } catch (DuplicateCtgrException de) {
                            PrintToast("이미 존재하는 카테고리입니다.");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                        } catch (Exception e) {
                            PrintToast("추가 실패");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                        }
                    }
                });
                // 취소 버튼을 클릭했을 때 동작하는 이벤트 처리
                cAddDlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                cAddDlg.show();  // 대화상자 화면 출력
            }
        });

        //**************************** 달성 이미지 버튼을 클릭했을 때 동작하는 이벤트 처리 ********************************
        cpltBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                achvEt.setText("100");  // 달성률을 100으로 설정
            }
        });

        //**************************** 현재 날짜 가져오기 위한 Calendar 클래스 ********************************
        Calendar calendar = Calendar.getInstance();
        y = calendar.get(Calendar.YEAR);
        m = calendar.get(Calendar.MONTH);  // m+1은 DatePickerDialog에서 해줌
        d = calendar.get(Calendar.DAY_OF_MONTH);

        //**************************** 목표일 지정 이미지 버튼을 클릭했을 때 동작하는 이벤트 처리 ********************************
        tgdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DatePickerDialog 클래스
                // 날짜를 설정할 때 동작하는 이벤트 처리
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // YYYY-MM-DD 형식으로 출력
                        tgdEt.setText(String.format("%d", year) + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", dayOfMonth));
                    }
                }, y, m, d);  // 기본으로 지정되어있는 날짜를 오늘 날짜로 설정

                datePickerDialog.getDatePicker().setCalendarViewShown(false);  // 스피너 형태로 설정
                datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);  // 데이트 피커를 띄워주는 대화상자의 배경을 투명하게 설정
                datePickerDialog.show();  // 날짜 선택 대화상자 화면 출력
            }
        });

        //**************************** 달성일 지정 이미지 버튼을 클릭했을 때 동작하는 이벤트 처리 ********************************
        achvdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DatePickerDialog 클래스
                // 날짜를 설정할 때 동작하는 이벤트 처리
                DatePickerDialog datePickerDialog2 = new DatePickerDialog(AddActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // YYYY-MM-DD 형식으로 출력
                        achvdEt.setText(String.format("%d", year) + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", dayOfMonth));
                    }
                }, y, m, d);  // 기본으로 지정되어있는 날짜를 오늘 날짜로 설정

                datePickerDialog2.getDatePicker().setCalendarViewShown(false);  // 스피너 형태로 설정
                datePickerDialog2.getWindow().setBackgroundDrawableResource(android.R.color.transparent);  // 데이트 피커를 띄워주는 대화상자의 배경을 투명하게 설정
                datePickerDialog2.show();  // 날짜 선택 대화상자 화면 출력
            }
        });
    }

    //**************************** 완료 버튼 메뉴 액션바에 집어 넣기 ********************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // menu_check.xml에서 메뉴 내용을 읽어와 인플레이션하여 메뉴 등록
        // 메뉴 xml 파일을 클래스와 바인딩하여 인플레이션할 수 있도록해주는 클래스의 싱글톤 이용
        getMenuInflater().inflate(R.menu.menu_check, menu);
        return true;
    }

    // 메뉴의 item(항목)을 선택했을 때 동작하는 이벤트 처리
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {  // 선택된 메뉴 판별 및 처리
            // 체크(완료) 버튼 누르면
            case R.id.checkB:

                // 입력 값 변수에 저장
                title = nameEt.getText().toString();
                memo = memoEt.getText().toString();
                target_date = tgdEt.getText().toString();

                if ((achvEt.getText().toString()).equals("")) {  // 달성률 빈칸이면
                    achievement_rate = 0;  // 0%로 설정
                } else {
                    achievement_rate = Integer.parseInt(achvEt.getText().toString());  // 입력 값으로 설정
                }

                int isEmpty = 0;  // 달성일 입력 안되었으면 0, 입력 되었으면 1
                if ((achvdEt.getText().toString()).equals("")) {  // 달성일 빈칸이면
                    completion_date = null;  // null로 설정
                } else {
                    completion_date = achvdEt.getText().toString();  // 입력 값으로 설정
                    isEmpty = 1;
                }

                try {
                    // 필수 항목 미기재 시
                    if (title.equals("") || target_date.equals("") || (cNameEt.getText().toString()).equals(""))
                        throw new EmptyCtgrException();  // 값을 입력하지 않은 경우 예외 처리
                    if ((achvdEt.getText().toString()).equals("") && achvdEt.getVisibility() == View.VISIBLE)
                        throw new EmptyCtgrException();  // 값을 입력하지 않은 경우 예외 처리

                    if (achievement_rate > 100 || achievement_rate < 0)
                        throw new ValidityException();  // 달성률 입력 값이 0~100 범위를 벗어나는 경우 예외 처리


                    //**************************** 추가를 위해 해당 액티비티를 접근했을 경우 ********************************
                    if (flag == 1) {
                        try {
                            sqlDB = myDBHelper.getWritableDatabase();  // 읽고 쓰기용 DB 열기

                            Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
                            // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
                            cursor = sqlDB.rawQuery("SELECT title FROM bucket WHERE title = '" + title + "';", null);

                            if (cursor.getCount() > 0) {  // 결과 값이 있는 경우
                                cursor.close();  // cursor 닫기
                                throw new DuplicateCtgrException();  // 입력한 버킷 명이 이미 존재하는 경우 사용자 정의 예외 처리
                            }
                            cursor.close();

                            // 입력한 버킷 정보들을 bucket 테이블에 INSERT
                            switch (isEmpty) {
                                // 달성일 null로 설정
                                case 0:
                                    sqlDB.execSQL("INSERT INTO bucket ( title, category_number, importance, achievement_rate, target_date, completion_date, memo )" +
                                            "VALUES ( '" + title + "', '" + category_number + "', '" + importance + "', '" + achievement_rate + "', '" +
                                            target_date + "', " + completion_date + ", '" + memo + "' );");
                                    break;
                                // 달성일 입력값으로 설정
                                case 1:
                                    sqlDB.execSQL("INSERT INTO bucket ( title, category_number, importance, achievement_rate, target_date, completion_date, memo )" +
                                            "VALUES ( '" + title + "', '" + category_number + "', '" + importance + "', '" + achievement_rate + "', '" +
                                            target_date + "', '" + completion_date + "', '" + memo + "' );");
                                    break;
                            }
                            sqlDB.close();
                            addSuccess = 1;
                            editSuccess = 0;

                        } catch (DuplicateCtgrException de) {
                            PrintToast("이미 존재하는 버킷명입니다.");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                        } catch (Exception e) {
                            PrintToast("버킷 추가 실패");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                            addSuccess = 0;
                        }
                    }

                    //**************************** 수정을 위해 해당 액티비티를 접근했을 경우 ********************************
                    else if (flag == 2) {
                        try {
                            sqlDB = myDBHelper.getWritableDatabase();  // 읽고 쓰기용 DB 열기

                            Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
                            // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
                            cursor = sqlDB.rawQuery("SELECT bucket_number FROM bucket WHERE title= '" + beforeEdit + "';", null);

                            while (cursor.moveToNext()) {  // 현재 커서의 다음 행으로 이동할 수 있을 때 까지 반복하여 데이터 operating
                                bucket_number = cursor.getInt(0);
                            }
                            cursor.close();  // cursor 닫기

                            // 수정한 버킷 정보들을 bucket 테이블에 UPDATE
                            switch (isEmpty) {
                                // 달성일 null로 설정
                                case 0:
                                    sqlDB.execSQL("UPDATE bucket SET title = '" + title + "', category_number = " + category_number + ", importance = " + importance + ", " +
                                            "achievement_rate = " + achievement_rate + ", target_date = '" + target_date + "', completion_date = " + completion_date + ", " +
                                            "memo = '" + memo + "' WHERE bucket_number = " + bucket_number + ";");
                                    break;
                                // 달성일 입력값으로 설정
                                case 1:
                                    sqlDB.execSQL("UPDATE bucket SET title = '" + title + "', category_number = " + category_number + ", importance = " + importance + ", " +
                                            "achievement_rate = " + achievement_rate + ", target_date = '" + target_date + "', completion_date = '" + completion_date + "', " +
                                            "memo = '" + memo + "' WHERE bucket_number = " + bucket_number + ";");
                            }
                            sqlDB.close();
                            editSuccess = 1;
                            addSuccess = 0;

                        } catch (Exception e) {
                            PrintToast("버킷 수정 실패");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                            editSuccess = 0;
                        }
                    }
                    // 명시적 인텐트를 사용하여 MainActivity 호출
                    Intent intent = new Intent(AddActivity.this, MainActivity.class);
                    startActivity(intent);

                    finish();

                    if (addSuccess == 1) PrintToast("버킷이 추가되었습니다.");
                    if (editSuccess == 1) PrintToast("버킷이 수정되었습니다.");

                    return true;
                } catch (EmptyCtgrException ee) {
                    PrintToast("필수 항목을 입력해 주세요.");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                } catch (ValidityException ve) {
                    PrintToast("달성률은 0~100 사이의 값을 입력해 주세요");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                } catch (Exception e) {
                    PrintToast("실패");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                }
        }
        return false;
    }


    //**************************** 로그인되어있나 확인하는 validateLoginState() 함수 정의 ********************************
    public void validateLoginState() throws UnauthorizedAccessException {
        if (loginUserId == NO_LOGIN_USER_ID)
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