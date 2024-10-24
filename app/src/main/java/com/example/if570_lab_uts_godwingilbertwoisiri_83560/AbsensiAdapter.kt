package com.example.if570_lab_uts_godwingilbertwoisiri_83560

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AbsensiAdapter(private val absensiList: List<Absensi>) :
    RecyclerView.Adapter<AbsensiAdapter.AbsensiViewHolder>() {

    class AbsensiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val absensiImage: ImageView = itemView.findViewById(R.id.absensiImage)
        val dateView: TextView = itemView.findViewById(R.id.dateView) // New date TextView
        val timestampView: TextView = itemView.findViewById(R.id.timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsensiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_absensi, parent, false)
        return AbsensiViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsensiViewHolder, position: Int) {
        val absensi = absensiList[position]
        Glide.with(holder.itemView.context).load(absensi.imageUrl).into(holder.absensiImage)

        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val formattedDate = dateFormat.format(Date(absensi.timestamp))
        val formattedTime = timeFormat.format(Date(absensi.timestamp))

        holder.dateView.text = formattedDate
        holder.timestampView.text = "Time: $formattedTime"
    }

    override fun getItemCount(): Int {
        return absensiList.size
    }
}
