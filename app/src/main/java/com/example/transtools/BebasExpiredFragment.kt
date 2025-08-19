package com.example.transtools

import DashboardExpiredModel
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.transtools.api.adapter.DasboardItemAdapter
import com.example.transtools.api.model.DashboardResponse
import com.example.transtools.api.model.TarikBarangModel
import com.example.transtools.api.retrofit.ApiConfig
import com.example.transtools.api.retrofit.ApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BebasExpiredFragment : Fragment() {
    private lateinit var kembali: ImageView
    private lateinit var didataText: TextView
    private lateinit var ditarikText: TextView
    private lateinit var showMoreText: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var loadingAnimation: ImageView
    private lateinit var adapter: DasboardItemAdapter
    private var didata = 0f // Variabel untuk radius
    private var ditarik = 0f // Variabel untuk radius
    private var nik: String? = null
    private var usp_dept: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bebas_expired, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dataExpired = view.findViewById<CardView>(R.id.data)
        val menarikExpired = view.findViewById<CardView>(R.id.tarik)
        kembali = view.findViewById(R.id.kembali)
        showMoreText = view.findViewById(R.id.showMore)
        didataText = view.findViewById(R.id.diDataId)
        ditarikText = view.findViewById(R.id.diTarikId)
        scrollView = view.findViewById(R.id.scrollViewD)
        loadingAnimation = view.findViewById(R.id.loadingAnimation) // Initialize loadingAnimation here
        

        dataExpired.setOnClickListener {
            findNavController().navigate(R.id.action_bebasExpiredFragment_to_dataExpiredFragment2)
        }
        menarikExpired.setOnClickListener {
            findNavController().navigate(R.id.action_bebasExpiredFragment_to_tarikBarangFragment)
        }

        showMoreText.setOnClickListener { findNavController().navigate(R.id.action_bebasExpiredFragment_to_tarikBarangFragment) }
        kembali.setOnClickListener { findNavController().navigate(R.id.action_bebasExpiredFragment_to_homeFragment) }

        setupTouchListener()

        displaySavedResponseUser()
        fetchDataFromAPIListExpired()
        fetchDataFromAPIDasboardList()

        // Inisialisasi adapter RecyclerView
        adapter = DasboardItemAdapter(emptyList())
        adapter.setOnItemClickListener(object : DasboardItemAdapter.OnItemClickListener {
            override fun onItemClick(item: TarikBarangModel) {
                // Navigasi ke DetailItemFragment saat item diklik
                findNavController().navigate(R.id.action_bebasExpiredFragment_to_detailItemFragment)
            }

        })

        // Inisialisasi RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.dExpired)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set adapter pada RecyclerView
        recyclerView.adapter = adapter
        displaySavedResponseD()

        fetchDataFromAPIDashboardModel()
        displaySavedResponse()
    }

    private fun fetchDataFromAPIListExpired() {
        displaySavedResponseUser()

        val uspUser = nik.toString()
        val dpt = usp_dept.toString()

        val apiService = ApiConfig.getApiService()

        apiService.getListExpired(uspUser, dpt).enqueue(object : Callback<List<TarikBarangModel>> {
            override fun onResponse(
                call: Call<List<TarikBarangModel>>, response: Response<List<TarikBarangModel>>
            ) {
                Log.d("ini BE getListExpired", response.toString())

                if (response.isSuccessful) {
                    val itemList = response.body()
                    Log.d("ini BE getListExpired response", itemList.toString())

                    itemList?.let {
                        adapter.setData(it)

                        Log.d("ini BE getListExpired", it.toString())
                        // Simpan data ke shared preferences setelah menerimanya
                        saveDataToSharedPreferences(requireContext(), it)
                    }
                } else {
                    // Tangani kesalahan jika respons tidak berhasil
                    Log.e("DataListExpierd", "Gagal mendapatkan data: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<TarikBarangModel>>, t: Throwable) {
                // Tangani kesalahan saat koneksi gagal
                Log.e("DataListExpierd", "Error: ${t.message}")
            }
        })
    }

    private fun fetchDataFromAPIDashboardModel() {
        val apiService = ApiConfig.getApiService()

        val uspUser = nik.toString()
        val dpt = usp_dept.toString()

        apiService.getDashboardData(uspUser, dpt).enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(
                call: Call<DashboardResponse>, response: Response<DashboardResponse>
            ) {
                if (response.isSuccessful) {
                    val dashboardResponse = response.body()
                    val dataListed = dashboardResponse?.dataListed
                    val dataWithdrawn = dashboardResponse?.dataWithdrawn

                    // Update UI
                    didataText.text = dataListed?.itemListedToday ?: "0"
                    ditarikText.text = dataWithdrawn?.itemWithdrawnToday ?: "0"

                } else {
                    // Show user-friendly error message
                    Toast.makeText(
                        requireContext(),
                        "Error fetching dashboard data: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("DasboardEspired", "Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(), "Failed to connect to server: ${t.message}", Toast.LENGTH_LONG
                ).show()
                Log.e("DasboardEspired", "Error: ${t.message}")
            }
        })
    }

    fun saveDataToSharedPreferences(context: Context, itemList: List<TarikBarangModel>) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(itemList)
        Log.d("ini BE json", json)
        editor.putString("listDataExpired", json)
        editor.apply()
    }

    private fun displaySavedResponse() {
        val sharedPreferences =
            requireContext().getSharedPreferences("dashboard", Context.MODE_PRIVATE)
        val responseJson = sharedPreferences.getString("response_dashboard", "")
        Log.d("ini hit responseJson", "${responseJson}")

        if (!responseJson.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(responseJson)
                val data_listed = jsonObject.getJSONObject("data_listed")
                val data_withdrawn = jsonObject.getJSONObject("data_withdrawn")

                val item_listed_today = data_listed.getDouble("item_listed_today").toFloat()
                val item_withdrawn_today =
                    data_withdrawn.getDouble("item_withdrawn_today").toFloat()

                didata = item_listed_today
                ditarik = item_withdrawn_today

                Log.d("ini hit Data", "$item_listed_today dan $item_withdrawn_today")

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

    }

    private fun displaySavedResponseD() {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val responseJson = sharedPreferences.getString("listDataExpired", "")

        // Check if responseJson is empty or null
        if (responseJson.isNullOrEmpty()) {
            Log.d("displaySavedResponseD", "No saved data found in SharedPreferences")
            return
        }

        try {
            // Convert JSON string back to List<TarikBarangModel>
            val itemType = object : TypeToken<List<TarikBarangModel>>() {}.type
            val itemList: List<TarikBarangModel> = Gson().fromJson(responseJson, itemType)

            Log.d("ini BE itemList", itemList.toString())

            // Show data in RecyclerView if available
            if (itemList.isNotEmpty()) {
                adapter.setData(itemList)
            } else {
                Log.d("displaySavedResponseD", "No data found after deserialization")
            }
        } catch (e: Exception) {
            Log.e("displaySavedResponseD", "Failed to parse JSON", e)
        }
    }


    private fun fetchDataFromAPIDasboardList() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url =
                    "https://api.agungriyadi.web.id/apiMobile/dashboard-expiringSoon?usp_user=$nik&usp_dept=$usp_dept"
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("HomeFragment", "Failed to retrieve data: ${response.code}")
                        return@use
                    }

                    val responseBody = response.body?.string()
                    responseBody?.let {
                        saveDataToSharedPreferencesListD(requireContext(), it)
                        parseAndLogApiResponse(it)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", e.message ?: "Unknown error")
            }
        }
    }

    private fun parseAndLogApiResponse(responseBody: String) {
        val gson = Gson()
        val apiResponse = gson.fromJson(responseBody, DashboardExpiredModel::class.java)
        Log.d(
            "HomeFragment",
            "Total item nearing expiration: ${apiResponse.totalItemNearingExpiration}"
        )
        Log.d("HomeFragment", "Nearest expiration date: ${apiResponse.nearestExpirationDate}")
        apiResponse.closestItems?.forEach {
            Log.d(
                "HomeFragment", "Item Name: ${it.ieItemName}, Remaining Days: ${it.remainingDays}"
            )
        }
    }

    private fun saveDataToSharedPreferencesListD(context: Context, responseBody: String) {
        val sharedPreferences =
            context.getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("itemDasboardList", responseBody)
        editor.apply()
    }

