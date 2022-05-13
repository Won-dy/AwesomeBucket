package com.example.awesomebucket.api;

import com.example.awesomebucket.dto.ResultDto;
import com.example.awesomebucket.dto.UserDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginAPIService {

    @POST("login")
    Call<ResultDto> login(@Body UserDto.LoginRequestDto loginRequestDto);

}
