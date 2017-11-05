package com.example.asus.auguris;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

// classes needed to initialize map
import com.example.asus.auguris.Navigation.NavigationView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;

// classes needed to add location layer
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import android.location.Location;

import com.mapbox.mapboxsdk.geometry.LatLng;

import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.location.LostLocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

// classes needed to add a marker
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;

// classes to calculate a route

import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.commons.models.Position;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;

// classes needed to launch navigation UI
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.asus.auguris.Navigation.NavigationLauncher;
import com.openxc.VehicleManager;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;

import static com.mapbox.mapboxsdk.style.layers.Filter.all;
import static com.mapbox.mapboxsdk.style.layers.Filter.gte;
import static com.mapbox.mapboxsdk.style.layers.Filter.lt;
import static com.mapbox.mapboxsdk.style.layers.Filter.neq;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

import com.openxc.VehicleManager;

import org.w3c.dom.Text;


public class MainActivity extends AppCompatActivity implements LocationEngineListener, PermissionsListener {

    private MapView mapView;

    // variables for adding location layer
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;

    // variables for adding a marker
    private Marker destinationMarker;
    private LatLng originCoord;
    private LatLng destinationCoord;
    private Location originLocation;

    // variables for calculating and drawing a route
    private Position originPosition;
    private Position destinationPosition;
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;

    private Button button;

    private static final String TAG2 = "StarterActivity";

    private VehicleManager mVehicleManager;
    private TextView mEngineSpeedView;
    private TextView vehicle_speed;
    private TextView brake_status;


//    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoiaHNuZ3J0IiwiYSI6ImNqOWs3cTFpejN0MHQyd3Q0bjVpbDI4Nm4ifQ.ii5f8pyk3JES4iasVPRucw");
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        vehicle_speed = (TextView) findViewById(R.id.vehicle_speed);
        brake_status = (TextView) findViewById(R.id.brake_status);

//        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocationListener);


        // Add user location to the map
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                //addClusteredGeoJsonSource(mapboxMap);

                map = mapboxMap;

                addClusteredGeoJsonSource(map);

                enableLocationPlugin();
                originCoord = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());

                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {


                        if (destinationMarker != null) {
                            mapboxMap.removeMarker(destinationMarker);
                        }

                        destinationCoord = point;

                        destinationMarker = mapboxMap.addMarker(new MarkerViewOptions()
                                .position(destinationCoord)
                        );
                        destinationPosition = Position.fromCoordinates(destinationCoord.getLongitude(), destinationCoord.getLatitude());
                        originPosition = Position.fromCoordinates(originCoord.getLongitude(), originCoord.getLatitude());
                        getRoute(originPosition, destinationPosition);
                        button.setEnabled(true);
                        button.setBackgroundResource(R.color.mapbox_blue);
                    }

                    ;
                });

                button = findViewById(R.id.startButton);
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Position origin = originPosition;
                        Position destination = destinationPosition;

                        // Pass in your Amazon Polly pool id for speech synthesis using Amazon Polly
                        // Set to null to use the default Android speech synthesizer
                        String awsPoolId = null;

                        boolean simulateRoute = false;


                        // Call this method with Context from within an Activity
                        NavigationLauncher.startNavigation(MainActivity.this, origin, destination, awsPoolId, simulateRoute);



                        List<Layer> layers = map.getLayers();
                        String id;

                        for (Layer layer : layers) {
                            layer.setProperties(PropertyFactory.visibility(Property.VISIBLE));

                        }
                    }
                });
            }

            ;
        });
    }

//        LocationListener mlocationListener = new LocationListener() {
//        public void onLocationChanged(Location location) {
//            // Called when a new location is found by the network location provider.
//            //makeUseOfNewLocation(location);
//
//            customMessageView.setText(location.getSpeed() +  " sp");
//        }
//
//        public void onStatusChanged(String provider, int status, Bundle extras) {}
//
//        public void onProviderEnabled(String provider) {}
//
//        public void onProviderDisabled(String provider) {}
//    };




//    EngineSpeed.Listener mSpeedListener = new EngineSpeed.Listener() {
//        public void receive(Measurement measurement) {
//            // When we receive a new EngineSpeed value from the car, we want to
//            // update the UI to display the new value. First we cast the generic
//            // Measurement back to the type we know it to be, an EngineSpeed.
//            final EngineSpeed speed = (EngineSpeed) measurement;
//            // In order to modify the UI, we have to make sure the code is
//            // running on the "UI thread" - Google around for this, it's an
//            // important concept in Android.
//            MainActivity.this.runOnUiThread(new Runnable() {
//                public void run() {
//                    // Finally, we've got a new value and we're running on the
//                    // UI thread - we set the text of the EngineSpeed view to
//                    // the latest value
//                    mEngineSpeedView.setText("Engine speed (RPM): "
//                            + speed.getValue().doubleValue());
//                }
//            });
//        }
//    };


    private void addClusteredGeoJsonSource(MapboxMap mapboxMap) {


        // Add a new source from our GeoJSON data and set the 'cluster' option to true.
        try {
            mapboxMap.addSource(
                    // Point to GeoJSON data. This example visualizes all M1.0+ earthquakes from
                    // 12/22/15 to 1/21/16 as logged by USGS' Earthquake hazards program.
                    new GeoJsonSource("earthquakes", new URL("https://trello-attachments.s3.amazonaws.com/59f8e06476eaa732d0b7f037/59fd945317230b7e5773b3ec/9a01746746486c13a13fd2b552805be0/map.geojson"),
                            new GeoJsonOptions()
                                    .withCluster(true)
                                    .withClusterMaxZoom(30) // Max zoom to cluster points on
                                    .withClusterRadius(20) // Use small cluster radius for the heatmap look
                    ));
        } catch (MalformedURLException malformedUrlException) {
            Log.e("heatmapActivity", "Check the URL " + malformedUrlException.getMessage());
        }

        // Use the earthquakes source to create four layers:
        // three for each cluster category, and one for unclustered points

        // Each point range gets a different fill color.
        final int[][] layers = new int[][]{
                new int[]{150, Color.parseColor("#E55E5E")},
                new int[]{20, Color.parseColor("#F9886C")},
                new int[]{0, Color.parseColor("#FBB03B")}
        };

        CircleLayer unclustered = new CircleLayer("unclustered-points", "earthquakes");
        unclustered.setProperties(
                circleColor(Color.parseColor("#FBB03B")),
                circleRadius(20f),
                circleBlur(1f));
        unclustered.setFilter(
                neq("cluster", true)
        );
        mapboxMap.addLayer(unclustered);

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

    private void getRoute(Position origin, Position destination) {
        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().getRoutes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().getRoutes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, map, R.style.NavigationMapRoute);

                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }



    @SuppressWarnings({"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create an instance of LOST location engine
            initializeLocationEngine();

            locationPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
            locationPlugin.setLocationLayerEnabled(LocationLayerMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings({"MissingPermission"})
    private void initializeLocationEngine() {
        locationEngine = new LostLocationEngine(MainActivity.this);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    private void setCameraPosition(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 13));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationPlugin();
        } else {
            finish();
        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            originLocation = location;
            setCameraPosition(location);
            locationEngine.removeLocationEngineListener(this);

            double asd = location.getLongitude();
            double nv = originLocation.getSpeed();

        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}