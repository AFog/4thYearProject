package com.aaronfogarty.huh;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class ContactsListActivity extends AppCompatActivity {

    ContactModel mContactModel;
    private Context mApplicationContext;
    private static final String TAG = "ContactsListActivity AA";
    private List<RosterContact> huhContacts;
    private ArrayList<String> contacts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);
        contacts = new ArrayList<>();
        createHuhContacts();

        List<Contact> mContacts = new ArrayList<>();
        Contact contact1 = new Contact("shay@win-h6g4cdqot7e");
        mContacts.add(contact1);
        Contact contact2 = new Contact("dean@win-h6g4cdqot7e");
        mContacts.add(contact2);
        Contact contact3 = new Contact("aaron@win-h6g4cdqot7e");
        mContacts.add(contact3);


        for (RosterContact c: huhContacts) {
            Log.d(TAG, "huhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());
            contacts.add(c.getJid());
        }


        //String[] contacts = {"Aaron", "Shay", "Dean"};
        //converts list contact to list items, simple_list_item_1 is a basic format
        //ListAdapter contactsAdapter = new ArrayAdapter<Contact>(this, android.R.layout.simple_list_item_1, mContacts);
        //ListAdapter contactsAdapter = new ContactsAdapter(this,mContacts);

//        ListAdapter contactsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts);
//        ListView contactsListView = (ListView) findViewById(R.id.contactsListView);
//        contactsListView.setAdapter(contactsAdapter);
//
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

        //converts contacts array into list item
        ListAdapter contactsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, contacts);
        ListView languagelistview = (ListView) findViewById(R.id.contactsListView);
        languagelistview.setAdapter(contactsAdapter);

        languagelistview.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String contactSelected = String.valueOf(parent.getItemAtPosition(position));
                        ///
                        Toast.makeText(ContactsListActivity.this, contactSelected, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ContactsListActivity.this
                                ,ChatActivity.class);
                        //intent.putExtra("EXTRA_CONTACT_JID",contact);
                        String jid = huhContacts.get(position).getphoneNumber() + "@win-h6g4cdqot7e";
                        String displayName = huhContacts.get(position).getJid() + "@win-h6g4cdqot7e";

                        Toast.makeText(ContactsListActivity.this, jid, Toast.LENGTH_SHORT).show();

                        intent.putExtra("EXTRA_CONTACT_JID", jid);
                        intent.putExtra("EXTRA_CONTACT_DISPLAY", displayName);
                        startActivity(intent);
                    }
                }
        );

//        contactsListView.setOnItemClickListener(
//
//                new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        String contact = String.valueOf(parent.getItemAtPosition(position));
//                        Toast.makeText(ContactsListActivity.this, contact, Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(ContactsListActivity.this
//                                ,ChatActivity.class);
//                        //intent.putExtra("EXTRA_CONTACT_JID",contact);
//                        intent.putExtra("EXTRA_CONTACT_JID", "shay@win-h6g4cdqot7e");
//                        startActivity(intent);
//                    }
//                }
//        );
    }

    public void createHuhContacts(){
        Log.d(TAG, "createHuhContacts()");

        huhContacts = new ArrayList<>();

        // load huh Contacts from preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            huhContacts = (ArrayList<RosterContact>) ObjectSerializer.deserialize(prefs.getString("huhContacts", ObjectSerializer.serialize(new ArrayList<RosterContact>())));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (RosterContact c: huhContacts) {
            Log.d(TAG, "huhContacts: Phone" + c.getphoneNumber() + " Name: " + c.getJid());
        }
    }

}