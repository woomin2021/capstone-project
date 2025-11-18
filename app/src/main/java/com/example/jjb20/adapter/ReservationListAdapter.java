package com.example.jjb20.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.R;
import com.example.jjb20.dto.ReservationDTO;

import java.util.List;

public class ReservationListAdapter extends RecyclerView.Adapter<ReservationListAdapter.ReservationViewHolder> {

    private Context context;
    private List<ReservationDTO> reservationList;
    private OnItemClickListener listener;

    // 아이템 클릭 인터페이스
    public interface OnItemClickListener {
        void onItemClick(ReservationDTO item);
    }

    public ReservationListAdapter(Context context, List<ReservationDTO> reservationList, OnItemClickListener listener) {
        this.context = context;
        this.reservationList = reservationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        ReservationDTO item = reservationList.get(position);

        holder.txtReservationId.setText("예약번호 #" + item.id);
        holder.txtDateRange.setText(item.checkinDate + " ~ " + item.checkoutDate);
        holder.txtGuestCount.setText("인원: " + item.guestCount + "명");
        holder.txtTotalPrice.setText("₩ " + item.totalPrice);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return reservationList != null ? reservationList.size() : 0;
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {

        TextView txtReservationId, txtDateRange, txtGuestCount, txtTotalPrice;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);

            txtReservationId = itemView.findViewById(R.id.txtReservationId);
            txtDateRange = itemView.findViewById(R.id.txtDateRange);
            txtGuestCount = itemView.findViewById(R.id.txtGuestCount);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
        }
    }
}
