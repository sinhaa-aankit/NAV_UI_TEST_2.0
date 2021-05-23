package com.example.ui_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.ui_test.activities.ContributionsActivity;
import com.example.ui_test.activities.Feedback;
import com.example.ui_test.activities.MySaves;
import com.example.ui_test.activities.SettingsActivity;
import com.example.ui_test.activities.WhatsNew;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.ui_test.R.id.ShareMyLocation;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, NavigationView.OnNavigationItemSelectedListener {

    MapboxMap mapboxmap;
    MapView mapview;
    PermissionsManager permissionmanager;
    LocationComponent locationcomponent;
    private List<Point> waypoints = new ArrayList<>();
    private boolean isInTrackingMode;
    DirectionsRoute route;
    NavigationMapRoute navigationMapRoute;

    //SearchView searchView;

    private Button button;
    Marker mark;

    FloatingActionButton recentre;

    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;

    NavigationView navigation_view;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.kesari");

        setContentView(R.layout.activity_main);

        // assigning ID of the toolbar to a variable
        Toolbar toolbar = (Toolbar) findViewById(R.id.appBar);

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);

        // This will display an Up icon (<-), will replace it later
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //drawerLayout
        drawerLayout = (DrawerLayout) findViewById(R.id.mainDrawer);

        setUpDrawerLayout();

        navigation_view = (NavigationView) findViewById(R.id.navigation_view);
        navigation_view.setNavigationItemSelectedListener(this);


        //
        mapview = findViewById(R.id.mapView);
        final SearchView searchView = (SearchView) findViewById(R.id.search);

        mapview.onCreate(savedInstanceState);
        mapview.getMapAsync(this);

        searchView.setQueryHint("Search for Places");

        recentre = (FloatingActionButton)findViewById(R.id.myLocationButton);



        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false); //Make search box active on click
            }
        });



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String s) {

                Log.d("Searbox11",s);

                MapboxGeocoding mapboxGeocoding1 = MapboxGeocoding.builder()
                        .accessToken("pk.eyJ1Ijoic291dmlrc2Fua2FyMjAxMyIsImEiOiJja2Fyc2VrczEwZDM3MnducW1hNm56dzJoIn0.dD4s9wv9UvF4xejzfUW2_Q")
                        .query(s)
                        .build();

                mapboxGeocoding1.enqueueCall(new Callback<GeocodingResponse>() {
                    @Override
                    public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                        List<CarmenFeature> results = response.body().features();
                        Point firstResultPoint = results.get(0).center();
                        Log.d("Souvik_geo", "onResponse: " + firstResultPoint.toString());

                        mapboxmap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(firstResultPoint.latitude(),firstResultPoint.longitude()))
                                        .zoom(14)
                                        .build()), 4000);

                        mapboxmap.addMarker(new MarkerOptions()
                                .position(new LatLng(firstResultPoint.latitude(),firstResultPoint.longitude()))
                                .title(s));

                        fetchroute(new LatLng(firstResultPoint.latitude(),firstResultPoint.longitude()));

