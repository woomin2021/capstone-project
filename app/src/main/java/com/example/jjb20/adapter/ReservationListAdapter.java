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

    private final Context context;
    private final List<ReservationDTO> reservationList;
    private final OnItemClickListener listener;

    // 아이템 클릭 인터페이스
    public interface OnItemClickListener {
        void onItemClick(ReservationDTO item);
    }

    public ReservationListAdapter(Context context,
                                  List<ReservationDTO> reservationList,
                                  OnItemClickListener listener) {
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

        // status → 한글로 변환해서 표시
        if (item.status != null) {
            String statusText;
            switch (item.status) {
                case "IN_PROGRESS":
                    statusText = "예약 진행중";
                    break;
                case "TRADE_DONE":
                    statusText = "거래 승인";
                    break;
                case "COMPLETED":
                    statusText = "이용 완료";
                    break;
                case "UNCOMPLETED":
                    statusText = "취소/거절";
                    break;
                default:
                    statusText = "알 수 없음";
            }
            holder.txtStatus.setText(statusText);
        } else {
            holder.txtStatus.setText("알 수 없음");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return reservationList != null ? reservationList.size() : 0;
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {

        TextView txtReservationId, txtDateRange, txtGuestCount, txtTotalPrice, txtStatus;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);

            txtReservationId = itemView.findViewById(R.id.txtReservationId);
            txtDateRange     = itemView.findViewById(R.id.txtDateRange);
            txtGuestCount    = itemView.findViewById(R.id.txtGuestCount);
            txtTotalPrice    = itemView.findViewById(R.id.txtTotalPrice);
            txtStatus        = itemView.findViewById(R.id.txtStatus);
        }
    }
}
