package com.example.firebasepdfupload

data class PdfFile(
    val name: String,
    val downloadUrl: String,

    var downloaded: Boolean = false

)
