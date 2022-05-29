package com.example.awesomebucket.activity;

import static com.example.awesomebucket.MyConstant.NO_ID;
import static com.example.awesomebucket.MyConstant.PREFERENCE_FILE_USER;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.awesomebucket.MySharedPreferences;
import com.example.awesomebucket.R;
import com.example.awesomebucket.api.APIClient;
import com.example.awesomebucket.api.BucketListApiService;
import com.example.awesomebucket.api.CategoryApiService;
import com.example.awesomebucket.dto.BucketListDto;
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

public class MainActivity<T> extends AppCompatActivity {

    // 변수 선언
    Retrofit client = APIClient.getClient();
    CategoryApiService categoryAPIService;
    BucketListApiService bucketListApiService;

    Long loginUserId;

    TextView instructionTv;
    Button ctgrManagerBtn;
    Spinner ctgrSpn, sortSpn;
    ArrayList<String> cSList, sSList;
    ArrayAdapter<String> cSAdapter, sSAdapter;
    ArrayList<BucketListDto.FindResponseDto> bucketListAll;  // 불러온 버킷리스트 목록

    ArrayList<MainRecyclerVItem> List = new ArrayList<>();
    MainRecyclerVAdapter mainRecyclerVAdapter;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    // 커스텀 토스트 메시지
    View toastView;
    TextView toastTv;
    Toast toast;

    String title, target_date;
    String bucketName, d_day, dDay;
    String seletedCategoryName;  // 카테고리 필터 스피너에서 선택 된 카테고리 이름
    String seletedSorting;  // 정렬 필터 스피너에서 선택된 정렬 기준
    boolean isMultipleFiltering;  // 다중 필터링 여부
    int achievement_rate, category_number;
    float importance;
    long bucketListId;
    long pressedTime = 0; //'뒤로가기' 버튼 클릭했을 때의 시간
    long start, end; // 함수 실행시간 측정을 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("My Bucket List");

        // findViewById() 메소드를 사용하여 activity_main.xml에 정의된 뷰를 inflation(객체화)하여 뷰 참조
        // 뷰 객체 변수에 인플레이팅된 뷰를 할당
        mRecyclerView = findViewById(R.id.rcV);
        ctgrSpn = findViewById(R.id.ctgrSpn);
        sortSpn = findViewById(R.id.sortSpn);
        ctgrManagerBtn = findViewById(R.id.ctgrManagerBtn);
        instructionTv = findViewById(R.id.instructionTv);

        // toast.xml을 View로 inflating하고 뷰 참조 후 뷰 객체 변수에 인플레이팅된 뷰를 할당
        toast = new Toast(getApplicationContext());
        toastView = (View) View.inflate(getApplicationContext(), R.layout.toast, null);
        toastTv = (TextView) toastView.findViewById(R.id.toastTv);

        // 로그인 한 User의 기본키 조회
        loginUserId = MySharedPreferences.getLoginUserId(getApplicationContext(), PREFERENCE_FILE_USER, "loginUserId");


        //**************************** 카테고리 관리 화면으로 이동 ********************************

