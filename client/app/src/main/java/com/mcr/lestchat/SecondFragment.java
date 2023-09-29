package com.mcr.lestchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mcr.lestchat.Adapter.MessageAdapter;
import com.mcr.lestchat.Model.MessageModel;
import com.mcr.lestchat.databinding.ChatRoomLayoutBinding;
import com.mcr.lestchat.util.Connection;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

public class SecondFragment extends Fragment {
    private MainActivity MA;

    private ChatRoomLayoutBinding binding;
    private Connection conn;
    private RecyclerView MessageArea;
    private RecyclerView.Adapter RvAdapter;
    private RecyclerView.LayoutManager RvManager;
    private MessageAdapter MsgAdapter;
    private SharedPreferences SP;
    private Socket mSocket;
    private ArrayList<MessageModel> messageData;

    boolean typing,onkey,handlerrun;
    String usertyping;

    public void Init(MainActivity MA, ArrayList<MessageModel> data){
        messageData = data;
        this.MA = MA;
    }

    Bundle bundl;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        this.setRetainInstance(true);

        SP = this.getContext().getSharedPreferences("mcr", Context.MODE_PRIVATE);
        String username = SP.getString("username", "");
        binding = ChatRoomLayoutBinding.inflate(inflater, container, false);
        MessageArea = binding.MessageArea;
        onkey = false;
        handlerrun = false;

        mSocket = MA.getSocket();
        mSocket.on("new message", onNewMessage);
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on("typing", onUserTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.emit("add user", username);

        MsgAdapter = new MessageAdapter(MA, MessageArea, messageData);
        RvAdapter = MsgAdapter;
        RvManager = new LinearLayoutManager(this.getContext());
        MessageArea.setAdapter(MsgAdapter);
        MessageArea.setLayoutManager(RvManager);
        MessageArea.scrollToPosition(MsgAdapter.getItemCount() - 1);

        bundl = savedInstanceState;

        return binding.getRoot();
    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(MA==null)this.getContext().startActivity(new Intent(this.getActivity(),MainActivity.class));


        binding.ButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(!onkey){
                    mSocket.emit("stop typing","MCr");
                    handlerrun = false;
                }else{
                    handler.postDelayed(this,1000);
                }

            }
        };

        binding.InputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!handlerrun)mSocket.emit("typing","MCr");
                onkey = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                onkey = false;
                if(!handlerrun){
                    handlerrun = true;
                    handler.postDelayed(runnable,1000);
                }


            }
        });
    }

    private void sendMessage(){
        String msg = binding.InputField.getText().toString().trim();
        if(msg.isEmpty()){
            return;
        }
        binding.InputField.setText("");
        MsgAdapter.addData(new MessageModel("Saya",msg,1,getTime(),true));
        mSocket.emit("new message",msg);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSocket.off("new message", onNewMessage);
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off("typing",onUserTyping);
        mSocket.off("stop typing",onStopTyping);
        mSocket.off("user joined",onUserJoined);
        mSocket.off("user left",onUserLeft);
        binding = null;
    }


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String username = "";
                    String message  ="";
                    try {
                        JSONObject data = (JSONObject) args[0];
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }
                    MsgAdapter.addData(new MessageModel(username,message,0,getTime(),true));
                    //MA.messageData.add(new MessageModel(username,message,0,getTime()) );
                }
            });
        }
    };

    private Emitter.Listener onUserTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String username = "";
                    String message  ="";
                    try {
                        JSONObject data = (JSONObject) args[0];
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    if(!typing){
                        typing = true;
                        usertyping = username;
                        binding.RoomStatus.setTextColor(ColorStateList.valueOf(Color.parseColor("#85F812")));
                        binding.RoomStatus.setText(username+" mengetik...");
                    }
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String username = "";
                    String message  ="";
                    try {
                        JSONObject data = (JSONObject) args[0];
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }

                    if(username.equals(usertyping)){
                        typing = false;
                        binding.RoomStatus.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFFFF")));
                        binding.RoomStatus.setText("Kelompok Sister");
                    }
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String username = "";
                    int numUser  = 0;
                    try {
                        JSONObject data = (JSONObject) args[0];
                        username = data.getString("username");
                        numUser = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Toast.makeText(SecondFragment.this.getContext(),e.toString(),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //MA.messageData.add(new MessageModel(username,username + " bergabung.",2,getTime()));
                    MsgAdapter.addData(new MessageModel(username,username + " bergabung.",2,getTime(),true));
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String username = "";
                    int numUser  = 0;
                    try {
                        JSONObject data = (JSONObject) args[0];
                        username = data.getString("username");
                        numUser = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Toast.makeText(SecondFragment.this.getContext(),e.toString(),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //MA.messageData.add(new MessageModel(username,username + " keluar.",2,getTime()));
                    MsgAdapter.addData(new MessageModel(username,username + " keluar.",2,getTime(),true));
                }
            });
        }
    };


    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SecondFragment.this.getContext(), String.valueOf("Entering Chat Room"), Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private String getTime(){
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        return currentTime;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}