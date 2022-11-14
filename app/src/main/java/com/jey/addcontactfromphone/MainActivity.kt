package com.jey.addcontactfromphone


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val contactsList = ArrayList<Contact>()
    private lateinit var contactAdapter: ContactAdapter


    var cols = listOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
    ).toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()

        contact_sv.clearFocus()
        contact_sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                fillerList(newText)
                return true
            }
        })

        contact_rv.layoutManager = LinearLayoutManager(this)
        contactAdapter = ContactAdapter(contactsList, onContactClick())
        contact_rv.adapter = contactAdapter
    }


    private fun fillerList(text: String) {
        val filteredList = ArrayList<Contact>()
        for (contact in contactsList) {
            if (contact.displayName.lowercase().contains(text.lowercase())) {
                filteredList.add(contact)
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No contact found", Toast.LENGTH_SHORT).show()
            filteredList.removeAll(filteredList.toSet())
            contactAdapter.setFilteredList(filteredList)
        } else {
            contactAdapter.setFilteredList(filteredList)
        }
    }


    private fun onContactClick(): (Contact) -> Unit = {

        sendInviteByWhatsApp(it.phoneNumber)
    }


    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getAllContacts()
            }
        }

    private fun checkPermission() {
        val isPermissionAlreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        )
        if (isPermissionAlreadyGranted == PackageManager.PERMISSION_GRANTED) {
            getAllContacts()
        } else {
            val needToExplainThePermission =
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_CONTACTS
                )
            if (needToExplainThePermission) {
                Toast.makeText(
                    this,
                    "We need your permission to find contact for you",
                    Toast.LENGTH_SHORT
                ).show()
                requestPermission.launch(Manifest.permission.READ_CONTACTS)
            } else {
                requestPermission.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    @SuppressLint("Range")
    fun getAllContacts() {

        val rs = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            cols,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )

        while (rs!!.moveToNext()) {
            val name =
                rs.getString(rs.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phone =
                rs.getString(rs.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val newContact = Contact(name, phone)
            contactsList.add(newContact)
        }


    }


    private fun appInstalledOrNot(url: String): Boolean {
        val appInstalled: Boolean = try {
            packageManager.getPackageInfo(url, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return appInstalled
    }

    private fun sendInviteByWhatsApp(phoneNumber: String) {
        val url = "https://api.whatsapp.com/send?phone="
        val message =
            "Hey, i wanna invite you to join the best car selling app ! download from PlayStore - LINK"

        if (appInstalledOrNot("com.whatsapp")) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("$url${phoneNumber}&text=$message")
            startActivity(intent)
        } else {
            Toast.makeText(this, "Whats app not installed on your device", Toast.LENGTH_SHORT)
                .show();
        }
    }
}

