package com.example.jjb20.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jjb20.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(String roomName);
    }

    private final List<String> roomList;
    private final OnRoomClickListener listener;
    private final Map<String, String> lastMessages = new HashMap<>(); // 🔹 방별 마지막 메시지 저장

    public ChatRoomListAdapter(List<String> roomList, OnRoomClickListener listener) {
        this.roomList = roomList;
        this.listener = listener;
    }

    public void setLastMessage(String roomName, String message) {
        lastMessages.put(roomName, message);
    }

    public void clearLastMessages() {
        lastMessages.clear();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatroom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String roomName = roomList.get(position);
        holder.roomNameTv.setText(roomName);
        holder.lastMsgTv.setText(lastMessages.getOrDefault(roomName, "마지막 메시지 없음"));
        holder.itemView.setOnClickListener(v -> listener.onRoomClick(roomName));
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView roomNameTv, lastMsgTv;
        ViewHolder(View itemView) {
            super(itemView);
            roomNameTv = itemView.findViewById(R.id.room_name_tv);
            lastMsgTv = itemView.findViewById(R.id.last_msg_tv);
        }
    }
}
