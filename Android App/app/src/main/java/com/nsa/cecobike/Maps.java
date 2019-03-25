package com.nsa.cecobike;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class Maps extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private final int STORAGE_PERMISSION_CODE = 1;
    LatLng previousLocation;
    Button start_journey, finish_journey;

    //List of Points for Database:
    ArrayList<Point> coordinates = new ArrayList<>();
    Double TotalDistance;

    public Maps() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_maps, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map));
        FragmentManager fm = getChildFragmentManager();
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        //button to start the journey

         start_journey = (Button) view.findViewById(R.id.start_journey_button);
         finish_journey = (Button) view.findViewById(R.id.finish_journey_button);
         start_journey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start journey actions start here


                //remove the Toast below when finished testing
                Toast.makeText(getContext(), "Start the journey button was clicked ", Toast.LENGTH_SHORT).show();
                getCurrentLocation();
                start_journey.setVisibility(View.GONE);
                finish_journey.setVisibility(View.VISIBLE);
            }
        });
         finish_journey.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //finish journey actions start here 

                 //Calculates Distance
                 getlatlon();

                 Journey journey = new Journey(TotalDistance, null, coordinates);

                 //remove the Toast below when finished testing
                 Toast.makeText(getContext(), "Finish journey button was clicked ", Toast.LENGTH_SHORT).show();

             }
         });




    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//
//        if (ActivityCompat.checkSelfPermission(this.getContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this.getContext(),
//                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            requestStoragePermission();
//            return;
//        }
    }

    public void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this.getContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
            return;
        }
        mMap.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16f));
        getCameraUpdates(location);
        previousLocation = new LatLng(location.getLatitude(), location.getLongitude());

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                30000,
                10, locationListenerGPS);

    }


    private void getCameraUpdates(Location location)
    {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                .zoom(17)// Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }
    private void requestStoragePermission() {
//        ActivityCompat.requestPermissions(this.getActivity(),
//                   new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                STORAGE_PERMISSION_CODE);
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this.getActivity(), "Access is now granted", Toast.LENGTH_SHORT).show();
                    // permission was granted, yay!
                } else {
                    Toast.makeText(this.getActivity(), "Access has been declined by user", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this.getActivity(), "Permission must be accepted to start", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    public void addPolyLinesToMap(final Location location) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                PolylineOptions polyline = new PolylineOptions().add(previousLocation)
                .add(new LatLng(location.getLatitude(), location.getLongitude())).width(20).color(Color.BLUE).geodesic(true);
//                mMap.addMarker(new MarkerOptions().position((previousLocation)).title("Old location"));
//                mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("new location"));
                coordinates.add(new Point(location.getLatitude(), location.getLongitude()));
                mMap.addPolyline(polyline);
                previousLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }
        });
    }

    public void getlatlon(){
        //Calculating the distance in meters
        double latitude = 0;
        double longitude = 0;

        for (int i = 0; i < coordinates.size(); i++){
            if (coordinates.get(i).getpLat() < coordinates.get(i+1).getpLat()){
                latitude = coordinates.get(i+1).getpLat() - coordinates.get(i).getpLat();
            }
            else if(coordinates.get(i).getpLat() > coordinates.get(i+1).getpLat()) {
                latitude = coordinates.get(i).getpLat() - coordinates.get(i+1).getpLat();
            }
            if (coordinates.get(i).getpLon() < coordinates.get(i+1).getpLon()){
                longitude = coordinates.get(i+1).getpLat() - coordinates.get(i).getpLat();
            }
            else if(coordinates.get(i).getpLon() > coordinates.get(i+1).getpLon()) {
                longitude = coordinates.get(i).getpLon() - coordinates.get(i+1).getpLon();
            }
            getcaldistance(latitude, longitude);
        }
    }

    public void getcaldistance(Double Latitude, Double Longitude){
        Latitude = Latitude * Latitude;
        Longitude = Longitude * Longitude;

        Double Distance = Math.sqrt(Latitude + Longitude);
        TotalDistance = TotalDistance + Distance;
    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getActivity(), "Location update", Toast.LENGTH_SHORT).show();
            getCameraUpdates(location);
//            previousLocation = location;
            addPolyLinesToMap(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}
