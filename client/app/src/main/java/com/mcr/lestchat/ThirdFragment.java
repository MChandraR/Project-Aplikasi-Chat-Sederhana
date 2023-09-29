package com.mcr.lestchat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mcr.lestchat.Adapter.MessageAdapter;
import com.mcr.lestchat.Model.MessageModel;
import com.mcr.lestchat.databinding.PersonalChatLayoutBinding;
import com.mcr.lestchat.util.Connection;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ThirdFragment extends Fragment {
    PersonalChatLayoutBinding binding ;
    String username,token;
    Connection conn;
    Socket mSocket;
    MainActivity MA;
    public MessageAdapter msgAdapter;
    public RecyclerView MessageArea;
    public RecyclerView.Adapter RvAdapter;
    public RecyclerView.LayoutManager RvManager;
    public ArrayList<MessageModel> msgData;
    private boolean handlerrun, onkey;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PersonalChatLayoutBinding.inflate(inflater,container,false);
        conn = new Connection();

        handlerrun = false;
        onkey = false;
        msgData = new ArrayList<>();
        if (MA != null) msgData = MA.personalChatData.get(username);
        Picasso.get().load(conn.getServerUrl() + "/" + username + ".png").into(binding.UserImages);
        binding.RoomName.setText(username);

        MessageArea = binding.MessageArea;
        msgAdapter = new MessageAdapter(MA, MessageArea, msgData);
        RvAdapter = msgAdapter;
        RvManager = new LinearLayoutManager(this.getContext());
        MessageArea.setAdapter(RvAdapter);
        MessageArea.setLayoutManager(RvManager);
        if (MA != null) mSocket = MA.getSocket();
        addEvent();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(MA==null)this.getContext().startActivity(new Intent(this.getActivity(),MainActivity.class));

    }

    public void setData(MainActivity MA, String username, String token, ArrayList<MessageModel> data){
        this.MA = MA;
        this.token = token;
        this.username = username;
        this.msgData = data;
    }

    public void addEvent(){
        binding.ButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(binding.InputField.getText().toString());
                binding.InputField.setText("");
            }
        });

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(!onkey && MA != null){
                    mSocket.emit("stop typing private",token);
                    handlerrun = false;
                }else{
                    handler.postDelayed(this::run,1000);
                }

            }
        };

        binding.InputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!handlerrun && MA!=null)mSocket.emit("typing private",token);
                onkey = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                onkey = false;
                if(!handlerrun && MA!=null){
                    handlerrun = true;
                    handler.postDelayed(runnable,1000);
                }


            }
        });

    }

    public void sendMessage(String message){
        if(MA==null)return;
        mSocket.emit("personal chat",new Object[]{username,token,message});
        msgAdapter.addData(new MessageModel(MA.getUsername(),message,1,MA.getTime(),true));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
