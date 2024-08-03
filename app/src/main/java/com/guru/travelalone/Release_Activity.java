package com.guru.travelalone;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;

//APK 추출 후 카카오 API 사용 위해 등록할 릴리즈 키 확인하는 용도
public class Release_Activity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_release);

        textView = findViewById(R.id.textView);

        getAppkKeyHash();
    }

    private void getAppkKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);

                // 출력
                textView.setText(keyHash);
            }
        } catch (Exception e) {
            Log.e("못찾음", e.toString());
        }
    }
}
