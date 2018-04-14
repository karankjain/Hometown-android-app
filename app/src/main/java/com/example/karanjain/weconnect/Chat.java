package com.example.karanjain.weconnect;

import java.util.Date;

/**
 * Created by karanjain on 4/12/17.
 */

public class Chat {

    private String chatText, chatUser;
    private long chatTime;

    public Chat(String chatText, String chatUser) {
        this.chatUser = chatUser;
        this.chatText = chatText;
        chatTime = new Date().getTime();
    }

    public Chat(){

    }

    public String getChatUser() {
        return chatUser;
    }

    public void setChatUser(String chatUser) {
        this.chatUser = chatUser;
    }

    public String getChatText() {
        return chatText;
    }

    public void setChatText(String chatText) {
        this.chatText = chatText;
    }

    public long getChatTime() {
        return chatTime;
    }

    public void setChatTime(long chatTime) {
        this.chatTime = chatTime;
    }
}
