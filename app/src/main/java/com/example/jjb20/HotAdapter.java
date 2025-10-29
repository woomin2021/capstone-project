package com.example.jjb20;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.entity.HotelItem;

import java.util.List;

public class HotAdapter extends RecyclerView.Adapter<HotAdapter.ViewHolder> {

    private final List<HotelItem> items;

    public HotAdapter(List<HotelItem> items) {
        this.items = items;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageHot;
        TextView textTitle, textLocation, textPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageHot = itemView.findViewById(R.id.imageHot);
            textTitle = itemView.findViewById(R.id.textHotTitle);
            textLocation = itemView.findViewById(R.id.textHotLocation);
            textPrice = itemView.findViewById(R.id.textHotPrice);
        }
    }

    @NonNull
    @Override
    public HotAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hot, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HotAdapter.ViewHolder holder, int position) {
        HotelItem item = items.get(position);
        holder.textTitle.setText(item.getTitle());
        holder.textLocation.setText(item.getLocation());
        holder.textPrice.setText(item.getPrice());
        holder.imageHot.setImageResource(item.getImageRes());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
