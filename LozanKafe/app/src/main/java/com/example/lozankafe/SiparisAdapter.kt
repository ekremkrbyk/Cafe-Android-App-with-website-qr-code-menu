package com.example.lozankafe

import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lozankafe.Models.Siparis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SiparisAdapter() : ListAdapter<Siparis, SiparisAdapter.SiparisViewHolder>(DiffCallback()),
    Parcelable {

    class SiparisViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(siparis: Siparis) {
            view.findViewById<TextView>(R.id.txtUrunAdi).text = siparis.urunAdi
            view.findViewById<TextView>(R.id.txtUrunFiyatı).text = "Fiyat: ${siparis.urunFiyati}₺"
            view.findViewById<TextView>(R.id.txtTutar).text = "Toplam: ${siparis.toplamTutar}₺"
            view.findViewById<TextView>(R.id.txtTarih).text =
                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(siparis.tarih))
            println("siparis: " +siparis.tarih)
            println("sistem tarihi: " +System.currentTimeMillis())
            if (siparis.tarih < System.currentTimeMillis()){
                //view.findViewById<ImageView>(R.id.sipDurum).setBackgroundColor(Color.Red)
                println("if bloğuna girdi")
            }
        }
    }

    constructor(parcel: Parcel) : this() {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiparisViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_siparis, parent, false)
        return SiparisViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiparisViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Siparis>() {
        override fun areItemsTheSame(old: Siparis, new: Siparis) = old.id == new.id
        override fun areContentsTheSame(old: Siparis, new: Siparis) = old == new
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SiparisAdapter> {
        override fun createFromParcel(parcel: Parcel): SiparisAdapter {
            return SiparisAdapter(parcel)
        }

        override fun newArray(size: Int): Array<SiparisAdapter?> {
            return arrayOfNulls(size)
        }
    }
}

