package com.example.jjb20;

import com.example.jjb20.dto.FirebaseLoginRequestDto;
import com.example.jjb20.dto.UserResponseDto;
import com.example.jjb20.entity.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    // 1) 로그인 (Firebase ID 토큰을 서버로 전달)
    @POST("api/auth/login") // ← 앞에 슬래시( / ) 없이! baseUrl이 http://host:8080/ 이라고 가정
    Call<UserResponseDto> login(@Body FirebaseLoginRequestDto body);

    // 2) 보호된 API – Bearer 토큰을 헤더로 전달
    @GET("api/users")
    Call<List<User>> getUsers(@Header("Authorization") String bearerToken);

    @POST("api/users")
    Call<User> addUser(@Header("Authorization") String bearerToken,
                       @Body User user);
}
