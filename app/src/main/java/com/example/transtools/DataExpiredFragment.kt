package com.example.transtools

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.transtools.api.model.BebasExpiredModel
import com.example.transtools.api.model.GetGondolaModel
import com.example.transtools.api.model.ItemResponse
import com.example.transtools.api.model.soldOutModel
import com.example.transtools.api.retrofit.ApiConfig
import com.example.transtools.api.retrofit.ApiService
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class DataExpiredFragment : Fragment() {

    private var createBy: String? = null
    private var idCreateBy: String? = null
    private var userstorecode: String? = null
    private var itemCode: String? = null
    private lateinit var btnScan: ImageButton
    private lateinit var btnScanGondala: ImageButton
    private lateinit var noGondalaEditText: AutoCompleteTextView
    private lateinit var kodeEditText: EditText
    private lateinit var tglEditText: EditText
    private lateinit var itmEditText: TextView
    private lateinit var statusEditText: TextView
    private lateinit var jumEditText: EditText 
    private lateinit var checkbox_konfirmasi: CheckBox
    private lateinit var btnTgl: ImageButton
    private lateinit var calendar: Calendar
    private lateinit var kembali: ImageView

    private lateinit var simpanButton : Button

    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_data_expired, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiConfig.getApiService()

        // Initialize views
        noGondalaEditText = view.findViewById(R.id.no_gondala)
        kodeEditText = view.findViewById(R.id.kode)
        tglEditText = view.findViewById(R.id.tgl)
        itmEditText = view.findViewById(R.id.itm)
        statusEditText = view.findViewById(R.id.status)
        jumEditText = view.findViewById(R.id.jum)
        checkbox_konfirmasi = view.findViewById(R.id.checkbox_konfirmasi)
        kembali = view.findViewById(R.id.kembali)
        btnTgl = view.findViewById(R.id.btnTgl)
        calendar = Calendar.getInstance()
        btnScan = view.findViewById(R.id.btnScan)
        btnScanGondala = view.findViewById(R.id.btnScanGondala)

        simpanButton  = view.findViewById(R.id.simpan)

        // Button listeners
        btnScan.setOnClickListener { scanner() }
        btnScanGondala.setOnClickListener { scanner2() }
        btnTgl.setOnClickListener { showDatePickerDialog() }
        tglEditText.setOnClickListener { showDatePickerDialog() }
        kembali.setOnClickListener { findNavController().navigate(R.id.action_dataExpiredFragment_to_bebasExpiredFragment2) }

//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        planeEditText.adapter = adapter

        kodeEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    itmEditText.text = "Data not yet available"
                    statusEditText.text = "Data not yet available"
                } else {
                    // Jika input tidak kosong, kirim ke API
                    val inBarcode = s.toString()
                    val inStoreCode = userstorecode
                    if (inStoreCode != null) {
                        sendBarcodeToAPI(inBarcode, inStoreCode)
                    }
                }
                validateInputs()
            }
        })

        validateInputs()

        displaySavedResponseUser()

        fetchGondolaSuggestions()

    }

    private fun scanner() {
        val options = ScanOptions()
        options.setPrompt("Volume up to Flash on")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        options.captureActivity = StartScan::class.java
        launcher.launch(options)
    }

    private var launcher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            kodeEditText.setText(result.contents)
        }
    }

    private fun scanner2() {
        val options = ScanOptions()
        options.setPrompt("Volume up to Flash on")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        options.captureActivity = StartScan::class.java
        launcher2.launch(options)
    }

    private var launcher2 = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            noGondalaEditText.setText(result.contents)
        }
    }

    private fun simpan() {
        showDataPopup()
    }

    private fun showDataPopup() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_data_popup, null)

        // Set nilai ke TextView di dialogView
        dialogView.findViewById<TextView>(R.id.noGondalaValue).text = noGondalaEditText.text
        dialogView.findViewById<TextView>(R.id.kodeBarangValue).text = kodeEditText.text
        dialogView.findViewById<TextView>(R.id.tanggalExpiredValue).text = tglEditText.text
        dialogView.findViewById<TextView>(R.id.namaItemValue).text = itmEditText.text
        dialogView.findViewById<TextView>(R.id.statusItemValue).text = statusEditText.text
        dialogView.findViewById<TextView>(R.id.jumlahValue).text = jumEditText.text
