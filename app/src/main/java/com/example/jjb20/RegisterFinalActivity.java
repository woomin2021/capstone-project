package com.example.jjb20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    private Uri selectedImageUri;   // 갤러리에서 선택한 1장의 사진
    private StorageReference storageReference;

    // 갤러리에서 이미지를 선택하기 위한 최신 방식 (ActivityResultLauncher)
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private void uploadImageToFirebase() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "업로드할 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading File...");
        progressDialog.show();

        // 파일명 생성
        String userId = PrefManager.get("uid", "unknown");
        String houseTitle = PrefManager.get("house_title", "no_title");

        if (userId.isEmpty()) userId = "임시_ID";
        if (houseTitle.isEmpty()) houseTitle = "임시_집_이름";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.KOREA);
        String fileName = userId + "_" + houseTitle + "_" + sdf.format(new Date());

        storageReference = FirebaseStorage.getInstance()
                .getReference("images/" + fileName);

        storageReference.putFile(selectedImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // 업로드 성공 → 다운로드 URL 받기
                    return storageReference.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    // PrefManager 또는 서버에 이미지 URL 저장
                    String imageUrl = downloadUri.toString();
                    PrefManager.put("house_image_url", imageUrl);

                    Toast.makeText(RegisterFinalActivity.this,
                            "성공적으로 업로드 되었습니다.",
                            Toast.LENGTH_SHORT).show();

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterFinalActivity.this,
                            "업로드 실패했습니다: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 0. PrefManager 초기화
        PrefManager.init(getApplicationContext());
        
        // 1. XML 레이아웃 파일 설정
        setContentView(R.layout.activity_register_final);

        // 2. Retrofit 초기화
        Retrofit retrofit = RetrofitClient.getInstance();
        apiService = retrofit.create(ApiService.class);

        // 3. 뷰 초기화
        initViews();

        // 4. ActivityResultLauncher 초기화 (뷰 초기화 후에 호출)
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;   // 선택된 이미지 저장

                // ImageView에 띄우기
                if (photoCounterText != null) {
                    photoCounterText.setText("1/5");
                }

                // 이미지 업로드
                uploadImageToFirebase();
            } else {
                Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. onBackPressedDispatcher()에 콜백 등록
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPressed();
            }
        });

        // 6. 이벤트 리스너 설정
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
        if (pickMedia != null) {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        } else {
            Toast.makeText(this, "갤러리를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

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

        PrefManager.put("house_price", pricePerNight);

        // 2. SharedPreferences에서 모든 데이터 가져오기
        String title = PrefManager.get("house_title");
        String description = PrefManager.get("house_description");
        String address = PrefManager.get("house_address");
        String city = PrefManager.get("house_city", "서울");
        String country = PrefManager.get("house_country", "한국");
        List<String> amenityCodes = PrefManager.getStringList("house_amenity_codes");
        int bedroomCount = PrefManager.getInt("house_bedroom_count", 0);
        int bedCount = PrefManager.getInt("house_bed_count", 0);
        int bathroomCount = PrefManager.getInt("house_bathroom_count", 0);
        String availableStartDate = PrefManager.get("house_available_start");
        String availableEndDate = PrefManager.get("house_available_end");

        // 디버깅용 로그 추가
        Log.d(TAG, "Registration data - title: " + title);
        Log.d(TAG, "Registration data - description: " + description);
        Log.d(TAG, "Registration data - address: " + address);
        Log.d(TAG, "Registration data - city: " + city + ", country: " + country);
        Log.d(TAG, "Registration data - bedroomCount: " + bedroomCount + ", bedCount: " + bedCount + ", bathroomCount: " + bathroomCount);
        Log.d(TAG, "Registration data - availableStartDate: " + availableStartDate + ", availableEndDate: " + availableEndDate);
        Log.d(TAG, "Registration data - pricePerNight: " + pricePerNight);
        Log.d(TAG, "Registration data - amenityCodes: " + amenityCodes);

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

        if (availableStartDate == null || availableStartDate.isEmpty() || 
            availableEndDate == null || availableEndDate.isEmpty()) {
            Toast.makeText(this, "예약 가능 기간을 선택해주세요.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Available dates are missing - start: " + availableStartDate + ", end: " + availableEndDate);
            return;
        }

        // 날짜 형식 검증 (yyyy-MM-dd 형식이어야 함)
        if (!availableStartDate.matches("\\d{4}-\\d{2}-\\d{2}") || 
            !availableEndDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "날짜 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Invalid date format - start: " + availableStartDate + ", end: " + availableEndDate);
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

        // 6. 이미지 URL 가져오기 (없을 수도 있음)
        String imageUrl = PrefManager.get("house_image_url");
        if (imageUrl == null) {
            imageUrl = ""; // 빈 문자열로 처리
        }

        // 7. 집 생성 API 호출
        HousesCreateRequestDto houseRequest = new HousesCreateRequestDto(
                title,
                description,
                address,
                city,
                country,
                pricePerNight,
                bedroomCount,
                bedCount,
                bathroomCount,
                availableStartDate,
                availableEndDate,
                imageUrl != null ? imageUrl : ""
        );

        Log.d(TAG, "Sending house creation request - address: " + address);
        Log.d(TAG, "Request DTO - addressLine1: " + houseRequest.getAddressLine1());

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
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response body: " + errorBody);
                            
                            // JSON에서 message 필드 추출 시도
                            if (errorBody.contains("\"message\"")) {
                                try {
                                    // 간단한 JSON 파싱 (message 필드 추출)
                                    int messageStart = errorBody.indexOf("\"message\"");
                                    if (messageStart != -1) {
                                        int valueStart = errorBody.indexOf("\"", messageStart + 10) + 1;
                                        int valueEnd = errorBody.indexOf("\"", valueStart);
                                        if (valueEnd > valueStart) {
                                            errorMessage = errorBody.substring(valueStart, valueEnd);
                                        }
                                    }
                                } catch (Exception e) {
                                    // 파싱 실패 시 전체 에러 바디 사용
                                    errorMessage = errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody;
                                }
                            } else {
                                errorMessage = errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Toast.makeText(RegisterFinalActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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