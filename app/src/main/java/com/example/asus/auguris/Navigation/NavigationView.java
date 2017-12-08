package com.example.asus.auguris.Navigation;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.amazonaws.util.IOUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.asus.auguris.MainActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.TextFormat;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.TurfTransformation;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Filter;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.location.MockLocationEngine;
import com.mapbox.services.android.navigation.ui.v5.NavigationContract;
import com.example.asus.auguris.Navigation.NavigationPresenter;
import com.mapbox.services.android.navigation.ui.v5.RecenterButton;
import com.mapbox.services.android.navigation.ui.v5.camera.NavigationCamera;
import com.mapbox.services.android.navigation.ui.v5.instruction.InstructionView;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.ui.v5.route.RouteViewModel;
import com.mapbox.services.android.navigation.ui.v5.summary.SummaryBottomSheet;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.api.utils.turf.TurfConstants;
import com.mapbox.services.api.utils.turf.TurfGrids;
import com.mapbox.services.api.utils.turf.TurfHelpers;
import com.mapbox.services.api.utils.turf.TurfInvariant;
import com.mapbox.services.api.utils.turf.TurfJoins;
import com.mapbox.services.api.utils.turf.TurfMeasurement;
import com.mapbox.services.api.utils.turf.TurfMeta;
import com.mapbox.services.api.utils.turf.TurfMisc;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.Geometry;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.geojson.Polygon;
import com.mapbox.services.commons.models.Position;
import com.example.asus.auguris.Navigation.LocationViewModel;
import com.example.asus.auguris.Navigation.ThemeSwitcher;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.location.MockLocationEngine;
import com.mapbox.services.android.navigation.ui.v5.instruction.InstructionView;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.ui.v5.route.RouteViewModel;
import com.mapbox.services.android.navigation.ui.v5.summary.SummaryBottomSheet;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.commons.models.Position;

/**
 * Activity that creates the drop-in UI.
 * <p>
 * Once started, this activity will check if launched with a {@link DirectionsRoute}.
 * Or, if not found, this activity will look for a set of {@link Position} coordinates.
 * In the latter case, a new {@link DirectionsRoute} will be retrieved from {@link NavigationRoute}.
 * </p><p>
 * Once valid data is obtained, this activity will immediately begin navigation
 * with {@link MapboxNavigation}.
 * If launched with the simulation boolean set to true, a {@link MockLocationEngine}
 * will be initialized and begin pushing updates.
 * <p>
 * This activity requires user permissions ACCESS_FINE_LOCATION
 * and ACCESS_COARSE_LOCATION have already been granted.
 * <p>
 * A Mapbox access token must also be set by the developer (to initialize navigation).
 *
 * @since 0.6.0
 * </p>
 */

import com.example.asus.auguris.Navigation.NavigationViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.example.asus.auguris.JsonClasses.JsonClasses;
import com.openxc.VehicleManager;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.VehicleSpeed;


import static com.mapbox.mapboxsdk.style.layers.Filter.all;
import static com.mapbox.mapboxsdk.style.layers.Filter.gte;
import static com.mapbox.mapboxsdk.style.layers.Filter.has;
import static com.mapbox.mapboxsdk.style.layers.Filter.lt;
import static com.mapbox.mapboxsdk.style.layers.Filter.neq;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

