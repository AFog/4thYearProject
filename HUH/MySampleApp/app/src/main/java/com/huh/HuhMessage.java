package com.huh;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by aaronfogartyfogarty on 05/03/2017.
 */

public class HuhMessage implements Serializable {


    public String body, sender, receiver, senderName;
    public String Date, Time;
    public String msgid;
    public boolean isMine;// Did I send the message.


    public HuhMessage(String Sender, String Receiver, String messageString, boolean isMINE) {
        body = messageString;
        isMine = isMINE;
        sender = Sender;
       // msgid = ID;
        receiver = Receiver;
        senderName = sender;
    }

    public void setMsgID() {

        msgid += "-" + String.format("%02d", new Random().nextInt(100));

    }

}
