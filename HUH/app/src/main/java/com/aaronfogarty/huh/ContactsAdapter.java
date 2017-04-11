package com.aaronfogarty.huh;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.resource;

/**
 * Created by aaronfogartyfogarty on 06/03/2017.
 */

public class ContactsAdapter extends ArrayAdapter<Contact>{

    List<Contact> listItem;

    //    //String Array
//    public ContactsAdapter(Context context, String[] contacts) {
//        super(context, R.layout.contacts_list_row ,contacts);
//    }
    //List of Contacts
    public ContactsAdapter(Context context, List<Contact> contacts) {
        super(context, R.layout.contacts_list_row ,contacts);
        this.listItem = contacts;
    }

//    @NonNull
//    @Override
//    //for the input Arrays of strings, this is how I want the layout
//    public View getView(int position, View convertView, ViewGroup parent) {
//        //when you see inflate it means prepare for rendering
//        LayoutInflater contactsInflater = LayoutInflater.from(getContext());
//        View customView = contactsInflater.inflate(R.layout.contacts_list_row,parent,false);
//
//        String singleContact = getItem(position);
//        TextView textView = (TextView) customView.findViewById(R.id.contactRowText);
//        ImageView contactImage = (ImageView) customView.findViewById(R.id.contactsRowImage);
//
//        textView.setText(singleContact);
//        contactImage.setImageResource(R.drawable.goomba);
//
//        return customView;
//    }

    @NonNull
    @Override
    //for the input List<Contact>, this is how I want the layout
    public View getView(int position, View convertView, ViewGroup parent) {
        //when you see inflate it means prepare for rendering
        LayoutInflater contactsInflater = LayoutInflater.from(getContext());
        View customView = contactsInflater.inflate(R.layout.contacts_list_row,parent,false);

        TextView textView = (TextView) customView.findViewById(R.id.contactRowText);
        ImageView contactImage = (ImageView) customView.findViewById(R.id.contactsRowImage);

        textView.setText(listItem.get(position).getJid());
        contactImage.setImageResource(R.drawable.goomba);

        return customView;
    }
}