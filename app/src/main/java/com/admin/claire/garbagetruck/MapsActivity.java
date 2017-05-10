package com.admin.claire.garbagetruck;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Delayed;

// 使用Google API用戶端，讓地圖元件類別實作需要的介面， GoogleApiClient
// 分別是在「com.google.android.gms.maps」套件下的ConnectionCallbacks與OnConnectionFailedListener
//接收位置更新資訊，需要在地圖顯示目前的位置，讓地圖元件類別實作需要的介面com.google.android.gms.location.LocationListener

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // Google API用戶端物件
    private GoogleApiClient googleApiClient;
    // Location請求物件
    private LocationRequest locationRequest;
    private LocationManager locationManager;

    // 記錄目前最新的位置
    private Location currentLocation;
    // 顯示目前與儲存位置的標記物件
    private Marker currentMarker;

    // 定位設備授權請求代碼
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 100;

    private GoogleMap mMap;
    //臺北市垃圾清運點位資訊
    private final String TRASH_TPE = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=aa9c657c-ea6f-4062-a645-b6c973d45564";
    private final String TRASH_TPE1 = "https://www.dropbox.com/s/f3yb3rvny6pwrj8/opendata_trash.json?dl=1";

    private String TAG = "JSON_TRASH";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 建立Google API用戶端物件
        configGoogleApiClient();
        // 建立Location請求物件
        // configLocationRequest();

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        getData();
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(25.068118999999999, 121.617786000000000);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // 建立位置的座標物件
//        LatLng place = sydney;
        // 移動地圖
//        moveMap(place);

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

    private void getData() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                TRASH_TPE1,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        parserJson(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: " + error.toString());

                        Toast.makeText(MapsActivity.this, error.toString(),
                                Toast.LENGTH_SHORT).show();

                    }
                }
        );
        //Volley.newRequestQueue(this).add(jsonObjectRequest);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void parserJson(JSONObject jsonObject) {

        try {
            JSONArray data = jsonObject.getJSONObject("result").getJSONArray("results");
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.getJSONObject(i);
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(o.getDouble("lat"), o.getDouble("lng")))
                        .title(o.getString("title"))
                        .snippet(o.getString("content"))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.garbagetruck))
                );

            }
        } catch (JSONException e) {
            e.printStackTrace();

        }

    }


    // ConnectionCallbacks
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // 已經連線到Google Services
        // 啟動位置更新服務
        Toast.makeText(MapsActivity.this, "Google API 連線成功", Toast.LENGTH_SHORT).show();
        Location location = enableLocationAndGetLastLocation(true);
        if (location != null) {
            Toast.makeText(MapsActivity.this, "成功取得上一次定位", Toast.LENGTH_SHORT).show();
            onLocationChanged(location);
        } else {
            Toast.makeText(MapsActivity.this, "沒有上一次定位的資料", Toast.LENGTH_SHORT).show();
        }
    }

    // ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int cause) {
        // Google Services無故連線中斷
        switch (cause) {
            case CAUSE_NETWORK_LOST:
                Toast.makeText(MapsActivity.this, "網路斷線，無法定位", Toast.LENGTH_SHORT).show();
                break;
            case CAUSE_SERVICE_DISCONNECTED:
                Toast.makeText(MapsActivity.this, "Google API 異常，無法定位",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Google Services連線失敗
        // ConnectionResult參數是連線失敗的資訊
        int errorCode = connectionResult.getErrorCode();

        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(this, R.string.google_play_service_missing,
                    Toast.LENGTH_LONG).show();
        }
    }

    // LocationListener
    @Override
    public void onLocationChanged(Location location) {
        // 位置改變
        // Location參數是目前的位置
        currentLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // 設定目前位置的標記
        if (currentMarker == null) {
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng));
        }
        else {
            currentMarker.setPosition(latLng);
        }
        // 移動地圖到目前的位置
        moveMap(latLng);
    }

    // 建立Google API用戶端物件
    private synchronized void configGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // 建立Location請求物件
    private void configLocationRequest() {
        locationRequest = new LocationRequest();
        // 設定讀取位置資訊的間隔時間為一秒（1000ms）
        locationRequest.setInterval(1000);
        // 設定讀取位置資訊最快的間隔時間為一秒（1000ms）
        locationRequest.setFastestInterval(1000);
        // 設定優先讀取高精確度的位置資訊（GPS）
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 連線到Google API用戶端
        if (!googleApiClient.isConnected() && currentMarker != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 移除位置請求服務
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 移除Google API用戶端連線
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    //啟用定位權限和最後一次定位記錄
    private Location enableLocationAndGetLastLocation(boolean on) {
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            //這項功能尚未取得使用者的同意
            //開始執行徵詢使用者的流程
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder altDlgBuilder =
                        new AlertDialog.Builder(MapsActivity.this);
                altDlgBuilder.setTitle("提示");
                altDlgBuilder.setMessage("App 需要啟動定位功能。");
                altDlgBuilder.setIcon(android.R.drawable.ic_dialog_info);
                altDlgBuilder.setCancelable(false);
                altDlgBuilder.setPositiveButton("確定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //顯示詢問使用者是否同意功能權限的對話盒
                                //使用者答覆後會執行 onRequestPermissionResult()
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_FINE_LOCATION_PERMISSION);
                            }
                        });
                altDlgBuilder.show();
                return null;
            } else {
                //顯示詢問使用者是否同意功能權限的對話盒
                //使用者答覆後會執行callback方法 onRequestPermissionResult()
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION_PERMISSION);
                return null;
            }
        }
        //這項功能之前已經取得使用者的同意，可以直接使用
        Location lastLocation = null;
        if (on) {
            //取得上一次定位資料
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            //準備一個LocationRequest物件，設定定位參數，在啟動定位時使用
            locationRequest = LocationRequest.create();
            //設定兩次定位之間的時間間隔，單位是千分之一秒
            locationRequest.setInterval(5000);
            //二次定位之間的最大距離，單位是公尺
            locationRequest.setSmallestDisplacement(5);

            //啟動定位，如果GPS功能有開啟，優先使用GPS定位，否則使用網路定位
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                Toast.makeText(MapsActivity.this, "使用GPS定位", Toast.LENGTH_SHORT).show();

            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                Toast.makeText(MapsActivity.this, "使用網路定位", Toast.LENGTH_SHORT).show();
            }

            //啟動定位功能
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);
        } else {
            //停止定位功能
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            Toast.makeText(MapsActivity.this, "停止定位", Toast.LENGTH_SHORT).show();
        }

        return lastLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //檢查收到的權限要求編號是否和我們送出的相同
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            if (grantResults.length != 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //再檢查一次就會進入同意的狀態，並且順利啟動
                Location location = enableLocationAndGetLastLocation(true);

                if (location != null) {
                    Toast.makeText(MapsActivity.this, "成功取得上一次定位",
                            Toast.LENGTH_SHORT).show();
                    //更新地圖的定位
                    onLocationChanged(location);
                } else {
                    Toast.makeText(MapsActivity.this, "沒有上一次定位的資料",
                            Toast.LENGTH_SHORT).show();

                    return;
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
