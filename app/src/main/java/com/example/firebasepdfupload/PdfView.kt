package com.example.firebasepdfupload

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.storage.BuildConfig
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class PdfView : AppCompatActivity(), PdfViewAdapter.setOnclick {
    private lateinit var storageRef: FirebaseStorage
    private lateinit var pdfAdapter:PdfViewAdapter
    private var pdfFIles = mutableListOf<PdfFile>()
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            it?.let {
                if (it) {
                    Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()

                }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)

        storageRef = FirebaseStorage.getInstance()
        val storage = storageRef.getReference("pdfs")
        storage.listAll().addOnSuccessListener { listResult ->

            for (item in listResult.items) {
                val name = item.name
                item.downloadUrl.addOnSuccessListener { uri ->

                    val downloadUri = uri.toString()
                    val pdfFile = PdfFile(name, downloadUri)
                    val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val tempFile = File(externalDir, "$name")
                    if (tempFile.exists()) {
                        pdfFile.downloaded = true
                    }
                    pdfFIles.add(pdfFile)
                    val recycle = findViewById<RecyclerView>(R.id.mrecyler)
                    recycle.setHasFixedSize(true)
                    recycle.layoutManager = LinearLayoutManager(this)
                     pdfAdapter = PdfViewAdapter(pdfFIles,this)
                    recycle.adapter = pdfAdapter
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed1", Toast.LENGTH_SHORT).show()
                }
            }


        }.addOnFailureListener {
            Toast.makeText(this, "Failed2", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCLick(string: String, btn: FloatingActionButton, btn2: ProgressBar): Boolean {
        btn2.visibility = View.VISIBLE
        btn.visibility = View.GONE
        var download: Boolean = false
        val storage = storageRef.reference.child("pdfs/$string")
        val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val tempFile = File(externalDir, "$string")

        storage.getFile(tempFile)
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                btn2.progress = progress
            }
            .addOnSuccessListener {
                if (tempFile.exists()) {
                    download = true
                }
                Toast.makeText(this, "Successfully Downloaded", Toast.LENGTH_SHORT).show()
                btn.visibility = View.GONE
                btn2.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to download", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                // Notify the adapter that the download status has changed
                if (download) {
                    val pdfFile = pdfFIles.find { it.name == string }
                    pdfFile?.downloaded = true
                    pdfAdapter.notifyDataSetChanged() // Update the RecyclerView
                }
            }

        return download
    }

    override fun openPdf(filePath: String) {
        // Get the file uri from the FileProvider
        val uri = FileProvider.getUriForFile(this, "com.example.firebasepdfupload.fileProvider", File(filePath))

        // Create an intent to open the PDF
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Start the activity
        this.startActivity(intent)
    }




}