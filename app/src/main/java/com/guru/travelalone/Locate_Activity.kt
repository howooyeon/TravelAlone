package com.guru.travelalone

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
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
import android.widget.Toast
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
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import java.util.Locale

class Locate_Activity : AppCompatActivity() {

    // 카카오 맵
    lateinit var mapView: MapView
    lateinit var kakaoMap: KakaoMap

    // location 가져오기
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

    // location 표시하기
    private lateinit var addressTextView: TextView
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView

    // 공유 버튼
    private lateinit var kakaoButton: ImageButton
    private lateinit var messageButton: ImageButton

    // 하단바 ----------
    lateinit var homeButton: ImageButton
    lateinit var locateButton: ImageButton
    lateinit var travbotButton: ImageButton
    lateinit var mypageButton: ImageButton
    lateinit var communityButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_locate)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addressTextView = findViewById(R.id.addressTextView)
        latitudeTextView = findViewById(R.id.latitudeTextView)
        longitudeTextView = findViewById(R.id.longitudeTextView)

        kakaoButton = findViewById(R.id.kakaoButton)
        messageButton = findViewById(R.id.messageButton)

        kakaoButton.setOnClickListener {
            shareLocationViaKakao()
        }

        messageButton.setOnClickListener {
            shareLocationViaMessage()
        }

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

        // progressBar
        progressBar = findViewById(R.id.progressBar)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 현재 위치가 너무 자주 업데이트 되는 관계로, 30초마다 업데이트 되도록 설정
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000L).build()  // 30초 간격으로 위치 업데이트

        // LocationCallback 내에서 Label 이동 및 초기화
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latLng = LatLng.from(location.latitude, location.longitude)
                    centerLabel.moveTo(latLng)
                    updateLocationInfo(location)  // Update location info
                }
            }
        }

        // 접근 권한 허용시, 주소 가져오기
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

        // Kakao SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_API_KEY)
    }

    // 카카오톡으로 위치 공유하기
    private fun shareLocationViaKakao() {
        val address = addressTextView.text.toString()
        val latitude = latitudeTextView.text.toString().split(":")[1].trim()
        val longitude = longitudeTextView.text.toString().split(":")[1].trim()

        val message = "현재 '$address (위도: $latitude\n, 경도: $longitude)'에서 여행 중이에요!"

        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.setPackage("com.kakao.talk")
            intent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(Intent.createChooser(intent, "카카오톡으로 공유"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "카카오톡이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show() // 예외 처리
        }
    }

    // 메시지로 위치 공유하기
    private fun shareLocationViaMessage() {
        val address = addressTextView.text.toString()
        val latitude = latitudeTextView.text.toString().split(":")[1].trim()
        val longitude = longitudeTextView.text.toString().split(":")[1].trim()

        val message = "현재 '$address (위도: $latitude\n, 경도: $longitude)'에서 여행 중이에요!"

        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("smsto:")
            intent.putExtra("sms_body", message)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) { // 예외처리
            Toast.makeText(this, "메시지 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 현재 위치 정보 업데이트하는 코드
    private fun updateLocationInfo(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        // Geocoder를 사용하여 주소를 가져오기
        val geocoder = Geocoder(this, Locale.getDefault())
        val addressList = geocoder.getFromLocation(latitude, longitude, 1)
        var address = addressList?.firstOrNull()?.getAddressLine(0) ?: "주소를 찾을 수 없습니다."

        // "대한민국" 제거하기
        address = address.replace("대한민국", "").trim()

        // 주소에서 중복된 단어 제거하기
        address = removeDuplicateWords(address)

        // 문자 다음에 숫자가 오는 경우에 띄어쓰기를 추가하기
        address = address.replace(Regex("([가-힣a-zA-Z])([0-9])"), "$1 $2")

        addressTextView.text = address
        latitudeTextView.text = "위도: $latitude"
        longitudeTextView.text = "경도: $longitude"
    }

    // 중복된 단어 제거 함수
    private fun removeDuplicateWords(input: String): String {
        val words = input.split(" ")
        val uniqueWords = linkedSetOf<String>()
        words.forEach { uniqueWords.add(it) }
        return uniqueWords.joinToString(" ")
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

                                // 현재 위치 마커 스케일링
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
                            )  // Default 값
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

    // 권한 요청 결과
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

    // 권한 거부시 다이얼로그
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
