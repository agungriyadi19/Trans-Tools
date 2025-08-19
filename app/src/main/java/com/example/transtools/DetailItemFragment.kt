package com.example.transtools

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.transtools.api.model.soldOutModel
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.cert.X509Certificate
import java.text.ParseException
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class DetailItemFragment : Fragment() {

    private lateinit var kembali: ImageView
    private lateinit var textNameItem: TextView
    private lateinit var textNoGondala: TextView
    private lateinit var textItemCode: TextView
    private lateinit var textExpiredDate: TextView
    private lateinit var textItemStatus: TextView
    private lateinit var textItemQty: TextView
    private lateinit var jumlahDitarik: TextView
    private lateinit var inputQty: EditText

    private lateinit var tarik: Button
    private lateinit var sold: Button

    private val CAMERA_PERMISSION_CODE = 100
    private val CAMERA_REQUEST_CODE = 101
    private lateinit var imageUri: Uri
    private lateinit var hasilFoto: ImageView

    private var barcode: String? = null
    private var idItem: String? = null
    private var idUser: String? = null

    private var itemName: String? = null
    private var noGondola: String? = null
    private var kodeBarang: String? = null
    private var tanggalExpired: String? = null
    private var statusItem: String? = null
//    private var jumlahItem: String? = null

    private var resizedBitmap2: Bitmap? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail_item, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textNameItem = view.findViewById(R.id.itemName)
        textNoGondala = view.findViewById(R.id.noGondalaValue)
        textItemCode = view.findViewById(R.id.kodeBarangValue)
        textExpiredDate = view.findViewById(R.id.tanggalExpiredValue)
        textItemStatus = view.findViewById(R.id.statusItemValue)
//        textItemQty = view.findViewById(R.id.jumlahValue)
        hasilFoto = view.findViewById(R.id.hasilFoto)
        tarik = view.findViewById(R.id.tarik)
        sold = view.findViewById(R.id.sold)
        inputQty = view.findViewById(R.id.jum)

        validateInputs()

        val cameraButton: CardView = view.findViewById(R.id.camera)

        cameraButton.setOnClickListener {
            checkCameraPermission()
        }

        tarik.setOnClickListener {
            showDataPopup()
        }
        sold.setOnClickListener {
            showDataPopupSold()
        }

//        val dataDelete = soldOutModel().apply {
//            ieGondolaNo = noGondalaEditText.text.toString()
//            ieUpdateUser = this@DataExpiredFragment.idCreateBy
//        }

        kembali = view.findViewById(R.id.kembali)
        kembali.setOnClickListener { requireActivity().onBackPressed() }

        displaySavedResponseitem()
        displaySavedResponseuser()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun displaySavedResponseitem() {
        val sharedPreferences = requireContext().getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)
        val responseJson = sharedPreferences.getString("item", "")
        Log.d("ini responseJson", responseJson.toString())
        if (!responseJson.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(responseJson)

                itemName = jsonObject.getString("ie_item_name")
                noGondola = jsonObject.getString("ie_gondola_no")
                kodeBarang = jsonObject.getString("ie_item_code")
                tanggalExpired = jsonObject.getString("ie_expired_date")
                statusItem = jsonObject.getString("ie_item_status")
//                jumlahItem = jsonObject.getInt("ie_qty").toString()
                idItem = jsonObject.getString("ie_id")

                val inputFormat = SimpleDateFormat("yyyy-MM-dd")
                val outputFormat = SimpleDateFormat("yyyy/MM/dd")
                try {
                    val date = inputFormat.parse(tanggalExpired)
                    val formattedDate = outputFormat.format(date)
                    textExpiredDate.text = formattedDate
                } catch (e: ParseException) {
                    e.printStackTrace()
                    // Handle jika terjadi kesalahan parsing tanggal
                }
                textNameItem.text = itemName
                textNoGondala.text = noGondola
                textItemCode.text = kodeBarang
//                textExpiredDate.text = tanggalExpired
                textItemStatus.text = statusItem
//                textItemQty.text = jumlahItem.toString()

                barcode = kodeBarang

//                Log.d("ResponseJson", "$itemName & $noGondola & $tanggalExpired & $statusItem ")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun displaySavedResponseuser() {
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

                idUser = user.optString("nik", "Unknown idUser")
//                storeUser = dbData?.optString("ms_name", "Unknown Store") ?: "Unknown Store"

//                Log.d("ResponseJson", "idUser: $idUser")
            } catch (e: Exception) {
                Log.e("ResponseJson", "Error parsing response string", e)
            }
        } else {
            Log.d("ResponseJson", "No response data found")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showDataPopup() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_data_popup_tarik, null)

        // Set nilai ke TextView di dialogView
        dialogView.findViewById<TextView>(R.id.noGondalaValue).text = noGondola
        dialogView.findViewById<TextView>(R.id.kodeBarangValue).text = kodeBarang
        dialogView.findViewById<TextView>(R.id.tanggalExpiredValue).text = tanggalExpired
        dialogView.findViewById<TextView>(R.id.namaItemValue).text = itemName
        dialogView.findViewById<TextView>(R.id.statusItemValue).text = statusItem
