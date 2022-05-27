package com.example.awesomebucket.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.awesomebucket.MyDBHelper;
import com.example.awesomebucket.R;

public class DetailActivity extends AppCompatActivity {

    // 변수 선언
    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;

    TextView bName, d_Day, crtDate, cName, achvRt, tgdtTv, achvdtTv, memoTv;
    ProgressBar pBar;
    RatingBar rBar;
    View toastView;
    TextView toastTv;
    Toast toast;

    String category_name, title, target_date, completion_date, creation_date, memo;
    int category_number, achievement_rate;
    float importance;
    long pressedTime = 0; //'뒤로가기' 버튼 클릭했을 때의 시간

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
        Intent inIntent = getIntent();  // bucketName, dDay 값 받기
        bName.setText(inIntent.getStringExtra("name"));
        title = bName.getText().toString();
        d_Day.setText(inIntent.getStringExtra("dDay"));

        myDBHelper = new MyDBHelper(this);  // DB와 Table 생성 (MyDBHelper.java의 생성자, onCreate() 호출)

        sqlDB = myDBHelper.getReadableDatabase();  // 읽기용 DB 열기

        Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
        // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
        cursor = sqlDB.rawQuery("SELECT * FROM bucket WHERE title = '" + title + "';", null);

        while (cursor.moveToNext()) {  // 현재 커서의 다음 행으로 이동할 수 있을 때 까지 반복하여 데이터 operating
            category_number = cursor.getInt(2);
            importance = cursor.getInt(3);
            achievement_rate = cursor.getInt(4);
            target_date = cursor.getString(5);
            completion_date = cursor.getString(6);
            creation_date = cursor.getString(7);
            memo = cursor.getString(8);
        }
        cursor.close();  // cursor 닫기

        // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
        cursor = sqlDB.rawQuery("SELECT category_name FROM category WHERE category_number = " + category_number + ";", null);

        while (cursor.moveToNext()) {  // 현재 커서의 다음 행으로 이동할 수 있을 때 까지 반복하여 데이터 operating
            category_name = cursor.getString(0);
        }
        cursor.close();  // cursor 닫기

        sqlDB.close();  // DB 닫기

        // 값 등록
        crtDate.setText("등록일 " + creation_date);
        rBar.setRating(importance);
        cName.setText("[" + category_name + "]");
        pBar.setProgress(achievement_rate);
        achvRt.setText(achievement_rate + "%");
        tgdtTv.setText("목표일 [" + target_date + "]");
        memoTv.setText(memo);
        memoTv.setBackgroundColor(Color.WHITE);

        // 달성일이 null이면 위젯 없애기
        if (TextUtils.isEmpty(completion_date))
            achvdtTv.setVisibility(View.GONE);
        else
            achvdtTv.setText("달성일 [" + completion_date + "]");

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

                        sqlDB = myDBHelper.getWritableDatabase();  // 읽고 쓰기용 DB 열기

                        sqlDB.execSQL("DELETE FROM bucket WHERE title = '" + title + "';");  // 버킷 삭제

                        sqlDB.close();  // DB 닫기

                        // 명시적 인텐트를 사용하여 MainActivity 호출
                        Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                        startActivity(intent);

                        finish();

                        PrintToast("[ " + title + " ] 삭제되었습니다.");  // PrintToast() 함수 호출하여 토스트 메세지 출력
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
