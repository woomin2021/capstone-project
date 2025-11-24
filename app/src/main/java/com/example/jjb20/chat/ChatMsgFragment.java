package com.example.jjb20.chat;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jjb20.PrefManager;
import com.example.jjb20.R;
import com.example.jjb20.chat.placeholder.PlaceholderContent;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class ChatMsgFragment extends Fragment implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    //채팅을 입력할 입력창과 전송 버튼
    EditText content_et;
    ImageView send_iv;

    //채팅 내뇽을 뿌려줄 RecyclerView와 Adapter
    RecyclerView rv;
    ChatAdapter mAdapter;

    String chatroom = "";

    ArrayList<ChatMsgVO> msgList = new ArrayList<>();

    FirebaseDatabase databases = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    //현재 유저 아이디
    private String currentUserId;


    public ChatMsgFragment() {
    }

    // 방 이름(roomId)을 받는 newInstance
    public static ChatMsgFragment newInstance(String chatroomName) {
        ChatMsgFragment fragment = new ChatMsgFragment();
        Bundle args = new Bundle();
        args.putString("chatroom", chatroomName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_msg, container, false);

        ImageView closeBtn = view.findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();

        });

        content_et = view.findViewById(R.id.content_et);
        send_iv = view.findViewById(R.id.send_iv);
        rv = view.findViewById(R.id.rv);

        TextView titleTv = view.findViewById(R.id.chatroom_title);

        send_iv.setOnClickListener(this);

        // 🔹 인자 안전하게 받기 + 로그
        Bundle args = getArguments();
        if (args != null) {
            chatroom = args.getString("chatroom", "");
        }
        currentUserId = PrefManager.get("uid", "guest");
        Log.d(TAG, "chatroom = " + chatroom + ", currentUserId = " + currentUserId);

        // 여기서 실제로 방 이름을 화면에 표시
        if (titleTv != null) {
            titleTv.setText(chatroom);
        }

        mAdapter = new ChatAdapter(msgList, currentUserId);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(mAdapter);

        //Firebase Database 초기화
        myRef = databases.getReference("chatrooms").child(chatroom).child("messages");

        //Firebase Database Listener
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatMsgVO chatMsgVO = snapshot.getValue(ChatMsgVO.class);
                if (chatMsgVO != null) {
                    msgList.add(chatMsgVO);
                    mAdapter.notifyItemInserted(msgList.size() - 1);
                    rv.scrollToPosition(msgList.size() - 1);
                }
                Log.d(TAG, "msgList size = " + msgList.size());
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_iv) {
            if (content_et.getText().toString().trim().length() >= 1) {
                Log.d(TAG, "입력처리");

                SimpleDateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss");
                String currentTime = df.format(new Date());

                String username = PrefManager.get("email", "Unknown user");

                ChatMsgVO msgVO = new ChatMsgVO(
                        currentUserId,
                        currentTime,
                        content_et.getText().toString().trim(),
                        username
                );

                myRef.push().setValue(msgVO);
                content_et.setText("");
            } else {
                Toast.makeText(getActivity(), "메시지를 입력하세요", Toast.LENGTH_SHORT).show();
            }
        }
    }
}