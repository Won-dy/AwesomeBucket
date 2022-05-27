package com.example.awesomebucket.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.awesomebucket.MyDBHelper;
import com.example.awesomebucket.R;

import java.util.ArrayList;


public class CtgrListVAdapter extends BaseAdapter {  // 일반 클래스

    // 변수 선언
    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;

    Context context;
    ListView listV;
    CtgrListVAdapter ctgrListVAdapter;
    View ctgrDialogView, toastView;
    TextView toastTv;
    Toast toast;
    EditText ctgrEt;

    String getctgrName, getEditctgrName;
    int ctgrNum;

    // 생성자
    public CtgrListVAdapter(Context _context) {
        // Context 객체를 통해 CategoryActivity를 전달
        context = _context;
        // context를 형변환 하여 CategoryActivity에 정의된 뷰에 접근
        listV = ((CategoryActivity) context).listV;
        ctgrListVAdapter = ((CategoryActivity) context).ctgrListVAdapter;
    }

    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList 선언 및 생성
    private ArrayList<CtgrListVItem> ctgrListVItems = new ArrayList<CtgrListVItem>();

    @Override
    public int getCount() {
        return ctgrListVItems.size();  // Adapter에 사용되는 아이템 개수 반환
    }

    @Override
    public Object getItem(int position) {
        return ctgrListVItems.get(position);  // 지정된 position에 있는 인덱스의 데이터 반환
    }

    @Override
    public long getItemId(int position) {
        return position;  // position에 있는 데이터와 관계된 아이템의 행 ID 반환
    }

