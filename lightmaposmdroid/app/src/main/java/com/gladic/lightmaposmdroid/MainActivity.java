package com.gladic.lightmaposmdroid;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import org.mapsforge.map.android.rendertheme.AssetsRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.osmdroid.config.Configuration;
import org.osmdroid.mapsforge.MapsForgeTileProvider;
import org.osmdroid.mapsforge.MapsForgeTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private MapView map;

    private CompassOverlay mCompassOverlay;
    private Overlay mRotationGestureOverlay;
    private ScaleBarOverlay mScaleBarOverlay;

    private MapsForgeTileSource fromFiles = null;
    private MapsForgeTileProvider forge = null;
    private XmlRenderTheme theme = null; //null is ok here, uses the default rendering theme if it's not set

    private Context ctx;

    private boolean mapsforge = false;
    private File[] maps = null;

    private String getFilename() {

        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath + "/Download/MAPS" );
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        map = (MapView) findViewById(R.id.mapview);

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string
        //inflate and create the map
        //setContentView(R.layout.activity_main);

        maps = new File(getFilename()).listFiles();

        if (maps == null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
        } else if (mapsforge) {

            /*
            * This should work on old versions too , but it simple fail now
            * Can load map from mapsforge but it gave illegal argument exception and this be more easy if got good response on github
            *  */
            try {
                //this file should be picked up by the mapsforge dependencies
                theme = new AssetsRenderTheme(ctx.getApplicationContext().getAssets(), "renderthemes/", "rendertheme-v4.xml");
                //alternative: theme = new ExternalRenderTheme(userDefinedRenderingFile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            fromFiles = MapsForgeTileSource.createFromFiles(maps, theme, "rendertheme-v4");
            forge = null;

            try {
                forge = new MapsForgeTileProvider(
                        new SimpleRegisterReceiver(ctx),
                        fromFiles, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

          //  map.setUseDataConnection(false);
            map.setTileProvider(forge);
            map.setTileSource(forge.getTileSource());
          //  map.setBuiltInZoomControls(true);
            //map.zoomToBoundingBox(fromFiles.getBoundsOsmdroid(), false);

        } else {
            map.setTileSource(TileSourceFactory.MAPNIK);

        }

        //COMPASS
        mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), map);
        mCompassOverlay.enableCompass();
        mCompassOverlay.setPointerMode(true);
        mCompassOverlay.setEnabled(true);
        map.getOverlays().add(mCompassOverlay);

        mRotationGestureOverlay = new RotationGestureOverlay(map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(mRotationGestureOverlay);

        final DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);

        //play around with these values to get the location on screen in the right place for your application
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        map.getOverlays().add(mScaleBarOverlay);

    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause()
    {
        map.onPause();
        super.onPause();
    }
}