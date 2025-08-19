package com.example.transtools.api.adapter

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.transtools.R
import com.example.transtools.api.model.TarikBarangModel
import com.google.gson.Gson
import java.text.ParseException

class TarikBarangAdapter(private var itemList: List<TarikBarangModel>) :
    RecyclerView.Adapter<TarikBarangAdapter.ViewHolder>() {

    // Interface untuk listener klik item
    interface OnItemClickListener {
        fun onItemClick(item: TarikBarangModel)
    }

    private var itemClickListener: OnItemClickListener? = null

    // Fungsi untuk mengatur listener klik item
    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_barang, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setData(newList: List<TarikBarangModel>) {
        itemList = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.name)
        private val tanggalTextView: TextView = itemView.findViewById(R.id.tanggal)
        private val noGondolaTextView: TextView = itemView.findViewById(R.id.noGondola)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusItem)
        private val hari: TextView = itemView.findViewById(R.id.hari)

        @RequiresApi(Build.VERSION_CODES.N)
        fun bind(item: TarikBarangModel) {
            nameTextView.text = item.ieItemName
            hari.text = item.remainingDays.toString()
            noGondolaTextView.text = item.ieGondolaNo.toString()
            statusTextView.text = item.ieItemStatus
//            statusTextView.text = if (item.ieItemStatus == "y") "returnable" else "non-returnable"

            // Format tanggal sesuai dengan "yyyy/MM/dd"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd")
            val outputFormat = SimpleDateFormat("yyyy/MM/dd")
            try {
                val date = inputFormat.parse(item.ieExpiredDate)
                val formattedDate = outputFormat.format(date)
                tanggalTextView.text = formattedDate
            } catch (e: ParseException) {
                e.printStackTrace()
                // Handle jika terjadi kesalahan parsing tanggal
            }

            // Menambahkan listener klik item
            itemView.setOnClickListener {
                itemClickListener?.onItemClick(item)

                val gson = Gson()
                val jsonString = gson.toJson(item)

                // Simpan item ke SharedPreferences dengan nama "item"
                val sharedPreferences = itemView.context.getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("item", jsonString) // Ubah item.toString() sesuai dengan representasi data item Anda
                editor.apply()
            }
        }
    }
}
