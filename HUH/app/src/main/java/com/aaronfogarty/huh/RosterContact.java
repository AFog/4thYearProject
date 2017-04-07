package com.aaronfogarty.huh;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by aaronfogartyfogarty on 03/04/2017.
 */

public class RosterContact implements Serializable{

    private String jid;

    private String phoneNumber;

    public RosterContact(String contactJid, String PhonNumber )
    {
        jid = contactJid;
        phoneNumber = PhonNumber;
    }

    public String getJid()
    {
        return jid;
    }

    public String getphoneNumber()
    {
        return phoneNumber;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public void setphoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(21, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                        append(phoneNumber).
                        toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RosterContact))
            return false;
        if (obj == this)
            return true;

        RosterContact rhs = (RosterContact) obj;
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                        append(phoneNumber, rhs.phoneNumber).isEquals();
    }

}
