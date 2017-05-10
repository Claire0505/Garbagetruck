package com.admin.claire.garbagetruck;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Garbagetruck_List_Activity extends FragmentActivity
        implements OnMapReadyCallback {

    private TextView mTilte, mContent, mLng, mLat;
    private GoogleMap mMap;
    private String TAG = "MAP: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garbagetruck__list_);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapfrag);
        mapFragment.getMapAsync(this);

       // initView();

    }

    private void initView() {
        mTilte = (TextView)findViewById(R.id.title_Text);
        mContent = (TextView)findViewById(R.id.content_Text);
        mLng = (TextView)findViewById(R.id.lng_Text);
        mLat = (TextView)findViewById(R.id.lat_Text);
        //取得傳遞過來的資料
        Intent  intent = this.getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String lng = intent.getStringExtra("lng");
        String lat = intent.getStringExtra("lat");

        mTilte.setText(title);
        mContent.setText(content);
        mLng.setText(getResources().getString(R.string.lng)+ lng);
        mLat.setText(getResources().getString(R.string.lat)+ lat);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mTilte = (TextView)findViewById(R.id.title_Text);
        mContent = (TextView)findViewById(R.id.content_Text);
        mLng = (TextView)findViewById(R.id.lng_Text);
        mLat = (TextView)findViewById(R.id.lat_Text);
        //取得傳遞過來的資料
        Intent  intent = this.getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String lng = intent.getStringExtra("lng");
        String lat = intent.getStringExtra("lat");

        mTilte.setText(title);
        mContent.setText(content);
        mLng.setText(getResources().getString(R.string.lng)+ lng);
        mLat.setText(getResources().getString(R.string.lat)+ lat);

        Double lngD  = Double.valueOf(lng);
        Double latD = Double.valueOf(lat);
        LatLng garbage = new LatLng(latD , lngD);

       // Log.e(TAG, "onMapReady: " + latD + " , " + lngD );

        mMap.addMarker(new MarkerOptions()
                        .position(garbage)
                        .title(title)
                        .snippet(content)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.garbagetruck)));


        //建立位置的座標物件
        LatLng place = garbage;
        //移動地圖
        moveMap(place);

    }

    // 移動地圖到參數指定的位置
    private void moveMap(LatLng place) {
        // 建立地圖攝影機的位置物件
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(place)
                        .zoom(17)
                        .build();

        // 使用動畫的效果移動地圖
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
