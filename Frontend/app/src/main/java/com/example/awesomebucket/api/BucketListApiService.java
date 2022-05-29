package com.example.awesomebucket.api;

import com.example.awesomebucket.dto.BucketListDto;
import com.example.awesomebucket.dto.ResultDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BucketListApiService {

    // 버킷리스트 목록 조회 및 정렬 API
    @GET("users/{userId}/buckets")
    Call<ResultDto> getBucketLists(@Path("userId") Long userId,
                                   @Query("sort") String sort,
                                   @Query("direction") String direction,
                                   @Query(value = "category", encoded = true) String categoryName);

    // 버킷리스트 등록 API
    @POST("buckets")
    Call<ResultDto> createBucketList(@Body BucketListDto.CreateUpdateRequestDto createUpdateRequestDto);

    // 버킷리스트 상세조회 API
    @GET("users/{userId}/buckets/{bucketId}")
    Call<ResultDto> getBucketListDetail(@Path("userId") Long userId, @Path("bucketId") Long bucketId);
}