//    private fun startLoadingAnimation() {
//        loadingAnimation.visibility = View.VISIBLE
//        val rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate)
//        loadingAnimation.startAnimation(rotateAnimation)
//    }

    private fun stopLoadingAnimation() {
        loadingAnimation.clearAnimation()
        loadingAnimation.visibility = View.GONE
    }

    private fun startLoadingAnimation() {
        // Pastikan loadingAnimation sudah diinisialisasi sebelum digunakan
        if (::loadingAnimation.isInitialized) {
            loadingAnimation.visibility = View.VISIBLE

            // Lakukan tindakan animasi tambahan jika diperlukan
            // Contoh: Menjalankan animasi
            loadingAnimation.animate().alpha(1.0f).setDuration(500).withEndAction {
                    // Sembunyikan animasi setelah selesai
                    loadingAnimation.animate().alpha(0.0f).setDuration(500).withEndAction {
                            loadingAnimation.visibility = View.GONE
                        }
                }
        } else {
            Log.e("BebasExpiredFragment", "loadingAnimation belum diinisialisasi")
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
                        }

                        MotionEvent.ACTION_MOVE -> {
                            if (!scrollView.canScrollVertically(-1) && event.y > startY) {
                                isPullingDown = true
                            }
                        }

                        MotionEvent.ACTION_UP -> {
                            if (isPullingDown) {
                                Log.d("ScrollView", "berhasil tarik ke atas")
//                                Toast.makeText(context, "Berhasil tarik ke atas", Toast.LENGTH_SHORT).show()
                                fetchDataFromAPIDasboardList()
                                fetchDataFromAPIListExpired()
                                displaySavedResponseD()
                                startLoadingAnimation()
                                // Stop the animation after a delay (e.g., 2 seconds)
                                scrollView.postDelayed({
                                    stopLoadingAnimation()
                                }, 2000)
                            }
                        }
                    }
                }
                return false
            }
        })
    }

    private fun displaySavedResponseUser() {
        val sharedPreferences =
            requireContext().getSharedPreferences("response_data", Context.MODE_PRIVATE)
        val responseJson = sharedPreferences.getString("response_json", "")
        Log.d("ResponseJson", "$responseJson")

        if (!responseJson.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(responseJson)

                // Extracting values from the JSON object
                val apiData = jsonObject.getJSONObject("apiData")
                val user = apiData.getJSONObject("user")
                val dbDataArray = jsonObject.getJSONArray("dbData")
                val dbData = if (dbDataArray.length() > 0) dbDataArray.getJSONObject(0) else null

//                nameUser = user.optString("name", "Unknown User")
                nik = user.optString("nik", "Unknown User")
                usp_dept = dbData?.optString("dept_code", "Unknown User")
//                uspStore = dbData?.optString("usp_store", "Unknown Store") ?: "Unknown Store"


                Log.d(
                    "ResponseJson", "uspUser: $nik" + ""
                )
            } catch (e: Exception) {
                Log.e("ResponseJson", "Error parsing response string", e)
            }
        } else {
            Log.d("ResponseJson", "No response data found")
        }
    }

}
