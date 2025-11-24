package com.example.jjb20;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class RetrofitClient {

    //retrofit 연결ㄴ
    private static Retrofit retrofit;
    //현재 서버에 스프링을 안올려놨기 때문에 10.0.0.2 사용
    // TODO 서버에 스프링 올릴시 223.130.130,141로 교체 해야함
    private static final String BASE_URL = "http://223.130.130.141:8080/";

    public static Retrofit getInstance() {
        if (retrofit == null) {

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {

                            // 로그인 시 PrefManager 에 저장해둔 Firebase ID Token
                            String idToken = PrefManager.get("idToken", null);

                            Request.Builder builder = chain.request().newBuilder();

                            if (idToken != null) {
                                builder.addHeader("Authorization", "Bearer " + idToken);
                            }

                            return chain.proceed(builder.build());
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
