package com.example.awesomebucket.api;

import com.example.awesomebucket.dto.ResultDto;
import com.example.awesomebucket.dto.UserDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface UserApiService {

    @POST("users")
    Call<ResultDto> join(@Body UserDto.JoinRequestDto joinRequestDto);

    @PATCH("users/pw/edit")
    Call<ResultDto> changePassword(@Body UserDto.LoginRequestDto loginRequestDto);

}
