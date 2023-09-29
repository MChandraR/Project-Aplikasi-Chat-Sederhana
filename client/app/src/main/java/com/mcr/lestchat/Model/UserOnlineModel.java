package com.mcr.lestchat.Model;

public class UserOnlineModel {
    String username,lastmessage,token;
    boolean readed,isTyping;

    public UserOnlineModel(String username, String last,String token,boolean readed){
        isTyping = false;
        this.token = token;
        this.username = username;
        this.lastmessage = last;
        this.readed = readed;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    public String getUsername() {
        return username;
    }

    public String getLastmessage() {
        return lastmessage;
    }

    public String getToken(){
        return token;
    }

    public boolean isReaded() {
        return readed;
    }

    public void updateMessage(String msg){
        lastmessage = msg;
    }

    public void setReaded(boolean readed){
        this.readed = readed;
    }
}
