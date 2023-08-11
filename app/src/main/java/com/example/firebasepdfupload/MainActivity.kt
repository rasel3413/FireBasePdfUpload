package com.example.firebasepdfupload

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity() {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

private lateinit var progressBar:ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressBar=findViewById(R.id.progressBar)

        val btnSHow=findViewById<Button>(R.id.btnShow)
        btnSHow.setOnClickListener {

            val intent=Intent(this,PdfView::class.java)
            startActivity(intent)
        }
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // The user selected a PDF file.
                val pdfUri = result.data?.data
                if (pdfUri != null) {
                    uploadPDFToFirebase(pdfUri)
                }
            }
        }
        val button = findViewById<Button>(R.id.upload)
        button.setOnClickListener {
            choosePDFFile()
        }


    }

    private fun choosePDFFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        activityResultLauncher.launch(intent)
    }

    private fun uploadPDFToFirebase(pdfUri: Uri) {
        progressBar.visibility = View.VISIBLE
        val storageRef = FirebaseStorage.getInstance().getReference("pdfs")
        val originalFileName = getOriginalFileName(pdfUri)
        val pdfRef = storageRef.child(originalFileName)

        pdfRef.putFile(pdfUri).addOnProgressListener{taskSnapshot ->

            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            Log.d("UPLOAD_PROGRESS", "Progress: $progress")
            progressBar.progress = progress
        }.addOnSuccessListener {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Uploade successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Failed to upload", Toast.LENGTH_SHORT).show()
        }
    }
    @SuppressLint("Range")
    private fun getOriginalFileName(uri: Uri): String {
        val contentResolver = contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName = it.getString(it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME))
                if (displayName != null) {
                    return displayName
                }
            }
        }
        // If the display name is not available, use a default name
        return "file.pdf"
    }

}