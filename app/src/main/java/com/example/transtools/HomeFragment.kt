package com.example.transtools

import DashboardExpiredModel
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.transtools.ProfileBottomSheetFragment
import com.example.transtools.api.model.DashboardResponse
import com.example.transtools.api.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {

    private var nameUser: String? = null
    private var storeUser: String? = null
    private var deptName: String? = null
    private var nik: String? = null
    private var usp_dept: String? = null

    private val CHANNEL_ID = "example_channel"
    private val notificationId = 101
    private val REQUEST_CODE_POST_NOTIFICATIONS = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bebasExpired = view.findViewById<CardView>(R.id.expired)

        // Perbaiki
        val nameText = view.findViewById<TextView>(R.id.name)
        val storeText = view.findViewById<TextView>(R.id.storeName)
        val idDep = view.findViewById<TextView>(R.id.idDep)
        val depName = view.findViewById<TextView>(R.id.depName)

        val buttonShowBottomSheet = view.findViewById<ImageView>(R.id.buttonShowBottomSheet)
        buttonShowBottomSheet.setOnClickListener {
            val bottomSheet = ProfileBottomSheetFragment()
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        bebasExpired.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_bebasExpiredFragment)
        }

        displaySavedResponseUser()
        nameText.text = this.nameUser
        storeText.text = this.storeUser
        idDep.text = this.usp_dept
        depName.text = this.deptName

        createNotificationChannel()
        fetchDataFromAPIDashboardModel()
        fetchDataFromAPIDasboardList()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_POST_NOTIFICATIONS)
        }
    }

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

                nameUser = user.optString("name", "Unknown User")
                nik = user.optString("nik", "Unknown User")
                usp_dept = dbData?.optString("dept_code", "Unknown User")
                deptName = dbData?.optString("dept_name", "Unknown User")
                storeUser = dbData?.optString("store_name", "Unknown Store") ?: "Unknown Store"

                val uspUser = nik.toString()

                checkSession(uspUser)

//                Log.d("ResponseJson", "Name: $nameUser, Store: $storeUser, usp_dept: $usp_dept, storeUser: $deptName")
            } catch (e: Exception) {
                Log.e("ResponseJson", "Error parsing response string", e)
            }
        } else {
            Log.d("ResponseJson", "No response data found")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Example Channel"
            val descriptionText = "This is an example channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun fetchDataFromAPIDashboardModel() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.agungriyadi.web.id/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val uspUser = nik.toString()
        val dpt = usp_dept.toString()

        apiService.getDashboardData(uspUser,dpt).enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                if (response.isSuccessful) {
                    val dashboardResponse = response.body()
                    saveDataToSharedPreferences(requireContext(), dashboardResponse)
                } else {
                    Log.e("DasboardEspired", "Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                // Tangani kesalahan koneksi atau respons gagal
                Log.e("DasboardEspired", "Error: ${t.message}")
            }
        })
    }

    private fun fetchDataFromAPIDasboardList() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://api.agungriyadi.web.id/apiMobile/dashboard-expiringSoon?usp_user=$nik&usp_dept=$usp_dept"
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("HomeFragment", "Failed to retrieve data: ${response.code}")
                        return@use
                    }

                    val responseBody = response.body?.string()
                    responseBody?.let {
                        saveDataToSharedPreferences(requireContext(), it)
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
        Log.d("HomeFragment", "Total item nearing expiration: ${apiResponse.totalItemNearingExpiration}")
        Log.d("HomeFragment", "Nearest expiration date: ${apiResponse.nearestExpirationDate}")
        apiResponse.closestItems?.forEach {
            Log.d("HomeFragment", "Item Name: ${it.ieItemName}, Remaining Days: ${it.remainingDays}")
            //send notif
//            sendNotification("${it.ieItemName} expired dalam ${it.remainingDays} hari lagi", it.hashCode())
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                fetchDataFromAPIDashboardModel()
                fetchDataFromAPIDasboardList()
            } else {
                // Handle the case where the user denies the permission
            }
        }
    }

    private fun saveDataToSharedPreferences(context: Context, itemList: DashboardResponse?) {
        val sharedPreferences = context.getSharedPreferences("dashboard", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(itemList)
        editor.putString("response_dashboard", json)
        editor.apply()
    }

    private fun saveDataToSharedPreferences(context: Context, responseBody: String) {
        val sharedPreferences = context.getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
//        val gson = Gson()
//        val json = gson.toJson(responseBody)
        editor.putString("itemDasboardList", responseBody)
        editor.apply()
    }

    private fun saveDataToSharedPreferencesNotif(context: Context, responseBody: String) {
        val sharedPreferences = context.getSharedPreferences("notif", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("itemNotifList", responseBody)  // Simpan responseBody langsung
        editor.apply()
    }

    fun checkSession(nik: String): Boolean {
        val sharedPreferences = context?.getSharedPreferences("MySession", Context.MODE_PRIVATE)
        val userId = sharedPreferences?.getString("userId", null)
        return userId != null
    }

}