        // 카테고리 관리 버튼을 클릭했을 때 동작하는 이벤트 처리
        ctgrManagerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 명시적 인텐트를 사용하여 CategoryActivity 호출
                Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                startActivity(intent);
            }
        });


        //**************************** 카테고리 필터 스피너 ********************************

        // 카테고리 필터 스피너
        // ArrayList 생성 및 값 넣기
        cSList = new ArrayList<>();
        cSList.add("전체");

        // 카테고리 불러오기
        try {
            validateLoginState();  // 로그인 상태 확인

            categoryAPIService = client.create(CategoryApiService.class);
            Call<ResultDto> call = categoryAPIService.getCategories(loginUserId);
            call.enqueue(new Callback<ResultDto>() {
                @Override
                public void onResponse(Call<ResultDto> call, Response<ResultDto> response) {

                    ResultDto result = response.body();  // 응답 결과 바디

                    if (result != null && response.isSuccessful()) {
                        ArrayList resultData = (ArrayList) result.getData();// 응답 데이터

                        // 응답 데이터를 CategoryResponseDto로 convert
                        ArrayList<CategoryDto.FindResponseDto> categories = new ArrayList<>();
                        for (Object resultDatum : resultData)
                            categories.add(new Gson().fromJson(new Gson().toJson(resultDatum), CategoryDto.FindResponseDto.class));

                        // 카테고리 리스트에 값 넣기
                        for (CategoryDto.FindResponseDto category : categories)
                            cSList.add(category.getName());

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

        // ArrayAdapter에 ArrayList 넣기
        cSAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, cSList);
        ctgrSpn.setAdapter(cSAdapter);  // spinner 객체에 ArrayAdapter 적용


        // 카테고리 Spinner의 Item을 선택했을 때 동작하는 이벤트 처리
        ctgrSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                seletedCategoryName = cSList.get(position);  // 선택된 item의 position의 값 저장
                sortSpn.setAdapter(sSAdapter);  // spinner 객체에 ArrayAdapter 재적용
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        //**************************** 버킷리스트 정렬 스피너 ********************************

        // 정렬 스피너
        // ArrayList 생성 및 값 넣기
        sSList = new ArrayList<>();
        sSList.add("최초 등록순");
        sSList.add("최근 등록순");
        sSList.add("중요도 높은순");
        sSList.add("중요도 낮은순");
        sSList.add("달성률 높은순");
        sSList.add("달성률 낮은순");

        // ArrayAdapter에 ArrayList 넣기
        sSAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, sSList);
        sortSpn.setAdapter(sSAdapter);  // Spinner 객체에 ArrayAdapter 적용

        // 정렬 Spinner의 Item을 선택했을 때 동작하는 이벤트 처리
        sortSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    validateLoginState();  // 로그인 상태 확인
                    start = System.currentTimeMillis();

                    seletedSorting = sSList.get(position);  // 선택된 item의 position의 값 저장
                    if (seletedCategoryName == null) return;
                    if (seletedCategoryName.equals("전체")) { // 전체를 선택했을 경우
                        isMultipleFiltering = false;
                    } else {  // 카테고리 중 하나를 선택 했을 경우
                        isMultipleFiltering = true;
                    }
                    // 정렬 기준 명시적으로 구분
                    if (seletedSorting.equals("최초 등록순")) {
                        if (isMultipleFiltering)
                            bucketListAll = loadBucketList("registeredDate", "ASC", seletedCategoryName);  // 카테고리별 정렬
                        else
                            bucketListAll = loadBucketList("registeredDate", "ASC", null);  // 전체 정렬
                    } else if (seletedSorting.equals("최근 등록순")) {
                        if (isMultipleFiltering)
                            bucketListAll = loadBucketList("registeredDate", "DESC", seletedCategoryName);  // 카테고리별 정렬
                        else
                            bucketListAll = loadBucketList("registeredDate", "DESC", null);  // 전체 정렬
                    } else if (seletedSorting.equals("중요도 높은순")) {
                        if (isMultipleFiltering)
                            bucketListAll = loadBucketList("importance", "DESC", seletedCategoryName);  // 카테고리별 정렬
                        else bucketListAll = loadBucketList("importance", "DESC", null);  // 전체 정렬
                    } else if (seletedSorting.equals("중요도 낮은순")) {
                        if (isMultipleFiltering)
                            bucketListAll = loadBucketList("importance", "ASC", seletedCategoryName);  // 카테고리별 정렬
                        else bucketListAll = loadBucketList("importance", "ASC", null);  // 전체 정렬
                    } else if (seletedSorting.equals("달성률 높은순")) {
                        if (isMultipleFiltering)
                            bucketListAll = loadBucketList("achievementRate", "DESC", seletedCategoryName);  // 카테고리별 정렬
                        else
                            bucketListAll = loadBucketList("achievementRate", "DESC", null);  // 전체 정렬
                    } else if (seletedSorting.equals("달성률 낮은순")) {
                        if (isMultipleFiltering)
                            bucketListAll = loadBucketList("achievementRate", "ASC", seletedCategoryName);  // 카테고리별 정렬
                        else
                            bucketListAll = loadBucketList("achievementRate", "ASC", null);  // 전체 정렬
                    }
                    addRecyclerViewItemList(bucketListAll);  // RecyclerView 항목에 버킷리스트 데이터를 추가
                    setInstructionVisibility(bucketListAll);  // 안내 텍스트 가시성 설정

                } catch (UnauthorizedAccessException e) {  // 인증되지 않은 사용자가 접근할 때 발생하는 예외
                    PrintToast(e.getMessage());  // 에러 메시지 출력
                    logout();
                } finally {
                    // RecyclerView의 어댑터로 설정
                    mainRecyclerVAdapter = new MainRecyclerVAdapter(List, getApplicationContext());   // MainRecyclerVAdapter List 넣기
                    mRecyclerView.setAdapter(mainRecyclerVAdapter);  //  RecyclerView 객체에 MainRecyclerVAdapter 적용
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        //**************************** RecyclerView ********************************
        mRecyclerView.setHasFixedSize(true);  // RecyclerView의 너비와 높이가 고정되어 어댑터 내용의 영향을 받지 않아 전체 목록의 정확한 크기를 알아 냄으로 써 RecyclerView가 더 잘 최적화 됨
        mLayoutManager = new LinearLayoutManager(this);  // LayoutManager > 배치 방법 결정, RecyclerView의 각 item들을 배치 LinearLayoutManager > 항목 배치 방법을 1차원 목록으로 정렬
        mRecyclerView.setLayoutManager(mLayoutManager);  // LayoutManager를 LinearLayoutManager로 적용

        // 구분선 넣기
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), new LinearLayoutManager(this).getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);  // 디바이더 적용

        // 제스처를 감지(구분)하는 클래스
        final GestureDetector gestureDetector = new GestureDetector(MainActivity.this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {  // 한번 터치 후 다음에 다시 터치 이벤트가 감지되지 않았을 때 즉, 한번 터치가 확실할 때 발생하는 이벤트
                        return true;
                    }
                });

        //**************************** RecyclerView의 Item을 터치했을 때 동작하는 이벤트 처리 ********************************
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

                // 터치한 위치가 몇 번째 아이템(항목)인지 알기위해
                // 손으로 터치한 곳의 좌표를 토대로 해당 Item의 View를 가져옴
                View child = rv.findChildViewUnder(e.getX(), e.getY());

                if (child != null && gestureDetector.onTouchEvent(e))  // item 위에서 터치가 발생했고, 한번 터치 됐을 때
                {
                    //현재 터치된 곳의 position을 가져옴
                    int position = rv.getChildAdapterPosition(child);

                    // ViewHolder를 얻어와 itemView에 정의된 뷰를 inflation(객체화)하여 뷰 참조 후 뷰 객체 변수에 인플레이팅된 뷰를 할당
                    TextView bucketListId = rv.getChildViewHolder(child).itemView.findViewById(R.id.bucketListId);
                    TextView dDdayTv = rv.getChildViewHolder(child).itemView.findViewById(R.id.dDay);
                    // 명시적 인텐트를 사용하여 DetailActivity 호출
                    // Hash 구조(키, 값)로 데이터를 저장하여 버킷리스트 ID, 디데이 값 전달
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra("bucketListId", Long.valueOf(bucketListId.getText().toString()));
                    intent.putExtra("dDay", dDdayTv.getText().toString());
                    startActivity(intent);
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
    }


    //**************************** 버킷 추가 화면으로 이동 ********************************
    // 버킷 추가 이미지 버튼을 메뉴 액션바에 등록
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // menu_main.xml에서 메뉴 내용을 읽어와 인플레이션하여 메뉴 등록
        // 메뉴 xml 파일을 클래스와 바인딩하여 인플레이션할 수 있도록해주는 클래스의 싱글톤 이용
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //**************************** 메뉴의 item(항목)을 선택했을 때 동작하는 이벤트 처리 ********************************
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {  // 선택된 메뉴 판별 및 처리
            case R.id.menu_add:  // 버킷 추가 버튼 선택
                // 명시적 인텐트를 사용하여 AddActivity 호출
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                intent.putExtra("flag", 1);  // Hash 구조(키, 값)로 데이터를 저장하여 flag 값 전달
                startActivity(intent);

                return true;

            case R.id.menu_logout:  // 로그아웃 버튼 선택
                PrintToast("로그아웃 되었습니다");
                logout();

                return true;
        }
        return false;
    }


    //**************************** 서버에서 버킷리스트를 불러오는 loadBucketList() 함수 정의 ********************************
    public ArrayList<BucketListDto.FindResponseDto> loadBucketList(String sort, String direction, String categoryName) {
        ArrayList<BucketListDto.FindResponseDto> bucketLists = new ArrayList<>();
        try {
            bucketListApiService = client.create(BucketListApiService.class);
            Call<ResultDto> call = bucketListApiService.getBucketLists(loginUserId, sort, direction, categoryName);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Response<ResultDto> response = call.execute();
                        ResultDto result = response.body();

                        if (result != null && response.isSuccessful()) {
                            ArrayList resultData = (ArrayList) result.getData();// 응답 데이터

                            // 응답 데이터를 ResponseDto로 convert
                            for (Object resultDatum : resultData)
                                bucketLists.add(new Gson().fromJson(new Gson().toJson(resultDatum), BucketListDto.FindResponseDto.class));

                            Log.i("Load BucketList", "SUCCESS");

                        } else {  // 버킷리스트 로드 실패
                            Log.i("Load BucketList", "FAIL");
                            Log.e("Response error", response.toString());

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
                        }
                    } catch (IOException e) {
                        Log.e("Throwable error", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }).start();
            end = System.currentTimeMillis();
            Thread.sleep((end - start) * 100);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return bucketLists;
    }


    //**************************** RecyclerView 항목에 버킷리스트 데이터를 추가하기 위한 addRecyclerViewItemList() 함수 정의 ********************************
    public void addRecyclerViewItemList(ArrayList<BucketListDto.FindResponseDto> bucketLists) {
        List = new ArrayList<>();
        for (BucketListDto.FindResponseDto bucketList : bucketLists) {
            bucketListId = bucketList.getId();
            title = bucketList.getTitle();
            achievement_rate = bucketList.getAchievementRate();
            importance = (float) bucketList.getImportance();
            target_date = bucketList.getTargetDate();
            d_day = calDate(target_date);  // 디데이 계산 함수

            if (achievement_rate == 100)
                List.add(new MainRecyclerVItem(bucketListId, title, achievement_rate, achievement_rate + "%", importance, d_day, true));  // ArrayList 값 넣기
            else
                List.add(new MainRecyclerVItem(bucketListId, title, achievement_rate, achievement_rate + "%", importance, d_day, false));  // ArrayList 값 넣기
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
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    //**************************** D-Day 계산을 위한 calDate() 함수 정의 ********************************
    public String calDate(String t_date) {  // int myear, int mmonth, int mday
        try {
            int y, m, d;  // 년, 월, 일
            final int ONE_DAY = 24 * 60 * 60 * 1000;  // Millisecond 형태의 하루 ( 86400000 = 24시간 * 60분 * 60초 * 1000(1초값) )
            Calendar today = Calendar.getInstance();  // 오늘 날짜
            Calendar dday = Calendar.getInstance();  // D-day 날짜

            // t_date > YYYY-MM-DD
            // 부분 문자열 함수를 사용하여 년, 월, 일 따로 변수에 저장
            y = Integer.parseInt(t_date.substring(0, 4));
            m = Integer.parseInt(t_date.substring(5, 7));
            d = Integer.parseInt(t_date.substring(8));

            dday.set(y, (m - 1), d);  // D-day 날짜 입력

            // 각각 날짜의 시간 값을 얻어옴
            long day = dday.getTimeInMillis() / ONE_DAY;
            long tday = today.getTimeInMillis() / ONE_DAY;

            long count = day - tday; // 오늘 날짜에서 D-day 날짜를 뺌

            // 출력 시 D-day 에 맞게 표시
            final String strFormat;
            if (count > 0) {  // 현재 날짜보다 미래면 D-
                strFormat = "D-%d";
            } else if (count == 0) {  // 현재 날짜와 같다면
                strFormat = "D-Day";
            } else {  // 현재 날짜보다 과거면 D+
                count *= -1;
                strFormat = "D+%d";
            }

            // 지정된 형식(strFormat)에 맞게 count 값을 문자열로 변환하여 strCount에 저장
            final String strCount = (String.format(strFormat, count));

            return strCount;

        } catch (Exception e) {  // 예외 처리
            e.printStackTrace();  // 에러 메세지의 발생 근원지를 찾아 단계별로 에러 출력
            return "fail";
        }
    }


    //**************************** 안내 텍스트 가시성 설정을 위한 setInstructionVisibility() 함수 정의 *******************************
    public void setInstructionVisibility(ArrayList<BucketListDto.FindResponseDto> bucketLists) {
        if (bucketLists.size() > 0)
            instructionTv.setVisibility(View.INVISIBLE);
        else
            instructionTv.setVisibility(View.VISIBLE);
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