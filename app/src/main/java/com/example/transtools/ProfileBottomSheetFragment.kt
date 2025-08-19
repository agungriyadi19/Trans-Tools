package com.example.transtools

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONObject

class ProfileBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var tvProfile: TextView
    private var nameUser: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_layout, container, false)

        tvProfile = view.findViewById(R.id.tvProfile)
        val tvLogout = view.findViewById<TextView>(R.id.tvLogout)

        // Panggil fungsi untuk mengambil dan menampilkan nama pengguna
        displaySavedResponse()

        tvLogout.setOnClickListener {
            // Buat dialog konfirmasi
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Konfirmasi")
            builder.setMessage("Apakah Anda yakin ingin keluar?")

            // Tombol untuk mengkonfirmasi
            builder.setPositiveButton("Ya") { dialog, _ ->
                // Tutup aktivitas
                findNavController().navigate(R.id.loginFragment)
                dialog.dismiss()
                clearSession()
            }

            // Tombol untuk membatalkan
            builder.setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }

            // Tampilkan dialog
            val dialog = builder.create()
            dialog.show()
        }

        return view
    }

    private fun displaySavedResponse() {
        val sharedPreferences = requireContext().getSharedPreferences("response_data", Context.MODE_PRIVATE)
        val responseJson = sharedPreferences.getString("response_json", "")

        if (!responseJson.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(responseJson)

                // Extracting values from the JSON object
                val apiData = jsonObject.getJSONObject("apiData")
                val user = apiData.getJSONObject("user")
                val dbDataArray = jsonObject.getJSONArray("dbData")
                val dbData = if (dbDataArray.length() > 0) dbDataArray.getJSONObject(0) else null

                nameUser = user.optString("name", "Unknown User")
                tvProfile.text = nameUser

//                Log.d("ResponseJson", "Name: $nameUser)")
            } catch (e: Exception) {
                Log.e("ResponseJson", "Error parsing response string", e)
            }
        } else {
            Log.d("ResponseJson", "No response data found")
        }
    }

    fun clearSession() {
        val sharedPreferences = context?.getSharedPreferences("MySession", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        if (editor != null) {
            editor.clear()
        }
        if (editor != null) {
            editor.apply()
        }
    }

}
