package com.guru.travelalone

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit


class Home_Activity : AppCompatActivity() {

    //하단바 ----------
    lateinit var homeButton: ImageButton
    lateinit var locateButton: ImageButton
    lateinit var travbotButton: ImageButton
    lateinit var mypageButton: ImageButton
    lateinit var communityButton: ImageButton
    //하단바 ----------

    // 날씨 api ----------------
    lateinit var weatherText : TextView
    private val apiKey = "" // key 숨기는 법을 몰라 그냥 빼눴습니다.
    private val city = "Seoul"
    lateinit var image1 : ImageView
    lateinit var image2 : ImageView
    lateinit var image3 : ImageView
    lateinit var image4 : ImageView
    lateinit var image5 : ImageView
    lateinit var title1 : TextView
    lateinit var title2 : TextView
    lateinit var title3 : TextView
    lateinit var title4 : TextView
    lateinit var title5 : TextView
    lateinit var sub1 : TextView
    lateinit var sub2 : TextView
    lateinit var sub3 : TextView
    lateinit var sub4 : TextView
    lateinit var sub5 : TextView
    // 날씨 api ----------------

    // FAB ---------------------
    lateinit var fab : FloatingActionButton
    lateinit var trip_bt : Button
    lateinit var trip_cd : CardView
    lateinit var trip_title : TextView
    lateinit var trip_date : TextView
    lateinit var trip_location : TextView
    lateinit var dbMangager: DBManager_1
    lateinit var sqlitedb: SQLiteDatabase
    private val dateFormat = SimpleDateFormat("MM.dd(E)", Locale.KOREAN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // 날씨 api ----------------
        weatherText = findViewById(R.id.weather_text)
        image1 = findViewById(R.id.image_1)
        image2 = findViewById(R.id.image_2)
        image3 = findViewById(R.id.image_3)
        image4 = findViewById(R.id.image_4)
        image5 = findViewById(R.id.image_5)
        title1 = findViewById(R.id.title_1)
        title2 = findViewById(R.id.title_2)
        title3 = findViewById(R.id.title_3)
        title4 = findViewById(R.id.title_4)
        title5 = findViewById(R.id.title_5)
        sub1 = findViewById(R.id.sub_1)
        sub2 = findViewById(R.id.sub_2)
        sub3 = findViewById(R.id.sub_3)
        sub4 = findViewById(R.id.sub_4)
        sub5 = findViewById(R.id.sub_5)
        getWeatherData() // 날씨 데이터 가져오는 함수 호출
        // 날씨 api ----------------

        // FAB -----------------
        fab = findViewById(R.id.fab)
        trip_bt = findViewById(R.id.bt_trip)
        trip_cd  = findViewById(R.id.cd_trip)
        trip_title = findViewById(R.id.trip_title)
        trip_date = findViewById(R.id.trip_date)
        trip_location = findViewById(R.id.trip_location)

        fab.setOnClickListener {
            val intent = Intent(
                this@Home_Activity,
                TripDate_Activity::class.java
            )
            startActivity(intent)
        }

        trip_bt.setOnClickListener {

        }
        // FAB -----------------

        // 데이터 베이스

        dbMangager = DBManager_1(this, "tripdate", null, 1)
        sqlitedb = dbMangager.readableDatabase
        var cursor: Cursor
        cursor = sqlitedb.rawQuery("SELECT * FROM tripdate;", null)
        if (cursor.count != 0) {
            fab.hide()
            cursor.moveToLast()
            cursor.moveToNext()
            while(cursor.getPosition() == cursor.count)
            {
                cursor.moveToPrevious()
                val str_title = cursor.getString(0)
                trip_title.text = str_title

                val str_location = cursor.getString(1)
                trip_location.text = str_location

                val long_start_date = cursor.getLong(2)
                val long_end_date: Long? = if (cursor.isNull(3)) null else cursor.getLong(3)
                if(long_end_date?.toInt() != 0)
                {
                    trip_date.text = "${dateFormat.format(long_start_date)}" +" ~ "+ "${dateFormat.format(long_end_date)}"
                }
                else
                {
                    trip_date.text = "${dateFormat.format(long_start_date)}"
                }
            }
            trip_cd.visibility = CardView.VISIBLE
            trip_bt.visibility = Button.VISIBLE
        } else {
            fab.show()
            trip_cd.visibility = CardView.GONE
            trip_bt.visibility = Button.GONE
        }
        cursor.close()
        sqlitedb.close()
        dbMangager.close()



        //하단바 ----------
        homeButton = findViewById(R.id.homeButton)
        locateButton = findViewById(R.id.locateButton)
        travbotButton = findViewById(R.id.travbotButton)
        mypageButton = findViewById(R.id.mypageButton)
        communityButton = findViewById(R.id.commuButton)

        locateButton.setOnClickListener {
            val intent = Intent(
                this@Home_Activity,
                Locate_Activity::class.java
            )
            startActivity(intent)
        }

        travbotButton.setOnClickListener {
            val intent = Intent(
                this@Home_Activity,
                Travbot_activity::class.java
            )
            startActivity(intent)
        }

        homeButton.setOnClickListener {
            val intent = Intent(
                this@Home_Activity,
                Home_Activity::class.java
            )
            startActivity(intent)
        }

        communityButton.setOnClickListener {
            val intent = Intent(
                this@Home_Activity,
                Community_Activity::class.java
            )
            startActivity(intent)
        }

        mypageButton.setOnClickListener {
            val intent = Intent(
                this@Home_Activity,
                Mypage_Activity::class.java
            )
            startActivity(intent)
        }

    }

