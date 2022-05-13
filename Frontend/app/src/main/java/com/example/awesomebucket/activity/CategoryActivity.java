package com.example.awesomebucket.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.awesomebucket.MyDBHelper;
import com.example.awesomebucket.R;

public class CategoryActivity extends AppCompatActivity {

    // 변수 선언
    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;

    ListView listV;
    CtgrListVAdapter ctgrListVAdapter;
    View toastView;
    TextView toastTv;
    Toast toast;

    String ctgr_name;
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
        toastView = (View)View.inflate(CategoryActivity.this, R.layout.toast, null);
        toastTv = (TextView)toastView.findViewById(R.id.toastTv);

        // 입력받는 방법을 관리하는 Manager 객체를 요청하여 InputMethodmanager에 반환
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        myDBHelper = new MyDBHelper(this);  // DB와 Table 생성 (MyDBHelper.java의 생성자, onCreate() 호출)

        sqlDB = myDBHelper.getReadableDatabase();  // 읽기용 DB 열기
        callCtgr();  // 카테고리 불러오기위해 callCtgr() 함수 호출
        sqlDB.close();  // DB 닫기

        listV.setAdapter(ctgrListVAdapter);  // ListView 객체에 CtgrListVAdapter 적용

        // 카테고리 추가 버튼을 클릭했을 때 동작하는 이벤트 처리
        ctgrAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ctgr_name = ctgrAddET.getText().toString();

                    if(ctgr_name.equals("")) {
                        throw new EmptyCtgrException();  // 카테고리에 빈칸을 입력한 경우 예외 처리
                    }
                    sqlDB = myDBHelper.getWritableDatabase();  // 읽고 쓰기용 DB 열기

                    Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
                    // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
                    cursor = sqlDB.rawQuery("SELECT category_name FROM category WHERE category_name = '" + ctgr_name + "';", null);

                    if(cursor.getCount()>0) {  // 결과 값이 존재하는 경우
                        cursor.close();  // cursor 닫기
                        throw new DuplicateCtgrException();  // 입력한 카테고리가 이미 존재하는 경우 예외 처리
                    }
                    cursor.close();  // cursor 닫기

                    sqlDB.execSQL("INSERT INTO category(category_name) VALUES ( '" + ctgr_name + "' );");  // 카테고리 Table에 카테고리 이름 값 삽입

                    callCtgr();  // 카테고리 불러오기
                    sqlDB.close();  // DB 닫기

                    listV.setAdapter(ctgrListVAdapter);  // ListView 객체에 ListViewAdapter 적용

                    ctgrAddET.setText("");  // 입력했던 내용을 비우고
                    ctgrAddET.clearFocus();  // 초점을 없앤다
                    imm.hideSoftInputFromWindow(ctgrAddET.getWindowToken(), 0);  // 키보드 숨기기

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

    }

    //**************************** 카테고리를 불러오기 위한 callCtgr() 함수 정의 ********************************
    public void callCtgr() {
        Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
        // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
        cursor = sqlDB.rawQuery("SELECT category_name FROM category;", null);  // 카테고리 이름 검색

        ctgrListVAdapter = new CtgrListVAdapter(this);  // CtgrListVAdapter 생성
        while(cursor.moveToNext()) {  // 현재 커서의 다음 행으로 이동할 수 있을 때 까지 반복하여 데이터 operating
            ctgrListVAdapter.addItem(cursor.getString(0));  // 카테고리 수만큼 항목 추가
        }
        cursor.close();  // cursor 닫기
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
        if(System.currentTimeMillis() > pressedTime + 2000){
            //현재 시간을 pressedTime 에 저장
            pressedTime = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
        }

        //마지막 '뒤로가기' 버튼 클릭시간이 이전의 '뒤로가기' 버튼 클릭 시간과의 차이가 2초보다 작으면
        else{
            moveTaskToBack(true);  // 태스크를 백그라운드로 이동
            finishAndRemoveTask();  // 액티비티 종료 + 태스크 리스트에서 지우기
            android.os.Process.killProcess(android.os.Process.myPid());  // 앱 프로세스 종료
        }
    }
}
