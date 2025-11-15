package com.example.jjb20.chat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jjb20.chat.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.jjb20.databinding.CustomChatMsgBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final ArrayList<ChatMsgVO> messages;
    private final String currentUserId;

    public ChatAdapter(ArrayList<ChatMsgVO> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }



    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CustomChatMsgBinding binding = CustomChatMsgBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {

        ChatMsgVO msg = messages.get(position);
        if (msg.getUserid().equals(currentUserId)) {
            holder.binding.myCl.setVisibility(View.VISIBLE);
            holder.binding.otherCl.setVisibility(View.GONE);

            holder.binding.contentTv2.setText(msg.getContent());
            holder.binding.dateTv2.setText(msg.getCrt_dt());
        } else {
            holder.binding.myCl.setVisibility(View.GONE);
            holder.binding.otherCl.setVisibility(View.VISIBLE);

            holder.binding.useridTv.setText(msg.getUserid());
            holder.binding.contentTv.setText(msg.getContent());
            holder.binding.dateTv.setText(msg.getCrt_dt());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final CustomChatMsgBinding binding;

        public ViewHolder(@NonNull CustomChatMsgBinding binding){
            super(binding.getRoot());
            this.binding = binding;

        }
        @Override
        public String toString() {
            return super.toString();
        }
    }
}