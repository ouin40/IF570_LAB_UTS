package com.example.if570_lab_uts_godwingilbertwoisiri_83560

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var storage: FirebaseStorage
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Disable the Check Out button initially
        findViewById<Button>(R.id.btn_out).isEnabled = false

        // Check for camera permission
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }

        findViewById<Button>(R.id.btn_absen).setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to take a picture",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Set up Check Out button
        findViewById<Button>(R.id.btn_out).setOnClickListener {
            updateAbsensi("checkOut") // Call the function to handle check-out
        }

        setupBottomNavigation()

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance().reference

        fetchUserName()

        val dateTextView: TextView = findViewById(R.id.dateTextView)
        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        dateTextView.text = currentDate
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun fetchUserName() {
        val uid = auth.currentUser?.uid ?: return
        val nameTextView = findViewById<TextView>(R.id.nama)
        database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "User"
                    nameTextView.text = "Halo, $name"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Gagal memuat nama pengguna",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val imageView: ImageView = findViewById(R.id.imageView)
            imageView.setImageBitmap(imageBitmap)

            val uid = auth.currentUser?.uid ?: return
            val storageRef =
                storage.reference.child("absensi/$uid/${System.currentTimeMillis()}.jpg")

            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageData = baos.toByteArray()

            storageRef.putBytes(imageData).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateAbsensi("checkIn", uri.toString())
                }
            }
        }
    }

    private fun updateAbsensi(action: String, imageUrl: String? = null) {
        val uid = auth.currentUser?.uid ?: return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val userAbsensiRef = database.child("absensi").child(uid)

        userAbsensiRef.orderByChild("date").equalTo(currentDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (action == "checkIn") {
                        if (snapshot.exists()) {
                            Toast.makeText(
                                this@HomeActivity,
                                "Anda sudah absen masuk hari ini.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val absensi =
                                Absensi(imageUrl!!, System.currentTimeMillis(), currentDate)
                            userAbsensiRef.push().setValue(absensi).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(
                                        this@HomeActivity,
                                        "Absen berhasil.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    findViewById<Button>(R.id.btn_out).isEnabled =
                                        true // Enable Check Out button
                                } else {
                                    Toast.makeText(
                                        this@HomeActivity,
                                        "Absen gagal.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else if (action == "checkOut") {
                        if (!snapshot.exists()) {
                            Toast.makeText(
                                this@HomeActivity,
                                "Anda harus absen masuk sebelum keluar.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            snapshot.children.forEach { childSnapshot ->
                                childSnapshot.ref.child("checkOutTime")
                                    .setValue(System.currentTimeMillis()).addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "Absen keluar berhasil.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "Absen keluar gagal.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Gagal memuat data absen.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@setOnItemSelectedListener true
                }

                else -> false
            }
        }
    }
}
