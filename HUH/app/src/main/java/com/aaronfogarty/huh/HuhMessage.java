package com.aaronfogarty.huh;

import java.io.Serializable;
import java.util.Random;

import static android.os.Build.ID;

/**
 * Created by aaronfogartyfogarty on 05/03/2017.
 */

public class HuhMessage implements  Serializable{


    private String body, sender, receiver, senderName;
    private String Date, Time;
    private String msgid;
    private boolean isMine;// Did I send the message.
    private long timestamp;

    public HuhMessage(String Sender, String Receiver, String messageString, boolean isMINE, long timestampIn) {
        body = messageString;
        isMine = isMINE;
        sender = Sender;
        // msgid = ID;
        receiver = Receiver;
        senderName = sender;
        timestamp = timestampIn;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMsgID() {

        msgid += "-" + String.format("%02d", new Random().nextInt(100));

    }

}
