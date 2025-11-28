package com.example.jjb20;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    //현재 서버에 스프링을 안올려놨기 때문에 10.0.2.2 사용
    // TODO 서버에 스프링 올릴시 223.130.130,141로 교체 해야함
    private static final String BASE_URL = "http://10.0.2.2:8080/";

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

            // Gson 설정: 순환 참조 및 알 수 없는 필드 처리
            // Gson은 기본적으로 알 수 없는 필드를 자동으로 무시하므로,
            // DTO에 정의되지 않은 필드(photos, amenities 등)는 자동으로 스킵됩니다.
            Gson gson = new GsonBuilder()
                    .setLenient()  // JSON 파싱을 더 관대하게 처리
                    .serializeNulls()  // null 값도 직렬화
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
