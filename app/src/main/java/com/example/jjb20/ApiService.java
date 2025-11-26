package com.example.jjb20;

import com.example.jjb20.dto.FirebaseLoginRequestDto;
import com.example.jjb20.dto.HouseAmenitiesCreateRequestDto;
import com.example.jjb20.dto.HouseDetailResponseDto;
import com.example.jjb20.dto.HouseDto;
import com.example.jjb20.dto.HouseUpdateRequestDto;
import com.example.jjb20.dto.HousesCreateRequestDto;
import com.example.jjb20.dto.HousesResponseDto;
import com.example.jjb20.dto.RegisterRequestDto;
import com.example.jjb20.dto.ReservationCreateRequestDto;
import com.example.jjb20.dto.ReservationDTO;
import com.example.jjb20.dto.ReviewRequestDto;
import com.example.jjb20.dto.UserResponseDto;
import com.example.jjb20.entity.User;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    //로그인 (Firebase ID 토큰을 서버로 전달)

    @POST("api/auth/login")
    Call<UserResponseDto> login(@Body FirebaseLoginRequestDto body);

    //회원가입(DB 등록)

    @POST("api/auth/register")
    Call<UserResponseDto> register(@Body RegisterRequestDto body);

    // 보호된 API – Authorization 헤더 필요 함

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



    // 하우스 목록
    @GET("api/houses")
    Call<List<HouseDto>> getHouses(@Header("Authorization") String bearerToken);

    //내 집 불러 오기
    @GET("/api/houses/my/{userId}")
    Call<List<HouseDto>> getMyHouses(@Path("userId") int userId);

    //내 예약 불러 오기
    @GET("/api/reservations/my")
    Call<List<ReservationDTO>> getReservations(@Query("userId") long userId);


    @GET("api/houses/{id}/detail")
    Call<HouseDetailResponseDto> getHouseDetail(
            @Path("id") long id
    );

    //리뷰 업로드
    @POST("api/reviews/houses")
    Call<Object> createHouseReview(@Body ReviewRequestDto dto);

    //예약 생성
    @POST("/api/reservations")
    Call<ReservationDTO> createReservation(@Body ReservationCreateRequestDto req);

    // 어메니티 포함 전체 상세
    @GET("api/houses/{id}/full-detail")
    Call<HouseDetailResponseDto> getHouseFullDetail(@Path("id") Long id);

    @PUT("api/houses/{id}")
    Call<Void> updateHouseBasicInfo(@Path("id") long id, @Body HouseUpdateRequestDto requestDto);
}
