package com.example.airpollutionpublicapi

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private var cancellationTokenSource : CancellationTokenSource? = null

    private lateinit var geocoder: Geocoder

    private var lon : Double = 0.0
    private var lat : Double = 0.0
    private var tmX : Double = 0.0
    private var tmY : Double = 0.0
    private var address : String = ""

    private var so2 = "" // 아황산가스
    private var co = "" // 일산화탄소
    private var o3 = "" // 오존
    private var no2 = "" // 일산화질소
    private var pm10 = ""
    private var pm25 = ""

    private var so2Grade = "" // 아황산가스
    private var coGrade = "" // 일산화탄소
    private var o3Grade = "" // 오존
    private var no2Grade = "" // 일산화질소
    private var pm10Grade = ""
    private var pm25Grade = ""
    private var khaiGrade = "" //통합대기질


    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        requestPermission()
        bindViews()

    }

    private fun bindViews() {
        binding.refresh.setOnRefreshListener {
            Log.d("testt refresh","refresh")
            binding.errorTextView.visibility = View.INVISIBLE
            binding.layout2.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
            getLocation()

        }
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

                    so2 = pollutionList?.so2Value.toString()
                    co = pollutionList?.coValue.toString()
                    o3 = pollutionList?.o3Value.toString()
                    no2 = pollutionList?.no2Value.toString()
                    pm10 = pollutionList?.pm10Value.toString()
                    pm25 = pollutionList?.pm25Value.toString()

                    khaiGrade = pollutionList?.khaiGrade.toString()
                    so2Grade = pollutionList?.so2Grade.toString()
                    coGrade = pollutionList?.coGrade.toString()
                    o3Grade = pollutionList?.o3Grade.toString()
                    no2Grade = pollutionList?.no2Grade.toString()
                    pm10Grade = pollutionList?.pm10Grade1h.toString()
                    pm25Grade = pollutionList?.pm25Grade1h.toString()

                    binding.so2ValueTextView.text = so2+" ppm"
                    binding.coValueTextView.text = co+" ppm"
                    binding.o3ValueTextView.text = o3+" ppm"
                    binding.no2ValueTextView.text = no2+" ppm"

                    Log.d("testt khaiGrade", "${khaiGrade}")

                    when(khaiGrade) {
                        "1" -> {
                            binding.totalGradeTextView.text = "좋음"
                            binding.totalGradleImage.setImageResource(R.drawable.verygood)
                            binding.layout2.setBackgroundResource(R.color.blue)
                        }
                        "2" -> {
                            binding.totalGradeTextView.text = "보통"
                            binding.totalGradleImage.setImageResource(R.drawable.bad)
                            binding.layout2.setBackgroundResource(R.color.green)
                        }
                        "3" -> {
                            binding.totalGradeTextView.text = "나쁨"
                            binding.totalGradleImage.setImageResource(R.drawable.bad2_1)
                            binding.layout2.setBackgroundResource(R.color.yellow)
                        }
                        "4" -> {
                            binding.totalGradeTextView.text = "매우나쁨"
                            binding.totalGradleImage.setImageResource(R.drawable.verybad)
                            binding.layout2.setBackgroundResource(R.color.red)
                        }
                        else -> {
                            binding.totalGradeTextView.text = "데이터 없음"
                            binding.totalGradleImage.setImageResource(R.drawable.question)
                            binding.layout2.setBackgroundResource(R.color.gray)
                        }
                    }

                    when(pm10Grade) {
                        "1" -> {
                            binding.pm10ValueTextView.text = "미세먼지 : $pm10㎍/㎥  😆"
                        }
                        "2" -> {
                            binding.pm10ValueTextView.text = "미세먼지 : $pm10㎍/㎥  😐"
                        }
                        "3" -> {
                            binding.pm10ValueTextView.text = "미세먼지 : $pm10㎍/㎥  🙁"
                        }
                        "4" -> {
                            binding.pm10ValueTextView.text = "미세먼지 : $pm10㎍/㎥  😫"
                        }
                        else -> {
                            binding.pm10ValueTextView.text = "미세먼지 : 데이터 없음 🧐"
                        }
                    }

                    when(pm25Grade) {
                        "1" -> {
                            binding.pm25ValueTextView.text = "초미세먼지 : ${pm25}㎍/㎥  😆"
                        }
                        "2" -> {
                            binding.pm25ValueTextView.text = "초미세먼지 : ${pm25}㎍/㎥  😐"
                        }
                        "3" -> {
                            binding.pm25ValueTextView.text = "초미세먼지 : ${pm25}㎍/㎥  🙁"
                        }
                        "4" -> {
                            binding.pm25ValueTextView.text = "초미세먼지 : ${pm25}㎍/㎥  😫"
                        }
                        else -> {
                            binding.pm25ValueTextView.text = "초미세먼지 : 데이터 없음 🧐"
                        }
                    }

                    when(so2Grade) {
                        "1" -> binding.so2GradeTextView.text = "좋음  😆"
                        "2" -> binding.so2GradeTextView.text = "보통  😐"
                        "3" -> binding.so2GradeTextView.text = "나쁨 🙁"
                        "4" -> binding.so2GradeTextView.text = "매우나쁨 😫"
                        else -> binding.so2GradeTextView.text = "데이터 없음 🧐"
                    }

                    when(coGrade) {
                        "1" -> binding.coGradeTextView.text = "좋음  😆"
                        "2" -> binding.coGradeTextView.text = "보통  😐"
                        "3" -> binding.coGradeTextView.text = "나쁨 🙁"
                        "4" -> binding.coGradeTextView.text = "매우나쁨 😫"
                        else -> binding.coGradeTextView.text = "데이터 없음 🧐"
                    }

                    when(o3Grade) {
                        "1" -> binding.o3GradeTextView.text = "좋음  😆"
                        "2" -> binding.o3GradeTextView.text = "보통  😐"
                        "3" -> binding.o3GradeTextView.text = "나쁨 🙁"
                        "4" -> binding.o3GradeTextView.text = "매우나쁨 😫"
                        else -> binding.o3GradeTextView.text = "데이터 없음 🧐"
                    }

                    when(no2Grade) {
                        "1" -> binding.no2GradeTextView.text = "좋음  😆"
                        "2" -> binding.no2GradeTextView.text = "보통  😐"
                        "3" -> binding.no2GradeTextView.text = "나쁨 🙁"
                        "4" -> binding.no2GradeTextView.text = "매우나쁨 😫"
                        else -> binding.no2GradeTextView.text = "데이터 없음 🧐"
                    }

                    binding.progressBar.visibility = View.INVISIBLE
                    binding.layout2.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<Pollution>, t: Throwable) {
                Log.d("testt","${t.message}")
                binding.progressBar.visibility = View.INVISIBLE
                binding.errorTextView.visibility = View.VISIBLE
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
                    val longAddress = station?.response?.body?.items?.firstOrNull()?.addr
                    address = stationAddress.toString()

                    binding.stationAddressTextView.text = "측정소 : ${longAddress}"
                    Log.d("testt station", "${station}")
                    Log.d("testt address", "${address}")
                    Log.d("testt address2", "${stationAddress}")

                    getAirPollution()
                }
            }

            override fun onFailure(call: Call<Station>, t: Throwable) {
                Log.d("testt","${t.message}")
                binding.progressBar.visibility = View.INVISIBLE
                binding.errorTextView.visibility = View.VISIBLE
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

                geocoder = Geocoder(this, Locale.getDefault())

                val address = geocoder.getFromLocation(lat, lon, 1)
                Log.d("testt subLocality","${address[0].subLocality}") // 송파구
                Log.d("testt thoroughfare","${address[0].thoroughfare}") // 문정동

                var subLocality = address[0].subLocality
                var thoroughfare = address[0].thoroughfare

                if (thoroughfare == null)
                    thoroughfare = ""

                binding.myAddress.text = "${subLocality} ${thoroughfare}"

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
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.errorTextView.visibility = View.VISIBLE
                    }
                })

            } catch (e : IOException) {
                e.printStackTrace()
                Toast.makeText(this,"error 발생 다시 시도", Toast.LENGTH_SHORT).show()
            } finally {
                Log.d("testt finish","finish")
                binding.refresh.isRefreshing = false
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































