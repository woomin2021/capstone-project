package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jjb20.dto.HouseAmenitiesCreateRequestDto;
import com.example.jjb20.dto.HousesCreateRequestDto;
import com.example.jjb20.dto.HousesResponseDto;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterFinalActivity extends AppCompatActivity {

    private static final String TAG = "RegisterFinal";

    // 뷰 변수 선언
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private TextView subtitleText;
    private MaterialButton registerButton;
    private MaterialCardView addPhotoButton;

    private TextInputEditText priceEditText;
    private TextView photoCounterText;

    // API 서비스
    private ApiService apiService;

    // 갤러리에서 이미지를 선택하기 위한 최신 방식 (ActivityResultLauncher)
    // "registerForActivityResult"는 onCreate 또는 클래스 멤버 변수 초기화 시에 호출되어야 합니다.
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    // 사용자가 이미지를 선택한 경우
                    // TODO: 1. 선택된 이미지를 (Glide/Coil 등으로) ImageView에 로드
                    // TODO: 2. HorizontalScrollView 내부의 LinearLayout에 동적으로 ImageView 추가
                    // TODO: 3. photoCounterText 업데이트 (예: "1/5")
                } else {
                    // 사용자가 선택을 취소한 경우
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. XML 레이아웃 파일 설정
        setContentView(R.layout.activity_register_final);

        // 2. Retrofit 초기화
        Retrofit retrofit = RetrofitClient.getInstance();
        apiService = retrofit.create(ApiService.class);

        // 3. 뷰 초기화
        initViews();

        // 4. 이벤트 리스너 설정
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        subtitleText = findViewById(R.id.subtitle_text);
        registerButton = findViewById(R.id.register_button);
        addPhotoButton = findViewById(R.id.add_photo_button);
        priceEditText = findViewById(R.id.price_edit_text);
        photoCounterText = findViewById(R.id.photoCounterText);
    }

    private void setupListeners() {
        // 툴바 (뒤로가기) - 취소 처리
        toolbar.setNavigationOnClickListener(v -> {
            handleBackPressed();
        });

        // '사진 추가' 버튼 클릭 리스너
        addPhotoButton.setOnClickListener(v -> {
            openGallery();
        });

        // '등록하기' 버튼 클릭 리스너
        registerButton.setOnClickListener(v -> {
            performRegistration();
        });
    }

    /**
     * 뒤로가기 버튼 처리 (등록 취소)
     */
    @Override
    public void onBackPressed() {
        handleBackPressed();
    }

    /**
     * 뒤로가기 또는 취소 버튼 클릭 시 처리
     * 집이 생성된 상태에서 취소하면 houseId를 정리
     */
    private void handleBackPressed() {
        // 집이 이미 생성된 상태라면 houseId 정리
        Long houseId = PrefManager.getLong("houseId");
        if (houseId != null && houseId != -1L) {
            // 집 생성 후 취소한 경우 - houseId만 정리 (다른 데이터는 유지할 수도 있음)
            PrefManager.remove("houseId");
            Log.d(TAG, "Registration cancelled. houseId cleared.");
        }
        finish();
    }

    /**
     * 갤러리를 열어 사진을 선택하게 합니다.
     * (최신 안드로이드 방식인 PickVisualMedia 사용)
     */
    private void openGallery() {
        // TODO: 사진 개수가 5개 미만일 때만 갤러리를 열도록 조건 추가
        // if (currentPhotoCount < 5) { ... }

        // 갤러리를 열어 이미지만 선택하도록 함
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());

        // 만약 여러 장의 사진을 한 번에 선택하게 하려면
        // (참고) pickMultipleMedia.launch(...)
    }

    /**
     * '등록하기' 버튼 클릭 시 호출되는 메서드
     */
    private void performRegistration() {
         // 1. 가격 정보 가져오기
        String priceStr = priceEditText.getText().toString().trim();
        if (priceStr.isEmpty()) {
             Toast.makeText(this, "가격을 입력해주세요.", Toast.LENGTH_SHORT).show();
             return;
         }

        int pricePerNight;
        try {
            pricePerNight = Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "올바른 가격을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. SharedPreferences에서 모든 데이터 가져오기
        String title = PrefManager.get("house_title");
        String description = PrefManager.get("house_description");
        String address = PrefManager.get("house_address");
        String city = PrefManager.get("house_city", "서울");
        String country = PrefManager.get("house_country", "한국");
        List<String> amenityCodes = PrefManager.getStringList("house_amenity_codes");

        // 3. 필수 데이터 검증
        if (title == null || title.isEmpty()) {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description == null || description.isEmpty()) {
            Toast.makeText(this, "설명을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (address == null || address.isEmpty()) {
            Toast.makeText(this, "주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. 버튼 비활성화
        registerButton.setEnabled(false);

        // 5. Firebase ID 토큰 가져오기
        String idToken = PrefManager.get("idToken");
        if (idToken == null || idToken.isEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            registerButton.setEnabled(true);
            return;
        }

        String bearerToken = "Bearer " + idToken;

        // 6. 집 생성 API 호출
        HousesCreateRequestDto houseRequest = new HousesCreateRequestDto(
                title,
                description,
                address,
                city,
                country,
                pricePerNight
        );

        Call<HousesResponseDto> houseCall = apiService.createHouse(bearerToken, houseRequest);
        houseCall.enqueue(new Callback<HousesResponseDto>() {
            @Override
            public void onResponse(Call<HousesResponseDto> call, Response<HousesResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long houseId = response.body().getId();
                    Log.d(TAG, "House created with id: " + houseId);

                    // houseId를 PrefManager에 저장 (다른 Activity에서 사용 가능하도록)
                    PrefManager.put("houseId", houseId);

                    // 7. 편의시설 저장 API 호출
                    if (amenityCodes != null && !amenityCodes.isEmpty()) {
                        saveAmenities(bearerToken, houseId, amenityCodes);
                    } else {
                        // 편의시설이 없어도 등록 완료
                        onRegistrationComplete();
                    }
                } else {
                    registerButton.setEnabled(true);
                    String errorMessage = "집 등록 실패: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Toast.makeText(RegisterFinalActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "House creation failed: " + response.code() + " - " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<HousesResponseDto> call, Throwable t) {
                registerButton.setEnabled(true);
                Toast.makeText(RegisterFinalActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "House creation failed", t);
            }
        });
    }

    /**
     * 편의시설 저장 API 호출
     */
    private void saveAmenities(String bearerToken, Long houseId, List<String> amenityCodes) {
        HouseAmenitiesCreateRequestDto amenitiesRequest = new HouseAmenitiesCreateRequestDto(
                houseId,
                amenityCodes
        );

        Call<Void> amenitiesCall = apiService.saveHouseAmenities(bearerToken, amenitiesRequest);
        amenitiesCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Amenities saved successfully");
                    onRegistrationComplete();
                } else {
                    registerButton.setEnabled(true);
                    String errorMessage = "편의시설 저장 실패: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Toast.makeText(RegisterFinalActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Amenities save failed: " + response.code() + " - " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                registerButton.setEnabled(true);
                Toast.makeText(RegisterFinalActivity.this, "편의시설 저장 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Amenities save failed", t);
            }
        });
    }

    /**
     * 등록 완료 처리
     */
    private void onRegistrationComplete() {
        // SharedPreferences에서 집 등록 데이터 삭제
        PrefManager.clearHouseRegistrationData();

        Toast.makeText(this, "등록이 완료되었습니다!", Toast.LENGTH_SHORT).show();

        // 메인 화면으로 이동
        Intent intent = new Intent(RegisterFinalActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // 현재 등록 플로우의 모든 액티비티 종료
    }
}