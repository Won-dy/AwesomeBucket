package com.example.awesomebucket.api;

import com.example.awesomebucket.dto.CategoryDto;
import com.example.awesomebucket.dto.ResultDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CategoryApiService {

    // 카테고리 목록 조회 API
    @GET("users/{userId}/categories")
    Call<ResultDto> getCategories(@Path("userId") Long userId);

    // 카테고리 등록 API
    @POST("categories")
    Call<ResultDto> createCategory(@Body CategoryDto.CreateUpdateRequestDto createUpdateRequestDto);
}
