package com.example.jjb20;

import com.example.jjb20.entity.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("/api/users")
    Call<List<User>> getUsers();

    @POST("/api/users")
    Call<User> addUser(@Body User user);
}
