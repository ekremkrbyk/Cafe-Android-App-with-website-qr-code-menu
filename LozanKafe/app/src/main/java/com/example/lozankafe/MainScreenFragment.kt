package com.example.lozankafe

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lozankafe.Models.Siparis
import com.example.lozankafe.databinding.FragmentMainScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainScreenFragment : Fragment() {

    private var _binding: FragmentMainScreenBinding? = null
    private val binding get() = _binding!!
    private  lateinit var  auth : FirebaseAuth
    private val adapter = SiparisAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createNotificationChannel()

        // Buton tıklanınca QR kod tarayıcı açılacak
        view.findViewById<Button>(R.id.qrButton).setOnClickListener {
            startQRScanner()
        }
        binding.kullaniciText.text = "Kullanıcı: "+auth.currentUser!!.email!!.trim().toString()

        binding.cikisBtn.setOnClickListener {
            auth.signOut()
            val action = MainScreenFragmentDirections.actionMainScreenFragmentToSingInFragment()
            view.findNavController().navigate(action)
        }
        // RecyclerView ayarları
        val recyclerView = binding.sipList
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = adapter

        // İlk veri yükleme
        siparisleriGetir()
        kullaniciBakiyesiniGetir()

        // SwipeRefreshLayout ayarları
        binding.swipeRefreshLayout.setOnRefreshListener {
            siparisleriGetir()
            kullaniciBakiyesiniGetir()
        }
        binding.cikisBtn.setOnClickListener { signOutUser() }
    }

    override fun onStop() {
        super.onStop()
        // Ekran yönünü geri eski haline getir
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private val barcodeLauncher = this.registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            // QR koddan alınan URL'yi işle
            Toast.makeText(requireContext(), "QR Kod: ${result.contents}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startQRScanner() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("QR kodu taratın")
        options.setCameraId(0) // Arka kamerayı kullan
        options.setBeepEnabled(true) // Tarama sonrası ses çıkar
        options.setBarcodeImageEnabled(true)

        barcodeLauncher.launch(options)
    }

    private fun siparisleriGetir() {
        val email = FirebaseAuth.getInstance().currentUser ?.email ?: return
        println("Kullanıcı Email: $email")
        val db = FirebaseFirestore.getInstance()
        val siparisListesi = mutableListOf<Siparis>()

        // Kullanıcının email'ine göre orders koleksiyonundaki belgeleri al
        db.collection("orders")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Her belgeyi kontrol et
                for (orderDoc in querySnapshot.documents) {
                    println("Belge ID: ${orderDoc.id}, Veriler: ${orderDoc.data}")

                    // Siparişler alt koleksiyonunu al
                    orderDoc.reference.collection("siparisler")
                        .get()
                        .addOnSuccessListener { siparisSnapshot ->
                            for (siparisDoc in siparisSnapshot.documents) {
                                println("Sipariş Belge ID: ${siparisDoc.id}, Veriler: ${siparisDoc.data}")

                                // Firestore'dan gelen verileri kullanarak Siparis nesnesini oluştur
                                val total = siparisDoc.getDouble("total") ?: 0.0
                                val timestamp = siparisDoc.getTimestamp("timestamp")?.seconds ?: 0L
                                val items = siparisDoc.get("items") as? List<Map<String, Any>> ?: emptyList()

                                // Ürün adlarını ve fiyatlarını birleştir
                                val urunAdi = items.joinToString(", ") { it["item"] as? String ?: "" }
                                val urunFiyati = items.joinToString(", ") { (it["price"] as? Number)?.toString() ?: "0" }

                                // Siparis nesnesini oluştur
                                val siparis = Siparis(
                                    id = siparisDoc.id,
                                    tarih = timestamp,
                                    urunAdi = urunAdi,
                                    urunFiyati = urunFiyati,
                                    toplamTutar = total
                                )

                                siparisListesi.add(siparis)
                            }

                            // Siparişleri tarih alanına göre sıralama
                            siparisListesi.sortBy { it.tarih } // Tarih alanına göre sıralama

                            val today = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                            // Tarih kontrolü ve bildirim gönderme
                            println("bugun " +today)
                            try {
                                for (siparis in siparisListesi) {
                                    println(siparis.tarih.toString())
                                    val siparisTarihi = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(siparis.tarih * 1000L))
                                    if (siparisTarihi == today) {

                                        bildirimGonder("Siparişini Aldık!", "Siparişini en kısa sürede seninle buluşturacağız.")
                                    }
                                }
                            }
                            catch (e:Exception) {
                                println(e)
                            }


                            // RecyclerView'a veriyi ver
                            adapter.submitList(siparisListesi)
                            println("Alınan Siparişler: $siparisListesi")
                            binding.swipeRefreshLayout.isRefreshing = false
                        }
                        .addOnFailureListener { ex ->
                            Toast.makeText(requireView().context, "Siparişler alınamadı", Toast.LENGTH_SHORT).show()
                            println(ex)
                        }
                }
            }
            .addOnFailureListener { ex ->
                Toast.makeText(requireView().context, "Kullanıcı belgeleri alınamadı", Toast.LENGTH_SHORT).show()
                println(ex)
            }
    }

    private fun kullaniciBakiyesiniGetir() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(uid!!)
            .get()
            .addOnSuccessListener { document ->
                val bakiye = document.getDouble("bakiye") ?: 0.0
                // bakiye ile işlem yap
                binding.bakiyeText.text = "Bakiye: ${bakiye}"
            }
            .addOnFailureListener { e->
                Log.e("Firestore", "Bakiye alınamadı", e)
            }
    }

    fun bildirimGonder(baslik: String, mesaj: String) {
        try {
            // Android 13 ve üzeri için izin kontrolü
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // İzin verilmemişse, kullanıcıdan izin iste
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // İzin istemek için launcher'ı kullan
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    // İzin verilene kadar bildirim göndermiyoruz
                    return
                }
            }

            // İzin verildiyse bildirim oluştur
            val builder = NotificationCompat.Builder(requireContext(), "siparis_kanali")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(baslik)
                .setContentText(mesaj)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            // Bildirimi gönder
            val notificationManager = NotificationManagerCompat.from(requireContext())
            notificationManager.notify(1, builder.build())
        }
        catch (e:Exception)
        {
            println(e)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // İzin verildi, bildirim göndermeye devam edebilirsiniz
            Toast.makeText(requireContext(), "Bildirim izni verildi", Toast.LENGTH_SHORT).show()
        } else {
            // İzin verilmedi
            Toast.makeText(requireContext(), "Bildirim izni verilmedi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "siparis_kanali",
                "Sipariş Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Sipariş bildirimleri için kanal"
            }

            val notificationManager: NotificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun signOutUser() {
        auth.signOut()
    }
}