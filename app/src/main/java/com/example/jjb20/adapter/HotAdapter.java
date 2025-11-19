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
import com.example.jjb20.RentHouseDetailActivity;
import com.example.jjb20.dto.HouseDto;
import com.example.jjb20.R;

import java.util.List;

public class HotAdapter extends RecyclerView.Adapter<HotAdapter.ViewHolder> {

    private final List<HouseDto> items;

    public HotAdapter(List<HouseDto> items) {
        this.items = items;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageHot;
        TextView textTitle, textLocation, textDate, textPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageHot    = itemView.findViewById(R.id.imageHot);
            textTitle   = itemView.findViewById(R.id.textHotTitle);
            textLocation= itemView.findViewById(R.id.textHotLocation);
            textDate    = itemView.findViewById(R.id.textDate);
            textPrice   = itemView.findViewById(R.id.textHotPrice);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hot, parent, false); // HOT 섹션 XML
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HouseDto item = items.get(position);

        // 제목
        holder.textTitle.setText(item.title != null ? item.title : "");

        // 위치(도시 + 주소)
        String loc = "";
        if (item.city != null) loc += item.city;
        if (item.addressLine1 != null) {
            if (!loc.isEmpty()) loc += " · ";
            loc += item.addressLine1;
        }
        holder.textLocation.setText(loc);

        // 날짜
        if (item.startDay != null && item.endDay != null) {
            holder.textDate.setText(item.startDay + " ~ " + item.endDay);
        } else {
            holder.textDate.setText("");
        }

        // 가격
        if (item.pricePerNight != null) {
            holder.textPrice.setText(String.format("%,d원", item.pricePerNight));
        } else {
            holder.textPrice.setText("");
        }

        // 대표 이미지
        if (item.coverPhotoUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(item.coverPhotoUrl)
                    .into(holder.imageHot);
        }

        // 카드 클릭 → 상세 페이지 이동
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