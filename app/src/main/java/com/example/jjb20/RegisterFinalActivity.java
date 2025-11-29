package com.example.jjb20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.jjb20.dto.HouseAmenitiesCreateRequestDto;
import com.example.jjb20.dto.HousePhotoDto;
import com.example.jjb20.dto.HouseUpdateRequestDto;
import com.example.jjb20.dto.HousesCreateRequestDto;
import com.example.jjb20.dto.HousesResponseDto;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterFinalActivity extends AppCompatActivity {

    private static final String TAG = "RegisterFinal";
    private static final int MAX_PHOTO_COUNT = 5;

    // 등록 모드용 (새 집 등록)
    private static final String PREF_KEY_PHOTOS = "house_image_urls";

    // 수정 모드용 (houseId별 사진 상태 저장)
    private static final String PREF_KEY_EDIT_PHOTOS_PREFIX = "house_edit_photos_";
    private static final String PREF_KEY_EDIT_PHOTO_IDS_PREFIX = "house_edit_photo_ids_";

    private MaterialToolbar toolbar;
    private ProgressBar progressBarStep;

    private TextView subtitleText;
    private MaterialButton registerButton;
    private MaterialCardView addPhotoButton;
    private LinearLayout photoContainer;
    private TextInputEditText priceEditText;
    private TextView photoCounterText;

    private boolean isEditMode = false;
    private String editTarget;
    private boolean wantsPriceUpdate = true;
    private boolean wantsPhotoUpdate = true;
    private long houseId = -1L;
    private Integer currentPrice;
    private String currentImageUrl;

    private ApiService apiService;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    // 여러 장 사진 관리
    private final List<PhotoItem> photoItems = new ArrayList<>();
    private final Queue<PhotoItem> uploadQueue = new ArrayDeque<>();
    private ProgressDialog uploadProgressDialog;
    private boolean isUploadingPhotos = false;
    private boolean userModifiedPhotos = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefManager.init(getApplicationContext());
        setContentView(R.layout.activity_register_final);

        if (!readIntentData()) return;

        Retrofit retrofit = RetrofitClient.getInstance();
        apiService = retrofit.create(ApiService.class);

        initViews();
        initPhotoPicker();
        loadInitialPhotos();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPressed();
            }
        });

        setupListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isEditMode) {
            saveEditPhotos();
        }
    }

    @Override
    protected void onDestroy() {
        dismissUploadDialog();
        super.onDestroy();
    }

    /**
     * Intent로부터 수정 여부 / 대상 / houseId / 기존 값들을 읽음
     */
    private boolean readIntentData() {
        String mode = getIntent().getStringExtra("mode");
        editTarget = getIntent().getStringExtra("target");
        isEditMode = "edit".equalsIgnoreCase(mode);

        // target: price / photos / null(둘 다)
        wantsPriceUpdate = TextUtils.isEmpty(editTarget) || "price".equalsIgnoreCase(editTarget);
        wantsPhotoUpdate = TextUtils.isEmpty(editTarget) || "photos".equalsIgnoreCase(editTarget);

        houseId = getIntent().getLongExtra("houseId", -1L);

        if (isEditMode) {
            if (houseId == -1L) {
                Toast.makeText(this, "집 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                finish();
                return false;
            }
            if (getIntent().hasExtra("currentPrice")) {
                int price = getIntent().getIntExtra("currentPrice", -1);
                if (price >= 0) currentPrice = price;
            }
            currentImageUrl = getIntent().getStringExtra("currentImageUrl");
        }
        return true;
    }

    /**
     * 뷰 초기화
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        subtitleText = findViewById(R.id.subtitle_text);
        registerButton = findViewById(R.id.register_button);
        addPhotoButton = findViewById(R.id.add_photo_button);
        photoContainer = findViewById(R.id.photo_container);
        priceEditText = findViewById(R.id.price_edit_text);
        photoCounterText = findViewById(R.id.photoCounterText);

        progressBarStep = findViewById(R.id.progressBarStep);
        if (progressBarStep != null) {
            progressBarStep.setMax(6);
            progressBarStep.setProgress(6); // 마지막 단계
        }

        if (photoCounterText != null) {
            photoCounterText.setText(String.format(Locale.getDefault(), "0/%d", MAX_PHOTO_COUNT));
        }

        if (isEditMode) {
            registerButton.setText("완료");
            toolbar.setTitle("집 정보 수정");

            if (wantsPriceUpdate && currentPrice != null) {
                priceEditText.setText(String.valueOf(currentPrice));
                priceEditText.setSelection(priceEditText.getText().length());
            }
            if (!wantsPriceUpdate) {
                priceEditText.setEnabled(false);
            }
        }

        if (!wantsPhotoUpdate) {
            addPhotoButton.setEnabled(false);
            addPhotoButton.setAlpha(0.5f);
        }
    }

    /**
     * 멀티 사진 선택용 PhotoPicker 초기화
     */
    private void initPhotoPicker() {
        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(MAX_PHOTO_COUNT),
                uris -> {
                    if (uris == null || uris.isEmpty()) {
                        Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int available = MAX_PHOTO_COUNT - photoItems.size();
                    if (available <= 0) {
                        Toast.makeText(this, "이미지는 최대 5장까지 등록할 수 있습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<PhotoItem> newlyAdded = new ArrayList<>();
                    for (int i = 0; i < uris.size() && i < available; i++) {
                        Uri uri = uris.get(i);
                        PhotoItem item = new PhotoItem(uri);
                        photoItems.add(item);
                        newlyAdded.add(item);
                    }

                    if (uris.size() > available) {
                        Toast.makeText(this, "최대 5장까지만 추가됩니다.", Toast.LENGTH_SHORT).show();
                    }

                    if (!newlyAdded.isEmpty()) {
                        userModifiedPhotos = true;
                    }

                    refreshPhotoPreviews();
                    enqueueUploads(newlyAdded);
                    saveEditPhotos(); // 수정 모드일 때 사진 상태 저장
                }
        );
    }

    /**
     * 처음 진입 시 사진 초기 로딩
     */
    private void loadInitialPhotos() {
        if (isEditMode) {
            // 저장된 편집 상태가 있으면 복원, 없으면 서버에서 로드
            if (restoreEditPhotos()) {
                userModifiedPhotos = true;
                refreshPhotoPreviews();
            } else {
                loadPhotosFromServer();
            }
        } else {
            // 등록 모드: 이전 사진 데이터 초기화
            photoItems.clear();
            PrefManager.remove(PREF_KEY_PHOTOS);
            refreshPhotoPreviews();
        }
    }

    /**
     * 수정 모드에서 현재 사진 상태 저장
     */
    private void saveEditPhotos() {
        if (!isEditMode || houseId == -1L) return;

        List<String> photoUrls = new ArrayList<>();
        List<String> photoIds = new ArrayList<>();

        for (PhotoItem item : photoItems) {
            if (!TextUtils.isEmpty(item.getRemoteUrl())) {
                photoUrls.add(item.getRemoteUrl());
                photoIds.add(item.getPhotoId() != null ? String.valueOf(item.getPhotoId()) : "");
            }
        }

        String photosKey = PREF_KEY_EDIT_PHOTOS_PREFIX + houseId;
        String idsKey = PREF_KEY_EDIT_PHOTO_IDS_PREFIX + houseId;

        PrefManager.putStringList(photosKey, photoUrls);
        PrefManager.putStringList(idsKey, photoIds);

        Log.d(TAG, "Saved edit photos for houseId: " + houseId + ", count: " + photoUrls.size());
    }

    /**
     * 수정 모드에서 저장된 사진 상태 복원
     */
    private boolean restoreEditPhotos() {
        if (!isEditMode || houseId == -1L) return false;

        String photosKey = PREF_KEY_EDIT_PHOTOS_PREFIX + houseId;
        String idsKey = PREF_KEY_EDIT_PHOTO_IDS_PREFIX + houseId;

        List<String> photoUrls = PrefManager.getStringList(photosKey);
        List<String> photoIds = PrefManager.getStringList(idsKey);

        if (photoUrls == null || photoUrls.isEmpty()) {
            Log.d(TAG, "No saved photos found for houseId: " + houseId);
            return false;
        }

        Log.d(TAG, "Restoring edit photos for houseId: " + houseId + ", count: " + photoUrls.size());

        photoItems.clear();
        for (int i = 0; i < photoUrls.size(); i++) {
            String url = photoUrls.get(i);
            if (TextUtils.isEmpty(url)) continue;

            String photoIdStr = (photoIds != null && i < photoIds.size()) ? photoIds.get(i) : "";
            Long photoId = null;
            if (!TextUtils.isEmpty(photoIdStr)) {
                try {
                    photoId = Long.parseLong(photoIdStr);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Failed to parse photoId: " + photoIdStr, e);
                }
            }
            addPhotoFromUrl(url, photoId);
        }

        return true;
    }

    /**
     * 수정 완료 시 저장된 사진 편집 상태 삭제
     */
    private void clearEditPhotos() {
        if (houseId == -1L) return;

        String photosKey = PREF_KEY_EDIT_PHOTOS_PREFIX + houseId;
        String idsKey = PREF_KEY_EDIT_PHOTO_IDS_PREFIX + houseId;
        PrefManager.remove(photosKey);
        PrefManager.remove(idsKey);

        Log.d(TAG, "Cleared edit photos for houseId: " + houseId);
    }

    /**
     * 서버에서 기존 집 사진 목록 로드
     */
    private void loadPhotosFromServer() {
        if (houseId == -1L) {
            if (!TextUtils.isEmpty(currentImageUrl)) {
                addPhotoFromUrl(currentImageUrl);
            }
            refreshPhotoPreviews();
            return;
        }

        apiService.getHousePhotos(houseId).enqueue(new Callback<List<HousePhotoDto>>() {
            @Override
            public void onResponse(Call<List<HousePhotoDto>> call, Response<List<HousePhotoDto>> response) {
                if (userModifiedPhotos) return;

                if (response.isSuccessful() && response.body() != null) {
                    photoItems.clear();
                    List<HousePhotoDto> photos = response.body();

                    Collections.sort(photos, Comparator.comparing(
                            dto -> dto.getSortOrder() != null ? dto.getSortOrder() : Integer.MAX_VALUE));

                    for (HousePhotoDto dto : photos) {
                        if (photoItems.size() >= MAX_PHOTO_COUNT) break;
                        addPhotoFromUrl(dto.getPhotoUrl(), dto.getId());
                    }
                } else if (!TextUtils.isEmpty(currentImageUrl)) {
                    photoItems.clear();
                    addPhotoFromUrl(currentImageUrl);
                }
                refreshPhotoPreviews();
            }

            @Override
            public void onFailure(Call<List<HousePhotoDto>> call, Throwable t) {
                if (userModifiedPhotos) return;

                if (!TextUtils.isEmpty(currentImageUrl)) {
                    photoItems.clear();
                    addPhotoFromUrl(currentImageUrl);
                }
                refreshPhotoPreviews();
            }
        });
    }

    private void addPhotoFromUrl(String url) {
        addPhotoFromUrl(url, null);
    }

    private void addPhotoFromUrl(String url, Long photoId) {
        if (TextUtils.isEmpty(url) || photoItems.size() >= MAX_PHOTO_COUNT) return;
        photoItems.add(new PhotoItem(url, photoId));
    }

    /**
     * 사진 미리보기 UI 갱신
     */
    private void refreshPhotoPreviews() {
        if (photoContainer == null) return;

        int childCount = photoContainer.getChildCount();
        // 0번 인덱스는 "사진 추가" 카드라고 가정 → 그 뒤로 제거
        for (int i = childCount - 1; i >= 1; i--) {
            photoContainer.removeViewAt(i);
        }

        for (PhotoItem item : photoItems) {
            photoContainer.addView(createPhotoCard(item));
        }

        updatePhotoCounter();
        updateAddPhotoButtonState();
    }

    private void updatePhotoCounter() {
        if (photoCounterText != null) {
            photoCounterText.setText(
                    String.format(Locale.getDefault(), "%d/%d", photoItems.size(), MAX_PHOTO_COUNT));
        }
    }

    private void updateAddPhotoButtonState() {
        if (addPhotoButton == null) return;
        addPhotoButton.setVisibility(hasRoomForMorePhotos() ? View.VISIBLE : View.GONE);
    }

    /**
     * 사진 카드 뷰 생성
     */
    private MaterialCardView createPhotoCard(PhotoItem item) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(dpToPx(100), dpToPx(100));
        params.setMargins(dpToPx(12), 0, 0, 0);
        card.setLayoutParams(params);
        card.setCardElevation(0f);
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(0xFFEEEEEE);
        card.setRadius(dpToPx(8));
        card.setCardBackgroundColor(0xFFF5F5F5);

        android.widget.RelativeLayout container = new android.widget.RelativeLayout(this);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        ImageView imageView = new ImageView(this);
        android.widget.RelativeLayout.LayoutParams imageParams =
                new android.widget.RelativeLayout.LayoutParams(
                        android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                        android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (item.getUploadUri() != null) {
            imageView.setImageURI(item.getUploadUri());
        } else if (!TextUtils.isEmpty(item.getRemoteUrl())) {
            Glide.with(this)
                    .load(item.getRemoteUrl())
                    .placeholder(android.R.color.transparent)
                    .error(android.R.color.transparent)
                    .centerCrop()
                    .into(imageView);
        } else {
            imageView.setImageResource(android.R.color.transparent);
        }

        imageView.setAlpha(item.isUploaded() ? 1f : 0.5f);
        container.addView(imageView);

        // 수정 모드 + 업로드된 사진일 때만 삭제 버튼 표시
        if (isEditMode && wantsPhotoUpdate && item.isUploaded()) {
            ImageView deleteButton = new ImageView(this);
            android.widget.RelativeLayout.LayoutParams deleteParams =
                    new android.widget.RelativeLayout.LayoutParams(dpToPx(28), dpToPx(28));
            deleteParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP);
            deleteParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END);
            deleteParams.setMargins(0, dpToPx(4), dpToPx(4), 0);
            deleteButton.setLayoutParams(deleteParams);

            android.graphics.drawable.GradientDrawable background =
                    new android.graphics.drawable.GradientDrawable();
            background.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            background.setColor(0xE6000000); // 반투명 검정
            deleteButton.setBackground(background);

            deleteButton.setImageResource(R.drawable.ic_close_24);
            deleteButton.setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
            deleteButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            deleteButton.setColorFilter(0xFFFFFFFF);

            deleteButton.setOnClickListener(v -> deletePhoto(item));

            container.addView(deleteButton);
        }

        card.addView(container);
        return card;
    }

    private boolean hasRoomForMorePhotos() {
        return photoItems.size() < MAX_PHOTO_COUNT;
    }

    /**
     * 새로 선택된 사진들을 업로드 큐에 넣고 업로드 시작
     */
    private void enqueueUploads(List<PhotoItem> newItems) {
        if (newItems == null || newItems.isEmpty()) return;

        for (PhotoItem item : newItems) {
            if (item.getUploadUri() != null) {
                uploadQueue.offer(item);
            }
        }
        if (!isUploadingPhotos) {
            uploadNextPhoto();
        }
    }

    private void uploadNextPhoto() {
        PhotoItem next = uploadQueue.poll();
        if (next == null) {
            isUploadingPhotos = false;
            dismissUploadDialog();
            refreshPhotoPreviews();
            return;
        }
        isUploadingPhotos = true;
        uploadPhotoItem(next);
    }

    /**
     * Firebase Storage 업로드 로직
     */
    private void uploadPhotoItem(PhotoItem item) {
        Uri localUri = item.getUploadUri();
        if (localUri == null) {
            uploadNextPhoto();
            return;
        }

        ProgressDialog dialog = getUploadProgressDialog();
        dialog.setMessage("사진을 업로드하고 있습니다...");
        if (!dialog.isShowing()) dialog.show();

        String userId = PrefManager.get("uid", "guest");
        String houseTitle = PrefManager.get("house_title", "no_title");
        if (TextUtils.isEmpty(userId)) userId = "guest";
        if (TextUtils.isEmpty(houseTitle)) houseTitle = "temporary";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.KOREA);
        String fileName = userId + "_" + houseTitle + "_" + sdf.format(new Date());
        StorageReference reference = FirebaseStorage.getInstance()
                .getReference("images/" + fileName);

        reference.putFile(localUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return reference.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    item.setRemoteUrl(downloadUri.toString());
                    persistPhotoUrlsToPrefs();
                    refreshPhotoPreviews();
                    saveEditPhotos();
                    uploadNextPhoto();
                })
                .addOnFailureListener(e -> {
                    photoItems.remove(item);
                    Toast.makeText(this, "업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    persistPhotoUrlsToPrefs();
                    refreshPhotoPreviews();
                    saveEditPhotos();
                    uploadNextPhoto();
                });
    }

    private ProgressDialog getUploadProgressDialog() {
        if (uploadProgressDialog == null) {
            uploadProgressDialog = new ProgressDialog(this);
            uploadProgressDialog.setCancelable(false);
        }
        return uploadProgressDialog;
    }

    private void dismissUploadDialog() {
        if (uploadProgressDialog != null && uploadProgressDialog.isShowing()) {
            uploadProgressDialog.dismiss();
        }
    }

    /**
     * 사진 삭제
     */
    private void deletePhoto(PhotoItem item) {
        // 최소 1장 유지
        int uploadedPhotoCount = 0;
        for (PhotoItem photo : photoItems) {
            if (photo.isUploaded()) uploadedPhotoCount++;
        }

        if (uploadedPhotoCount <= 1) {
            Toast.makeText(this, "최소 1장 이상의 사진이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 아직 서버에 없는 새 사진이면 로컬에서만 삭제
        if (item.getPhotoId() == null) {
            int index = photoItems.indexOf(item);
            if (index != -1) {
                photoItems.remove(index);
                userModifiedPhotos = true;
                refreshPhotoPreviews();
                saveEditPhotos();
            }
            return;
        }

        // 서버에 삭제 요청
        String idToken = PrefManager.get("idToken");
        if (idToken == null || idToken.isEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String bearerToken = "Bearer " + idToken;
        Long photoId = item.getPhotoId();

        apiService.deleteHousePhoto(bearerToken, photoId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    int index = photoItems.indexOf(item);
                    if (index != -1) {
                        photoItems.remove(index);
                        userModifiedPhotos = true;
                        refreshPhotoPreviews();
                        saveEditPhotos();
                        Toast.makeText(RegisterFinalActivity.this, "사진이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterFinalActivity.this,
                            "사진 삭제에 실패했습니다. (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegisterFinalActivity.this,
                        "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Photo deletion failed", t);
            }
        });
    }

    /**
     * 등록 모드에서만 Pref에 사진 URL 리스트 저장
     */
    private void persistPhotoUrlsToPrefs() {
        if (isEditMode) return;
        PrefManager.putStringList(PREF_KEY_PHOTOS, collectUploadedPhotoUrls());
    }

    private List<String> collectUploadedPhotoUrls() {
        List<String> urls = new ArrayList<>();
        for (PhotoItem item : photoItems) {
            if (!TextUtils.isEmpty(item.getRemoteUrl())) {
                urls.add(item.getRemoteUrl());
            }
        }
        return urls;
    }

    private List<HousesCreateRequestDto.PhotoRequest> buildPhotoRequests(List<String> urls) {
        List<HousesCreateRequestDto.PhotoRequest> requests = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            boolean isCover = i == 0;
            requests.add(new HousesCreateRequestDto.PhotoRequest(urls.get(i), isCover, i + 1));
        }
        return requests;
    }

    private boolean hasPendingUploads() {
        if (isUploadingPhotos || !uploadQueue.isEmpty()) return true;

        for (PhotoItem item : photoItems) {
            if (!item.isUploaded()) return true;
        }
        return false;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * 사진 1장을 나타내는 내부 클래스
     */
    private static class PhotoItem {
        private final Uri localUri;
        private String remoteUrl;
        private Long photoId;

        PhotoItem(Uri localUri) {
            this.localUri = localUri;
        }

        PhotoItem(String remoteUrl) {
            this.localUri = null;
            this.remoteUrl = remoteUrl;
        }

        PhotoItem(String remoteUrl, Long photoId) {
            this.localUri = null;
            this.remoteUrl = remoteUrl;
            this.photoId = photoId;
        }

        Uri getUploadUri() {
            return localUri;
        }

        String getRemoteUrl() {
            return remoteUrl;
        }

        void setRemoteUrl(String remoteUrl) {
            this.remoteUrl = remoteUrl;
        }

        Long getPhotoId() {
            return photoId;
        }

        void setPhotoId(Long photoId) {
            this.photoId = photoId;
        }

        boolean isUploaded() {
            return !TextUtils.isEmpty(remoteUrl);
        }
    }

    /**
     * 리스너 설정
     */
    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> handleBackPressed());

        addPhotoButton.setOnClickListener(v -> {
            if (!wantsPhotoUpdate) {
                Toast.makeText(this, "사진 수정 화면이 아닙니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!hasRoomForMorePhotos()) {
                Toast.makeText(this, "이미지는 최대 5장까지 등록할 수 있습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            openGallery();
        });

        registerButton.setOnClickListener(v -> {
            if (isEditMode) {
                submitEdit();
            } else {
                performRegistration();
            }
        });
    }

    /**
     * 수정 모드에서 '완료' 클릭 시
     */
    private void submitEdit() {
        if (houseId == -1L) {
            Toast.makeText(this, "집 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        HouseUpdateRequestDto dto = new HouseUpdateRequestDto();
        boolean hasChanges = false;

        if (wantsPriceUpdate) {
            String priceStr = priceEditText.getText() != null
                    ? priceEditText.getText().toString().trim() : "";
            if (TextUtils.isEmpty(priceStr)) {
                Toast.makeText(this, "가격을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int pricePerNight = Integer.parseInt(priceStr);
                dto.setPricePerNight(pricePerNight);
                hasChanges = true;
            } catch (NumberFormatException e) {
                Toast.makeText(this, "올바른 가격을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (wantsPhotoUpdate) {
            if (hasPendingUploads()) {
                Toast.makeText(this, "사진 업로드가 완료될 때까지 기다려주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            List<String> photoUrls = collectUploadedPhotoUrls();
            String imageUrl = !photoUrls.isEmpty() ? photoUrls.get(0) : currentImageUrl;
            if (TextUtils.isEmpty(imageUrl)) {
                Toast.makeText(this, "사진을 한 장 이상 등록해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            dto.setImageUrl(imageUrl);
            hasChanges = true;
        }

        if (!hasChanges) {
            Toast.makeText(this, "변경할 내용이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("저장 중...");

        apiService.updateHouseBasicInfo(houseId, dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    clearEditPhotos();
                    Toast.makeText(RegisterFinalActivity.this, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    restoreEditButtonState();
                    Toast.makeText(RegisterFinalActivity.this,
                            "수정에 실패했습니다. (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                restoreEditButtonState();
                Toast.makeText(RegisterFinalActivity.this,
                        "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restoreEditButtonState() {
        registerButton.setEnabled(true);
        registerButton.setText(isEditMode ? "완료" : "등록하기");
    }

    private Double parseDoubleOrNull(String value) {
        if (TextUtils.isEmpty(value)) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid coordinate value: " + value, e);
            return null;
        }
    }

    /**
     * 뒤로가기 처리
     */
    private void handleBackPressed() {
        if (isEditMode) {
            finish();
            return;
        }
        Long houseId = PrefManager.getLong("houseId");
        if (houseId != null && houseId != -1L) {
            PrefManager.remove("houseId");
            Log.d(TAG, "Registration cancelled. houseId cleared.");
        }
        finish();
    }

    /**
     * 갤러리 열기
     */
    private void openGallery() {
        if (pickMedia != null) {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        } else {
            Toast.makeText(this, "갤러리를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 등록 모드에서 '등록하기' 클릭 시
     */
    private void performRegistration() {
        // 1. 가격
        String priceStr = priceEditText.getText() != null
                ? priceEditText.getText().toString().trim() : "";
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

        // 2. Pref에서 데이터 읽기
        String title = PrefManager.get("house_title");
        String description = PrefManager.get("house_description");
        String address = PrefManager.get("house_address");
        String shortDescription = PrefManager.get("house_summary");
        String addressLine1 = PrefManager.get("house_address_line1", address != null ? address : "");
        String addressLine2 = PrefManager.get("house_address_line2");
        String city = PrefManager.get("house_city", "서울");
        String country = PrefManager.get("house_country", "한국");
        List<String> amenityCodes = PrefManager.getStringList("house_amenity_codes");
        int bedroomCount = PrefManager.getInt("house_bedroom_count", 0);
        int bedCount = PrefManager.getInt("house_bed_count", 0);
        int bathroomCount = PrefManager.getInt("house_bathroom_count", 0);
        String startDay = PrefManager.get("start_day");
        String endDay = PrefManager.get("end_day");
        String latitudeStr = PrefManager.get("house_latitude");
        String longitudeStr = PrefManager.get("house_longitude");
        Double latitude = parseDoubleOrNull(latitudeStr);
        Double longitude = parseDoubleOrNull(longitudeStr);

        Log.d(TAG, "Registration data - title: " + title);
        Log.d(TAG, "Registration data - description: " + description);
        Log.d(TAG, "Registration data - address: " + address);
        Log.d(TAG, "Registration data - shortDescription: " + shortDescription);
        Log.d(TAG, "Registration data - city: " + city + ", country: " + country);
        Log.d(TAG, "Registration data - addressLine1: " + addressLine1 + ", addressLine2: " + addressLine2);
        Log.d(TAG, "Registration data - bedroomCount: " + bedroomCount + ", bedCount: " + bedCount + ", bathroomCount: " + bathroomCount);
        Log.d(TAG, "Registration data - availableStartDate: " + startDay + ", availableEndDate: " + endDay);
        Log.d(TAG, "Registration data - pricePerNight: " + pricePerNight);
        Log.d(TAG, "Registration data - amenityCodes: " + amenityCodes);
        Log.d(TAG, "Registration data - latitude: " + latitude + ", longitude: " + longitude);

        // 3. 필수 값 체크
        if (title == null || title.isEmpty()) {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description == null || description.isEmpty()) {
            Toast.makeText(this, "설명을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (shortDescription == null || shortDescription.isEmpty()) {
            Toast.makeText(this, "한줄 설명을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (address == null || address.isEmpty()) {
            Toast.makeText(this, "주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(addressLine1)) {
            addressLine1 = address;
        }
        if (addressLine2 == null) {
            addressLine2 = "";
        }

        if (startDay == null || startDay.isEmpty() ||
                endDay == null || endDay.isEmpty()) {
            Toast.makeText(this, "예약 가능 기간을 선택해주세요.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Available dates are missing - start: " + startDay + ", end: " + endDay);
            return;
        }

        if (!startDay.matches("\\d{4}-\\d{2}-\\d{2}") ||
                !endDay.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "날짜 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Invalid date format - start: " + startDay + ", end: " + endDay);
            return;
        }

        // 4. 버튼 비활성화
        registerButton.setEnabled(false);

        // 5. Firebase ID 토큰
        String idToken = PrefManager.get("idToken");
        if (idToken == null || idToken.isEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            registerButton.setEnabled(true);
            return;
        }

        String bearerToken = "Bearer " + idToken;

        // 6. 사진 업로드 여부 체크
        if (hasPendingUploads()) {
            Toast.makeText(this, "사진 업로드가 완료될 때까지 기다려주세요.", Toast.LENGTH_SHORT).show();
            registerButton.setEnabled(true);
            return;
        }

        List<String> photoUrls = collectUploadedPhotoUrls();
        String imageUrl = photoUrls.isEmpty() ? "" : photoUrls.get(0);

        // 7. 집 생성 요청 DTO 구성
        HousesCreateRequestDto houseRequest = new HousesCreateRequestDto(
                title,
                description,
                shortDescription,
                addressLine1,
                addressLine2,
                city,
                country,
                pricePerNight,
                bedroomCount,
                bedCount,
                bathroomCount,
                startDay,
                endDay,
                imageUrl != null ? imageUrl : "",
                latitude,
                longitude
        );
        houseRequest.setPhotos(buildPhotoRequests(photoUrls));

        Log.d(TAG, "Sending house creation request - address: " + address);
        Log.d(TAG, "Request DTO - addressLine1: " + houseRequest.getAddressLine1());
        Log.d(TAG, "Amenity codes before API call: " + (amenityCodes != null ? amenityCodes.size() + " items" : "null"));

        Call<HousesResponseDto> houseCall = apiService.createHouse(bearerToken, houseRequest);
        Log.d(TAG, "About to enqueue house creation request");

        houseCall.enqueue(new Callback<HousesResponseDto>() {
            @Override
            public void onResponse(Call<HousesResponseDto> call, Response<HousesResponseDto> response) {
                Log.d(TAG, "House creation response - code: " + response.code() + ", isSuccessful: " + response.isSuccessful());

                if (response.isSuccessful()) {
                    Long createdHouseId = null;

                    // 1) 응답 헤더에서 houseId 추출 시도
                    String houseIdHeader = response.headers().get("X-House-Id");
                    Log.d(TAG, "X-House-Id header: " + houseIdHeader);

                    if (houseIdHeader != null && !houseIdHeader.isEmpty()) {
                        try {
                            createdHouseId = Long.parseLong(houseIdHeader);
                            Log.d(TAG, "House ID from header: " + createdHouseId);
                        } catch (NumberFormatException e) {
                            Log.w(TAG, "Failed to parse houseId from header: " + houseIdHeader, e);
                        }
                    }

                    // 2) 헤더에서 못 찾으면 body에서 추출 시도
                    if (createdHouseId == null) {
                        Log.d(TAG, "Trying to get houseId from response body");
                        try {
                            if (response.body() != null) {
                                createdHouseId = response.body().getId();
                                Log.d(TAG, "House ID from body: " + createdHouseId);
                            } else {
                                Log.w(TAG, "Response body is null");
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Failed to parse response body due to circular reference", e);
                        }
                    }

                    Log.d(TAG, "Final createdHouseId: " + createdHouseId + ", amenityCodes: " + (amenityCodes != null ? amenityCodes.size() + " items" : "null"));

                    if (createdHouseId != null && createdHouseId != -1L) {
                        Log.d(TAG, "House created with id: " + createdHouseId);
                        PrefManager.put("houseId", createdHouseId);

                        if (amenityCodes != null && !amenityCodes.isEmpty()) {
                            Log.d(TAG, "Calling saveAmenities with houseId: " + createdHouseId + ", codes: " + amenityCodes.size());
                            saveAmenities(bearerToken, createdHouseId, amenityCodes);
                        } else {
                            Log.w(TAG, "No amenity codes to save, completing registration");
                            onRegistrationComplete();
                        }
                    } else {
                        // 파싱 문제지만 실제 등록은 성공했을 가능성
                        Log.w(TAG, "Could not extract houseId from header or body, but registration likely succeeded. createdHouseId: " + createdHouseId);
                        onRegistrationComplete();
                    }
                } else {
                    registerButton.setEnabled(true);
                    String errorMessage = "집 등록 실패: " + response.code();

                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response body: " + errorBody);

                            if (errorBody.contains("\"message\"")) {
                                try {
                                    int messageStart = errorBody.indexOf("\"message\"");
                                    if (messageStart != -1) {
                                        int valueStart = errorBody.indexOf("\"", messageStart + 10) + 1;
                                        int valueEnd = errorBody.indexOf("\"", valueStart);
                                        if (valueEnd > valueStart) {
                                            errorMessage = errorBody.substring(valueStart, valueEnd);
                                        }
                                    }
                                } catch (Exception e) {
                                    errorMessage = errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody;
                                }
                            } else {
                                errorMessage = errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorMessage;
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
                Log.e(TAG, "House creation onFailure", t);
                Log.e(TAG, "Throwable type: " + t.getClass().getName() + ", message: " + t.getMessage());

                // photos 때문에 MalformedJsonException인 경우 → 실제로는 성공했을 수도 있음
                if (t instanceof com.google.gson.stream.MalformedJsonException &&
                        t.getMessage() != null && t.getMessage().contains("photos")) {

                    Log.w(TAG, "JSON parsing error due to circular reference, but house might be created", t);

                    Long houseIdFromPref = PrefManager.getLong("houseId", -1L);
                    if (houseIdFromPref != null && houseIdFromPref > 0) {
                        Log.d(TAG, "Using houseId from PrefManager: " + houseIdFromPref);
                        if (amenityCodes != null && !amenityCodes.isEmpty()) {
                            saveAmenities(bearerToken, houseIdFromPref, amenityCodes);
                        } else {
                            onRegistrationComplete();
                        }
                    } else {
                        Toast.makeText(RegisterFinalActivity.this,
                                "집 등록이 완료되었을 수 있습니다. 목록에서 확인해주세요.",
                                Toast.LENGTH_LONG).show();
                        onRegistrationComplete();
                    }
                } else {
                    registerButton.setEnabled(true);
                    Toast.makeText(RegisterFinalActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "House creation failed", t);
                }
            }
        });
    }

    /**
     * 편의시설 저장 API 호출
     */
    private void saveAmenities(String bearerToken, Long houseId, List<String> amenityCodes) {
        Log.d(TAG, "saveAmenities called - houseId: " + houseId + ", amenityCodes: " + amenityCodes);

        if (houseId == null || houseId <= 0) {
            Log.e(TAG, "Invalid houseId: " + houseId);
            Toast.makeText(this, "집 ID가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
            onRegistrationComplete();
            return;
        }

        if (amenityCodes == null || amenityCodes.isEmpty()) {
            Log.w(TAG, "No amenity codes to save");
            onRegistrationComplete();
            return;
        }

        HouseAmenitiesCreateRequestDto amenitiesRequest = new HouseAmenitiesCreateRequestDto(
                houseId,
                amenityCodes
        );

        Log.d(TAG, "Calling saveHouseAmenities API - houseId: " + houseId + ", codes count: " + amenityCodes.size());

        Call<Void> amenitiesCall = apiService.saveHouseAmenities(bearerToken, amenitiesRequest);
        amenitiesCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "saveHouseAmenities response - code: " + response.code() + ", isSuccessful: " + response.isSuccessful());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Amenities saved successfully");
                    onRegistrationComplete();
                } else {
                    registerButton.setEnabled(true);
                    String errorMessage = "편의시설 저장 실패: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                            Log.e(TAG, "Error response body: " + errorMessage);
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
                Log.e(TAG, "saveHouseAmenities onFailure", t);
                Toast.makeText(RegisterFinalActivity.this, "편의시설 저장 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 등록 흐름 전체 완료 처리
     */
    private void onRegistrationComplete() {
        PrefManager.clearHouseRegistrationData();
        Toast.makeText(this, "등록이 완료되었습니다!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(RegisterFinalActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