    // 날씨 API 호출
    private fun getWeatherData() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(OpenWeatherApi::class.java)
        api.getWeather(city, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val weatherCondition = it.weather.first().description
                        val temperature = it.main.temp - 273.15 // Convert from Kelvin to Celsius
                        // 온도, 비 여부에 따라 문구 바뀌게 설정
                        val weather_id = when {
                            // rain
                            weatherCondition.contains("rain", ignoreCase = true) -> {
                                "rain"
                            }
                            // snow
                            weatherCondition.contains("snow", ignoreCase = true) -> {
                                "snow"
                            }
                            // 10도 미만
                            temperature < 10 -> {
                                "cold"
                            }
                            // 10도 이상 26도 미만
                            temperature in 10.0..25.0 -> {
                                "normal"
                            }
                            // 26도 이상
                            else -> {
                                "hot"
                            }
                        }

                        if(weather_id == "rain")
                        {
                            weatherText.text = "비오는날${String(Character.toChars(0x1F302))},\n따뜻한 실내에서 즐기는 힐링 타임!"
                            image1.setImageResource(R.drawable.rain_1)
                            title1.text = "애니메이션 박물관"
                            sub1.text = "강원 춘천시 서면 \n박사로 854 애니메이션박물관"
                            image2.setImageResource(R.drawable.rain_2)
                            title2.text = "서울 식물원"
                            sub2.text = "서울 강서구 \n마곡동로 161 서울식물원"
                            image3.setImageResource(R.drawable.rain_3)
                            title3.text = "테디베어뮤지엄"
                            sub3.text = "제주 서귀포시 \n중문관광로110번길 31"
                            image4.setImageResource(R.drawable.rain_4)
                            title4.text = "광명동굴"
                            sub4.text = "경기 광명시 가학로85번길 142"
                            image5.setImageResource(R.drawable.rain_5)
                            title5.text = "엑스더스카이"
                            sub5.text = "부산 해운대구 달맞이길 30"
                        }
                        else if(weather_id == "snow")
                        {
                            weatherText.text = "눈 내리는 겨울왕국으로 떠나보세요!\n 겨울 명소 추천${String(Character.toChars(0x2744))}"
                            image1.setImageResource(R.drawable.snow_1)
                            title1.text = "청송 얼음골"
                            sub1.text = "경북 청송군 주왕산면 \n팔각산로 228"
                            image2.setImageResource(R.drawable.snow_2)
                            title2.text = "청양 알프스마을"
                            sub2.text = "충남 청양군 정산면 \n천장호길 223-35"
                            image3.setImageResource(R.drawable.snow_3)
                            title3.text = "아산 공세리성당"
                            sub3.text = "충남 아산시 인주면 \n공세리성당길 10 공세리성당"
                            image4.setImageResource(R.drawable.snow_4)
                            title4.text = "나홀로나무"
                            sub4.text = "서울 송파구 올림픽로 424"
                            image5.setImageResource(R.drawable.snow_5)
                            title5.text = "서도역"
                            sub5.text = "전북 남원시 서도길 32"
                        }
                        else if(weather_id == "cold")
                        {
                            weatherText.text = "현재 기온은 ${"%.1f".format(temperature)}°C입니다.\n차가운 바람을 피해 따뜻한 실내로! \n오늘 가볼 만한 곳${String(Character.toChars(0x26C4))}"
                            image1.setImageResource(R.drawable.cold_1)
                            title1.text = "원마운트 스노우파크"
                            sub1.text = "경기 고양시 일산서구 \n한류월드로 300"
                            image2.setImageResource(R.drawable.cold_2)
                            title2.text = "빛의 벙커"
                            sub2.text = "제주 서귀포시 성산읍 \n서성일로1168번길 89-17 A동"
                            image3.setImageResource(R.drawable.cold_3)
                            title3.text = "씨라이프 아쿠아리움"
                            sub3.text = "부산 해운대구 \n해운대해변로 266"
                            image4.setImageResource(R.drawable.cold_4)
                            title4.text = "북수원 온천"
                            sub4.text = "경기 수원시 장안구 \n서부로 2139 SK허브 8층"
                            image5.setImageResource(R.drawable.cold_5)
                            title5.text = "롯데월드"
                            sub5.text = "서울 송파구 올림픽로 240"
                        }
                        else if(weather_id == "normal")
                        {
                            weatherText.text = "현재 기온은 ${"%.1f".format(temperature)}°C입니다.\n맑고 화창한 오늘, \n산책을 즐겨보세요!${String(Character.toChars(0x1F333))}"
                            image1.setImageResource(R.drawable.normal_1)
                            title1.text = "생각하는 공원"
                            sub1.text = "제주 제주시 한경면 \n녹차분재로 675"
                            image2.setImageResource(R.drawable.normal_2)
                            title2.text = "송도 센트럴파크"
                            sub2.text = "인천 연수구 컨벤시아대로 160"
                            image3.setImageResource(R.drawable.normal_3)
                            title3.text = "서울숲"
                            sub3.text = "서울 성동구 뚝섬로 273"
                            image4.setImageResource(R.drawable.normal_4)
                            title4.text = "소래습지 생태공원"
                            sub4.text = "인천 남동구 논현동 1-55"
                            image5.setImageResource(R.drawable.normal_5)
                            title5.text = "담양 죽녹원"
                            sub5.text = "전남 담양군 담양읍 \n죽녹원로 119"
                        }
                        else if(weather_id == "hot")
                        {

                            weatherText.text = "현재 기온은 ${"%.1f".format(temperature)}°C입니다.\n더운 여름날,\n시원한 물놀이가 최고!${String(Character.toChars(0x1F3C4))}"
                            image1.setImageResource(R.drawable.hot_1)
                            title1.text = "선셋비치"
                            sub1.text = "강원 양양군 현남면 \n새나루길 43 선셋비치"
                            image2.setImageResource(R.drawable.hot_2)
                            title2.text = "설리 해수욕장"
                            sub2.text = "경남 남해군 미조면 송정리"
                            image3.setImageResource(R.drawable.hot_3)
                            title3.text = "윤돌섬"
                            sub3.text = "경남 거제시 일운면 구조라리"
                            image4.setImageResource(R.drawable.hot_4)
                            title4.text = "철원 안양골"
                            sub4.text = "강원 철원군 철원읍 독서당길 225-134"
                            image5.setImageResource(R.drawable.hot_5)
                            title5.text = "캐리비안베이"
                            sub5.text = "경기 용인시 처인구 포곡읍 \n에버랜드로 199"


                        }
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                weatherText.text = "날씨 정보를 가져오는데 실패했습니다."
            }
        })
    }


}