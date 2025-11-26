package com.example.jjb20.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jjb20.R;
import com.example.jjb20.dto.HouseDto;

import java.util.List;

public class HouseListAdapter extends RecyclerView.Adapter<HouseListAdapter.HouseViewHolder> {

    private Context context;
    private List<HouseDto> houseList;
    private OnItemClickListener listener;

    // 클릭 콜백 인터페이스
    public interface OnItemClickListener {
        void onItemClick(HouseDto item);
        void onEditClick(HouseDto item);
    }

    // ✔ 클릭 없는 기본 생성자
    public HouseListAdapter(Context context, List<HouseDto> houseList) {
        this.context = context;
        this.houseList = houseList;
        this.listener = null;
    }

    // ✔클릭 포함 생성자
    public HouseListAdapter(Context context, List<HouseDto> houseList, OnItemClickListener listener) {
        this.context = context;
        this.houseList = houseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_house_list, parent, false);
        return new HouseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HouseViewHolder holder, int position) {
        HouseDto item = houseList.get(position);

        holder.txtTitle.setText(item.title);
        holder.txtAddress.setText(item.addressLine1);
        holder.txtPrice.setText(item.pricePerNight + " / 박");


        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(item));
        } else {
            holder.itemView.setOnClickListener(null);
            holder.btnEdit.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return houseList != null ? houseList.size() : 0;
    }

    static class HouseViewHolder extends RecyclerView.ViewHolder {

        ImageView imgHouse;
        TextView txtTitle, txtAddress, txtPrice;
        Button btnEdit;

        public HouseViewHolder(@NonNull View itemView) {
            super(itemView);

            imgHouse = itemView.findViewById(R.id.imgHouse);
            txtTitle = itemView.findViewById(R.id.txtHouseName);
            txtAddress = itemView.findViewById(R.id.txtHouseAddress);
            txtPrice = itemView.findViewById(R.id.txtHousePrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
