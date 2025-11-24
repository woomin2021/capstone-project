package com.example.jjb20;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jjb20.dto.UserUpdateRequestDto;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private TextView email, name, phone;



    private ApiService apiService;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PrefManager.init(this);

        setContentView(R.layout.activity_edit_profile_info);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        RelativeLayout edit_email_btn = findViewById(R.id.edit_email_btn);
        RelativeLayout edit_name_btn = findViewById(R.id.edit_name_button);
        RelativeLayout edit_phone_btn = findViewById(R.id.edit_phone_button);
        RelativeLayout edit_password_button = findViewById(R.id.edit_password_button);
        RelativeLayout edit_profile_image_btn = findViewById(R.id.edit_profile_image_button);

        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);

        email.setText(PrefManager.get("email"));
        name.setText(PrefManager.get("userName"));
        phone.setText(PrefManager.get("phone"));


        edit_email_btn.setOnClickListener(v -> {
            showEditEmailDialog(PrefManager.get("email"));
        });

        edit_name_btn.setOnClickListener(v -> {
            showEditNameDialog(PrefManager.get("userName"));
        });

        edit_phone_btn.setOnClickListener(view -> {
            showEditPhoneDialog(PrefManager.get("phone"));
        });

        edit_password_button.setOnClickListener(v -> {
            showEditPasswordDialog();
        });

    }

    //이름 변경
    private void showEditNameDialog(String currentName) { //이름 바꾸기 다이얼로그
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_name, null); //이름수저 다이얼로그 인플레이트
        EditText etName = view.findViewById(R.id.editNameInput);
        etName.setText(currentName);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create(); //다이얼로그로 등록

        //취소 버튼
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            dialog.dismiss();
        });

        //확인 버튼
        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();

            if (!newName.isEmpty()) {
                UserUpdateRequestDto updateRequestDto = new UserUpdateRequestDto();
                updateRequestDto.name = newName;

                String token = PrefManager.get("idToken");
                String bearer = "Bearer " + token;

                apiService.updateMe(updateRequestDto, bearer).enqueue(new Callback<UserUpdateRequestDto>() {
                    @Override
                    public void onResponse(Call<UserUpdateRequestDto> call, Response<UserUpdateRequestDto> response) {
                        PrefManager.put("userName", newName);


                        Toast.makeText(EditProfileActivity.this, "변경 완료", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        name.setText(newName);
                    }

                    @Override
                    public void onFailure(Call<UserUpdateRequestDto> call, Throwable t) {
                        Toast.makeText(EditProfileActivity.this, "서버 오류", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        dialog.show();
    }

    //이메일 변경 ( 스프링 email 변경 + firebase email 변경)
    private void showEditEmailDialog(String currentName) { //이름 바꾸기 다이얼로그
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_email, null); //이름수저 다이얼로그 인플레이트
        EditText etEmail = view.findViewById(R.id.editEmailInput);
        etEmail.setText(currentName);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create(); //다이얼로그로 등록

        //취소 버튼
        view.findViewById(R.id.btnCancelEmail).setOnClickListener(v -> {
            dialog.dismiss();
        });

        //확인 버튼
        view.findViewById(R.id.btnConfirmEmail).setOnClickListener(v -> {
            String newEmail = etEmail.getText().toString().trim();

            if (!newEmail.isEmpty()) {
                UserUpdateRequestDto updateRequestDto = new UserUpdateRequestDto();
                updateRequestDto.email = newEmail;

                String token = PrefManager.get("idToken");
                String bearer = "Bearer " + token;

                apiService.updateMe(updateRequestDto, bearer).enqueue(new Callback<UserUpdateRequestDto>() {
                    @Override
                    public void onResponse(Call<UserUpdateRequestDto> call, Response<UserUpdateRequestDto> response) {
                        user.updateEmail(newEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        });
                        PrefManager.put("email", newEmail);
                        email.setText(newEmail);
                        Toast.makeText(EditProfileActivity.this, "변경 완료", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<UserUpdateRequestDto> call, Throwable t) {
                        Toast.makeText(EditProfileActivity.this, "서버 오류", Toast.LENGTH_SHORT).show();
                    }
                });

                PrefManager.put("Email", newEmail);

            }
        });
        dialog.show();
    }

    // 전화번호 변경
    private void showEditPhoneDialog(String currentPhone) { //이름 바꾸기 다이얼로그
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_phone, null); //이름수저 다이얼로그 인플레이트
        EditText etPhone = view.findViewById(R.id.editEmailInput);
        etPhone.setText(currentPhone);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create(); //다이얼로그로 등록

        //취소 버튼
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            dialog.dismiss();
        });

        //확인 버튼
        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String newPhone = etPhone.getText().toString().trim();

            if (!newPhone.isEmpty()) {
                UserUpdateRequestDto updateRequestDto = new UserUpdateRequestDto();
                updateRequestDto.phone = newPhone;

                String token = PrefManager.get("idToken");
                String bearer = "Bearer " + token;
                apiService.updateMe(updateRequestDto, bearer).enqueue(new Callback<UserUpdateRequestDto>() {
                    @Override
                    public void onResponse(Call<UserUpdateRequestDto> call, Response<UserUpdateRequestDto> response) {
                        Toast.makeText(EditProfileActivity.this, "변경 완료", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        PrefManager.put("phone", newPhone);
                        phone.setText(newPhone);
                    }

                    @Override
                    public void onFailure(Call<UserUpdateRequestDto> call, Throwable t) {
                        Toast.makeText(EditProfileActivity.this, "서버 오류", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        dialog.show();
    }

    //firebase 비밀번호 수정
    private void showEditPasswordDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_password, null);
        EditText newPassword = view.findViewById(R.id.editPasswordInput);
        EditText confirmPassword = view.findViewById(R.id.editPasswordConfirm);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        //취소 버튼
        view.findViewById(R.id.btnCancelPassword).setOnClickListener(v -> dialog.dismiss());

        //확인 버튼
        view.findViewById(R.id.btnConfirmPassword).setOnClickListener(v -> {
            String pass1 = newPassword.getText().toString();
            String pass2 = confirmPassword.getText().toString();

            //비밀번호 유효성 검사
            if (!pass1.equals(pass2)) {
                Toast.makeText(this, "비밀번호 불일치", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pass1.length() < 6) {
                Toast.makeText(this, "6자 이상 입력", Toast.LENGTH_SHORT).show();
                return;
            }


            //Firebase 비밀번호 수정
            user.updatePassword(pass1)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "비밀번호 변경 완료", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "실패: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show();
    }
}