public class NavigationView extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnScrollListener,

        NavigationContract.View {

    private MapView mapView;
    private InstructionView instructionView;
    private SummaryBottomSheet summaryBottomSheet;
    private View largeLabel;
    private BottomSheetBehavior summaryBehavior;
    private ImageButton cancelBtn;
    private ImageButton expandArrow;
    private View summaryDirections;
    private View summaryOptions;
    private View directionsOptionLayout;
    private View sheetShadow;
    private RecenterButton recenterBtn;
    private FloatingActionButton soundFab;

    private NavigationPresenter navigationPresenter;
    private NavigationViewModel navigationViewModel;
    private RouteViewModel routeViewModel;
    private com.example.asus.auguris.Navigation.LocationViewModel locationViewModel;
    public MapboxMap map;
    private NavigationMapRoute mapRoute;
    private NavigationCamera camera;
    private LocationLayerPlugin locationLayer;
    private boolean resumeState;
    LocationManager mLocationManager;
    GeoJsonSource source;

    private static final String ENDPOINT = "https://trello-attachments.s3.amazonaws.com/59f8e06476eaa732d0b7f037/59fd945317230b7e5773b3ec/9a01746746486c13a13fd2b552805be0/map.geojson";
    private RequestQueue requestQueue;
    private Gson gson;
    JsonClasses.RootObject posts;

    public static final double CHECK_DISTANCE_METERS = 50;

    Boolean BrakeControl = false;

    private VehicleManager mVehicleManager;

    Feature lastClusterTriggeredFeature = null;

    VehicleSpeed carSpeed;

    ArrayList<VehicleSpeed> vSpeedList;

    AlertDialog alertDialog;
    boolean isAlertShowing = false;

    int clusterIdIncrement = 0;

    final int[][] layers = new int[][]{
            new int[]{150, Color.parseColor("#E55E5E")},
            new int[]{75, Color.parseColor("#F9886C")},
            new int[]{0, Color.parseColor("#FBB03B")}
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        ThemeSwitcher.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(com.mapbox.services.android.navigation.ui.v5.R.layout.navigation_view_layout);
        resumeState = savedInstanceState != null;
        bind();
        initViewModels();
        initClickListeners();
        initSummaryBottomSheet();
        initMap(savedInstanceState);

        Intent intent = new Intent(this, VehicleManager.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.setDateFormat("M/d/yy hh:mm a");
        gson = gsonBuilder.create();


        isAlertShowing = false;
        clusterIdIncrement = 0;

        fetchPosts();

        vSpeedList = new ArrayList<>();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                0, mLocationListener);



    }

    VehicleManager vehicle;
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the VehicleManager service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            //Log.i(TAG, "Bound to VehicleManager");
            vehicle = ((VehicleManager.VehicleBinder)  // VehicleManager.VehicleManagerBinder
                    service).getService();
            // When the VehicleManager starts up, we store a reference to it
            // here in "mVehicleManager" so we can call functions on it
            // elsewhere in our code.
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            // We want to receive updates whenever the EngineSpeed changes. We
            // have an EngineSpeed.Listener (see above, mSpeedListener) and here
            // we request that the VehicleManager call its receive() method
            // whenever the EngineSpeed changes
            mVehicleManager.addListener(VehicleSpeed.class, mVehicleSpeedListener);
            mVehicleManager.addListener(BrakePedalStatus.class, mBreakPedalListener);
//          mVehicleManager.addListener();
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            //Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
            vehicle = null;
        }
    };

    VehicleSpeed.Listener mVehicleSpeedListener = new VehicleSpeed.Listener() {
        public void receive(Measurement measurement) {
            carSpeed = (VehicleSpeed) measurement;

            if(vSpeedList.size() == 5){
                vSpeedList.remove(0);
            }
            vSpeedList.add(carSpeed);
            int difference = vSpeedList.get(vSpeedList.size()-1).getValue().intValue() - vSpeedList.get(0).getValue().intValue();
            if(difference < -5 && difference > -10){
                System.out.println("huge difference when slowing down");
                //recording in the dataset

            }

            if(difference > 5 && difference < 10){
                System.out.println("huge difference when speeding up");
            }


            NavigationView.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //vehicle_speed.setText("Vehicle Speed (kph): " + speed.getValue().intValue());
                }
            });
        }
    };

    BrakePedalStatus.Listener mBreakPedalListener = new BrakePedalStatus.Listener() {
        public void receive(Measurement measurement) {
            final BrakePedalStatus brake = (BrakePedalStatus) measurement;

            NavigationView.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BrakeControl = brake.getValue().booleanValue();
                    if (BrakeControl == true && isAlertShowing) {
                        isAlertShowing = false;
                        alertDialog.dismiss();
                    }
                }
            });
        }
    };

    public static void geocoding(String addr) throws Exception {
        // build a URL
        String s = ENDPOINT;
        s += URLEncoder.encode(addr, "UTF-8");
        URL url = new URL(s);

        // read from the URL
        Scanner scan = new Scanner(url.openStream());
        String str = new String();
        while (scan.hasNext())
            str += scan.nextLine();
        scan.close();

        // build a JSON object
        JSONObject obj = new JSONObject(str);
        if (!obj.getString("status").equals("OK"))
            return;

        // get the first result
        JSONObject res = obj.getJSONArray("results").getJSONObject(0);
        System.out.println(res.getString("formatted_address"));
        JSONObject loc =
                res.getJSONObject("geometry").getJSONObject("location");
        System.out.println("lat: " + loc.getDouble("lat") +
                ", lng: " + loc.getDouble("lng"));
    }


    LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            //PointF a = new PointF((float) location.getLatitude(), (float) location.getLongitude());

            if (map != null) {
                final PointF point = map.getProjection().toScreenLocation(new LatLng(location));
                List<Feature> ftrs = map.queryRenderedFeatures(point, "cluster-2");
                ArrayList<JsonClasses.Feature> pocketFeatures = new ArrayList<JsonClasses.Feature>();
                if (ftrs.size() > 0 && posts != null) {
                    if (lastClusterTriggeredFeature != ftrs.get(0)) {
                        ftrs.get(0).setId(Integer.toString(clusterIdIncrement));
                        lastClusterTriggeredFeature = ftrs.get(0);
                        ArrayList<JsonClasses.Feature> features = posts.getFeatures();
                        for (int i = 0; i < features.size(); i++) {
                            double lat = features.get(i).getGeometry().getCoordinates().get(1);
                            double lon = features.get(i).getGeometry().getCoordinates().get(0);
                            double dist = TurfMeasurement.distance(Point.fromCoordinates(new double[]{lat, lon}), Point.fromCoordinates(new double[]{location.getLatitude(), location.getLongitude()}), TurfConstants.UNIT_METERS);
                            if (dist < CHECK_DISTANCE_METERS) {
                                pocketFeatures.add(features.get(i));
                            }
                        }

                        double averageSpeed = 0;
                        double totalRisk = 0;
                        double averageSeverity = 0;
                        if (pocketFeatures.size() > 0) {

                            for (int i = 0; i < pocketFeatures.size(); i++) {
                                averageSpeed = averageSpeed + pocketFeatures.get(i).getProperties().getSpeed();
                                averageSeverity = averageSeverity + pocketFeatures.get(i).getProperties().getSeverity();

                            }

                            averageSpeed = averageSpeed / pocketFeatures.size();
                            averageSeverity = averageSeverity / pocketFeatures.size();

                            double speedMultiplier;
                            double mySpeed;
                            if(carSpeed != null){
                                mySpeed  = carSpeed.getValue().doubleValue();
                            } else {
                                mySpeed = (int) ((location.getSpeed()*3600)/1000);
                                //turn on openxc enabler!!!
                            }


                            if (averageSpeed >= mySpeed) {
                                speedMultiplier = 0;
                            } else {
                                speedMultiplier = 1 - (averageSpeed / mySpeed);
                            }

                            double severityMultiplier = (averageSeverity / 2);

                            totalRisk = (speedMultiplier * 13) + (severityMultiplier * 7) / 20;
                            // 1-(35/40) 0.25 + 0.33

                            //totalRisk > 0.5, then show message


                            Toast.makeText(NavigationView.this, "Total Risk : " + Double.toString(totalRisk),
                                    Toast.LENGTH_SHORT).show();


                            if (totalRisk > 2.5 && !isAlertShowing) {
                                alertDialog = new AlertDialog.Builder(NavigationView.this).create();
//                                alertDialog.dismiss();

                                alertDialog.setTitle("High Speed Warning!");
                                alertDialog.setMessage("If you don't want to die, try to go slower: " + lastClusterTriggeredFeature);
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int i) {
                                                isAlertShowing = false;
                                                dialog.dismiss();
                                            }
                                        });
                                isAlertShowing = true;
                                alertDialog.show();
                            }

//                            if (BrakeControl == true)
//                                alertDialog.dismiss();
                        }


