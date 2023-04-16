package com.example.taller2cm

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ArrayAdapter
import com.example.taller2cm.databinding.ActivityContactsactivityBinding

class Contactsactivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactsactivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the list of contacts
        val contacts = getContacts()

        // Create an adapter to display the contacts in the ListView
        val adapter = ArrayAdapter<String>(
            this,
            R.layout.simple_list_item_1,
            contacts
        )

        // Set the adapter on the ListView
        binding.contactsListView.adapter = adapter
    }

    private fun getContacts(): List<String> {
        val contacts = mutableListOf<String>()

        // Query the contacts content provider for the contact ID and display name
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )

        // Iterate over the cursor and add the contact ID and display name to the list
        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
            val displayNameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            while (it.moveToNext()) {
                val id = if (idIndex >= 0) it.getString(idIndex) else ""
                val displayName = if (displayNameIndex >= 0) it.getString(displayNameIndex) else ""
                contacts.add("$id: $displayName")
            }
        }

        return contacts
    }

}