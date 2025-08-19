package com.example.transtools

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Toast
import android.widget.ArrayAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.transtools.api.adapter.TarikBarangAdapter
import com.example.transtools.api.model.TarikBarangModel
import com.example.transtools.api.retrofit.ApiService
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TarikBarangFragment : Fragment() {

    private lateinit var adapter: TarikBarangAdapter
    private lateinit var kembali: ImageView
    private lateinit var filter: ImageView
    private lateinit var scrollView: RecyclerView
    private lateinit var loadingAnimation: ImageView

    private var nik: String? = null
    private var usp_dept: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tarik_barang, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kembali = view.findViewById(R.id.kembali)
        filter = view.findViewById(R.id.filter)

        kembali.setOnClickListener { findNavController().navigate(R.id.action_tarikBarangFragment_to_bebasExpiredFragment) }
        filter.setOnClickListener { showFilterDialog() }

        scrollView = view.findViewById(R.id.recyclerView)
        loadingAnimation = view.findViewById(R.id.loadingAnimation)

        setupTouchListener()

        // Initialize RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapter
        adapter = TarikBarangAdapter(emptyList())
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener(object : TarikBarangAdapter.OnItemClickListener {
            override fun onItemClick(item: TarikBarangModel) {
                findNavController().navigate(R.id.action_tarikBarangFragment_to_detailItemFragment)
            }
        })

        // Fetch data from API
        fetchDataFromAPIListExpired()
        displaySavedResponse()
    }

    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_filter, null)

//        val spinnerGondola = view.findViewById<Spinner>(R.id.no_gondala)
//        val editBarcode = view.findViewById<EditText>(R.id.kode)
//        val btnApply = view.findViewById<Button>(R.id.simpan)
//
//        // Contoh isi dropdown gondola
//        val gondolaList = listOf("Semua", "Gondola 1", "Gondola 2", "Gondola 3")
//        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, gondolaList)
//        spinnerGondola.adapter = adapter
//
//        btnApply.setOnClickListener {
//            val selectedGondola = spinnerGondola.selectedItem.toString()
//            val barcode = editBarcode.text.toString()
//
//            // TODO: Disini bisa panggil ulang API filter sesuai hasil input
//            Toast.makeText(requireContext(), "Gondola: $selectedGondola, Barcode: $barcode", Toast.LENGTH_SHORT).show()
//
//            // Kalau mau, kamu bisa filter list yang sudah ada disini:
//            filterList(selectedGondola, barcode)
//
//            dialog.dismiss()
//        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun filterList(gondola: String, barcode: String) {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val responseJson = sharedPreferences.getString("listDataExpired", "")

        val itemType = object : TypeToken<List<TarikBarangModel>>() {}.type
        val fullList: List<TarikBarangModel> = Gson().fromJson(responseJson, itemType)

        val filteredList = fullList.filter { item ->
            val matchGondola = if (gondola == "Semua") true else item.ieGondolaNo == gondola
            val matchBarcode = if (barcode.isEmpty()) true else item.ieBarcode == barcode
            matchGondola && matchBarcode
        }

        adapter.setData(filteredList)
    }



    private fun displaySavedResponse() {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val responseJson = sharedPreferences.getString("listDataExpired", "")

        // Convert JSON string back to List<TarikBarangModel>
        val itemType = object : TypeToken<List<TarikBarangModel>>() {}.type
        val itemList: List<TarikBarangModel> = Gson().fromJson(responseJson, itemType)

        // Show data in RecyclerView if available
        if (itemList.isNotEmpty()) {
            adapter.setData(itemList)
        } else {
            Log.d("displaySavedResponse", "No saved data")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        scrollView.setOnTouchListener(object : View.OnTouchListener {
            private var startY = 0f
            private var isPullingDown = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event != null) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startY = event.y
                            isPullingDown = false
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (!scrollView.canScrollVertically(-1) && event.y > startY) {
                                isPullingDown = true
                            }
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            if (isPullingDown) {
                                // Refresh data when pulling down
                                fetchDataFromAPIListExpired()
                                startLoadingAnimation()
                            }
                            isPullingDown = false
                            return true
                        }
                    }
                }
                return false
            }
        })
    }

    private fun stopLoadingAnimation() {
        loadingAnimation.clearAnimation()
        loadingAnimation.visibility = View.GONE
    }

    private fun startLoadingAnimation() {
        if (::loadingAnimation.isInitialized) {
            loadingAnimation.visibility = View.VISIBLE
            loadingAnimation.animate()
                .alpha(1.0f)
                .setDuration(500)
                .withEndAction {
                    loadingAnimation.animate()
                        .alpha(0.0f)
                        .setDuration(500)
                        .withEndAction {
                            loadingAnimation.visibility = View.GONE
                        }
                }
        } else {
            Log.e("BebasExpiredFragment", "loadingAnimation not initialized")
        }
    }

    private fun fetchDataFromAPIListExpired() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.agungriyadi.web.id/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        displaySavedResponseUser()

        val uspUser = nik.toString()
        val dpt = usp_dept.toString()

        apiService.getListExpired(uspUser, dpt).enqueue(object : Callback<List<TarikBarangModel>> {
            override fun onResponse(call: Call<List<TarikBarangModel>>, response: Response<List<TarikBarangModel>>) {
                if (response.isSuccessful) {
                    val itemList = response.body()
                    itemList?.let {
                        // Update the adapter with new data
                        adapter.setData(it)
                        saveDataToSharedPreferences(requireContext(), it)
                    }
                } else {
                    Log.e("ListExpired", "Failed to get data: ${response.message()}")
                }
                stopLoadingAnimation() // Stop loading animation after response
            }

            override fun onFailure(call: Call<List<TarikBarangModel>>, t: Throwable) {
                Log.e("YourFragment", "Error: ${t.message}")
                stopLoadingAnimation() // Ensure loading animation stops on failure
            }
        })
    }

    private fun saveDataToSharedPreferences(context: Context, itemList: List<TarikBarangModel>) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(itemList)
        editor.putString("listDataExpired", json)
        editor.apply()
    }

    private fun displaySavedResponseUser() {
        val sharedPreferences = requireContext().getSharedPreferences("response_data", Context.MODE_PRIVATE)
        val responseJson = sharedPreferences.getString("response_json", "")

        if (!responseJson.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(responseJson)
                val apiData = jsonObject.getJSONObject("apiData")
                val user = apiData.getJSONObject("user")
                val dbDataArray = jsonObject.getJSONArray("dbData")
                val dbData = if (dbDataArray.length() > 0) dbDataArray.getJSONObject(0) else null

                nik = user.optString("nik", "Unknown User")
                usp_dept = dbData?.optString("nik", "Unknown User")
            } catch (e: Exception) {
                Log.e("ResponseJson", "Error parsing response string", e)
            }
        } else {
            Log.d("ResponseJson", "No response data found")
        }
    }
}
