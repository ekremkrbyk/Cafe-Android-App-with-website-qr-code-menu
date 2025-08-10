package com.example.lozankafe.Models

data class Siparis(
    val id: String = "",
    val tarih: Long = 0L, // Firestore'dan gelen timestamp
    val urunAdi: String = "",
    val urunFiyati: String = "",
    val toplamTutar: Double = 0.0
)
