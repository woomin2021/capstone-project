package com.example.jjb20.chat;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jjb20.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ChatRoomFragment";

    private EditText chatroom_et;
    private Button enter_btn;
    private RecyclerView chatroom_list;

    private FirebaseDatabase database;
    private DatabaseReference chatRef;

    private List<String> chatroomNames = new ArrayList<>();
    private ChatRoomListAdapter adapter;

    public ChatRoomFragment() {}

    public static ChatRoomFragment newInstance() {
        return new ChatRoomFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        chatRef = database.getReference("chatrooms");  // 모든 채팅방 경로
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_room, container, false);

        chatroom_et = rootView.findViewById(R.id.chatroom_et);
        enter_btn = rootView.findViewById(R.id.enter_btn);
        chatroom_list = rootView.findViewById(R.id.chatroom_list);

        enter_btn.setOnClickListener(this);

        // RecyclerView 설정
        chatroom_list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatRoomListAdapter(chatroomNames, this::openChatRoom);
        chatroom_list.setAdapter(adapter);

        // Firebase에서 채팅방 목록 + 마지막 메시지 불러오기
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatroomNames.clear();
                adapter.clearLastMessages(); // 마지막 메시지 초기화

                for (DataSnapshot room : snapshot.getChildren()) {
                    String roomName = room.getKey();
                    chatroomNames.add(roomName);

                    // 🔹 채팅방 안의 마지막 메시지 가져오기
                    DataSnapshot lastMsgSnapshot = null;
                    for (DataSnapshot msg : room.getChildren()) {
                        lastMsgSnapshot = msg; // 마지막 루프에 남는 게 가장 최근 메시지
                    }

                    if (lastMsgSnapshot != null && lastMsgSnapshot.child("content").getValue() != null) {
                        String lastMsg = lastMsgSnapshot.child("content").getValue(String.class);
                        adapter.setLastMessage(roomName, lastMsg);
                    } else {
                        adapter.setLastMessage(roomName, "메시지 없음");
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase load error: " + error.getMessage());
            }
        });

        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.enter_btn) {
            String roomName = chatroom_et.getText().toString().trim();

            if (!roomName.isEmpty()) {
                Log.d(TAG, "입장 처리: " + roomName);

                // Firebase에 채팅방 생성 (없으면 새로 생성)
                chatRef.child(roomName).setValue("active");

                openChatRoom(roomName);

            } else {
                Toast.makeText(getActivity(), "채팅방 이름을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 채팅방 입장 (ChatMsgFragment 전환)
    private void openChatRoom(String roomName) {
        ChatMsgFragment chatMsgFragment = new ChatMsgFragment();
        Bundle args = new Bundle();
        args.putString("chatroom", roomName);
        chatMsgFragment.setArguments(args);

        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        ft.replace(R.id.main, chatMsgFragment);
        ft.addToBackStack(null);
        ft.commit();
    }
}
