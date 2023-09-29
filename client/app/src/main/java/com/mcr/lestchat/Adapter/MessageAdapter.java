package com.mcr.lestchat.Adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mcr.lestchat.MainActivity;
import com.mcr.lestchat.Model.MessageModel;
import com.mcr.lestchat.R;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {
    ArrayList<MessageModel> messageData;
    RecyclerView RView;
    MainActivity MA;

    public MessageAdapter(MainActivity MA,RecyclerView view,ArrayList<MessageModel> data){
        messageData = new ArrayList<>();
        RView = view;
        messageData = data;
        this.MA = MA;
    }

    public void addData(MessageModel data){
        messageData.add(data);
        this.notifyItemChanged(messageData.size()-1);
        RView.scrollToPosition(messageData.size()-1);
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messagemodellayout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        MessageModel cur = messageData.get(position);
        if(cur.getModel()==2){
            int enter = cur.getMessage().indexOf("bergabung.");
            if(enter > -1){
                holder.UserJoined.setTextColor(ColorStateList.valueOf(Color.parseColor("#64B50E")));
            }else holder.UserJoined.setTextColor(ColorStateList.valueOf(Color.parseColor("#F44336")));

            holder.MsgModel0.setVisibility(View.GONE);
            holder.MsgModel1.setVisibility(View.GONE);
            holder.MsgModel2.setVisibility(View.VISIBLE);
            holder.UserJoined.setText(cur.getMessage());
            holder.itemView.setAlpha(0.0f);
            holder.itemView.animate().alpha(1.0f).translationX(1.0f).setListener(null);
            return;
        }

        if(cur.getModel()==1){
            holder.MsgModel1.setVisibility(View.VISIBLE);
            holder.MsgModel0.setVisibility(View.GONE);
            holder.MsgModel2.setVisibility(View.GONE);
            holder.SenderMe.setText(cur.getSender());
            holder.MessageMe.setText(cur.getMessage());
            holder.TimeMe.setText(cur.getTime());
            holder.itemView.setAlpha(0.0f);
            holder.itemView.animate().alpha(1.0f).translationX(1.0f).setListener(null);
            return;
        }
        holder.MsgModel1.setVisibility(View.GONE);
        holder.MsgModel2.setVisibility(View.GONE);
        holder.MsgModel0.setVisibility(View.VISIBLE);
        holder.Sender.setText(cur.getSender());
        holder.Message.setText(cur.getMessage());
        holder.Time.setText(cur.getTime());
        holder.itemView.setAlpha(0.0f);
        holder.itemView.animate().alpha(1.0f).translationX(1.0f).setListener(null);
    }

    @Override
    public int getItemCount() {
        return messageData.size();
    }

    public class MessageHolder extends RecyclerView.ViewHolder{
        TextView Sender,Message,Time,SenderMe,MessageMe,TimeMe,UserJoined;
        LinearLayout MsgModel0,MsgModel1,MsgModel2;
        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            Sender = itemView.findViewById(R.id.Sender);
            Message = itemView.findViewById(R.id.Message);
            Time = itemView.findViewById(R.id.Time);
            SenderMe = itemView.findViewById(R.id.Me);
            MessageMe = itemView.findViewById(R.id.Messages);
            TimeMe = itemView.findViewById(R.id.TimeMe);
            MsgModel0 = itemView.findViewById(R.id.MessageModel0);
            MsgModel1 = itemView.findViewById(R.id.MessageModel1);
            MsgModel2 = itemView.findViewById(R.id.MessageModel2);
            UserJoined = itemView.findViewById(R.id.UserJoinedMessage);
        }
    }
}
