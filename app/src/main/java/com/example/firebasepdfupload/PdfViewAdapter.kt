package com.example.firebasepdfupload

import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class PdfViewAdapter(private val pdfFiles:List<PdfFile>,val listener:setOnclick):
RecyclerView.Adapter<PdfViewAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewAdapter.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.rowcontent,parent,false)
        return ViewHolder(view)
    }
    interface  setOnclick{
        fun onCLick(string: String, button: FloatingActionButton, btn2:ProgressBar):Boolean
        fun openPdf(fielPath:String)
    }

    override fun onBindViewHolder(holder: PdfViewAdapter.ViewHolder, position: Int) {
        val pdfFile = pdfFiles[position]
        holder.tv.text = pdfFile.name

        if (pdfFile.downloaded) {
            holder.btn.visibility = View.GONE
            holder.tv.setOnClickListener {
                val externalDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val filePath = File(externalDir, "${pdfFile.name}").absolutePath

                listener.openPdf(filePath)
            }
        } else {
            holder.btn.setOnClickListener {
                val updated = listener.onCLick(pdfFile.name, holder.btn, holder.btn2)
                if (updated) {
                    pdfFile.downloaded = true
                    notifyDataSetChanged() // Notify the adapter to update the UI
                }
            }
        }
    }

    override fun getItemCount(): Int {
      return pdfFiles.size
    }
     class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
      val tv:TextView=itemView.findViewById(R.id.tvPdfName)
         val btn=itemView.findViewById<FloatingActionButton>(R.id.btnDownload)
         val btn2=itemView.findViewById<ProgressBar>(R.id.progressBar)

    }
}