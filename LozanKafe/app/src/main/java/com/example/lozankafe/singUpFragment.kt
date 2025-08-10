package com.example.lozankafe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.lozankafe.databinding.FragmentSingUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.navigation.findNavController
import com.bumptech.glide.Glide

class singUpFragment : Fragment() {

    private var _binding: FragmentSingUpBinding? = null
    private val binding get() = _binding!!
    private  lateinit var  auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSingUpBinding.inflate(inflater, container, false)
        val view = binding.root
        val gifLoader = binding.gifLoader
        Glide.with(this)
            .asGif()
            .load(R.drawable.proges_gif2)
            .into(gifLoader)
        binding.kayitBtn.setOnClickListener { singUp(it,gifLoader) }

        return view
    }

    fun singUp (view: View,gifLoader:ImageView)
    {
        val email = binding.KAdi.text.trim().toString()
        val sifre = binding.Sifre.text.trim().toString()
        if (email.isNotEmpty() && sifre.isNotEmpty())
        {
            gifLoader.visibility = View.VISIBLE
            auth.createUserWithEmailAndPassword(email,sifre)
                .addOnSuccessListener {
                    gifLoader.visibility = View.INVISIBLE
                Toast.makeText(requireContext(),"Kayıt Başarılı !",Toast.LENGTH_SHORT).show()
                    val action = singUpFragmentDirections.actionSingUpFragment2ToMainScreenFragment()
                    view.findNavController().navigate(action)
                }
                .addOnFailureListener{
                    gifLoader.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(),"Kayıt Başarısız internet lütfen bağlantınızı kontrol ediniz !",Toast.LENGTH_SHORT)
                        .show()
                }
        }else
        {
            Toast.makeText(requireContext(),"Kullanıcı adı ve şifre kısmı boş olamaz!",Toast.LENGTH_LONG).show()
        }
    }

}