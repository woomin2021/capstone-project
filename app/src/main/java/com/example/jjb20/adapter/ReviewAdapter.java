package com.example.jjb20.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.R;
import com.example.jjb20.dto.HouseReviewDTO;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private ArrayList<HouseReviewDTO> list;

    public ReviewAdapter(ArrayList<HouseReviewDTO> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_house_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HouseReviewDTO item = list.get(position);

        // 사용자 이름 (현재 DTO에 없으므로 reviewer_guest_user_id 로 대체)
        holder.userName.setText("사용자 " + item.getReviewer_guest_user_id());

        // 날짜
        holder.date.setText(item.getCreatedAt());

        // 별점
        holder.ratingBar.setRating(item.getRating());

        // 코멘트
        holder.content.setText(item.getComment());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImg;
        TextView userName, date, content;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImg = itemView.findViewById(R.id.review_profile_img);
            userName = itemView.findViewById(R.id.review_user_name);
            date = itemView.findViewById(R.id.review_date);
            ratingBar = itemView.findViewById(R.id.review_rating);
            content = itemView.findViewById(R.id.review_text);
        }
    }
}
