package com.example.awesomebucket.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.awesomebucket.R;

import java.util.ArrayList;

public class MainRecyclerVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // RecyclerVeiw Adapter 구현 - RecyclerView에 표시될 아이템 뷰를 생성하는 역할

    // RecyclerView의 행을 표시하는 view를 저장하는 뷰홀더 클래스
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView bucketName, achvRate, dDay;
        ProgressBar pB;
        RatingBar rB;

        // 생성자
        MyViewHolder(View view){
            super(view);
            // findViewById() 메소드를 사용하여 view에 정의된 뷰를 inflation(객체화)하여 뷰 참조
            // 뷰 객체 변수에 인플레이팅된 뷰를 할당
            bucketName = view.findViewById(R.id.bucketName);
            pB = view.findViewById(R.id.pB);
            achvRate = view.findViewById(R.id.achvRate);
            rB = view.findViewById(R.id.rB);
            dDay = view.findViewById(R.id.dDay);
        }
    }

    // 생성자
    private ArrayList<MainRecyclerVItem> List;
    private Context context;
    // ArrayList<RecyclerViewItem> 객체를 전달 받음
    MainRecyclerVAdapter(ArrayList<MainRecyclerVItem> List, Context context){
        this.List = List;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // RecyclerView의 행을 표시하는데 사용되는 레이아웃 xml을 가져오는 역할
        // 뷰 홀더가 콘텐츠를 표시하기 위해 사용하는 뷰를 설정 > main_recyclerview_item.xml을 확장(inflate)
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_recyclerview_item, parent, false);

        return new MyViewHolder(v);  // 뷰 홀더 객체 생성 및 리턴
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // onCreateViewHolder에서 만든 View와 실제 입력되는 데이터를 연결
        // 적절한 데이터를 가져와 뷰홀더의 레이아웃에 값 표시
        // RecyclerView의 행에 보여질 뷰 설정
        MyViewHolder myViewHolder = (MyViewHolder) holder;

        // 각 뷰 홀더는 뷰를 사용하여 단일 항목을 표시함
        // 뷰 홀더를 데이터에 바인딩하여 특정 위치에 할당
        myViewHolder.bucketName.setText(List.get(position).bucketName);
        myViewHolder.pB.setProgress(List.get(position).pB);
        myViewHolder.achvRate.setText(List.get(position).achvRate);
        myViewHolder.rB.setRating(List.get(position).rB);

        if(List.get(position).fin) {
            myViewHolder.bucketName.setTextColor(Color.argb(100,0,0,0));
            myViewHolder.bucketName.setPaintFlags(myViewHolder.bucketName.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);

            Drawable progressDrawable = context.getResources().getDrawable(R.drawable.progressbar_custom_2);
            progressDrawable.setBounds(myViewHolder.pB.getProgressDrawable().getBounds());
            myViewHolder.pB.setProgressDrawable(progressDrawable);

            myViewHolder.dDay.setText("달성");
            myViewHolder.dDay.setTextColor(Color.RED);
        }
        else if(!(List.get(position).fin))   {
            myViewHolder.bucketName.setTextColor(Color.argb(255,0,0,0));
            myViewHolder.bucketName.setPaintFlags(0);

            Drawable progressDrawable = context.getResources().getDrawable(R.drawable.progressbar_custom);
            progressDrawable.setBounds(myViewHolder.pB.getProgressDrawable().getBounds());
            myViewHolder.pB.setProgressDrawable(progressDrawable);

            myViewHolder.dDay.setText(List.get(position).dDay);
            myViewHolder.dDay.setTextColor(Color.argb(255,62,112,174));
        }
    }

    @Override
    public int getItemCount() {
        // RecyclerView의 행 개수 리턴
        return List.size();
    }
}
