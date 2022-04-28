package com.example.awesomebucket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // 변수 선언
    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;

    Button ctgrManagerBtn;
    Spinner ctgrSpn, sortSpn;
    ArrayList<String> cSList, sSList;
    ArrayAdapter<String> cSAdapter, sSAdapter;

    ArrayList<MainRecyclerVItem> List;
    MainRecyclerVAdapter mainRecyclerVAdapter;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    String title, target_date;
    String bucketName, d_day, dDay;
    String sltCtgr;
    int category_num, sltCtgr_flag = 0;
    int achievement_rate, category_number;
    float importance;
    long pressedTime = 0; //'뒤로가기' 버튼 클릭했을 때의 시간

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("My Awesome Bucket List");

        // findViewById() 메소드를 사용하여 activity_main.xml에 정의된 뷰를 inflation(객체화)하여 뷰 참조
        // 뷰 객체 변수에 인플레이팅된 뷰를 할당
        mRecyclerView = findViewById(R.id.rcV);
        ctgrSpn = findViewById(R.id.ctgrSpn);
        sortSpn = findViewById(R.id.sortSpn);
        ctgrManagerBtn = findViewById(R.id.ctgrManagerBtn);

        myDBHelper = new MyDBHelper(this);  // DB와 Table 생성 (MyDBHelper.java의 생성자, onCreate() 호출)

        //**************************** Table에 초기 데이터 넣기 ********************************
        try {
            sqlDB = myDBHelper.getWritableDatabase();  // 읽고 쓰기용 DB 열기

            // 카테고리의 초기 데이터 삽입
            sqlDB.execSQL("INSERT INTO category VALUES ( 0, '기타' );");
            sqlDB.execSQL("INSERT INTO category(category_name) VALUES ( '자기 계발' );");
            sqlDB.execSQL("INSERT INTO category(category_name) VALUES ( '자산 관리' );");
            sqlDB.execSQL("INSERT INTO category(category_name) VALUES ( '취미' );");

            // 버킷리스트 초기 데이터 삽입
            sqlDB.execSQL("INSERT INTO bucket ( title, category_number, importance, achievement_rate, target_date, memo ) " +
                    "VALUES ( '1억 모으기', 2, 3, 60, '2024-12-31', '3년 안에 1억 모으자 현재 잔고 : 6천만원' );");
            sqlDB.execSQL("INSERT INTO bucket ( title, category_number, importance, achievement_rate, target_date, memo ) " +
                    "VALUES ( '몸무게 5KG 빼기', 0, 2, 0, '2022-08-01', '목표 몸무게 : 45kg' );");
            sqlDB.execSQL("INSERT INTO bucket ( title, category_number, importance, achievement_rate, target_date, memo ) " +
                    "VALUES ( '책 10권 읽기', 1, 2, 10, '2022-12-31', '독서는 마음의 양식!' );");
            sqlDB.execSQL("INSERT INTO bucket ( title, category_number, importance, achievement_rate, target_date, completion_date, memo ) " +
                    "VALUES ( '운전면허 따기', 1, 1, 100, '2021-02-20', '2021-02-24', '합격~!' );");
            sqlDB.close();  // DB 닫기
        } catch (Exception e) {
            // 애플리케이션 설치 후 최초 실행 시에 한번만 초기 데이터가 삽입되고 이후에는 예외처리를 통해 트라이 문이 실행되지 않아 데이터가 삽입되지 않는다
        }


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
        sqlDB = myDBHelper.getReadableDatabase();  // 읽기용 DB 열기
        Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
        // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
        cursor = sqlDB.rawQuery("SELECT category_name FROM category;", null);

        // ArrayList 생성 및 값 넣기
        cSList = new ArrayList<>();
        cSList.add("전체");
        while (cursor.moveToNext()) {  // 현재 커서의 다음 행으로 이동할 수 있을 때 까지 반복하여 데이터 operating
            cSList.add(cursor.getString(0));
        }
        cursor.close();  // cursor 닫기
        sqlDB.close();  // DB 닫기

        // ArrayAdapter에 ArrayList 넣기
        cSAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, cSList);
        ctgrSpn.setAdapter(cSAdapter);  // spinner 객체에 ArrayAdapter 적용

        // 카테고리 Spinner의 Item을 선택했을 때 동작하는 이벤트 처리
        ctgrSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sltCtgr = cSList.get(position);  // 선택된 item의 position의 값 저장

                // RecyclerView에 표시할 데이터 리스트 생성 및 BucketInfo() 함수를 호출하여 값 넣기
                if ((cSList.get(position)).equals("전체")) {  // 전체를 선택했을 경우
                    List = new ArrayList<>();

                    sqlDB = myDBHelper.getReadableDatabase();  // 읽기용 DB 열기

                    Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
                    // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
                    cursor = sqlDB.rawQuery("SELECT title, achievement_rate, importance, target_date FROM bucket;", null);

                    BucketInfo(cursor);  // BucketInfo() 함수 호출

                    cursor.close();  // cursor 닫기
                    sqlDB.close();  // DB 닫기
                } else {  // 카테고리 중 하나를 선택 했을 경우
                    List = new ArrayList<>();

                    sqlDB = myDBHelper.getReadableDatabase();  // 읽기용 DB 열기
                    Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
                    // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
                    cursor = sqlDB.rawQuery("SELECT category_number FROM category WHERE category_name= '" + cSList.get(position) + "';", null);

                    while (cursor.moveToNext()) {  // 현재 커서의 다음 행으로 이동할 수 있을 때 까지 반복하여 데이터 operating
                        category_number = cursor.getInt(0);
                    }
                    cursor.close();  // cursor 닫기

                    // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
                    cursor = sqlDB.rawQuery("SELECT title, achievement_rate, importance, target_date FROM bucket WHERE category_number = " + category_number + ";", null);

                    BucketInfo(cursor);  // BucketInfo() 함수 호출

                    cursor.close();  // cursor 닫기
                    sqlDB.close();  // DB 닫기
                }
                sortSpn.setAdapter(sSAdapter);  // spinner 객체에 ArrayAdapter 적용


                // RecyclerView의 어댑터로 설정
                mainRecyclerVAdapter = new MainRecyclerVAdapter(List, getApplicationContext());   // MainRecyclerVAdapter List 넣기
                mRecyclerView.setAdapter(mainRecyclerVAdapter);  //  RecyclerView 객체에 MainRecyclerVAdapter 적용
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

                // 다중 정렬을 위해
                sqlDB = myDBHelper.getReadableDatabase();  // 읽기용 DB 열기

                Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
                // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
                cursor = sqlDB.rawQuery("SELECT category_number FROM category WHERE category_name= '" + sltCtgr + "';", null);

                if (cursor.getCount() == 0) {  // 전체를 선택했을 경우
                    sltCtgr_flag = 0;
                    cursor.close();  // cursor 닫기
                } else {  // 카테고리 중 하나를 선택 했을 경우
                    sltCtgr_flag = 1;
                    while (cursor.moveToNext()) {  // 현재 커서의 다음 행으로 이동할 수 있을 때 까지 반복하여 데이터 operating
                        category_num = cursor.getInt(0);  // 카테고리 번호 얻어오기
                    }
                    cursor.close();  // cursor 닫기
                }

                sqlDB.close();  // DB 닫기

                switch (position) {  // spinner의 선택된 item의 position 값 판별 후 sortBucket() 함수 호출하여 정렬
                    case 0:  // 최초 등록순
                        if (sltCtgr_flag == 0)
                            sortBucket("SELECT title, achievement_rate, importance, target_date FROM bucket ORDER BY bucket_number ASC;");
                        else
                            sortBucket("SELECT title, achievement_rate, importance, target_date FROM bucket WHERE category_number = " + category_num + " ORDER BY bucket_number ASC;");
                        break;
                    case 1:  // 최근 등록순
                        if (sltCtgr_flag == 0)
                            sortBucket("SELECT title, achievement_rate, importance, target_date FROM bucket ORDER BY bucket_number DESC;");
                        else
                            sortBucket("SELECT title, achievement_rate, importance, target_date FROM bucket WHERE category_number = " + category_num + " ORDER BY bucket_number DESC;");
                        break;
                    case 2:  // 중요도 높은순
                        if (sltCtgr_flag == 0)
                            sortBucket("SELECT title, achievement_rate, importance, target_date FROM bucket ORDER BY importance DESC;");
                        else
                            sortBucket("SELECT title, achievement_rate, importance, target_date FROM bucket WHERE category_number = " + category_num + " ORDER BY importance DESC;");
                        break;
                    case 3:  // 중요도 낮은순
                        if (sltCtgr_flag == 0)
                            sortBucket("SELECT title, achievement_rate, importance, target_date FROM bucket ORDER BY importance ASC;");
                        else
                            sortBucket("SELECT title, achievement_rate, importance, target_date FROM bucket WHERE category_number = " + category_num + " ORDER BY importance ASC;");
                        break;
                    case 4:  // 달성률 높은순
                        if (sltCtgr_flag == 0)
                            sortBucket("SELECT title, achievement_rate, importance, target_date FROM bucket ORDER BY achievement_rate DESC;");
                        else
                            sortBucket("SELECT title, achievement_rate, importance, target_date FROM bucket WHERE category_number = " + category_num + " ORDER BY achievement_rate DESC;");
                        break;
                    case 5:  // 달성률 낮은순
                        if (sltCtgr_flag == 0)
                            sortBucket("SELECT title, achievement_rate, importance, target_date FROM bucket ORDER BY achievement_rate ASC;");
                        else
                            sortBucket("SELECT title, achievement_rate, importance, target_date FROM bucket WHERE category_number = " + category_num + " ORDER BY achievement_rate ASC;");
                        break;
                }
                // RecyclerView의 어댑터로 설정
                mainRecyclerVAdapter = new MainRecyclerVAdapter(List, getApplicationContext());   // MainRecyclerVAdapter List 넣기
                mRecyclerView.setAdapter(mainRecyclerVAdapter);  //  RecyclerView 객체에 MainRecyclerVAdapter 적용
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
                    TextView bName = rv.getChildViewHolder(child).itemView.findViewById(R.id.bucketName);
                    TextView bDday = rv.getChildViewHolder(child).itemView.findViewById(R.id.dDay);
                    bucketName = bName.getText().toString();
                    dDay = bDday.getText().toString();
                    // Toast.makeText(getApplicationContext(), bucketName+position+"번 째 항목 선택", Toast.LENGTH_SHORT).show();

                    // 명시적 인텐트를 사용하여 DetailActivity 호출
                    // Hash 구조(키, 값)로 데이터를 저장하여 bucketName, dDay 값 전달
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra("name", bucketName);
                    intent.putExtra("dDay", dDay);
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
        // menu_addbucket.xml에서 메뉴 내용을 읽어와 인플레이션하여 메뉴 등록
        // 메뉴 xml 파일을 클래스와 바인딩하여 인플레이션할 수 있도록해주는 클래스의 싱글톤 이용
        getMenuInflater().inflate(R.menu.menu_add_bucket, menu);
        return true;
    }


    //**************************** 메뉴의 item(항목)을 선택했을 때 동작하는 이벤트 처리 ********************************
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {  // 선택된 메뉴 판별 및 처리
            case R.id.menu_add:  // 버킷 추가 화면으로 이동
                // 명시적 인텐트를 사용하여 AddActivity 호출
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                intent.putExtra("flag", 1);  // Hash 구조(키, 값)로 데이터를 저장하여 flag 값 전달
                startActivity(intent);

                return true;
        }
        return false;
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


    //**************************** 버킷리스트를 불러오기 위한 BucketInfo() 함수 정의 ********************************
    // RecyclerView에 표시할 데이터 리스트에 조회된 data set 값을 넣고, 그 데이터 리스트를 List에 넣기
    public void BucketInfo(Cursor cursor) {
        while (cursor.moveToNext()) {  // 현재 커서의 다음 행으로 이동할 수 있을 때 까지 반복하여 데이터 operating
            title = cursor.getString(0);
            achievement_rate = cursor.getInt(1);
            importance = cursor.getInt(2);
            target_date = cursor.getString(3);
            d_day = calDate(target_date);  // 디데이 계산 함수

            if (achievement_rate == 100)
                List.add(new MainRecyclerVItem(title, achievement_rate, achievement_rate + "%", importance, d_day, true));  // ArrayList 값 넣기
            else
                List.add(new MainRecyclerVItem(title, achievement_rate, achievement_rate + "%", importance, d_day, false));  // ArrayList 값 넣기
        }
    }


    //**************************** 정렬을 위한 sortBucket() 함수 정의 ********************************
    // 쿼리문을 받아와 BucketInfo() 함수를 호출하여 정렬
    public void sortBucket(String sqlstmt) {
        List = new ArrayList<>();  // ArrayList 생성
        sqlDB = myDBHelper.getReadableDatabase();
        Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
        // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
        cursor = sqlDB.rawQuery(sqlstmt, null);
        BucketInfo(cursor);  // BucketInfo() 함수 호출
        cursor.close();
        sqlDB.close();
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