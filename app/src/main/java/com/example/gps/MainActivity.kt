package com.example.gps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.example.gps.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    //this key should be unique if you are using another permissions in the app
    private val PERMISSION_ID = 100

    //Google's API for location services.
    private lateinit var fusedLocation: FusedLocationProviderClient

    //Location request is config file for all settings related to FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //to disable the night mode in the application enable this class
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Now initiate the fusedlocationproviderclient variable
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        binding.locationButton.setOnClickListener {

            getLastLocation()

            //now we will get the city name and country name
        }

    }

    //Firstly, we create a function that will check the uses permission
    private fun checkPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    //Secondly, if the function returns false, then we need the permission from user, so we will request permission
    //from the user, for this,  create a function
    //this function will allow the app to tell the user to give the necessary permission if they are not granted
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_ID
        )
    }
    //Third, Now we have the permission we still need to check the location service is enabled, so for that create another function

    private fun isLocationEnabled(): Boolean {
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    //Fourth, this built-in function will check permission result
    //we will use this just for debugging our code
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Debug", "You have the permission")
            }
        }
    }

    //Fifth, now we will create a function that allow us to get the last location
    private fun getLastLocation() {
        //first we check the permission

        if (checkPermission()) {
            //now check the location service is enabled
            if (isLocationEnabled()) {
                //Now, let's get the location
                fusedLocation.lastLocation.addOnCompleteListener { task ->
                    val location = task.result
                    if (location == null) {
                        //if the location is null then we will get the new user location
                        //Sixth, for that we need to create the function to get new location
                        getNewLocation()

                    } else {
                        binding.latitude.text = "Latitude: "+location.latitude
                        binding.longitude.text = "Longitude: " + location.longitude
                        //now add function to get the city name and country name
                        binding.cityText.text = "Your City: "+getCityName(location.latitude,location.longitude)
                        binding.countryText.text = "Your Country: "+getCountryName(location.latitude,location.longitude)

                    }
                }

            } else {
                Toast.makeText(this, "Please enable your location service", Toast.LENGTH_SHORT)
                    .show()
            }

        } else {
            requestPermission()
        }
    }

    private fun getNewLocation() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocation!!.requestLocationUpdates(
            locationRequest,locationCallback, Looper.myLooper()
        )
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation = p0.lastLocation
            //now we will set the new location
            binding.latitude.text = "Latitude: " + lastLocation!!.latitude
            binding.longitude.text = "Longitude: " + lastLocation!!.longitude
            //now add function to get the city name and country name
            binding.cityText.text = "Your City: "+getCityName(lastLocation.latitude,lastLocation.longitude)
            binding.countryText.text = "Your Country: "+getCountryName(lastLocation.latitude,lastLocation.longitude)

        }
    }
    //function to get the city name
    private fun getCityName(lat:Double, lon:Double):String{
        var cityName=""
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address = geoCoder.getFromLocation(lat,lon,1)
        cityName = address!!.get(0).locality
        return cityName
    }
    //function to get the country name
    private fun getCountryName(lat:Double, lon:Double):String{
        var countryName=""
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address = geoCoder.getFromLocation(lat,lon,1)
        countryName = address!!.get(0).countryName
        return countryName
    }

}