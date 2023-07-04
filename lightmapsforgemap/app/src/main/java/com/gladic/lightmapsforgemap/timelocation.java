package com.gladic.lightmapsforgemap;

import static org.mapsforge.map.android.graphics.AndroidGraphicFactory.getPaint;
import static java.lang.System.exit;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.layers.MyLocationOverlay;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;

public class timelocation  extends Service implements LocationListener {

    private MapView mapView;

    private IBinder binder;

    private boolean allowRebind;

    protected LocationManager locationManager;
    private MyLocationOverlay myLocationOverlay;

    String endlatitude ;
    String endlongitude ;

    String endaccurancy;

    @Override
    public void onCreate() {

        binder = new LocalBinder();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return -1;
            exit(-1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if(location.isFromMockProvider())
            {
                return;
            }
        }

        if(mapView == null) {

        }else
        {
            endlatitude = String.valueOf(location.getLatitude());
            endlongitude = String.valueOf(location.getLongitude());
            endaccurancy = String.valueOf(location.getAccuracy());

           // System.out.println("latitude:" + endlatitude + " " + "longitude:"+endlongitude);

            if(endlatitude == null && endlongitude == null) {}
            else{
                if(myLocationOverlay != null)
                    myLocationOverlay.setPosition(Double.valueOf(endlatitude), Double.valueOf(endlongitude), Float.valueOf(endaccurancy));

                mapView.setCenter(new LatLong(Double.valueOf(endlatitude),Double.valueOf(endlongitude)));
                //mapView.setZoomLevel((byte) 12);
            }
        }
    }


    public class LocalBinder extends Binder {
        timelocation getService() {
            // Return this instance of LocalService so clients can call public methods
            return timelocation.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        Log.d("bound", "bound");
        return binder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return allowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }
    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        /*AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                while(true){}
            }
        });
        */
        return android.app.Service.START_STICKY;
    }

    public void set_mapview(MapView mapView, MyLocationOverlay myLocationOverlay)
    {
        this.mapView = mapView;
        this.myLocationOverlay = myLocationOverlay;
    }

}
