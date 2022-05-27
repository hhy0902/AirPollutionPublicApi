package com.example.airpollutionpublicapi

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.airpollutionpublicapi.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private var cancellationTokenSource : CancellationTokenSource? = null

    private lateinit var geocoder: Geocoder

    private var lon : Double = 0.0
    private var lat : Double = 0.0

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
                Log.d("testt location ", "latitude : ${location.latitude}, longitude : ${location.longitude}")

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































