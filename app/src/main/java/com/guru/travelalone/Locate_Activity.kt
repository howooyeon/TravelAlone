package com.guru.travelalone

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.OnSuccessListener
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

    companion object {
        //onRequestPermissionsResult에서 권한 요청 결과를 받기 위한 request code
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        //요청할 위치 권한 목록
        private val locationPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private val fusedLocationClient: FusedLocationProviderClient? = null
    private var startPosition: LatLng? = null
    private val progressBar: ProgressBar? = null
    private var centerLabel: Label? = null
    private var requestingLocationUpdates = false
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    private val readyCallback: KakaoMapReadyCallback = object : KakaoMapReadyCallback() {
        override fun onMapReady(kakaoMap: KakaoMap) {
            progressBar!!.visibility = View.GONE
            val layer = kakaoMap.labelManager!!.layer
            centerLabel = layer!!.addLabel(
                LabelOptions.from("centerLabel", startPosition)
                    .setStyles(
                        LabelStyle.from(R.drawable.red_dot_marker).setAnchorPoint(0.5f, 0.5f)
                    )
                    .setRank(1)
            )
            val trackingManager = kakaoMap.trackingManager
            trackingManager!!.startTracking(centerLabel)
            startLocationUpdates()
        }

        override fun getPosition(): LatLng {
            return startPosition!!
        }

        override fun getZoomLevel(): Int {
            return 17
        }
    }

    //하단바 ----------
    lateinit var homeButton: ImageButton
    lateinit var locateButton: ImageButton
    lateinit var travbotButton: ImageButton
    lateinit var mypageButton: ImageButton
    lateinit var communityButton: ImageButton
    //하단바 ----------

   @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_locate)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //하단바 ----------
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

        // Check and request location permissions
        if (ContextCompat.checkSelfPermission(this, locationPermissions[0]) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, locationPermissions[1]) == PackageManager.PERMISSION_GRANTED) {
            // Permissions are already granted
            getCurLocation()
        } else {
            // Request location permissions
            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION_REQUEST_CODE)
        }

        //kakao map api
        mapView = findViewById(R.id.map_view)
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API가 정상적으로 종료될 때 호출
                Log.d("KakaoMap", "onMapDestroy: ")
            }

            override fun onMapError(error: Exception) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출
                Log.e("KakaoMap", "onMapError: ", error)
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                // 정상적으로 인증이 완료되었을 때 호출
                // KakaoMap 객체를 얻어 옵니다.
                kakaoMap = map
            }
        })

//    fun enableEdgeToEdge() {
//        // Your implementation for enabling edge-to-edge UI
//    }
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient!!.removeLocationUpdates(locationCallback!!)
    }

    @SuppressLint("MissingPermission")
    private fun getCurLocation() {
        fusedLocationClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            ?.addOnSuccessListener(
                this,
                OnSuccessListener<Location> { location: Location? ->
                    if (location != null) {
                        startPosition =
                            LatLng.from(location.latitude, location.longitude)
                        mapView.start(readyCallback)
                    }
                })
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        requestingLocationUpdates = true
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    // 사용자가 권한 요청 다이얼로그에 응답하면 이 메소드가 실행
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 위치 권한 요청에 대한 응답일 경우
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 사용자가 위치 권한을 허가했을 경우
                getCurLocation() // 현재 위치 가져오는 메서드
            } else {
                // 위치 권한이 거부되었을 경우, 다이얼로그를 띄워서 사용자에게 앱을 종료할지, 권한 설정 화면으로 이동할지 선택
                showPermissionDeniedDialog()
            }
        }
    }

//    private fun getCurLocation() {
//        TODO("Not yet implemented")
//    }

    // Show a dialog when location permissions are denied
    private fun showPermissionDeniedDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("위치 권한 거부시 앱을 사용할 수 없습니다.")
            .setPositiveButton("권한 설정하러 가기") { dialogInterface, i ->
                // 권한 설정하러 가기 버튼 클릭시 해당 앱의 상세 설정 화면으로 이동
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:$packageName"))
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                    startActivity(intent)
                } finally {
                    finish()
                }
            }
            .setNegativeButton("앱 종료하기") { dialogInterface, i ->
                // 앱 종료하기 버튼 클릭시 앱을 종료
                finish()
            }
            .setCancelable(false)
            .show()
    }
}