//        dialogView.findViewById<TextView>(R.id.jumlahValue).text = jumlahItem
        dialogView.findViewById<TextView>(R.id.jumlahDitarik).text = inputQty.text.toString()
        val imageView = dialogView.findViewById<ImageView>(R.id.hasilFotoB)

        resizedBitmap2?.let {
            imageView.setImageBitmap(it)
        }

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Penarikan Barang")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                kirim()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showDataPopupSold() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_data_popup_tarik, null)

        // Set nilai ke TextView di dialogView
        dialogView.findViewById<TextView>(R.id.noGondalaValue).text = noGondola
        dialogView.findViewById<TextView>(R.id.kodeBarangValue).text = kodeBarang
        dialogView.findViewById<TextView>(R.id.tanggalExpiredValue).text = tanggalExpired
        dialogView.findViewById<TextView>(R.id.namaItemValue).text = itemName
        dialogView.findViewById<TextView>(R.id.statusItemValue).text = statusItem
//        dialogView.findViewById<TextView>(R.id.jumlahValue).text = jumlahItem
        dialogView.findViewById<TextView>(R.id.jumlahDitarik).text = inputQty.text.toString()
        val imageView = dialogView.findViewById<ImageView>(R.id.hasilFotoB)

        resizedBitmap2?.let {
            imageView.setImageBitmap(it)
        }

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Barang habis terjual")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                kirim()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    
    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            openCamera()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.transtools.fileprovider",
                it
            )
            imageUri = photoURI
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (::imageUri.isInitialized) {
                imageUri.let { uri ->
                    resizeAndSetImage(uri)
                }
            } else {
                imageUri = createImageFile()?.toUri()!!
                if (::imageUri.isInitialized) {
                    resizeAndSetImage(imageUri)
                } else {
                    Log.e("DetailItemFragment", "Gagal membuat file gambar")
                }
            }
        }
    }

    private fun resizeAndSetImage(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            inputStream?.let {
                val originalBitmap = BitmapFactory.decodeStream(it)
                val resizedBitmap = resizeBitmap(originalBitmap, 800, 800) // Resize to desired dimensions
                hasilFoto.visibility = View.VISIBLE
                hasilFoto.setImageBitmap(resizedBitmap)
                resizedBitmap2 = resizedBitmap
                validateInputs()
//                Log.d("ini hasil nya", "$uri")
                it.close()
            }
        } catch (e: Exception) {
            Log.e("DetailItemFragment", "Error loading image", e)
        }
    }

    private fun resizeBitmap(source: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val aspectRatio: Float = source.width.toFloat() / source.height.toFloat()
        val width: Int
        val height: Int

        if (source.width > source.height) {
            width = maxWidth
            height = (maxWidth / aspectRatio).toInt()
        } else {
            height = maxHeight
            width = (maxHeight * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    private val httpClient: OkHttpClient by lazy {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        val sslSocketFactory = sslContext.socketFactory

        OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun createImageFileFromBitmap(bitmap: Bitmap): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            file
        } catch (e: IOException) {
            Log.e("DetailItemFragment", "Error creating image file from bitmap", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun kirim() {

//        Log.d("ini adalah", "${idItem}")
        // Prepare the image file (make sure 'image' is a file path or a URI)
        val bitmap = (hasilFoto.drawable as? BitmapDrawable)?.bitmap
        if (bitmap == null) {
            Toast.makeText(context, "Gambar belum diambil", Toast.LENGTH_SHORT).show()
            return
        }

        val imageFile = createImageFileFromBitmap(bitmap)

        val kodeBarang = barcode.toString()
        val qty = inputQty.text.toString()
        val ie_id = idItem.toString()
//        Log.d("ini adalah qty", "kirim: $qty")
        val requestBody = imageFile?.let {
            MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", it.name, it.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                .addFormDataPart("ie_id", ie_id)
                .addFormDataPart("ie_update_user", "$idUser")
                .addFormDataPart("ie_qty_pull", "$qty")
                .build()
        }

        val request = requestBody?.let {
            Request.Builder()
                .url("https://api.agungriyadi.web.id/apiMobile/upload-photo")
                .post(it)
                .build()
        }

        if (request != null) {
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string() // Baca konten respons
//                    Log.d("API_RESPONSE", responseBody ?: "Empty response")

                    if (response.isSuccessful && responseBody != null) {
                        activity?.runOnUiThread {
                            Toast.makeText(context, "Berhasil", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_detailItemFragment_to_tarikBarangFragment)
                            Log.d("berhasil nih", "berhasil upload")
                        }
                    } else {
                        Log.e("API_CALL_ERROR", "Error: ${response.code}")
                        activity?.runOnUiThread {
                            Toast.makeText(context, "Gagal. Kode status: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.e("API_CALL_ERROR", "Failed to execute request", e)
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun validateInputs() {
        fun checkInputs() {
            val isQtyFilled = inputQty.text.toString().isNotEmpty()
            val isImageTaken = resizedBitmap2 != null

            tarik.isEnabled = isQtyFilled && isImageTaken
            // Atur status tombol dan warna latar belakang
            if (isQtyFilled && isImageTaken) {
                tarik.isEnabled = true
                tarik.setBackgroundResource(R.drawable.button_shape)
                tarik.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                tarik.isEnabled = false
                tarik.setBackgroundResource(R.drawable.button_shape_grey)
                tarik.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkGrey))
            }

//            Log.d("tess", "checkInputs: $isImageTaken")
//            Log.d("tess", "checkInputs: $isQtyFilled")
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                checkInputs()
            }
        }

        inputQty.addTextChangedListener(textWatcher)

        checkInputs()

        fun setImage(bitmap: Bitmap?) {
            resizedBitmap2 = bitmap
            checkInputs()
        }
    }

}
