package com.aaronfogarty.huh;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class ContactsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);

        List<Contact> mContacts = new ArrayList<>();
        Contact contact1 = new Contact("shay@win-h6g4cdqot7e");
        mContacts.add(contact1);
        Contact contact2 = new Contact("dean@win-h6g4cdqot7e");
        mContacts.add(contact2);
        Contact contact3 = new Contact("aaron@win-h6g4cdqot7e");
        mContacts.add(contact3);

        String[] contacts = {"Aaron", "Shay", "Dean"};
        //converts list contact to list items, simple_list_item_1 is a basic format
        //ListAdapter contactsAdapter = new ArrayAdapter<Contact>(this, android.R.layout.simple_list_item_1, mContacts);
       // ListAdapter contactsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts);
        ListAdapter contactsAdapter = new ContactsAdapter(this,mContacts);
        ListView contactsListView = (ListView) findViewById(R.id.contactsListView);
        contactsListView.setAdapter(contactsAdapter);

//        contactsListView.setOnItemClickListener(
//
//                new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        String contact = String.valueOf(parent.getItemAtPosition(position));
//                        Toast.makeText(ContactsListActivity.this, contact, Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(ContactsListActivity.this
//                                ,ChatActivity.class);
//                       // intent.putExtra("EXTRA_CONTACT_JID",mContact.getJid());
//                        intent.putExtra("EXTRA_CONTACT_JID", "shay@win-h6g4cdqot7e");
//                        startActivity(intent);
//                    }
//                }
//        );

        contactsListView.setOnItemClickListener(

                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String contact = String.valueOf(parent.getItemAtPosition(position));
                        Toast.makeText(ContactsListActivity.this, contact, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ContactsListActivity.this
                                ,ChatActivity.class);
                        intent.putExtra("EXTRA_CONTACT_JID",contact);
                        intent.putExtra("EXTRA_CONTACT_JID", "shay@win-h6g4cdqot7e");
                        startActivity(intent);
                    }
                }
        );
    }
}