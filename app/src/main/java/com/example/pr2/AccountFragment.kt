package com.example.pr2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream

class AccountFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var etAccountName: EditText
    private lateinit var etAccountAddress: EditText
    private lateinit var etAccountEmail: EditText
    private lateinit var etAccountContact: EditText
    private lateinit var etAccountDOB: EditText
    private lateinit var btnSaveProfile: Button

    private lateinit var dbHelper: DatabaseHelper
    private var currentImageBase64: String? = null

    // Fixed Camera Launcher using modern ActivityResultContracts
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            profileImage.setImageBitmap(bitmap)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            currentImageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
            saveProfileData()
            Toast.makeText(requireContext(), "Photo captured & saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_account,
            container,
            false
        )

        dbHelper = DatabaseHelper(requireContext())

        profileImage = view.findViewById(R.id.profileImage)
        etAccountName = view.findViewById(R.id.etAccountName)
        etAccountAddress = view.findViewById(R.id.etAccountAddress)
        etAccountEmail = view.findViewById(R.id.etAccountEmail)
        etAccountContact = view.findViewById(R.id.etAccountContact)
        etAccountDOB = view.findViewById(R.id.etAccountDOB)
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)

        val pictureButton = view.findViewById<Button>(R.id.pictureButton)

        pictureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePictureLauncher.launch(null)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        btnSaveProfile.setOnClickListener {
            saveProfileData()
            Toast.makeText(requireContext(), "Account details saved to SQLite DB!", Toast.LENGTH_SHORT).show()
        }

        loadProfileData()

        return view
    }

    override fun onPause() {
        super.onPause()
        saveProfileData()
    }

    private fun loadProfileData() {
        if (!isAdded) return
        val pref = requireContext().getSharedPreferences("JournalData", Context.MODE_PRIVATE)
        val activeEmail = pref.getString("email", "") ?: ""

        if (activeEmail.isNotEmpty()) {
            val cv = dbHelper.getUserProfile(activeEmail)
            if (cv != null) {
                etAccountName.setText(cv.getAsString(DatabaseHelper.COL_USER_NAME) ?: "")
                etAccountEmail.setText(cv.getAsString(DatabaseHelper.COL_USER_EMAIL) ?: activeEmail)
                etAccountContact.setText(cv.getAsString(DatabaseHelper.COL_USER_PHONE) ?: "")
                etAccountAddress.setText(cv.getAsString(DatabaseHelper.COL_USER_ADDRESS) ?: "")
                etAccountDOB.setText(cv.getAsString(DatabaseHelper.COL_USER_DOB) ?: "")

                val imgStr = cv.getAsString(DatabaseHelper.COL_USER_IMAGE)
                if (!imgStr.isNullOrEmpty()) {
                    currentImageBase64 = imgStr
                    try {
                        val decodedBytes = Base64.decode(imgStr, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        if (bitmap != null) {
                            profileImage.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun saveProfileData() {
        if (!isAdded) return
        val pref = requireContext().getSharedPreferences("JournalData", Context.MODE_PRIVATE)
        val activeEmail = pref.getString("email", "") ?: etAccountEmail.text.toString().trim()

        if (activeEmail.isNotEmpty()) {
            val name = etAccountName.text.toString().trim()
            val address = etAccountAddress.text.toString().trim()
            val phone = etAccountContact.text.toString().trim()
            val dob = etAccountDOB.text.toString().trim()

            dbHelper.updateUserProfile(activeEmail, name, address, phone, dob, currentImageBase64)
        }
    }
}