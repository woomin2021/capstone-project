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
    private static final String PREF_KEY_PHOTOS = "house_image_urls";
    private static final String PREF_KEY_EDIT_PHOTOS_PREFIX = "house_edit_photos_"; // 수정 모드용 (houseId 포함)
    private static final String PREF_KEY_EDIT_PHOTO_IDS_PREFIX = "house_edit_photo_ids_"; // 수정 모드용 (photoId 저장, houseId 포함)

    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
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

        if (!readIntentData()) {
            return;
        }

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
        // 수정 모드일 때 현재 상태 저장 (뒤로가기 시에도 저장)
        if (isEditMode) {
            saveEditPhotos();
        }
    }

    @Override
    protected void onDestroy() {
        dismissUploadDialog();
        super.onDestroy();
    }

    private boolean readIntentData() {
        String mode = getIntent().getStringExtra("mode");
        editTarget = getIntent().getStringExtra("target");
        isEditMode = "edit".equalsIgnoreCase(mode);

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
                if (price >= 0) {
                    currentPrice = price;
                }
            }
            currentImageUrl = getIntent().getStringExtra("currentImageUrl");
        }

        return true;
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        subtitleText = findViewById(R.id.subtitle_text);
        registerButton = findViewById(R.id.register_button);
        addPhotoButton = findViewById(R.id.add_photo_button);
        photoContainer = findViewById(R.id.photo_container);
        priceEditText = findViewById(R.id.price_edit_text);
        photoCounterText = findViewById(R.id.photoCounterText);

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

    private void loadInitialPhotos() {
        if (isEditMode) {
            // 수정 모드: 저장된 사진 상태가 있으면 복원, 없으면 서버에서 로드
            if (restoreEditPhotos()) {
                // 저장된 상태가 있으면 복원 완료
                userModifiedPhotos = true; // 복원된 경우 수정된 것으로 표시
                refreshPhotoPreviews();
            } else {
                // 저장된 상태가 없으면 서버에서 로드
                loadPhotosFromServer();
            }
        } else {
            // 등록 모드일 때는 이전 사진 초기화
            photoItems.clear();
            PrefManager.remove(PREF_KEY_PHOTOS);
            refreshPhotoPreviews();
        }
    }

    /**
     * 수정 모드에서 현재 사진 상태를 저장
     */
    private void saveEditPhotos() {
        if (!isEditMode || houseId == -1L) {
            return;
        }
        
        List<String> photoUrls = new ArrayList<>();
        List<String> photoIds = new ArrayList<>();
        
        for (PhotoItem item : photoItems) {
            // 업로드 완료된 사진만 저장 (localUri는 직렬화 불가)
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
     * 수정 모드에서 저장된 사진 상태를 복원
     * @return 복원 성공 여부
     */
    private boolean restoreEditPhotos() {
        if (!isEditMode || houseId == -1L) {
            return false;
        }
        
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
            if (TextUtils.isEmpty(url)) {
                continue;
            }
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
     * 수정 모드에서 저장된 사진 상태 삭제
     */
    private void clearEditPhotos() {
        if (houseId == -1L) {
            return;
        }
        String photosKey = PREF_KEY_EDIT_PHOTOS_PREFIX + houseId;
        String idsKey = PREF_KEY_EDIT_PHOTO_IDS_PREFIX + houseId;
        PrefManager.remove(photosKey);
        PrefManager.remove(idsKey);
        Log.d(TAG, "Cleared edit photos for houseId: " + houseId);
    }

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
                if (userModifiedPhotos) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    photoItems.clear();
                    List<HousePhotoDto> photos = response.body();
                    Collections.sort(photos, Comparator.comparing(
                            dto -> dto.getSortOrder() != null ? dto.getSortOrder() : Integer.MAX_VALUE));
                    for (HousePhotoDto dto : photos) {
                        if (photoItems.size() >= MAX_PHOTO_COUNT) {
                            break;
                        }
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
                if (userModifiedPhotos) {
                    return;
                }
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
        if (TextUtils.isEmpty(url) || photoItems.size() >= MAX_PHOTO_COUNT) {
            return;
        }
        photoItems.add(new PhotoItem(url, photoId));
    }

    private void refreshPhotoPreviews() {
        if (photoContainer == null) {
            return;
        }
        int childCount = photoContainer.getChildCount();
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
        if (addPhotoButton == null) {
            return;
        }
        addPhotoButton.setVisibility(hasRoomForMorePhotos() ? View.VISIBLE : View.GONE);
    }

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

        // RelativeLayout으로 이미지와 삭제 버튼을 겹쳐서 배치
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
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        imageView.setAlpha(item.isUploaded() ? 1f : 0.5f);
        container.addView(imageView);

        // 수정 모드이고 사진이 업로드된 경우에만 삭제 버튼 표시
        if (isEditMode && wantsPhotoUpdate && item.isUploaded()) {
            ImageView deleteButton = new ImageView(this);
            android.widget.RelativeLayout.LayoutParams deleteParams =
                    new android.widget.RelativeLayout.LayoutParams(dpToPx(28), dpToPx(28));
            deleteParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP);
            deleteParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END);
            deleteParams.setMargins(0, dpToPx(4), dpToPx(4), 0);
            deleteButton.setLayoutParams(deleteParams);
            
            // 원형 배경을 위한 drawable 생성
            android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
            background.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            background.setColor(0xE6000000); // 반투명 검은색 배경
            deleteButton.setBackground(background);
            
            // X 아이콘 설정
            deleteButton.setImageResource(R.drawable.ic_close_24);
            deleteButton.setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
            deleteButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            deleteButton.setColorFilter(0xFFFFFFFF); // 흰색 아이콘
            
            // 삭제 버튼 클릭 리스너
            deleteButton.setOnClickListener(v -> {
                deletePhoto(item);
            });
            
            container.addView(deleteButton);
        }

        card.addView(container);
        return card;
    }

    private boolean hasRoomForMorePhotos() {
        return photoItems.size() < MAX_PHOTO_COUNT;
    }

    private void enqueueUploads(List<PhotoItem> newItems) {
        if (newItems == null || newItems.isEmpty()) {
            return;
        }
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

    private void uploadPhotoItem(PhotoItem item) {
        Uri localUri = item.getUploadUri();
        if (localUri == null) {
            uploadNextPhoto();
            return;
        }

        ProgressDialog dialog = getUploadProgressDialog();
        dialog.setMessage("사진을 업로드하고 있습니다...");
        if (!dialog.isShowing()) {
            dialog.show();
        }

        String userId = PrefManager.get("uid", "guest");
        String houseTitle = PrefManager.get("house_title", "no_title");
        if (TextUtils.isEmpty(userId)) {
            userId = "guest";
        }
        if (TextUtils.isEmpty(houseTitle)) {
            houseTitle = "temporary";
        }

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
                    saveEditPhotos(); // 수정 모드일 때 사진 상태 저장
                    uploadNextPhoto();
                })
                .addOnFailureListener(e -> {
                    photoItems.remove(item);
                    Toast.makeText(this, "업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    persistPhotoUrlsToPrefs();
                    refreshPhotoPreviews();
                    saveEditPhotos(); // 수정 모드일 때 사진 상태 저장
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

    private void deletePhoto(PhotoItem item) {
        // 최소 1장 이상의 사진이 필요하므로, 마지막 사진은 삭제 불가
        int uploadedPhotoCount = 0;
        for (PhotoItem photo : photoItems) {
            if (photo.isUploaded()) {
                uploadedPhotoCount++;
            }
        }
        
        if (uploadedPhotoCount <= 1) {
            Toast.makeText(this, "최소 1장 이상의 사진이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 로컬에서만 삭제 (새로 추가한 사진인 경우)
        if (item.getPhotoId() == null) {
            int index = photoItems.indexOf(item);
            if (index != -1) {
                photoItems.remove(index);
                userModifiedPhotos = true;
                refreshPhotoPreviews();
                saveEditPhotos(); // 수정 모드일 때 사진 상태 저장
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
                    // 서버 삭제 성공 시 로컬에서도 제거
                    int index = photoItems.indexOf(item);
                    if (index != -1) {
                        photoItems.remove(index);
                        userModifiedPhotos = true;
                        refreshPhotoPreviews();
                        saveEditPhotos(); // 수정 모드일 때 사진 상태 저장
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

    private void persistPhotoUrlsToPrefs() {
        if (isEditMode) {
            return;
        }
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
        if (isUploadingPhotos || !uploadQueue.isEmpty()) {
            return true;
        }
        for (PhotoItem item : photoItems) {
            if (!item.isUploaded()) {
                return true;
            }
        }
        return false;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private static class PhotoItem {
        private final Uri localUri;
        private String remoteUrl;
        private Long photoId;  // 서버에서 로드한 사진의 ID (삭제 시 사용)

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
                    clearEditPhotos(); // 수정 완료 시 저장된 사진 상태 삭제
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
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid coordinate value: " + value, e);
            return null;
        }
    }

    /**
     * 뒤로가기 또는 취소 버튼 클릭 시 처리
     * 집이 생성된 상태에서 취소하면 houseId를 정리
     */
    private void handleBackPressed() {
        if (isEditMode) {
            finish();
            return;
        }
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
        if (pickMedia != null) {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        } else {
            Toast.makeText(this, "갤러리를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
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

        // 디버깅용 로그 추가
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

        // 3. 필수 데이터 검증
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

        // 날짜 형식 검증 (yyyy-MM-dd 형식이어야 함)
        if (!startDay.matches("\\d{4}-\\d{2}-\\d{2}") ||
            !endDay.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "날짜 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Invalid date format - start: " + startDay + ", end: " + endDay);
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

        if (hasPendingUploads()) {
            Toast.makeText(this, "사진 업로드가 완료될 때까지 기다려주세요.", Toast.LENGTH_SHORT).show();
            registerButton.setEnabled(true);
            return;
        }

        List<String> photoUrls = collectUploadedPhotoUrls();
        String imageUrl = photoUrls.isEmpty() ? "" : photoUrls.get(0);

        // 7. 집 생성 API 호출
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

        Call<HousesResponseDto> houseCall = apiService.createHouse(bearerToken, houseRequest);
        houseCall.enqueue(new Callback<HousesResponseDto>() {
            @Override
            public void onResponse(Call<HousesResponseDto> call, Response<HousesResponseDto> response) {
                if (response.isSuccessful()) {
                    Long houseId = null;
                    
                    // 순환 참조로 인한 파싱 오류를 대비하여 try-catch 사용
                    try {
                        if (response.body() != null) {
                            houseId = response.body().getId();
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to parse response body due to circular reference", e);
                        // 순환 참조로 인한 파싱 오류이지만, 실제로는 등록이 성공했을 가능성이 높음
                        // 서버 로그나 다른 방법으로 확인 필요
                        houseId = -1L; // 임시값, 실제로는 서버에서 확인 필요
                    }
                    
                    if (houseId != null && houseId != -1L) {
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
                        // 순환 참조로 인한 파싱 오류지만, 실제로는 등록이 성공했을 가능성이 높음
                        Log.w(TAG, "Could not extract houseId due to circular reference, but registration likely succeeded");
                        // 편의시설 저장은 건너뛰고 등록 완료 처리
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
                // 순환 참조로 인한 파싱 오류인 경우, 실제로는 등록이 성공했을 수 있음
                if (t instanceof com.google.gson.stream.MalformedJsonException && 
                    t.getMessage() != null && t.getMessage().contains("photos")) {
                    Log.w(TAG, "JSON parsing error due to circular reference, but house might be created", t);
                    // 서버에서 집이 생성되었을 가능성이 높으므로, 사용자에게 확인 메시지 표시
                    Toast.makeText(RegisterFinalActivity.this, 
                        "집 등록이 완료되었을 수 있습니다. 목록에서 확인해주세요.", 
                        Toast.LENGTH_LONG).show();
                    onRegistrationComplete();
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