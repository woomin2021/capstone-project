package com.example.jjb20;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    //retrofit 연결ㄴ
    private static Retrofit retrofit;
    //현재 서버에 스프링을 안올려놨기 때문에 10.0.0.2 사용
    // TODO 서버에 스프링 올릴시 223.130.130,141로 교체 해야함
    private static final String BASE_URL = "http://10.0.2.2:8080/";

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
