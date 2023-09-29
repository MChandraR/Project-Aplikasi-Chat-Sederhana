package com.mcr.lestchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mcr.lestchat.Adapter.UserOnlineAdapter;
import com.mcr.lestchat.Model.UserOnlineModel;
import com.mcr.lestchat.databinding.MainMenuBinding;
import com.mcr.lestchat.util.Connection;
import com.mcr.lestchat.util.Feature;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class FirstFragment extends Fragment {
    private SharedPreferences.Editor SP;
    private SharedPreferences SPget;
    private MainMenuBinding binding;
    private MainActivity MA;
    private RecyclerView Rv;
    public UserOnlineAdapter userAdapter;
    private RecyclerView.Adapter RvAdapter;
    private RecyclerView.LayoutManager RvManager;
    private ArrayList<UserOnlineModel> userData ;
    Feature appFeature;
    TextView Judul ;

    public void Init(MainActivity MA){
        this.MA = MA;
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        SP = this.getContext().getSharedPreferences("mcr", Context.MODE_PRIVATE).edit();
        SPget = this.getContext().getSharedPreferences("mcr", Context.MODE_PRIVATE);
        setInputDialog();
        binding = MainMenuBinding.inflate(inflater, container, false);
        if (MA != null) userData = MA.userData;
        else userData = new ArrayList<>();
        Rv = binding.UserOnlineList;
        userAdapter = new UserOnlineAdapter(MA, userData);
        RvAdapter = userAdapter;
        RvManager = new LinearLayoutManager(this.getContext());
        Rv.setAdapter(RvAdapter);
        Rv.setLayoutManager(RvManager);



        Judul = binding.textviewSecond;

        return binding.getRoot();
    }

    private void setInputDialog(){

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(MA==null){
            this.getContext().startActivity(new Intent(this.getActivity(),MainActivity.class));
        }

        binding.RoomChatEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MA.SF.Init(MA,MA.messageData);
                MA.changeFragment(MA.SF);
            }
        });

        binding.Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.UsernameInput.setText(SPget.getString("username",""));
                if(binding.EditArea.getVisibility()==View.GONE){
                    binding.EditArea.setVisibility(View.VISIBLE);
                }else{
                    binding.EditArea.setVisibility(View.GONE);
                }
            }
        });

        binding.SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SP.putString("username",binding.UsernameInput.getText().toString());
                SP.apply();
                binding.EditArea.setVisibility(View.GONE);
            }
        });

        Judul.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                binding.IPInput.setText(SPget.getString("server_ip",""));
                if(binding.IPArea.getVisibility()==View.GONE){
                    binding.IPArea.setVisibility(View.VISIBLE);
                }else{
                    binding.IPArea.setVisibility(View.GONE);
                }
                return false;
            }
        });

        binding.SaveIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SP.putString("server_ip",binding.IPInput.getText().toString());
                SP.apply();
                binding.IPArea.setVisibility(View.GONE);
            }
        });

        binding.Broadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MA!=null){
                    binding.BroadcastArea.setAlpha(0.0f);
                    if(binding.BroadcastArea.getVisibility()==View.GONE)binding.BroadcastArea.setVisibility(View.VISIBLE);
                    else binding.BroadcastArea.setVisibility(View.GONE);
                    binding.BroadcastArea.animate().alpha(1.0f).translationX(1.0f).setListener(null);
                }
            }
        });

        binding.DoBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.BroadcastArea.setAlpha(1.0f);
                binding.BroadcastArea.animate().alpha(0.0f).translationX(1.0f).setListener(null);
                if(MA!=null){
                    MA.mainSocket.emit("broadcast",binding.BroadcastInput.getText().toString());
                    binding.BroadcastInput.setText("");
                }
                binding.BroadcastArea.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}