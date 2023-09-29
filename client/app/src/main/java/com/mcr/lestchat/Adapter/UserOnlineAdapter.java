package com.mcr.lestchat.Adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mcr.lestchat.MainActivity;
import com.mcr.lestchat.Model.UserOnlineModel;
import com.mcr.lestchat.R;
import com.mcr.lestchat.ThirdFragment;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserOnlineAdapter extends  RecyclerView.Adapter<UserOnlineAdapter.UserOnlineHolder> {
    private ArrayList<UserOnlineModel> userData;
    private MainActivity MA;

    public UserOnlineAdapter(MainActivity MA, ArrayList<UserOnlineModel> userData){
        this.userData = userData;
        this.MA = MA;
    }

    public void addData(String username,String token){
        boolean found = false;
        for(UserOnlineModel udata : userData){
            if(udata.getUsername().equals(username)){
                found = true;
                break;
            }
        }
        if(!found){
            userData.add(new UserOnlineModel(username,"...",token,true));
            this.notifyDataSetChanged();
        }
    }
    public void removeUser(String user){
        for(int i=0;i<userData.size();i++){
            if(userData.get(i).getUsername().equals(user)){
                userData.remove(i);
                break;
            }
        }
        this.notifyDataSetChanged();
    }
RecyclerView Rv;
    public void setStatus(String username,boolean status){
        for(int i=0;i<userData.size();i++){
            if(userData.get(i).getUsername().equals(username)){
                userData.get(i).setTyping(status);
                this.notifyItemChanged(i);
                break;
            }
        }
    }

    public void updateUser(String user,String msg){
        for(int i=0;i<userData.size();i++){
            if(userData.get(i).getUsername().equals(user)){
                userData.get(i).updateMessage(msg);
                userData.get(i).setReaded(false);
                break;
            }
        }this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserOnlineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserOnlineHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.userlistview,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserOnlineHolder holder, int position) {
        holder.itemView.setAlpha(0.0f);
        holder.itemView.animate().alpha(1.0f).translationX(1.0f).setListener(null);
        UserOnlineModel current = userData.get(position);
        holder.NewMessageIcon.setVisibility(View.INVISIBLE);
        if(!current.isReaded()) holder.NewMessageIcon.setVisibility(View.VISIBLE);
        Picasso.get().load(MA.getValidUrl()+current.getUsername()+".png").networkPolicy(NetworkPolicy.NO_CACHE).into(holder.UserImage);
        holder.Username.setText(current.getUsername());
        holder.LastMessage.setTextColor(ColorStateList.valueOf(Color.parseColor("#757575")));
        if(current.isTyping()){
            holder.LastMessage.setTextColor(ColorStateList.valueOf(Color.parseColor("#85F812")));
            holder.LastMessage.setText("mengetik...");
        }
        else if(!current.getLastmessage().isEmpty()) holder.LastMessage.setText(current.getLastmessage());
        holder.userParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(MA != null && MA.TF != null){
                    if(MA.personalChatData.get(current.getUsername()) == null) {
                        MA.personalChatData.put(current.getUsername(),new ArrayList<>()) ;
                    }current.setReaded(true);
                    MA.TF.setData(MA,current.getUsername(), current.getToken(),MA.personalChatData.get(current.getUsername()));
                    MA.changeFragment(MA.TF);
                }
            }
        });
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull UserOnlineHolder holder) {
        holder.itemView.setAlpha(1.0f);
        holder.itemView.animate().alpha(0.0f).translationX(1.0f).setListener(null);
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public int getItemCount() {
        return userData.size();
    }

    public class UserOnlineHolder extends RecyclerView.ViewHolder {
        CircleImageView UserImage,NewMessageIcon;
        LinearLayout userParent;
        TextView Username,LastMessage;
        public UserOnlineHolder(@NonNull View itemView) {
            super(itemView);
            UserImage = itemView.findViewById(R.id.UserImage);
            NewMessageIcon = itemView.findViewById(R.id.NewMessageIcon);
            Username = itemView.findViewById(R.id.Username);
            LastMessage = itemView.findViewById(R.id.LastMessage);
            userParent = itemView.findViewById(R.id.UserListParent);
        }
    }
}
