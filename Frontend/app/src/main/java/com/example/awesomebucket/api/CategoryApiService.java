package com.example.awesomebucket.api;

import com.example.awesomebucket.dto.ResultDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CategoryApiService {

    // 카테고리 목록 조회 API
    @GET("users/{userId}/categories")
    Call<ResultDto> getCategories(@Path("userId") Long userId);

}