//                        Toast.makeText(NavigationView.this, "Features Count: " + Integer.toString(pocketFeatures.size()),
//                                Toast.LENGTH_SHORT).show();
                    }
/*
                  for(int j=0; j<((List<List<Position>>) ftrs.get(0).getGeometry().getCoordinates()).size(); j++){
                      double lat = coordinates.get(j).get(0).getLatitude();
                      double lon = coordinates.get(j).get(0).getLongitude();
//                    for(int c=0; c<coordinates.size(); c++){
////                        source.querySourceFeatures(gte("point_count", layers[i][0]) :)
//                    }
                      ArrayList<JsonClasses.Feature> features = posts.getFeatures();
                      for (int i=0; i<features.size(); i++){
                          if(features.get(i).getGeometry().getCoordinates().get(0) == lat && features.get(i).getGeometry().getCoordinates().get(1) == lon){
                              System.out.println("boo");
                          }
                      }
                  }*/


//                    Toast.makeText(NavigationView.this, Integer.toString(ftrs.size()),
//                            Toast.LENGTH_SHORT).show();
//                    holder[i] =  Integer.toString(ftrs.size());
//                    i++;


                } else {
                    if (alertDialog != null) {
                        isAlertShowing = false;
                        alertDialog.dismiss();
                    }
                }
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void fetchPosts() {
        StringRequest request = new StringRequest(Request.Method.GET, ENDPOINT, onPostsLoaded, onPostsError);
        requestQueue.add(request);
    }

    private final Response.Listener<String> onPostsLoaded = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            posts = gson.fromJson(response, JsonClasses.RootObject.class);
