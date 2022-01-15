package android.example.navigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.example.navigation.databinding.ActivityMainBinding;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
//import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private final String  API_KEY = "";
    private MapView mMapView;
    private LocationDisplay mLocationDisplay;
    private Spinner mSpinner;

    private final int requestCode = 2;
    private final String[] reqPermissions = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
            .ACCESS_COARSE_LOCATION };

    /*latitude*/
    private double latitude = 35.4494718;
    /*longitude*/
    private double longitude = 139.646464;
    /*scale*/
    private int scale = 30;


    private Callout mCallout;
    private ServiceFeatureTable mServiceFeatureTable;


    private ActivityMainBinding binding;
    private boolean weatherButton = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ////////////////////navigation Bar////////////////////

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        ////////////////////map////////////////////
        // authentication with an API key or named user is required to access basemaps and other location services
        //ArcGISRuntimeEnvironment.setApiKey(API_KEY);

        // Get the Spinner from layout
        mSpinner = findViewById(R.id.spinner);

        // Get the MapView from layout and set a map
        mMapView = findViewById(R.id.mapView);
        // create a map with the a topographic basemap
        ArcGISMap map;
        map = new ArcGISMap(Basemap.Type.OPEN_STREET_MAP,latitude,longitude,scale);
        // set the map to be displayed in this view
        mMapView.setMap(map);
        //mMapView.setViewpoint(new Viewpoint(34.056295, -117.195800, 10000));

        // get the callout that shows attributes
        mCallout = mMapView.getCallout();

        // create the service feature table
        mServiceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));
        // create the feature layer using the service feature table
        final FeatureLayer featureLayer = new FeatureLayer(mServiceFeatureTable);
        // add the layer to the map
        map.getOperationalLayers().add(featureLayer);

        // get the MapView's LocationDisplay
        mLocationDisplay = mMapView.getLocationDisplay();

        // Listen to changes in the status of the location data source.
        mLocationDisplay.addDataSourceStatusChangedListener(dataSourceStatusChangedEvent -> {

            // If LocationDisplay started OK, then continue.
            if (dataSourceStatusChangedEvent.isStarted())
                return;

            // No error is reported, then continue.
            if (dataSourceStatusChangedEvent.getError() == null)
                return;

            // If an error is found, handle the failure to start.
            // Check permissions to see if failure may be due to lack of permissions.
            boolean permissionCheck1 = ContextCompat.checkSelfPermission(this, reqPermissions[0]) ==
                    PackageManager.PERMISSION_GRANTED;
            boolean permissionCheck2 = ContextCompat.checkSelfPermission(this, reqPermissions[1]) ==
                    PackageManager.PERMISSION_GRANTED;

            if (!(permissionCheck1 && permissionCheck2)) {
                // If permissions are not already granted, request permission from the user.
                ActivityCompat.requestPermissions(this, reqPermissions, requestCode);
            } else {
                // Report other unknown failure types to the user - for example, location services may not
                // be enabled on the device.
                String message = String.format("Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent
                        .getSource().getLocationDataSource().getError().getMessage());
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                // Update UI to reflect that the location display did not actually start
                mSpinner.setSelection(0, true);
            }
        });
        // Populate the list for the Location display options for the spinner's Adapter
        ArrayList<ItemData> list = new ArrayList<>();
        list.add(new ItemData("Stop", R.drawable.locationdisplaydisabled));
        list.add(new ItemData("On", R.drawable.locationdisplayon));
        list.add(new ItemData("Re-Center", R.drawable.locationdisplayrecenter));
        list.add(new ItemData("Navigation", R.drawable.locationdisplaynavigation));
        list.add(new ItemData("Compass", R.drawable.locationdisplayheading));

        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.spinner_layout, R.id.txt, list);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("debug","onItemSelected()");

                switch (position) {
                    case 0:
                        // Stop Location Display
                        if (mLocationDisplay.isStarted())
                            mLocationDisplay.stop();
                        break;
                    case 1:
                        // Start Location Display
                        if (!mLocationDisplay.isStarted())
                            mLocationDisplay.startAsync();
                        break;
                    case 2:
                        // Re-Center MapView on Location
                        // AutoPanMode - Default: In this mode, the MapView attempts to keep the location symbol on-screen by
                        // re-centering the location symbol when the symbol moves outside a "wander extent". The location symbol
                        // may move freely within the wander extent, but as soon as the symbol exits the wander extent, the MapView
                        // re-centers the map on the symbol.
                        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
                        if (!mLocationDisplay.isStarted())
                            mLocationDisplay.startAsync();
                        break;
                    case 3:
                        // Start Navigation Mode
                        // This mode is best suited for in-vehicle navigation.
                        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
                        if (!mLocationDisplay.isStarted())
                            mLocationDisplay.startAsync();
                        break;
                    case 4:
                        // Start Compass Mode
                        // This mode is better suited for waypoint navigation when the user is walking.
                        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.COMPASS_NAVIGATION);
                        if (!mLocationDisplay.isStarted())
                            mLocationDisplay.startAsync();
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        // set an on touch listener to listen for click events
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.d("debug","onSingleTapConfirmed()");
                // remove any existing callouts
                if (mCallout.isShowing()) {
                    mCallout.dismiss();
                }
                // get the point that was clicked and convert it to a point in map coordinates
                final Point screenPoint = new Point(Math.round(e.getX()), Math.round(e.getY()));
                // create a selection tolerance
                int tolerance = 10;
                // use identifyLayerAsync to get tapped features
                final ListenableFuture<IdentifyLayerResult> identifyLayerResultListenableFuture = mMapView
                        .identifyLayerAsync(featureLayer, screenPoint, tolerance, false, 1);
                identifyLayerResultListenableFuture.addDoneListener(() -> {
                    try {
                        IdentifyLayerResult identifyLayerResult = identifyLayerResultListenableFuture.get();
                        // create a textview to display field values
                        TextView calloutContent = new TextView(getApplicationContext());
                        calloutContent.setTextColor(Color.BLACK);
                        calloutContent.setSingleLine(false);
                        calloutContent.setVerticalScrollBarEnabled(true);
                        calloutContent.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                        calloutContent.setMovementMethod(new ScrollingMovementMethod());
                        calloutContent.setLines(5);
                        for (GeoElement element : identifyLayerResult.getElements()) {
                            Feature feature = (Feature) element;
                            // create a map of all available attributes as name value pairs
                            Map<String, Object> attr = feature.getAttributes();
                            Set<String> keys = attr.keySet();
                            for (String key : keys) {
                                Object value = attr.get(key);
                                // format observed field value as date
                                if (value instanceof GregorianCalendar) {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                                    value = simpleDateFormat.format(((GregorianCalendar) value).getTime());
                                }
                                // append name value pairs to text view
                                calloutContent.append(key + " | " + value + "\n");
                            }
                            // center the mapview on selected feature
                            Envelope envelope = feature.getGeometry().getExtent();
                            mMapView.setViewpointGeometryAsync(envelope, 200);
                            // show callout
                            mCallout.setLocation(envelope.getCenter());
                            mCallout.setContent(calloutContent);
                            mCallout.show();
                        }
                    } catch (Exception e1) {
                        Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e1.getMessage());
                    }
                });
                return super.onSingleTapConfirmed(e);
            }
        });

        TextView weather_text = findViewById(R.id.weather_info);
        TextView location_text = findViewById(R.id.location_info);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder weather_display = new StringBuilder();
                StringBuilder location_display = new StringBuilder();
                if(weatherButton){
                    Log.d("debug","true");
                    weather_display.append("天気:");
                    weather_display.append("晴れ");
                    location_display.append("緯度:");
                    location_display.append(latitude);
                    location_display.append("  ");
                    location_display.append("経度:");
                    location_display.append(longitude);
                    weather_text.setText(weather_display.toString());
                    location_text.setText(location_display.toString());
                    weatherButton = false;
                }else{
                    Log.d("debug","false");
                    weather_text.setText("");
                    location_text.setText("");
                    weatherButton = true;
                }
            }
        });
    }

    /**
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("debug","onRequestPermissionsResult()");
        // If request is cancelled, the result arrays are empty.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Location permission was granted. This would have been triggered in response to failing to start the
            // LocationDisplay, so try starting this again.
            mLocationDisplay.startAsync();
        } else {
            // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
            // request permission UX will be shown again, option should be shown to allow never showing the UX again.
            // Alternative would be to disable functionality so request is not shown again.
            Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();

            // Update UI to reflect that the location display did not actually start
            mSpinner.setSelection(0, true);
        }
    }

    /**
     *
     */
    @Override
    protected void onStart() {
        Log.d("debug","onStart()");
        super.onStart();
    }

    /**
     *
     */
    @Override
    protected void onResume() {
        Log.d("debug","onResume()");
        super.onResume();
        mMapView.resume();


    }

    /**
     *
     */
    @Override
    protected void onPause() {
        Log.d("debug","onPause()");
        mMapView.pause();
        super.onPause();
    }

    /**
     *
     */
    @Override
    protected void onDestroy() {
        Log.d("debug","onDestroy()");
        mMapView.dispose();
        super.onDestroy();
    }


    /**
     * onSaveInstanceState().<br>
     * 画面の回転時に呼び出される（データを保存する）<br>
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e("debug", "onSaveInstanceState()");
    }

    /**
     * onRestoreInstanceState().<br>
     * アクティビティが再起動するときに呼び出される（データ読み出し）<br>
     *
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.e("debug", "onRestoreInstanceState()");
    }
}