//                        MapboxMap.AddOnMapLongClickListener(firstResultPoint.latitude(),firstResultPoint.longitude());

                    }

                    @Override
                    public void onFailure(Call<GeocodingResponse> call, Throwable t) {

                    }
                });


                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }


        });




        button = findViewById(R.id.startButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean simulateRoute = false;
                //navigationView.retrieveNavigationMapboxMap().retrieveMap().setStyle(new Style.Builder().fromUri("http://95.217.212.221:8080/style/style.json"));

                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(route)
                        .shouldSimulateRoute(simulateRoute)
                        .build();

                NavigationLauncher.startNavigation(MainActivity.this, options);
                //navlaunch_kesari.startNavigation(MainActivity.this,options);

                //navigationView.startNavigation(options);

                Log.d("Click", "Button Clicked");
            }
        });
    }

    void setUpDrawerLayout() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                R.string.app_name,
                R.string.app_name);
        actionBarDrawerToggle.syncState();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.mySaves: {
                Intent intentMySaves = new Intent(this, MySaves.class);
                startActivity(intentMySaves);
                break;
            }
            case R.id.myApps: {
                Toast.makeText(this, "Going to My Apps", Toast.LENGTH_SHORT).show();
                break;
            }
            case ShareMyLocation: {
                Toast.makeText(this, "Location Share Successful", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.getPPin: {
                Toast.makeText(this, "Generating P Pin", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.addPlaces: {
                Toast.makeText(this, "Adding Place Successful", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.settings: {
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentSettings);
                break;
            }
            case R.id.whatsNew: {
                Intent intentWhatsNew = new Intent(this, WhatsNew.class);
                startActivity(intentWhatsNew);
                break;
            }
            case R.id.feedback: {
                Intent intentFeedback = new Intent(this, Feedback.class);
                startActivity(intentFeedback);
                break;
            }
            case R.id.rateUsOnPlayStore: {
                final ReviewManager manager = ReviewManagerFactory.create(this);
                Task<ReviewInfo> request = manager.requestReviewFlow();
                request.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Getting the ReviewInfo object
                        ReviewInfo reviewInfo = task.getResult();

                        Task <Void> flow = manager.launchReviewFlow(this, reviewInfo);
                        flow.addOnCompleteListener(task1 -> {
                            Toast.makeText(this, "Thank you for your Feedback", Toast.LENGTH_SHORT).show();
                        });
                    }
                });

                break;
            }
            case R.id.contributions: {
                Intent intentContributions = new Intent(this, ContributionsActivity.class);
                startActivity(intentContributions);
                break;
            }
        }
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //    private SearchView search_view = findViewById(R.id.search);
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu1, menu);
//
//        // Associate searchable configuration with the SearchView
//        SearchManager searchManager =
//                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        final SearchView searchView = (SearchView) findViewById(R.id.search);
////                (SearchView) menu.findItem(R.id.search).getActionView();
//
//        searchView.setSearchableInfo(
//
//                searchManager.getSearchableInfo(getComponentName()));
//
//        searchView.setQueryHint("Search for Places");
//        searchView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                searchView.setIconified(false); //Make search box active on click
//            }
//        });
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(final String query) {
//
//                Log.d("Searbox11",query);
//
//                MapboxGeocoding mapboxGeocoding1 = MapboxGeocoding.builder()
//                        .accessToken("pk.eyJ1Ijoic291dmlrc2Fua2FyMjAxMyIsImEiOiJja2Fyc2VrczEwZDM3MnducW1hNm56dzJoIn0.dD4s9wv9UvF4xejzfUW2_Q")
//                        .query(query)
//                        .build();
//
//                mapboxGeocoding1.enqueueCall(new Callback<GeocodingResponse>() {
//                    @Override
//                    public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
//
//                        List<CarmenFeature> results = response.body().features();
//                        Point firstResultPoint = results.get(0).center();
//                        Log.d("Souvik_geo", "onResponse: " + firstResultPoint.toString());
//
//                        mapboxmap.animateCamera(CameraUpdateFactory.newCameraPosition(
//                                new CameraPosition.Builder()
//                                        .target(new LatLng(firstResultPoint.latitude(),firstResultPoint.longitude()))
//                                        .zoom(14)
//                                        .build()), 4000);
//
//                        mapboxmap.addMarker(new MarkerOptions()
//                                .position(new LatLng(firstResultPoint.latitude(),firstResultPoint.longitude()))
//                                .title(query));
//
//                        fetchroute(new LatLng(firstResultPoint.latitude(),firstResultPoint.longitude()));
//
//
//
//
//
//                    }
//
//                    @Override
//                    public void onFailure(Call<GeocodingResponse> call, Throwable t) {
//
//                    }
//                });
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
//
//        return true;
//    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "Location Explanation", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {

            mapboxmap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {

            Toast.makeText(this, "Please grant the permission", Toast.LENGTH_LONG).show();
            finish();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionmanager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {


        this.mapboxmap = mapboxMap;
        mapboxmap.getUiSettings().setAttributionEnabled(false);
        mapboxmap.getUiSettings().setLogoEnabled(false);


        mapboxMap.setStyle(new Style.Builder().fromUri("http://95.217.212.221:8080/style/style.json"), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);

                final Point originPoint = Point.fromLngLat(locationcomponent.getLastKnownLocation().getLongitude(),
                        locationcomponent.getLastKnownLocation().getLatitude());


                recentre.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mapboxmap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(originPoint.latitude(),originPoint.longitude()))
                                        .zoom(14)
                                        .build()), 4000);

                    }
                });


                //Map Long Click Function
                mapboxmap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    public boolean onMapLongClick(@NonNull LatLng point) {

                        if (mark != null) {

                            mapboxMap.removeMarker(mark);
                        }
                        Toast.makeText(MainActivity.this, String.format("User clicked at: %s", point.toString()), Toast.LENGTH_LONG).show();

                        mark = mapboxmap.addMarker(new MarkerOptions()
                                .position(point));
                                //.title("Eiffel Tower"));

                        MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
                                .accessToken("pk.eyJ1Ijoic291dmlrc2Fua2FyMjAxMyIsImEiOiJja2Fyc2VrczEwZDM3MnducW1hNm56dzJoIn0.dD4s9wv9UvF4xejzfUW2_Q")
                                .query(Point.fromLngLat(point.getLongitude(),point.getLatitude()))
                                //.geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                                .build();

                        reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
                            @Override
                            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                                Log.d("Reverse", String.valueOf(response.body().features().get(0).placeName()));

                                mark.setTitle(String.valueOf(response.body().features().get(0).placeName()));
                            }

                            @Override
                            public void onFailure(Call<GeocodingResponse> call, Throwable t) {

                            }
                        });




                        waypoints.add(Point.fromLngLat(point.getLongitude(), point.getLatitude()));

                        fetchroute(point);  //For getting the route through Direction API
                        return true;
                    }
                });


            }
        });

    }





    private void fetchroute(LatLng point) {


        //Taking the latest location of the user

        Point originPoint = Point.fromLngLat(locationcomponent.getLastKnownLocation().getLongitude(),
                locationcomponent.getLastKnownLocation().getLatitude());

        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());

        NavigationRoute.Builder builder = NavigationRoute.builder(this)
                //.accessToken("pk." + "123")
                .accessToken("pk.c082599c-5f14-496a-a9a4-1c72ef9f9a18")

                .baseUrl("https://graphhopper.com/api/1/navigate/") // Graphopper API
                //.baseUrl("http://95.217.212.221:8082/HelloREST-0.0.2-SNAPSHOT/navigate/")   //Using Custom Direction API

                .origin(originPoint)
                .destination(destinationPoint)


                .user("gh")
                .profile("driving")
                .alternatives(true);

        builder.build().getRoute(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                route = response.body().routes().get(0);

                // Draw the route on the map
                if (navigationMapRoute != null) {
                    navigationMapRoute.removeRoute();
                } else {
                    navigationMapRoute = new NavigationMapRoute(null, mapview, mapboxmap, R.style.NavigationMapRoute);
                }
                navigationMapRoute.addRoute(route);



                button.setEnabled(true);
                button.setBackgroundResource(R.drawable.my_button_enabled);


            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                Log.d("Failure", String.valueOf(t));

                Snackbar.make(mapview, "There was a error calculating the route", Snackbar.LENGTH_LONG).show();

            }
        });




    }



    private void enableLocationComponent(Style loadedMapStyle) {


        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
                    .elevation(5).accuracyAlpha(.6f)
                    .accuracyColor(Color.BLUE).build();

            locationcomponent = mapboxmap.getLocationComponent();

            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .locationComponentOptions(customLocationComponentOptions)
                            .build();

            locationcomponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
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
            locationcomponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationcomponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationcomponent.setRenderMode(RenderMode.COMPASS);

        } else {

            permissionmanager = new PermissionsManager(this);
            permissionmanager.requestLocationPermissions(this);
        }



    }

    @SuppressWarnings({"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapview.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapview.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapview.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapview.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapview.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapview.onLowMemory();
    }


}
