package com.example.awesomebucket.api;

import com.example.awesomebucket.dto.ResultDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BucketListApiService {

    // 버킷리스트 목록 조회 API
    @GET("users/{userId}/buckets")
    Call<ResultDto> getBucketLists(@Path("userId") Long userId);

}
