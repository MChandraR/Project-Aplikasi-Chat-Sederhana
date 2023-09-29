package com.mcr.lestchat.Model;

public class MessageModel {
    String sender,message,time;
    int model; boolean wasread;

    public MessageModel(String sender,String message,int model,String time,boolean isRead){
        this.sender = sender;
        this.message = message;
        this.model = model;
        this.time = time;
    }

    public boolean isWasread() {
        return wasread;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public int getModel(){
        return  model;
    }

    public String getTime(){
        return time;
    }
}
