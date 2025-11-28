package com.example.jjb20;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jjb20.dto.HouseUpdateRequestDto;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterDetailsActivity extends AppCompatActivity {

    // 뷰 변수 선언
    private MaterialToolbar toolbar;
    private MaterialButton nextButton;

    // 입력 필드 변수
    private TextInputLayout addressInputLayout;
    private TextInputEditText addressEditText;
    private TextInputLayout addressDetailInputLayout;
    private TextInputEditText addressDetailEditText;
    private TextInputLayout descriptionInputLayout;
    private TextInputEditText descriptionEditText;
    private TextInputLayout summaryInputLayout;
    private TextInputEditText summaryEditText;

    private ActivityResultLauncher<Intent> addressSearchLauncher;

    private boolean isEditMode = false;
    private long houseId = -1L;
    private String editTarget;
    private String currentAddress;
    private String currentAddressDetail;
    private String currentDescription;
    private String currentSummary;
    private String currentCity;
    private String currentCountry;
    private Double selectedLatitude;
    private Double selectedLongitude;
    private ApiService apiService;


    private ProgressBar progressBarStep;

    // 모든 EditText의 변경을 감지할 공용 TextWatcher
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            // 텍스트가 변경될 때마다 버튼 상태 확인
            checkButtonState();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. XML 레이아웃 파일 설정
        setContentView(R.layout.activity_register_details);

        String mode = getIntent().getStringExtra("mode");
        isEditMode = "edit".equalsIgnoreCase(mode);
        editTarget = getIntent().getStringExtra("target");
        houseId = getIntent().getLongExtra("houseId", -1L);
        currentAddress = getIntent().getStringExtra("currentAddress");
        currentAddressDetail = getIntent().getStringExtra("currentAddressDetail");
        currentDescription = getIntent().getStringExtra("currentDescription");
        currentSummary = getIntent().getStringExtra("currentSummary");
        currentCity = getIntent().getStringExtra("houseCity");
        currentCountry = getIntent().getStringExtra("houseCountry");

        if (isEditMode && houseId == -1L) {
            Toast.makeText(this, "집 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // 2. ActivityResultLauncher 초기화
        // AddressSearchActivity가 반환한 주소값을 받아서 addressEditText에 설정
        addressSearchLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("address");
                        if (address != null && addressEditText != null) {
                            addressEditText.setText(address);
                            // 주소를 받으면 상세 주소 입력 필드로 포커스 이동
                            if (addressDetailEditText != null) {
                                addressDetailEditText.requestFocus();
                            }
                            resolveAddressCoordinates(address);
                        }
                    }
                }
        );

        // 3. 뷰 초기화
        initViews();

        // 4. 이벤트 리스너 설정
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        nextButton = findViewById(R.id.next_button);

        // 주소
        addressInputLayout = findViewById(R.id.address_input_layout);
        addressEditText = findViewById(R.id.address_edit_text);

        // 상세 주소
        addressDetailInputLayout = findViewById(R.id.address_detail_input_layout);
        addressDetailEditText = findViewById(R.id.address_detail_edit_text);

        // 설명
        descriptionInputLayout = findViewById(R.id.description_input_layout);
        descriptionEditText = findViewById(R.id.description_edit_text);

        // 한줄 설명
        summaryInputLayout = findViewById(R.id.summary_input_layout);
        summaryEditText = findViewById(R.id.summary_edit_text);

        // 진행바 추가
        progressBarStep = findViewById(R.id.progressBarStep);
        progressBarStep.setMax(6);
        progressBarStep.setProgress(4);
        if (isEditMode) {
            nextButton.setText("완료");
            toolbar.setTitle("집 정보 수정");

            if (!TextUtils.isEmpty(currentAddress) && addressEditText != null) {
                addressEditText.setText(currentAddress);
            }
            if (!TextUtils.isEmpty(currentAddressDetail) && addressDetailEditText != null) {
                addressDetailEditText.setText(currentAddressDetail);
                addressDetailEditText.setSelection(currentAddressDetail.length());
            }
            if (!TextUtils.isEmpty(currentDescription) && descriptionEditText != null) {
                descriptionEditText.setText(currentDescription);
                descriptionEditText.setSelection(currentDescription.length());
            }
            if (!TextUtils.isEmpty(currentSummary) && summaryEditText != null) {
                summaryEditText.setText(currentSummary);
                summaryEditText.setSelection(currentSummary.length());
            }
        }
    }

    private void setupListeners() {
        // 툴바 (뒤로가기)
        toolbar.setNavigationOnClickListener(v -> finish());

        // '다음' 버튼 클릭 리스너
        nextButton.setOnClickListener(v -> {
            if (nextButton.isEnabled()) {
                // 입력 데이터 가져오기
                String address = (addressEditText != null) ? addressEditText.getText().toString().trim() : "";
                String addressDetail = (addressDetailEditText != null) ? addressDetailEditText.getText().toString().trim() : "";
                String description = (descriptionEditText != null) ? descriptionEditText.getText().toString().trim() : "";
                String summary = (summaryEditText != null) ? summaryEditText.getText().toString().trim() : "";
                
                // 주소와 상세 주소를 합쳐서 저장
                String fullAddress = address;
                if (!addressDetail.isEmpty()) {
                    fullAddress += " " + addressDetail;
                }
                
                if (isEditMode) {
                    submitEdit(address, addressDetail, description, summary);
                } else {
                    // SharedPreferences에 저장
                    PrefManager.put("house_address", fullAddress);
                    PrefManager.put("house_description", description);
                    PrefManager.put("house_summary", summary);
                    PrefManager.put("house_address_line1", address);
                    if (!TextUtils.isEmpty(addressDetail)) {
                        PrefManager.put("house_address_line2", addressDetail);
                    } else {
                        PrefManager.remove("house_address_line2");
                    }
                    if (selectedLatitude != null && selectedLongitude != null) {
                        PrefManager.put("house_latitude", String.valueOf(selectedLatitude));
                        PrefManager.put("house_longitude", String.valueOf(selectedLongitude));
                    } else {
                        PrefManager.remove("house_latitude");
                        PrefManager.remove("house_longitude");
                    }
                    // city와 country는 일단 기본값으로 설정 (나중에 주소에서 파싱하거나 별도 입력 가능)
                    PrefManager.put("house_city", "서울");
                    PrefManager.put("house_country", "한국");

                    // 다음 액티비티로 이동
                    Intent intent = new Intent(RegisterDetailsActivity.this, RegisterCalendarActivity.class);
                    startActivity(intent);
                }
            }
        });

        // 주소 EditText 클릭 리스너 (주소 검색 실행)
        if (addressEditText != null) {
            addressEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RegisterDetailsActivity.this, SearchAddressActivity.class);
                    addressSearchLauncher.launch(intent);
                }
            });
            // TextWatcher는 계속 유지 (주소가 설정될 때 버튼 상태 체크)
            addressEditText.addTextChangedListener(textWatcher);
        }

        // 3개의 EditText에 공용 TextWatcher 연결 (null 체크)
        if (addressDetailEditText != null) {
            addressDetailEditText.addTextChangedListener(textWatcher);
        }
        if (descriptionEditText != null) {
            descriptionEditText.addTextChangedListener(textWatcher);
        }
        if (summaryEditText != null) {
            summaryEditText.addTextChangedListener(textWatcher);
        }
    }
    private final ActivityResultLauncher<Intent> getSearchResult=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Search Activity 로부터의 결과값이 이곳으로 전달됨(by setResult)
                if(result.getResultCode()==RESULT_OK){
                    if(result.getData()!=null){
                        String data=result.getData().getStringExtra("address");
                        addressEditText.setText(data);
                        resolveAddressCoordinates(data);
                    }
                }
            }
    );

    /**
     * 모든 필수 입력 필드가 채워졌는지 확인하고 '다음' 버튼 상태를 업데이트
     */
    private void checkButtonState() {
        // 각 EditText가 null인지 먼저 확인
        String address = (addressEditText != null) ? addressEditText.getText().toString().trim() : "";
        String detail = (addressDetailEditText != null) ? addressDetailEditText.getText().toString().trim() : "";
        String description = (descriptionEditText != null) ? descriptionEditText.getText().toString().trim() : "";
        String summary = (summaryEditText != null) ? summaryEditText.getText().toString().trim() : "";

        boolean allFieldsFilled;
        if (isEditMode) {
            if ("address".equalsIgnoreCase(editTarget)) {
                allFieldsFilled = !address.isEmpty() && !detail.isEmpty();
            } else if ("description".equalsIgnoreCase(editTarget)) {
                allFieldsFilled = !description.isEmpty() && !summary.isEmpty();
            } else {
                allFieldsFilled = !address.isEmpty() && !detail.isEmpty() && !description.isEmpty();
            }
        } else {
            // 모든 필드가 비어있지 않은지 확인
            allFieldsFilled = !address.isEmpty() &&
                    !detail.isEmpty() &&
                    !description.isEmpty() &&
                    !summary.isEmpty();
        }

        // 모든 필드가 채워졌으면 버튼 활성화
        nextButton.setEnabled(allFieldsFilled);

        // 색상 변경 로직
        if (allFieldsFilled) {
            // 활성화 상태: #00CBA8
            int color = Color.parseColor("#00CBA8");
            nextButton.setBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            // 비활성화 상태: XML에 지정된 #BDBDBD 색상 유지
            int color = Color.parseColor("#BDBDBD");
            nextButton.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    private void submitEdit(String addressLine1, String addressLine2, String description, String summary) {
        nextButton.setEnabled(false);
        nextButton.setText("저장 중...");

        HouseUpdateRequestDto dto = new HouseUpdateRequestDto();
        boolean wantsAddressUpdate = "address".equalsIgnoreCase(editTarget) || TextUtils.isEmpty(editTarget);
        boolean wantsDescriptionUpdate = "description".equalsIgnoreCase(editTarget) || TextUtils.isEmpty(editTarget);

        if (wantsAddressUpdate) {
            dto.setAddressLine1(addressLine1);
            dto.setAddressLine2(!TextUtils.isEmpty(addressLine2) ? addressLine2 : "");
            dto.setCity(!TextUtils.isEmpty(currentCity) ? currentCity : "서울");
            dto.setCountry(!TextUtils.isEmpty(currentCountry) ? currentCountry : "한국");
        }

        if (wantsDescriptionUpdate) {
            dto.setDescription(description);
            dto.setShortDescription(summary);
        }

        apiService.updateHouseBasicInfo(houseId, dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterDetailsActivity.this, "집 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    restoreButtonState();
                    Toast.makeText(RegisterDetailsActivity.this,
                            "수정에 실패했습니다. (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                restoreButtonState();
                Toast.makeText(RegisterDetailsActivity.this,
                        "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restoreButtonState() {
        nextButton.setEnabled(true);
        nextButton.setText(isEditMode ? "완료" : "다음");
    }

    private void resolveAddressCoordinates(String address) {
        if (TextUtils.isEmpty(address)) {
            selectedLatitude = null;
            selectedLongitude = null;
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> results = geocoder.getFromLocationName(address, 1);
            if (results != null && !results.isEmpty()) {
                Address resolved = results.get(0);
                selectedLatitude = resolved.getLatitude();
                selectedLongitude = resolved.getLongitude();
                Log.d("RegisterDetails", "Resolved coords lat=" + selectedLatitude + ", lng=" + selectedLongitude);
            } else {
                selectedLatitude = null;
                selectedLongitude = null;
                Log.w("RegisterDetails", "No coordinates found for address: " + address);
            }
        } catch (IOException e) {
            selectedLatitude = null;
            selectedLongitude = null;
            Log.e("RegisterDetails", "Failed to resolve coordinates", e);
        }
    }
}