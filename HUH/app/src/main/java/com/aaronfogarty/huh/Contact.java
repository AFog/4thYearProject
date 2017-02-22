package com.aaronfogarty.huh;

/**
 * Created by aaronfogartyfogarty on 15/02/2017.
 */

public class Contact {

    private String jid;

    public Contact(String contactJid )
    {
        jid = contactJid;
    }

    public String getJid()
    {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

}
