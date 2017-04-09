package com.huh;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mysampleapp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactListActivity extends AppCompatActivity {

    private static final String TAG = "ContactListActivity";

    private RecyclerView contactsRecyclerView;
    private ContactAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        contactsRecyclerView = (RecyclerView) findViewById(R.id.contact_list_recycler_view);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

//        ContactModel model = ContactModel.get(getBaseContext());
//        List<Contact> contacts = model.getContacts();

  //      ContactModel model = ContactModel.get(getBaseContext());
          List<Contact> contacts = getPhoneContacts();

        mAdapter = new ContactAdapter(contacts);
        contactsRecyclerView.setAdapter(mAdapter);
    }

    public List<Contact> getPhoneContacts(){
        ArrayList<RosterContact> phoneContacts = new ArrayList<>();
        List<Contact> contacts = new ArrayList<>();

        // load Phone Contacts from preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            phoneContacts = (ArrayList<RosterContact>) ObjectSerializer.deserialize(prefs.getString("huhContacts", ObjectSerializer.serialize(new ArrayList<RosterContact>())));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        for (RosterContact c:phoneContacts) {
            Contact temp = new Contact(c.getphoneNumber());
            contacts.add(temp);
            Log.d(TAG, "returned roster contacts: \nUser: " + c.getJid() + "\nPhoneNumber: " + c.getphoneNumber());
        }
        return contacts;
    }

    //innner class
    private class ContactHolder extends RecyclerView.ViewHolder
    {
        private TextView contactTextView;
        private Contact mContact;
        public ContactHolder (View itemView)
        {
            super(itemView);

            contactTextView = (TextView) itemView.findViewById(R.id.contact_jid);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Inside here we start the chat activity
                    Intent intent = new Intent(ContactListActivity.this
                            ,ChatActivity.class);
                    intent.putExtra("EXTRA_CONTACT_JID",mContact.getJid());
                    startActivity(intent);
                }
            });
        }


        public void bindContact( Contact contact)
        {
            mContact = contact;
            if (mContact == null)
            {
                Log.d(TAG,"Trying to work on a null Contact object ,returning.");
               // Toast.makeText(getApplicationContext(), "HuhConnection Constructor called", Toast.LENGTH_LONG).show();

                return;
            }
            contactTextView.setText(mContact.getJid());

        }
    }

//innner class
    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder>
    {
        private List<Contact> mContacts;

        public ContactAdapter( List<Contact> contactList)
        {

            mContacts = contactList;
        }

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater
                    .inflate(R.layout.list_item_contact, parent,
                            false);
            return new ContactHolder(view);
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            Contact contact = mContacts.get(position);
            holder.bindContact(contact);

        }

        @Override
        public int getItemCount() {
            return mContacts.size();
        }
    }

}