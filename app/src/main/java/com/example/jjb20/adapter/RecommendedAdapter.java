package com.example.jjb20.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jjb20.R;
import com.example.jjb20.RentHouseDetailActivity;
import com.example.jjb20.dto.HouseDto;

import java.util.List;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.ViewHolder> {

    private final List<HouseDto> items;

    public RecommendedAdapter(List<HouseDto> items) {
        this.items = items;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageHotel;
        TextView textTitle, textDate, textAddress, textPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageHotel  = itemView.findViewById(R.id.imageHotel);
            textTitle   = itemView.findViewById(R.id.textTitle);
            textDate    = itemView.findViewById(R.id.textDate);
            textAddress = itemView.findViewById(R.id.textAddress);
            textPrice   = itemView.findViewById(R.id.textPrice);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommend, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HouseDto item = items.get(position);

        // 제목
        holder.textTitle.setText(item.title != null ? item.title : "");

        // 날짜
        if (item.startDay != null && item.endDay != null) {
            holder.textDate.setText(item.startDay + " ~ " + item.endDay);
        } else {
            holder.textDate.setText("");
        }

        // 주소
        String addr = "";
        if (item.city != null) addr += item.city + " ";
        if (item.addressLine1 != null) addr += item.addressLine1;
        holder.textAddress.setText(addr.trim());

        // 가격
        if (item.pricePerNight != null) {
            holder.textPrice.setText(String.format("%,d원 · 1박", item.pricePerNight));
        } else {
            holder.textPrice.setText("");
        }

        // 사진 (coverPhotoUrl 사용 안하면 사진이 안나옴 겁나 오래 걸림 )
        if (item.coverPhotoUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(item.coverPhotoUrl)
                    .into(holder.imageHotel);
        }

        // 카드 클릭  >>>>>>>>>>> 상세 화면으로 가겠지?
        holder.itemView.setOnClickListener(v -> {
            Context ctx = holder.itemView.getContext();
            Intent intent = new Intent(ctx, RentHouseDetailActivity.class);
            intent.putExtra(RentHouseDetailActivity.EXTRA_HOUSE, item);
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
}