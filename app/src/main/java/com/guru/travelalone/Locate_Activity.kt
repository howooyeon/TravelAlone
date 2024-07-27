package com.guru.travelalone

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle

class Locate_Activity : AppCompatActivity() {

    lateinit var mapView: MapView
    lateinit var kakaoMap: KakaoMap

    // 하단바 ----------
    lateinit var homeButton: ImageButton
    lateinit var locateButton: ImageButton
    lateinit var travbotButton: ImageButton
    lateinit var mypageButton: ImageButton
    lateinit var communityButton: ImageButton
    // 하단바 ----------

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var startPosition: LatLng? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var centerLabel: Label
    private var requestingLocationUpdates = false

    private lateinit var addressTextView: TextView
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_locate)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize TextViews
        addressTextView = findViewById(R.id.addressTextView)
        latitudeTextView = findViewById(R.id.latitudeTextView)
        longitudeTextView = findViewById(R.id.longitudeTextView)

        // 하단바 ----------
        homeButton = findViewById(R.id.homeButton)
        locateButton = findViewById(R.id.locateButton)
        travbotButton = findViewById(R.id.travbotButton)
        mypageButton = findViewById(R.id.mypageButton)
        communityButton = findViewById(R.id.commuButton)

        locateButton.setOnClickListener {
            val intent = Intent(
                this@Locate_Activity,
                Locate_Activity::class.java
            )
            startActivity(intent)
        }

        travbotButton.setOnClickListener {
            val intent = Intent(
                this@Locate_Activity,
                Travbot_activity::class.java
            )
            startActivity(intent)
        }

        homeButton.setOnClickListener {
            val intent = Intent(
                this@Locate_Activity,
                Home_Activity::class.java
            )
            startActivity(intent)
        }

        communityButton.setOnClickListener {
            val intent = Intent(
                this@Locate_Activity,
                Community_Activity::class.java
            )
            startActivity(intent)
        }

        mypageButton.setOnClickListener {
            val intent = Intent(
                this@Locate_Activity,
                Mypage_Activity::class.java
            )
            startActivity(intent)
        }

        // Kakao map api
        mapView = findViewById(R.id.map_view)

        // progressBar 초기화 - 수정된 부분
        progressBar = findViewById(R.id.progressBar)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 현재 위치가 너무 자주 업데이트되는 관계로, 30초마다 업데이트 되도록 설정
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000L).build()  // 30초 간격으로 위치 업데이트

        // LocationCallback 내에서 Label 이동 및 초기화 - 수정된 부분
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latLng = LatLng.from(location.latitude, location.longitude)
                    centerLabel.moveTo(latLng)
                    updateLocationInfo(location)  // Update location info
                }
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                locationPermissions[0]
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                locationPermissions[1]
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getStartLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                locationPermissions,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun updateLocationInfo(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        // Use a Geocoder to get the address (requires additional permissions)
        // Example:
        // val geocoder = Geocoder(this, Locale.getDefault())
        // val addressList = geocoder.getFromLocation(latitude, longitude, 1)
        // val address = addressList?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
        val address = "Address not available" // Placeholder

        addressTextView.text = "Address: $address"
        latitudeTextView.text = "Latitude: $latitude"
        longitudeTextView.text = "Longitude: $longitude"
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun getStartLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    startPosition = LatLng.from(location.latitude, location.longitude)
                    mapView.start(object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {
                            Log.d("KakaoMap", "onMapDestroy: ")
                        }

                        override fun onMapError(error: Exception) {
                            Log.e("KakaoMap", "onMapError: ", error)
                        }
                    }, object : KakaoMapReadyCallback() {

                        private fun getScaledBitmap(drawableResId: Int, scaleFactor: Float): Bitmap {
                            val originalBitmap = BitmapFactory.decodeResource(resources, drawableResId)
                            val width = (originalBitmap.width * scaleFactor).toInt()
                            val height = (originalBitmap.height * scaleFactor).toInt()
                            return Bitmap.createScaledBitmap(originalBitmap, width, height, true)
                        }

                        override fun onMapReady(map: KakaoMap) {
                            kakaoMap = map
                            progressBar.visibility = View.GONE  // 지도 로드가 완료되면 progressBar 숨기기

                            val layer = kakaoMap.labelManager?.layer
                            if (layer != null) {

                                // 마커 스케일링
                                val scaledBitmap = getScaledBitmap(R.drawable.red_dot_marker, 0.7f)
                                val customLabelStyle = LabelStyle.from(scaledBitmap)
                                    .setAnchorPoint(0.5f, 0.5f)

                                centerLabel = layer.addLabel(
                                    LabelOptions.from("centerLabel", startPosition)
                                        .setStyles(customLabelStyle)
                                        .setRank(1)
                                )
                            }
                            kakaoMap.trackingManager?.startTracking(centerLabel)
                            startLocationUpdates()
                        }

                        override fun getPosition(): LatLng {
                            return startPosition ?: LatLng.from(
                                37.5665,
                                126.9780
                            )  // Default to Seoul if no location
                        }

                        override fun getZoomLevel(): Int {
                            return 17
                        }
                    })
                }
            }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        requestingLocationUpdates = true
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getStartLocation()
            } else {
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setMessage("위치 권한 거부시 앱을 사용할 수 없습니다.")
            .setPositiveButton("권한 설정하러 가기") { dialogInterface, i ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:$packageName"))
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                    startActivity(intent)
                } finally {
                    finish()
                }
            }
            .setNegativeButton("앱 종료하기") { dialogInterface, i -> finish() }
            .setCancelable(false)
            .show()
    }
}
