package com.mcr.lestchat;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Message;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mcr.lestchat.Adapter.UserOnlineAdapter;
import com.mcr.lestchat.Model.MessageModel;
import com.mcr.lestchat.Model.UserOnlineModel;
import com.mcr.lestchat.databinding.ActivityMainBinding;
import com.mcr.lestchat.util.Connection;
import com.mcr.lestchat.util.Feature;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

public class MainActivity extends AppCompatActivity {
    public SharedPreferences SP;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private FrameLayout frameLayout;
    public FirstFragment FF;
    public SecondFragment SF;
    public ThirdFragment TF;
    public ArrayList<MessageModel> messageData;
    public Socket mainSocket ;
    private Connection conn;
    private Fragment curFragment;
    public ArrayList<UserOnlineModel> userData;
    public HashMap<String,ArrayList<MessageModel>> personalChatData;
    public boolean typing;
    public String usertyping;
    public View parent;
    Feature appFeature;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        userData = new ArrayList<>();
        personalChatData = new HashMap<>();
        SP = MainActivity.this.getSharedPreferences("mcr",MODE_PRIVATE);
        messageData = new ArrayList<>();
        FF = new FirstFragment();
        FF.Init(this);
        SF = new SecondFragment();
        SF.Init(this,messageData);
        TF = new ThirdFragment();

        parent = findViewById(R.id.MainParent);
        appFeature = new Feature(MainActivity.this,parent);
        changeFragment(FF);
        InitializeSocket();
        addSocketEvent();
    }

    public void addSocketEvent(){
        mainSocket.connect();
        mainSocket.on("user joined",onUserJoined);
        mainSocket.on("user left",onUserLeft);
        mainSocket.on("typing private",onUserTyping);
        mainSocket.on("stop typing private",onUserStopTyping);
        mainSocket.on("personal new chat",onPersonalChat);
        mainSocket.on("broadcast",onBroadCast);
        mainSocket.emit("add user",SP.getString("username",""));
        mainSocket.emit("fetch_user","MCR");

    }

    public void InitializeSocket(){
        conn = new Connection();
        String serverUrl = SP.getString("server_ip","");
        try {
            IO.Options opts = new IO.Options();
            opts.transports = new String[] { WebSocket.NAME };
            if(serverUrl.equals("") || serverUrl.isEmpty()) mainSocket = IO.socket(conn.getServerUrl());
            else mainSocket = IO.socket("http://" + serverUrl);
        } catch (URISyntaxException e) {
            Toast.makeText(MainActivity.this, "Gagal terhubung ke server !", Toast.LENGTH_SHORT).show();
        }
    }



    public Socket getSocket(){
        return this.mainSocket;
    }

    public void changeFragment(Fragment fragment){
        curFragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.FragmentArea,fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainSocket.disconnect();

    }

    @Override
    public void onBackPressed() {
        if(curFragment==SF) changeFragment(FF);
        else if(curFragment==TF) changeFragment(FF);
        else finishAffinity();
    }

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String username = "";
                    JSONArray onlineUser ;
                    try {
                        JSONObject data = (JSONObject) args[0];
                        username = data.getString("username");
                        onlineUser = data.getJSONArray("onlineUser");
                        if(username.equals(TF.username)){
                            TF.binding.RoomStatus.setText("Online");
                            TF.binding.RoomStatus.setTextColor(ColorStateList.valueOf(Color.parseColor("#85F812")));
                        }
                        appFeature.showSnackbar("System : \n User " + username +  " telah login !");
                        for(int i=0;i<onlineUser.length();i++){
                            JSONArray uData = onlineUser.getJSONArray(i);
                            if(String.valueOf(uData.get(0)).equals(SP.getString("username","")))continue;
                            if(String.valueOf(uData.get(0)).equals("") || String.valueOf(uData.get(1)).isEmpty()) continue;
                            if(FF.userAdapter != null) FF.userAdapter.addData(String.valueOf(uData.get(0)),String.valueOf(uData.get(1)));
                            else userData.add(new UserOnlineModel(String.valueOf(uData.get(0)),"Last msg !",String.valueOf(uData.get(1)),true));
                        }
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //Toast.makeText(MainActivity.this, String.valueOf(onlineUser), Toast.LENGTH_SHORT).show();
                    //MsgAdapter.addData(new MessageModel(username,message,0,getTime()));
                    //messageData.add(new MessageModel(username,message,0,getTime()) );
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String username = "";
                    JSONArray onlineUser ;
                    try {
                        JSONObject data = (JSONObject) args[0];
                        username = data.getString("username");
                        if(FF.userAdapter != null) FF.userAdapter.removeUser(username);
                        else RemoveUser(username);
                        if(username.equals(TF.username)){
                            TF.binding.RoomStatus.setText("Offline");
                            TF.binding.RoomStatus.setTextColor(ColorStateList.valueOf(Color.parseColor("#F44336")));
                        }

                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onPersonalChat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
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
                    if(personalChatData.get(username)==null) personalChatData.put(username,new ArrayList<>());
                    if(TF != null && TF.msgAdapter != null) TF.msgAdapter.addData(new MessageModel(username,message,0,getTime(),true));
                    else personalChatData.get(username).add(new MessageModel(username,message,0,getTime(),false));
                    if(FF != null) FF.userAdapter.updateUser(username,message);
                }
            });
        }
    };

    private Emitter.Listener onUserTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
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
                        if(TF.binding != null && curFragment == TF){
                            TF.binding.RoomStatus.setTextColor(ColorStateList.valueOf(Color.parseColor("#85F812")));
                            TF.binding.RoomStatus.setText(username+" mengetik...");
                        }else if(FF != null && curFragment == FF){
                            FF.userAdapter.setStatus(username,true);
                        }

                    }
                }
            });
        }
    };

    private Emitter.Listener onUserStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
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
                        if(TF.binding != null && curFragment == TF)TF.binding.RoomStatus.setText("Online");
                        else if(FF != null ){
                            FF.userAdapter.setStatus(username,false);
                        }
                    }
                }
            });
        }
    };

    private Emitter.Listener onBroadCast = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String username = "";
                    String message = "";
                    JSONArray onlineUser ;
                    try {
                        JSONObject data = (JSONObject) args[0];
                        username = data.getString("username");
                        message = data.getString("message");
                        appFeature.showSnackbar("[ Broadcast dari : <b> " + username +
                                "</b> ]<br>" + '"' + message + '"');
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            });
        }
    };

    private void RemoveUser(String user){
        for(int i=0;i<userData.size();i++){
            if(userData.get(i).getUsername().equals(user)){
                userData.remove(i);
                break;
            }
        }
    }

    public String getTime(){
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        return currentTime;
    }

    public String getUsername(){
        return SP.getString("username","");
    }

    public String getValidUrl(){
        if(SP.getString("server_ip","").isEmpty()) return "http://192.168.1.7:3000/";
        else return "http://"+SP.getString("server_ip","")+"/";
    }

}