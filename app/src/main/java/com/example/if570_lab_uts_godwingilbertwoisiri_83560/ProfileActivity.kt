package com.example.if570_lab_uts_godwingilbertwoisiri_83560

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    lateinit var database: DatabaseReference
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupBottomNavigation()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val nameField = findViewById<EditText>(R.id.name)
        val nimField = findViewById<EditText>(R.id.nim)
        val save = findViewById<Button>(R.id.btn_save)
        val logoutButton: Button = findViewById(R.id.btn_logout)
        logoutButton.setOnClickListener {
            logout()
        }

        fetchUserName()

        save.setOnClickListener {
            val name = nameField.text.toString()
            val nim = nimField.text.toString()

            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val user = User(name, nim)

            if (name.isEmpty() || nim.isEmpty()) {
                Toast.makeText(this, "Nama dan NIM tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            database.child("users").child(uid).setValue(user).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("ProfileActivity", "Data saved successfully for UID: $uid")
                    Toast.makeText(this, "Datanya kesimpen cuy", Toast.LENGTH_SHORT).show()

                    // Call fetchUserName to refresh the name
                    fetchUserName()

                    // Optionally, you can navigate back to HomeActivity
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    Log.e("ProfileActivity", "Failed to save data: ${it.exception}")
                    Toast.makeText(this, "Gagal simpen data cuy", Toast.LENGTH_SHORT).show()
                }
            }



        }

    }

    private fun fetchUserName() {
        val uid = auth.currentUser?.uid ?: return
        val nameTextView = findViewById<TextView>(R.id.nama)
        Log.d("ProfileActivity", "Fetching name for UID: $uid")

        database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java)
                        Log.d("ProfileActivity", "Fetched name: $name") // Log nama yang diambil
                        nameTextView.text = "$name"
                    } else {
                        Log.d("ProfileActivity", "No data found for UID: $uid")
                        nameTextView.text = "User"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileActivity", "Database error: ${error.message}")
                    Toast.makeText(
                        this@ProfileActivity,
                        "Gagal memuat nama pengguna",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Redirect to LoginActivity or MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Close ProfileActivity
    }


    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_profile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }

                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }

                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}

data class User(val name: String, val nim: String)