//        dialogView.findViewById<TextView>(R.id.planeValue).text = planeEditText.selectedItem.toString()

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Bebas Expired Data")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                sendDataToApi()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun sendDataToApi() {

        val dataDelete = soldOutModel().apply {
            ieGondolaNo = noGondalaEditText.text.toString()
            ieUpdateUser = this@DataExpiredFragment.idCreateBy
        }

        val data = BebasExpiredModel().apply {
            ieStoreCode = this@DataExpiredFragment.userstorecode
            ieGondolaNo = noGondalaEditText.text.toString()
            ieBarcode = kodeEditText.text.toString()
            ieItemCode = itemCode
            ieItemName = itmEditText.text.toString()
            ieItemStatus = statusEditText.text.toString()
            ieExpiredDate = tglEditText.text.toString()
            ieQty = jumEditText.text.toString().toInt()
            ieAction = "1"
            ieInsertUser = this@DataExpiredFragment.idCreateBy
            ieUpdateUser = ieInsertUser
        }

        val gson = Gson()
        val jsonData = gson.toJson(data)

        Log.e("save data", jsonData)
        Log.e("save data", data.toString())
        val isChecked = checkbox_konfirmasi.isChecked

        if (isChecked) {
            apiService.deleteData(dataDelete).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.e("save data", data.toString())
                        Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT)
                            .show()
                        findNavController().navigate(R.id.action_dataExpiredFragment_to_bebasExpiredFragment2)

                        reset()
                    } else {
                        Toast.makeText(context, "Gagal menghapus data", Toast.LENGTH_SHORT).show()

                        val errorMessage =
                            "Gagal menghapus data. Kode status: ${response.code()}, Pesan: ${response.message()}"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("API_CALL_ERROR", errorMessage)
                    }

                    validateInputs()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("API_CALL_ERROR", "Terjadi kesalahan: ${t.message}", t)
                }
            })
        }
        
        apiService.postData(data).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.e("save data", data.toString())
                    Toast.makeText(context, "Data berhasil tersimpan", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_dataExpiredFragment_to_bebasExpiredFragment2)

                    reset()
                } else {
                    Toast.makeText(context, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()

                    val errorMessage = "Gagal menyimpan data. Kode status: ${response.code()}, Pesan: ${response.message()}"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("API_CALL_ERROR", errorMessage)

                }

                validateInputs()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("API_CALL_ERROR", "Terjadi kesalahan: ${t.message}", t)
            }
        })
    }

    private fun reset() {
        noGondalaEditText.setText("")
        kodeEditText.setText("")
        tglEditText.setText("")
        itmEditText.setText("Data not yet available")
        statusEditText.setText("Data not yet available")
        jumEditText.setText("")
        validateInputs()
    }

    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        DatePickerDialog(
            requireContext(), dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateInView() {
        val myFormat = "yyyy/MM/dd"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        tglEditText.setText(sdf.format(calendar.time))
    }

    //   local stored
//    private fun displaySavedResponse() {
//        val sharedPreferences = requireContext().getSharedPreferences("response_data", Context.MODE_PRIVATE)
//        val responseJson = sharedPreferences.getString("response_json", "")
//        Log.d("ini hit responseJson", "${responseJson}")
//
//        if (!responseJson.isNullOrEmpty()) {
//            try {
//                val jsonObject = JSONObject(responseJson)
//                val userObject = jsonObject.getJSONObject("user")
//                val userName = userObject.getString("name")
//                val userId = userObject.getString("nik")
//                val storecode = userObject.getString("locationCode")
//                // Ambil nilai 'name' untuk 'createBy'
//                createBy = userName
//                idCreateBy = userId
//                userstorecode = storecode
//
//            } catch (e: JSONException) {
//                e.printStackTrace()
//            }
//        }
//    }

    private fun displaySavedResponseUser() {
        val sharedPreferences = requireContext().getSharedPreferences("response_data", Context.MODE_PRIVATE)
        val responseJson = sharedPreferences.getString("response_json", "")
//        Log.d("ResponseJson", "$responseJson")

        if (!responseJson.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(responseJson)

                // Extracting values from the JSON object
                val apiData = jsonObject.getJSONObject("apiData")
                val user = apiData.getJSONObject("user")
                val dbDataArray = jsonObject.getJSONArray("dbData")
                val dbData = if (dbDataArray.length() > 0) dbDataArray.getJSONObject(0) else null

                createBy = user.optString("name", "Unknown name")
                idCreateBy = user.optString("nik", "Unknown nik")
                userstorecode = dbData?.optString("store_code", "Unknown Store") ?: "Unknown Store"

//                Log.d("ResponseJson", "createBy: $createBy, idCreateBy: $idCreateBy userstorecode: $userstorecode")
            } catch (e: Exception) {
                Log.e("ResponseJson", "Error parsing response string", e)
            }
        } else {
            Log.d("ResponseJson", "No response data found")
        }
    }

    private fun sendBarcodeToAPI(barcode: String, storeCode: String) {
        // Inisialisasi Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://backend.transmart.co.id/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Buat instance service
        val service = ApiConfig.getApiService()

        val jsonObject = JsonObject().apply {
            addProperty("barcode", barcode)
            addProperty("storecode", storeCode)
        }


//        Log.d("ini adalah requestBody", "$barcode & $storeCode")

        // Kirim permintaan ke API menggunakan Retrofit
        service.sendBarcode(jsonObject).enqueue(object : Callback<ItemResponse> {
            override fun onResponse(call: Call<ItemResponse>, response: Response<ItemResponse>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    Log.d("API master Response", "Response from server: $responseData")
                    responseData?.let {
                        itmEditText.text = it.item_name
                        statusEditText.text = if (responseData.returnable == "Y") "returnable" else "non-returnable"
                        itemCode = it.item_code
//                        Log.d("itemCode", "itemCode: $itemCode")

                        Toast.makeText(context, "Data ditemukan", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.d("API master Error", "Failed to send barcode: $errorBody")
                    Toast.makeText(context, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                    itmEditText.setText("Data not yet available")
                    statusEditText.setText("Data not yet available")
                }

                validateInputs()
            }

            override fun onFailure(call: Call<ItemResponse>, t: Throwable) {
                Log.d("API Error", "Failed to send barcode: ${t.message}")
            }
        })
    }

    private fun validateInputs(){
        // Save button click listener
        simpanButton.setOnClickListener { simpan() }
        simpanButton.isEnabled = false

        fun checkInputs(){
            val itmText = itmEditText.text.toString()
            val statusText = statusEditText.text.toString()

            val additionalConditionsMet = itmText != "Data not yet available" &&
                    statusText != "Data not yet available"
            val allFieldsNotEmpty = noGondalaEditText.text.isNotEmpty() &&
                    kodeEditText.text.isNotEmpty() &&
                    tglEditText.text.isNotEmpty() &&
                    jumEditText.text.isNotEmpty()

            // Atur status tombol dan warna latar belakang
            if (allFieldsNotEmpty && additionalConditionsMet) {
                simpanButton.isEnabled = true
                simpanButton.setBackgroundResource(R.drawable.button_shape)
                simpanButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                simpanButton.isEnabled = false
                simpanButton.setBackgroundResource(R.drawable.button_shape_grey)
                simpanButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkGrey))
            }
        }

        checkInputs()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                checkInputs()
            }

        }

        noGondalaEditText.addTextChangedListener(textWatcher)
        kodeEditText.addTextChangedListener(textWatcher)
        tglEditText.addTextChangedListener(textWatcher)
        jumEditText.addTextChangedListener(textWatcher)
    }

    private fun fetchGondolaSuggestions() {
        Log.d("start get gondola", "start get gondola")
//        Log.d("ini adalah userstorecode gondola", "$idCreateBy")

        val uspUser = idCreateBy // or however you determine this
        if (uspUser != null) {
            apiService.getListGondola(uspUser).enqueue(object : Callback<List<GetGondolaModel>> {
                override fun onResponse(call: Call<List<GetGondolaModel>>, response: Response<List<GetGondolaModel>>) {
//                    val dummyGondolas = listOf(
//                        GetGondolaModel("1001101A01", "01A01"),
//                        GetGondolaModel("1001101A02", "01A02"),
//                        GetGondolaModel("1001101A03", "01A03"),
//                        GetGondolaModel("1001101B01", "01B01"),
//                        GetGondolaModel("1001102A01", "02A01"),
//                        GetGondolaModel("1001102A02", "02A02"),
//                        GetGondolaModel("1001102B01", "02B01"),
//                        GetGondolaModel("1001102B02", "02B02")
//                    )

                    // Simulate the response
//                    val response = Response.success()
//                    Log.d("ini adalah response gondola", "$response")
                    if (response.isSuccessful) {
                        val gondolas = response.body()?.map { it.id ?: "" } ?: emptyList()
//                        Log.d("ini adalah gondola", "$gondolas")

                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, gondolas)
                        noGondalaEditText.setAdapter(adapter)
                    } else {
                        Toast.makeText(context, "Failed to fetch gondola data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<GetGondolaModel>>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

}