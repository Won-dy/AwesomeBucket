package com.example.awesomebucket.activity;

public class MainRecyclerVItem {  // 하나의 행에 대한 정보를 담을 클래스
    public String bucketName;
    public int pB;
    public String achvRate;
    public float rB;
    public String dDay;
    boolean fin;  // 달성 여부

    public MainRecyclerVItem(String bucketName, int pB, String achvRate, float rB, String dDay, boolean fin){
        this.bucketName = bucketName;
        this.pB = pB;
        this.achvRate = achvRate;
        this.rB = rB;
        this.dDay = dDay;
        this.fin = fin;
    }
}

