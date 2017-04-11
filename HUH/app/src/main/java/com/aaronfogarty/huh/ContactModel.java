package com.aaronfogarty.huh;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by aaronfogartyfogarty on 15/02/2017.
 */

public class ContactModel {

    private static ContactModel sContactModel;
    private List<Contact> mContacts;
    Context mContext;
    String TAG = "ContactModel()";

    public static ContactModel get(Context context)
    {
        if(sContactModel == null)
        {
            sContactModel = new ContactModel(context);
        }
        return  sContactModel;
    }

    private ContactModel(Context context)
    {
        mContacts = new ArrayList<>();
        populateWithInitialContacts(context);
        //getPhoneContacts();

    }

    private void populateWithInitialContacts(Context context)
    {
        //Create the contacts and add them to the list;


        Contact contact1 = new Contact("tom@win-h6g4cdqot7e");
        mContacts.add(contact1);
        Contact contact2 = new Contact("dean@win-h6g4cdqot7e");
        mContacts.add(contact2);
        Contact contact3 = new Contact("aaron@win-h6g4cdqot7e");
        mContacts.add(contact3);
        Contact contact4 = new Contact("0851329485@win-h6g4cdqot7e");
        mContacts.add(contact4);
        Contact contact5 = new Contact("0872126887@win-h6g4cdqot7e");
        mContacts.add(contact5);
        Contact contact6 = new Contact("0872544889@win-h6g4cdqot7e");
        mContacts.add(contact6);
//        Contact contact7 = new Contact("shay@win-h6g4cdqot7e/Huh");
//        mContacts.add(contact7);
    }

    public List<Contact> getContacts()
    {
        return mContacts;
    }
}