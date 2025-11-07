package com.example.jjb20;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    //retrofit 연결ㄴ
    private static Retrofit retrofit;
    private static final String BASE_URL = "http://223.130.130.141:8080/";

    public static  Retrofit getInstance(){
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;

    }
}
