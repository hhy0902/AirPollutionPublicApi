package com.example.airpollutionpublicapi

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.airpollutionpublicapi.AirPollutionData.Pollution
import com.example.airpollutionpublicapi.databinding.ActivityMainBinding
import com.example.airpollutionpublicapi.stationData.Station
import com.example.airpollutionpublicapi.tmData.TmCordinatesResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private var cancellationTokenSource : CancellationTokenSource? = null

    private lateinit var geocoder: Geocoder

    private var lon : Double = 0.0
    private var lat : Double = 0.0
    private var tmX : Double = 0.0
    private var tmY : Double = 0.0
    private var address : String = ""

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        requestPermission()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("testt", "승낙")
                getLocation()
            } else {
                Log.d("testt", "거부")
                finish()
            }
        }
    }

    private fun getAirPollution() {
        val url = "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?serviceKey=JCrJa4%2F4eF07FKbnkSi7BDDUvnJXCE1CTiyt%2FfnxJ%2B7jewHaXTp5hrKQzOKdWYctQB%2B3a%2FHLuUHkTPq4hqrxvA%3D%3D&returnType=json&numOfRows=100&pageNo=1&stationName=%EB%B3%B5%EC%A0%95%EB%8F%99&dataTerm=DAILY&ver=1.3"

        val retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retrofitService = retrofit.create(RetrofitService::class.java)

        retrofitService.getPollution(address).enqueue(object : Callback<Pollution> {
            override fun onResponse(call: Call<Pollution>, response: Response<Pollution>) {
                if (response.isSuccessful) {
                    val main = response.body()

                    val pollutionList = main?.response?.body?.items?.firstOrNull()

                    Log.d("testt main","${main}")
                    Log.d("testt pollutionList","${pollutionList}")
                    Log.d("testt pm25","${pollutionList?.pm25Value}")
                }
            }

            override fun onFailure(call: Call<Pollution>, t: Throwable) {
                Log.d("testt","${t.message}")
            }
        })
    }

    private fun getStationAddress() {
        val url = "http://apis.data.go.kr/B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList?serviceKey=JCrJa4%2F4eF07FKbnkSi7BDDUvnJXCE1CTiyt%2FfnxJ%2B7jewHaXTp5hrKQzOKdWYctQB%2B3a%2FHLuUHkTPq4hqrxvA%3D%3D&returnType=json&tmX=244148.546388&tmY=412423.75772"

        val retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retrofitService = retrofit.create(RetrofitService::class.java)

        retrofitService.getStation(tmX, tmY).enqueue(object : Callback<Station> {
            override fun onResponse(call: Call<Station>, response: Response<Station>) {
                if (response.isSuccessful) {
                    val station = response.body()

                    val stationAddress = station?.response?.body?.items?.firstOrNull()?.stationName
                    address = stationAddress.toString()

                    Log.d("testt station", "${station}")
                    Log.d("testt address", "${address}")
                    Log.d("testt address2", "${stationAddress}")

                    getAirPollution()
                }
            }

            override fun onFailure(call: Call<Station>, t: Throwable) {
                Log.d("testt","${t.message}")
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        cancellationTokenSource = CancellationTokenSource()
        fusedLocationProviderClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource!!.token
        ).addOnSuccessListener { location ->
            try {
                lat = location.latitude
                lon = location.longitude
                Log.d("testt location ", "latitude : ${lat}, longitude : ${lon}")

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://dapi.kakao.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val retrofitService = retrofit.create(RetrofitService::class.java)

                retrofitService.getTmCoordinates(lon, lat).enqueue(object : Callback<TmCordinatesResponse> {
                    override fun onResponse(
                        call: Call<TmCordinatesResponse>,
                        response: Response<TmCordinatesResponse>) {
                        if (response.isSuccessful) {
                            val main = response.body()
                            val tm = main?.documents

                            tmX = tm?.get(0)?.x!!
                            tmY = tm?.get(0)?.y!!
                            Log.d("testt main","${main}")
                            Log.d("testt tm", "${tmX}, ${tmY}")

                            getStationAddress()
                        }
                    }

                    override fun onFailure(call: Call<TmCordinatesResponse>, t: Throwable) {
                        Log.d("testt","${t.message}")
                    }

                })


            } catch (e : IOException) {
                e.printStackTrace()
                Toast.makeText(this,"error 발생 다시 시도", Toast.LENGTH_SHORT).show()
            } finally {
                Log.d("testt finish","finish")
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ), REQUEST_ACCESS_LOCATION_PERMISSIONS
        )
    }

    companion object {
        private const val REQUEST_ACCESS_LOCATION_PERMISSIONS = 1000
    }
}































