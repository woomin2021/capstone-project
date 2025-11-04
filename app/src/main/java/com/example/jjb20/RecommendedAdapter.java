package com.example.jjb20;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.entity.HotelItem;

import java.util.List;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.ViewHolder> {

    private final List<HotelItem> items;

    public RecommendedAdapter(List<HotelItem> items) {
        this.items = items;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageHotel;
        TextView textTitle, textDate, textPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageHotel = itemView.findViewById(R.id.imageHotel);
            textTitle  = itemView.findViewById(R.id.textTitle);
            textDate   = itemView.findViewById(R.id.textDate);
            textPrice  = itemView.findViewById(R.id.textPrice);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommend, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HotelItem item = items.get(position);

        holder.textTitle.setText(item.getTitle());
        holder.textDate.setText(item.getDate());
        holder.textPrice.setText(item.getPrice());
        holder.imageHotel.setImageResource(item.getImageRes());

        // 상세 페이지로 이동
        holder.itemView.setOnClickListener(v -> {
            Context ctx = holder.itemView.getContext();
            Intent intent = new Intent(ctx, RentHouseDetailActivity.class);
            intent.putExtra(RentHouseDetailActivity.EXTRA_HOUSE, item);
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}