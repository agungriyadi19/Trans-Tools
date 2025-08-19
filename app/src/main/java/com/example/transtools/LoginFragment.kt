package com.example.transtools

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.transtools.api.model.DbData
import com.example.transtools.api.model.LoginResponse
import com.example.transtools.api.model.SaveTokenResponse
import com.example.transtools.api.retrofit.ApiConfig
import com.example.transtools.api.retrofit.ApiService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginFragment : Fragment() {

    private lateinit var username: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var btnLogin: Button
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = context?.getSharedPreferences("MySession", Context.MODE_PRIVATE)
        val userId = sharedPreferences?.getString("userId", null)
        if (userId != null) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }

        username = view.findViewById(R.id.username)
        password = view.findViewById(R.id.password)
        btnLogin = view.findViewById(R.id.btn_login)

        btnLogin.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                login(user, pass)
            } else {
                Toast.makeText(context, "Mohon isi username dan password", Toast.LENGTH_SHORT).show()
            }
        }

        // Memanggil fungsi validasi input
        validateInputs()
    }

    private fun saveResponseToSharedPreferences(responseJson: String) {
        val sharedPreferences = requireContext().getSharedPreferences("response_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("response_json", responseJson)
        editor.apply()
    }

    private fun saveResponseToSharedPreferencesT(responseJson: List<DbData>) {
        val sharedPreferences = requireContext().getSharedPreferences("response_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("dbData", Gson().toJson(responseJson))
        editor.apply()
    }
    
    private fun login(username: String, password: String) {
        progressDialog = ProgressDialog(context)
        progressDialog?.setMessage("Loading...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
        
        val service = ApiConfig.getApiService()

        val jsonObject = JsonObject().apply {
            addProperty("username", username)
            addProperty("password", password)
        }

        service.login(jsonObject).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                progressDialog?.dismiss()
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        val apiData = loginResponse.apiData
                        val dbData = loginResponse.dbData
                        val error = apiData.error
                        val user = loginResponse.apiData.user.nik

                        saveSession(user)

                        val loginResponseJson = Gson().toJson(loginResponse)
                        saveResponseToSharedPreferences(loginResponseJson)
                        saveResponseToSharedPreferencesT(dbData ?: listOf())
                        if (!error) {
                            activity?.runOnUiThread {
                                Toast.makeText(context, "Login berhasil", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                            }
                        } else {
                            Toast.makeText(context, "Gagal login. Terdapat error dari server", Toast.LENGTH_SHORT).show()
                        }

                        // get token firebase for notif
                        FirebaseMessaging.getInstance().token.addOnCompleteListener(
                            OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                                return@OnCompleteListener
                            }

                            // Get new FCM registration token
                            val token = task.result

                            // Log and toast
                            if (token != null) {
                                saveToken(apiData.uid, token)
                            }
                        })
                    } else {
                        Log.e("LoginError", "Response body is null")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("LoginError", "Error: $errorBody")
                    activity?.runOnUiThread {
//                        Toast.makeText(context, "Gagal login. Kode status: $errorBody", Toast.LENGTH_SHORT).show()
                        Toast.makeText(context, "Gagal login", Toast.LENGTH_SHORT).show()
                        progressDialog?.dismiss()
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                progressDialog?.dismiss()
                Log.d("API Error", "Failed to send barcode: ${t.message}")
                Toast.makeText(context, "Gagal login. Terjadi kesalahan saat menghubungi server", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun validateInputs() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                checkInputs()
            }
        }

        username.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)

        // Memanggil fungsi untuk mengecek input setelah layout telah ditampilkan
        checkInputs()
    }

    private fun checkInputs() {
        val allFieldsNotEmpty = username.text?.isNotEmpty() ?: false &&
                password.text?.isNotEmpty() ?: false

        // Atur status tombol dan warna latar belakang
        btnLogin.isEnabled = allFieldsNotEmpty
        btnLogin.setBackgroundResource(if (allFieldsNotEmpty) R.drawable.button_shape else R.drawable.button_shape_grey)
        btnLogin.setTextColor(ContextCompat.getColor(requireContext(), if (allFieldsNotEmpty) R.color.white else R.color.darkGrey))
    }

    fun saveSession(userId: String) {
        val sharedPreferences = context?.getSharedPreferences("MySession", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        if (editor != null) {
            editor.putString("userId", userId)
        }
        if (editor != null) {
            editor.apply()
        }
    }

    fun saveToken(usp_user:String, token:String){
        val service = ApiConfig.getApiService()

        val jsonObject = JsonObject().apply {
            addProperty("usp_user", usp_user)
            addProperty("token", token)
        }
        service.saveToken(jsonObject).enqueue(object : Callback<SaveTokenResponse> {
            override fun onResponse(call: Call<SaveTokenResponse>, response: Response<SaveTokenResponse>) {
                progressDialog?.dismiss()
                if (response.isSuccessful) {
                    val saveTokenResponse = response.body()
                    if (saveTokenResponse == null) {
                        Log.e("LoginError", "Response body is null") 
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("LoginError", "Error: $errorBody")
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Gagal login", Toast.LENGTH_SHORT).show()
                        progressDialog?.dismiss()
                    }
                }
            }

            override fun onFailure(call: Call<SaveTokenResponse>, t: Throwable) {
                progressDialog?.dismiss()
                Log.d("API Error", "Failed to send barcode: ${t.message}")
                Toast.makeText(context, "Gagal login. Terjadi kesalahan saat menghubungi server", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
