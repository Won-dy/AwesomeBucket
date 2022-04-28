package com.example.awesomebucket;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {

    // 생성자 호출
    public MyDBHelper(Context context){
        super(context, "bucketDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {  // 새로운 테이블 생성 시 한번만 호출
        // DB 버전 1의 테이블 생성
        // 카테고리 테이블, 버킷 테이블 생성
        db.execSQL("CREATE TABLE category (category_number INTEGER PRIMARY KEY, category_name VARCHAR(10) NOT NULL UNIQUE);");
        db.execSQL("CREATE TABLE bucket (bucket_number INTEGER PRIMARY KEY, title VARCHAR(30) NOT NULL UNIQUE, " +
                "category_number INTEGER DEFAULT 0, importance INTEGER DEFAULT 0, " +
                "achievement_rate INTEGER DEFAULT 0, target_date DATE NOT NULL, completion_date DATE, " +
                "creation_date DATE DEFAULT (date('now','localtime')), memo VARCHAR(255), " +
                "FOREIGN KEY(category_number) REFERENCES category(category_number) ON UPDATE CASCADE);");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  // 초기화의 역할
        // 기존 테이블 삭제 후 테이블 다시 생성, 버전 변경될 때 마다 호출
        db.execSQL("DROP TABLE IF EXISTS bucket");
        db.execSQL("DROP TABLE IF EXISTS category");
        onCreate(db);
    }
}