    public View getView(int position, View convertView, ViewGroup parent) {  // (항목의 위치, 현재 위치에 해당하는 뷰 객체, 부모 컨테이너 객체)
        // position에 있는 데이터를 화면에 출력하는데 사용될 View를 구성하여 리턴
        final int pos = position;
        final Context context = parent.getContext();

        // toast.xml을 View로서 inflating하고 뷰 참조 후 뷰 객체 변수에 인플레이팅된 뷰를 할당
        toast = new Toast(context);
        toastView = (View) View.inflate(context, R.layout.toast, null);
        toastTv = (TextView) toastView.findViewById(R.id.toastTv);

        myDBHelper = new MyDBHelper(context);  // DB와 Table 생성 (MyDBHelper.java의 생성자, onCreate() 호출)

        // "category_listview_item" Layout을 inflate하여 convertView 참조
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.category_listview_item, parent, false);
        }

        // 뷰 객체 변수에 인플레이팅된 뷰를 할당
        final TextView ctgrName = convertView.findViewById(R.id.ctgrName);
        final ImageButton ctgrEditBtn = convertView.findViewById(R.id.ctgrEditBtn);
        final ImageButton ctgrDltBtn = convertView.findViewById(R.id.ctgrDltBtn);

        // Data Set(ctgrListVItems)에서 position에 위치한 데이터 참조하여 값을 얻음
        CtgrListVItem ctgrListVItem = ctgrListVItems.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        ctgrName.setText(ctgrListVItem.getName());

        // 디폴트 카테고리는 수정, 삭제 불가
        if (ctgrListVItem.isDefault()) {
            ctgrEditBtn.setVisibility(View.INVISIBLE);
            ctgrDltBtn.setVisibility(View.INVISIBLE);
        } else {
            ctgrEditBtn.setVisibility(View.VISIBLE);
            ctgrDltBtn.setVisibility(View.VISIBLE);
        }

        //**************************** 카테고리 수정 이미지 버튼을 클릭했을 때 동작하는 이벤트 처리 ********************************
        ctgrEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getctgrName = ctgrName.getText().toString();

                // AlertDialog.Builder 클래스로 대화상자 생성
                AlertDialog.Builder cEditDlg = new AlertDialog.Builder(context);
                cEditDlg.setTitle("카테고리 수정");  // 제목 설정
                cEditDlg.setIcon(R.drawable.ctgr_edit_dlg);  // 아이콘 설정
                // category_add.xml을 View로서 inflating하고 뷰 참조 후 뷰 객체 변수에 인플레이팅된 뷰를 할당
                ctgrDialogView = View.inflate(context, R.layout.category_add, null);
                cEditDlg.setView(ctgrDialogView);  // 대화상자에 ctgrDialogView 설정
                ctgrEt = ctgrDialogView.findViewById(R.id.ctgrEt);
                ctgrEt.setText(getctgrName);

                // 수정 버튼을 클릭했을 때 동작하는 이벤트 처리
                cEditDlg.setPositiveButton("수정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            // 카테고리의 수정
                            getEditctgrName = ctgrEt.getText().toString();
                            if (getEditctgrName.equals("")) {
                                throw new EmptyCtgrException();  // 카테고리에 빈칸을 입력한 경우 예외 처리
                            }

                            sqlDB = myDBHelper.getWritableDatabase();  // 읽고 쓰기용 DB 열기
                            Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
                            // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
                            cursor = sqlDB.rawQuery("SELECT category_name FROM category WHERE category_name = '" + getEditctgrName + "';", null);

                            if (cursor.getCount() > 0) {  // 결과 값이 존재하는 경우
                                cursor.close();  // cursor 닫기
                                throw new DuplicateCtgrException();  // 입력한 카테고리가 이미 존재하는 경우 예외 처리
                            }
                            cursor.close();  // cursor 닫기

                            // 카테고리 수정을 위해 UPDATE
                            sqlDB.execSQL("UPDATE category SET category_name = '" + getEditctgrName + "' WHERE category_name = '" + getctgrName + "';");
                            sqlDB.close();  // DB 닫기

                            recallCtgr();  // 카테고리 목록 갱신

                            PrintToast("카테고리 수정 : " + getctgrName + " -> " + getEditctgrName);  // PrintToast() 함수 호출하여 토스트 메세지 출력
                        } catch (EmptyCtgrException ee) {
                            PrintToast("카테고리를 입력하세요.");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                        } catch (DuplicateCtgrException de) {
                            PrintToast("이미 존재하는 카테고리입니다.");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                        } catch (Exception e) {
                            PrintToast("수정 실패");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                        }
                    }
                });
                // 취소 버튼을 클릭했을 때 동작하는 이벤트 처리
                cEditDlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                cEditDlg.show();  // 대화상자 화면 출력
            }

        });

        //**************************** 카테고리 삭제 이미지 버튼을 클릭했을 때 동작하는 이벤트 처리 ********************************
        ctgrDltBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getctgrName = ctgrName.getText().toString();
                // AlertDialog.Builder 클래스로 대화상자 생성
                AlertDialog.Builder dltDlg = new AlertDialog.Builder(context);
                dltDlg.setMessage("[ " + getctgrName + " ]" + " 카테고리를 삭제하시겠습니까?");  // 내용 입력

                // 삭제 버튼을 클릭했을 때 동작하는 이벤트 처리
                dltDlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            sqlDB = myDBHelper.getWritableDatabase();  // 읽고 쓰기용 DB 열기
                            Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
                            // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
                            cursor = sqlDB.rawQuery("SELECT category_number FROM category WHERE category_name = '" + getctgrName + "';", null);

                            while (cursor.moveToNext()) {  // 현재 커서의 다음 행으로 이동할 수 있을 때 까지 반복하여 데이터 operating
                                ctgrNum = Integer.parseInt(cursor.getString(0));  // category_number
                            }
                            cursor.close();  // cursor 닫기

                            // 삭제된 카테고리를 가진 버킷의 카테고리를 기타로 변경하기 위해 UPDATE
                            sqlDB.execSQL("UPDATE bucket SET category_number = 0 WHERE category_number = " + ctgrNum + ";");

                            // 카테고리 삭제를 위해 DELETE
                            sqlDB.execSQL("DELETE FROM category WHERE category_name = '" + getctgrName + "';");
                            sqlDB.close();  // DB 닫기

                            recallCtgr();  // 카테고리 목록 갱신

                            PrintToast("카테고리 삭제 : " + getctgrName);  // PrintToast() 함수 호출하여 토스트 메세지 출력
                        } catch (Exception e) {
                            PrintToast("삭제 실패");  // PrintToast() 함수 호출하여 토스트 메세지 출력
                        }
                    }
                });
                // 취소 버튼을 클릭했을 때 동작하는 이벤트 처리
                dltDlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dltDlg.show();  // 대화상자 화면 출력
            }
        });

        return convertView;
    }

    //**************************** 아이템 데이터 추가를 위한 함수 ********************************
    public void addItem(long id, boolean isDefault, String name) {
        CtgrListVItem item = new CtgrListVItem();  // CtgrListVItem 생성
        item.setName(name);  // CtgrListVItem에 아이템 setting
        item.setId(id);
        item.setDefault(isDefault);
        ctgrListVItems.add(item);  // ctgrListVItems에 item 추가
    }

    //**************************** 카테고리 갱신을 위한 recallCtgr() 함수 정의 ********************************
    public void recallCtgr() {
        sqlDB = myDBHelper.getReadableDatabase();  // 읽기용 DB 열기

        Cursor cursor;  // 조회된 data set을 담고있는 결과 집합인 cursor 선언
        // 쿼리의 결과 값을 리턴하는 rawQuery메소드를 이용하여 cursor에 저장
        cursor = sqlDB.rawQuery("SELECT category_name FROM category;", null);  // 카테고리 이름 검색

        ctgrListVAdapter = new CtgrListVAdapter((CategoryActivity) context);  // CategoryActivity에 CtgrListVAdapter 생성
        while (cursor.moveToNext()) {  // 현재 커서의 다음 행으로 이동할 수 있을 때 까지 반복하여 데이터 operating
//            ctgrListVAdapter.addItem(cursor.getString(0));  // 카테고리 수만큼 항목 추가
        }
        cursor.close();  // cursor 닫기
        sqlDB.close();  // DB 닫기

        listV.setAdapter(ctgrListVAdapter);  // ListView 객체에 CtgrListVAdapter 적용
    }

    //**************************** 커스텀 토스트 메세지 출력을 위한 PrintToast() 함수 정의 ********************************
    public void PrintToast(String msg) {
        // toast.xml이 msg 내용의 토스트 메세지로 나오도록 설정
        toastTv.setText(msg);
        toast.setView(toastView);
        toast.show();
    }

}