//
//            Log.i("PostActivity", posts + " posts loaded.");
//            for (JsonClasses.RootObject post : posts.toString()) {
//                Log.i("PostActivity", post.getFeatures().get(0).toString());
//            }

        }
    };

    private final Response.ErrorListener onPostsError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("PostActivity", error.toString());
        }
    };

    @SuppressWarnings({"MissingPermission"})
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if (mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(getString(com.mapbox.services.android.navigation.ui.v5.R.string.bottom_sheet_state),
                summaryBehavior.getState());
        outState.putBoolean(getString(com.mapbox.services.android.navigation.ui.v5.R.string.recenter_btn_visible),
                recenterBtn.getVisibility() == View.VISIBLE);
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean isVisible = savedInstanceState.getBoolean(getString(com.mapbox.services.android.navigation.ui.v5.R.string.recenter_btn_visible));
        recenterBtn.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        int bottomSheetState = savedInstanceState.getInt(getString(com.mapbox.services.android.navigation.ui.v5.R.string.bottom_sheet_state));
        resetBottomSheetState(bottomSheetState);
    }

    /**
     * Fired after the map is ready, this is our cue to finish
     * setting up the rest of the plugins / location engine.
     * <p>
     * Also, we check for launch data (coordinates or route).
     *
     * @param mapboxMap used for route, camera, and location UI
     * @since 0.6.0
     */
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        map.setOnScrollListener(this);
        initRoute();
        initCamera();
        initLocationLayer();
        initLifecycleObservers();
        initNavigationPresenter();
        subscribeViews();
        routeViewModel.extractLaunchData(this);
        addClusteredGeoJsonSource(map);


    }


    private void addClusteredGeoJsonSource(MapboxMap mapboxMap) {


        // Add a new source from our GeoJSON data and set the 'cluster' option to true.
        try {
            source = new GeoJsonSource("earthquakes", new URL(ENDPOINT),
                    new GeoJsonOptions()
                            .withCluster(true)
                            .withClusterMaxZoom(75) // Max zoom to cluster points on
                            .withClusterRadius(20) // Use small cluster radius for the heatmap look
            );

//
//            String asas = source.getAttribution();
//
//            List<Feature> features = source.querySourceFeatures(has("weather"));
//            String asd = " asd ";
            mapboxMap.addSource(source);

            //mapboxMap.getSource(source.getId());


        } catch (MalformedURLException malformedUrlException) {
            Log.e("heatmapActivity", "Check the URL " + malformedUrlException.getMessage());
        }

        // Use the earthquakes source to create four layers:
        // three for each cluster category, and one for unclustered points

        // Each point range gets a different fill color.


        CircleLayer unclustered = new CircleLayer("unclustered-points", "earthquakes");
        unclustered.setProperties(
                circleColor(Color.parseColor("#FBB03B")),
                circleRadius(20f),
                circleBlur(1f));
        unclustered.setFilter(
                neq("cluster", true)
        );
        mapboxMap.addLayer(unclustered);
        unclustered.getSourceLayer();

        for (int i = 0; i < layers.length; i++) {

            CircleLayer circles = new CircleLayer("cluster-" + i, "earthquakes");
            circles.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(70f),
                    circleBlur(1f)
            );


            circles.setFilter(
                    i == 0
                            ? gte("point_count", layers[i][0]) :
                            all(gte("point_count", layers[i][0]), lt("point_count", layers[i - 1][0]))
            );
            mapboxMap.addLayer(circles);
        }
    }

    /**
     * Listener this activity sets on the {@link MapboxMap}.
     * <p>
     * Used as a cue to hide the {@link SummaryBottomSheet} and stop the
     * camera from following location updates.
     *
     * @since 0.6.0
     */
    @Override
    public void onScroll() {
        if (!summaryBehavior.isHideable()) {
            navigationPresenter.onMapScroll();
        }
    }

    @Override
    public void setSummaryBehaviorState(int state) {
        summaryBehavior.setState(state);
    }

    @Override
    public void setSummaryBehaviorHideable(boolean isHideable) {
        summaryBehavior.setHideable(isHideable);
    }

    @Override
    public void setSummaryOptionsVisibility(boolean isVisible) {
        summaryOptions.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSummaryDirectionsVisibility(boolean isVisible) {
        summaryDirections.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean isSummaryDirectionsVisible() {
        return summaryDirections.getVisibility() == View.VISIBLE;
    }

    @Override
    public void setSheetShadowVisibility(boolean isVisible) {
        sheetShadow.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setCameraTrackingEnabled(boolean isEnabled) {
        camera.setCameraTrackingLocation(isEnabled);
    }

    @Override
    public void resetCameraPosition() {
        camera.resetCameraPosition();
    }

    @Override
    public void showRecenterBtn() {
        recenterBtn.show();
    }

    @Override
    public void hideRecenterBtn() {
        recenterBtn.hide();
    }

    @Override
    public void showInstructionView() {
        instructionView.show();
    }

    @Override
    public void drawRoute(DirectionsRoute directionsRoute) {
        mapRoute.addRoute(directionsRoute);
    }

    @Override
    public void setMuted(boolean isMuted) {
        navigationViewModel.setMuted(isMuted);
    }

    @Override
    public void setCancelBtnClickable(boolean isClickable) {
        cancelBtn.setClickable(isClickable);
    }

    @Override
    public void animateCancelBtnAlpha(float value) {
        cancelBtn.animate().alpha(value).setDuration(0).start();
    }

    @Override
    public void animateExpandArrowRotation(float value) {
        expandArrow.animate().rotation(value).setDuration(0).start();
    }

    @Override
    public void animateInstructionViewAlpha(float value) {
        instructionView.animate().alpha(value).setDuration(0).start();
    }

    /**
     * Creates a marker based on the
     * {@link Position} destination coordinate.
     *
     * @param position where the marker should be placed
     */
    @Override
    public void addMarker(Position position) {
        LatLng markerPosition = new LatLng(position.getLatitude(),
                position.getLongitude());
        map.addMarker(new MarkerOptions()
                .position(markerPosition)
                .icon(ThemeSwitcher.retrieveMapMarker(this)));
    }

    @Override
    public void finishNavigationView() {
        finish();
    }

    /**
     * Used when starting this {@link android.app.Activity}
     * for the first time.
     * <p>
     * Zooms to the beginning of the {@link DirectionsRoute}.
     *
     * @param directionsRoute where camera should move to
     */
    public void startCamera(DirectionsRoute directionsRoute) {
        if (!resumeState) {
            camera.start(directionsRoute);
        }
    }

    /**
     * Used after configuration changes to resume the camera
     * to the last location update from the Navigation SDK.
     *
     * @param location where the camera should move to
     */
    public void resumeCamera(Location location) {
        if (resumeState && recenterBtn.getVisibility() != View.VISIBLE) {
            camera.resume(location);
            resumeState = false;
        }
    }

    /**
     * Binds all necessary views.
     */
    private void bind() {
        mapView = findViewById(com.mapbox.services.android.navigation.ui.v5.R.id.mapView);
        instructionView = findViewById(com.mapbox.services.android.navigation.ui.v5.R.id.instructionView);
        summaryBottomSheet = findViewById(com.mapbox.services.android.navigation.ui.v5.R.id.summaryBottomSheet);
        cancelBtn = findViewById(com.mapbox.services.android.navigation.ui.v5.R.id.cancelBtn);
        expandArrow = findViewById(com.mapbox.services.android.navigation.ui.v5.R.id.expandArrow);
        summaryOptions = findViewById(com.mapbox.services.android.navigation.ui.v5.R.id.summaryOptions);
        summaryDirections = findViewById(com.mapbox.services.android.navigation.ui.v5.R.id.summaryDirections);
        directionsOptionLayout = findViewById(com.mapbox.services.android.navigation.ui.v5.R.id.directionsOptionLayout);
        sheetShadow = findViewById(com.mapbox.services.android.navigation.ui.v5.R.id.sheetShadow);
        recenterBtn = findViewById(com.mapbox.services.android.navigation.ui.v5.R.id.recenterBtn);
        soundFab = findViewById(com.mapbox.services.android.navigation.ui.v5.R.id.soundFab);
    }

    private void initViewModels() {
        locationViewModel = ViewModelProviders.of(this).get(com.example.asus.auguris.Navigation.LocationViewModel.class);
        routeViewModel = ViewModelProviders.of(this).get(RouteViewModel.class);
        navigationViewModel = ViewModelProviders.of(this).get(NavigationViewModel.class);
    }

    /**
     * Sets click listeners to all views that need them.
     */
    private void initClickListeners() {
        directionsOptionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationPresenter.onDirectionsOptionClick();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationPresenter.onCancelBtnClick();
            }
        });
        expandArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationPresenter.onExpandArrowClick(summaryBehavior.getState());
            }
        });
        recenterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationPresenter.onRecenterClick();
            }
        });
        soundFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationPresenter.onMuteClick(instructionView.toggleMute());
            }
        });
    }

    /**
     * Sets up the {@link MapboxMap}.
     *
     * @param savedInstanceState from onCreate()
     */
    private void initMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        ThemeSwitcher.setMapStyle(this, mapView);
    }

    /**
     * Initializes the {@link BottomSheetBehavior} for {@link SummaryBottomSheet}.
     */
    private void initSummaryBottomSheet() {
        summaryBehavior = BottomSheetBehavior.from(summaryBottomSheet);
        summaryBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        navigationPresenter.onSummaryBottomSheetExpanded();
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        navigationPresenter.onSummaryBottomSheetCollapsed();
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        navigationPresenter.onSummaryBottomSheetHidden();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                navigationPresenter.onBottomSheetSlide(slideOffset,
                        sheetShadow.getVisibility() != View.VISIBLE);
            }
        });
    }

    /**
     * Sets the {@link BottomSheetBehavior} based on the last state stored
     * in {@link Bundle} savedInstanceState.
     *
     * @param bottomSheetState retrieved from savedInstanceState
     */
    private void resetBottomSheetState(int bottomSheetState) {
        boolean isShowing = bottomSheetState == BottomSheetBehavior.STATE_COLLAPSED
                || bottomSheetState == BottomSheetBehavior.STATE_EXPANDED;
        summaryBehavior.setHideable(!isShowing);
        summaryBehavior.setState(bottomSheetState);
    }

    /**
     * Initializes the {@link NavigationMapRoute} to be used to draw the
     * route.
     */
    private void initRoute() {
        mapRoute = new NavigationMapRoute(mapView, map, NavigationConstants.ROUTE_BELOW_LAYER);
    }

    /**
     * Initializes the {@link NavigationCamera} that will be used to follow
     * the {@link Location} updates from {@link MapboxNavigation}.
     */
    private void initCamera() {
        camera = new NavigationCamera(this, map, navigationViewModel.getNavigation());
    }

    /**
     * Initializes the {@link LocationLayerPlugin} to be used to draw the current
     * location.
     */
    @SuppressWarnings({"MissingPermission"})
    private void initLocationLayer() {
        locationLayer = new LocationLayerPlugin(mapView, map, null);
        locationLayer.setLocationLayerEnabled(LocationLayerMode.NAVIGATION);
    }

    /**
     * Add lifecycle observers to ensure these objects properly
     * start / stop based on the Android lifecycle.
     */
    private void initLifecycleObservers() {
        getLifecycle().addObserver(locationLayer);
        getLifecycle().addObserver(locationViewModel);
        getLifecycle().addObserver(navigationViewModel);
    }

    /**
     * Initialize a new presenter for this Activity.
     */
    private void initNavigationPresenter() {
        navigationPresenter = new NavigationPresenter(this);
    }

    /**
     * Initiate observing of ViewModels by Views.
     */
    private void subscribeViews() {
        /*instructionView.subscribe(navigationViewModel);
        summaryBottomSheet.subscribe(navigationViewModel);*/

        locationViewModel.rawLocation.observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                if (location != null) {
                    routeViewModel.updateRawLocation(location);
                }
            }
        });

        locationViewModel.locationEngine.observe(this, new Observer<LocationEngine>() {
            @Override
            public void onChanged(@Nullable LocationEngine locationEngine) {
                if (locationEngine != null) {
                    navigationViewModel.updateLocationEngine(locationEngine);
                }
            }
        });

        routeViewModel.route.observe(this, new Observer<DirectionsRoute>() {
            @Override
            public void onChanged(@Nullable DirectionsRoute directionsRoute) {
                if (directionsRoute != null) {
                    navigationViewModel.updateRoute(directionsRoute);
                    locationViewModel.updateRoute(directionsRoute);
                    navigationPresenter.onRouteUpdate(directionsRoute);
                    startCamera(directionsRoute);
                }
            }
        });

        routeViewModel.destination.observe(this, new Observer<Position>() {
            @Override
            public void onChanged(@Nullable Position position) {
                if (position != null) {
                    navigationPresenter.onDestinationUpdate(position);
                }
            }
        });

        navigationViewModel.isRunning.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isRunning) {
                if (isRunning != null) {
                    if (isRunning && !resumeState) {
                        navigationPresenter.onNavigationRunning();
                    } else if (!isRunning) {
                        finish();
                    }
                }
            }
        });

        navigationViewModel.navigationLocation.observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                if (location != null && location.getLongitude() != 0 && location.getLatitude() != 0) {
                    locationLayer.forceLocationUpdate(location);
                    resumeCamera(location);
                }
            }
        });

        navigationViewModel.newOrigin.observe(this, new Observer<Position>() {
            @Override
            public void onChanged(@Nullable Position newOrigin) {
                if (newOrigin != null) {
                    routeViewModel.fetchRouteNewOrigin(newOrigin);
                    // To prevent from firing on rotation
                    navigationViewModel.newOrigin.setValue(null);
                }
            }
        });
    }
}
