package com.example.jjb20;

import com.example.jjb20.dto.FirebaseLoginRequestDto;
import com.example.jjb20.dto.HouseAmenitiesCreateRequestDto;
import com.example.jjb20.dto.HousesCreateRequestDto;
import com.example.jjb20.dto.HousesResponseDto;
import com.example.jjb20.dto.RegisterRequestDto;
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
    //    결과: UserResponseDto(우리 DB 기준의 유저 정보)
    @POST("api/auth/login")
    Call<UserResponseDto> login(@Body FirebaseLoginRequestDto body);

    // 2) 회원가입(DB 등록)
    //    Firebase 계정은 이미 만들어져 있고, idToken + name + phone 으로 DB에 저장
    @POST("api/auth/register")
    Call<UserResponseDto> register(@Body RegisterRequestDto body);

    // 3) 보호된 API – Authorization 헤더 필요
    //    예: 유저 목록 조회
    @GET("api/users")
    Call<List<User>> getUsers(@Header("Authorization") String bearerToken);

    //유저 생성
    @POST("api/users")
    Call<User> addUser(
            @Header("Authorization") String bearerToken,
            @Body User user
    );

    //  집 생성
    @POST("api/houses")
    Call<HousesResponseDto> createHouse(@Header("Authorization") String bearerToken,
                                        @Body HousesCreateRequestDto requestDto);

    //  집 편의시설 저장
    @POST("api/houses/amenities")
    Call<Void> saveHouseAmenities(@Header("Authorization") String bearerToken,
                                  @Body HouseAmenitiesCreateRequestDto requestDto);
}
