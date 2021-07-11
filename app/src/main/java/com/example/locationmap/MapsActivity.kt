package com.example.locationmap

import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import com.example.locationmap.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var currentLocation: Location? = null
    var currentMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchCurrentLocation()
    }

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1000
            )
            return
        }
        val task = fusedLocationProviderClient?.lastLocation
        task?.addOnSuccessListener { location ->
            if (location != null) {
                this.currentLocation = location
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val latLong = LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
        drawMarker(latLong)
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDrag(p0: Marker) {
            }

            override fun onMarkerDragStart(p0: Marker) {

            }

            override fun onMarkerDragEnd(p0: Marker) {
                if (currentMarker != null) {
                    currentMarker?.remove()
                    val newLatLong = LatLng(p0.position.latitude, p0.position.longitude)
                    drawMarker(newLatLong)
                }
            }
        })
    }

    private fun drawMarker(latLong: LatLng) {
        val marker = MarkerOptions().position(latLong).title("Location")
            .snippet(getAddress(latLong.latitude, latLong.longitude)).draggable(true)
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLong))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f))
        currentMarker = mMap.addMarker(marker)
        currentMarker?.showInfoWindow()
    }

    private fun getAddress(latitude: Double, longitude: Double): String? {
        val geoCoder = Geocoder(this, Locale.getDefault())
        val addressList = geoCoder.getFromLocation(latitude, longitude, 1)
        return addressList[0].getAddressLine(0).toString()
    }
}