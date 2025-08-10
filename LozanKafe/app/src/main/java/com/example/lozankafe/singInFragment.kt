package com.example.lozankafe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.lozankafe.databinding.FragmentSingInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class singInFragment : Fragment() {

    private var _binding: FragmentSingInBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth //Firebase Authentication yani kayıt olma ve giriş yapmak için.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSingInBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gifLoader = binding.gifLoader
        Glide.with(this)
            .asGif()
            .load(R.drawable.proges_gif2)
            .into(gifLoader)
        if (binding.KAdi.text != null && binding.Sifre.text != null) {
            binding.girisBtn.setOnClickListener { singIn(it,gifLoader) }
            binding.kayitBtn.setOnClickListener {
                val action = singInFragmentDirections.actionSingInFragmentToSingUpFragment2()
                Navigation.findNavController(it).navigate(action)
            }
        }
    }

    fun singIn(view: View,gifLoader : ImageView) {
        val email = binding.KAdi.text.trim().toString()
        val password = binding.Sifre.text.trim().toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            gifLoader.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { task -> //İşlem tamamlanırsa //İşlem başarılı dönerse
                        val guncelkullanici = auth.currentUser
                        val action = singInFragmentDirections.actionSingInFragmentToMainScreenFragment() //Üstteki iki işlemi daha kısa bir şekilde .addOnSuccessListener ile değiştirirsen task ı kontol etmene gerek kalmaz direk işlem başarılı dönerse çalışır.
                        Navigation.findNavController(view).navigate(action)
                        Toast.makeText(requireContext(), "Giriş Başarılı !", Toast.LENGTH_SHORT)
                            .show()

                }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Giriş Başarısız eposta veya şifre yanlış olabilir yada internet bağlantınızı kontrol ediniz !", Toast.LENGTH_SHORT)
                .show()
                    println("Hata Mesajı: "+exception)
            }
        } else {
            Toast.makeText(
                requireContext(),
                "İşlem Başarısız Lütfen Tekrar deneyin",
                Toast.LENGTH_LONG
            ).show()
        }
        gifLoader.visibility = View.INVISIBLE
    }
}
