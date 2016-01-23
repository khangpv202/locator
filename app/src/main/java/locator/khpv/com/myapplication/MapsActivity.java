package locator.khpv.com.myapplication;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    LatLng receivedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri data = getIntent().getData();
        if (data != null) {
            String strData = data.toString();
            if (strData.contains("http://www.YFL.com")) {
                String[] locations = strData.split("/");
                receivedData = new LatLng(Double.parseDouble(locations[locations.length - 2]),
                        Double.parseDouble(locations[locations.length - 1]));
            }
        }

        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        //set up view
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        List<String> titleList = new ArrayList<>();
        titleList.add("Send your Location");
        titleList.add("Tracking Location");

        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, titleList));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    startLocationUpdates();
                    String smsBody = "http://www.YFL.com/" + markerOptions.getPosition().latitude
                            + "/" + markerOptions.getPosition().longitude;
                    Uri uri = Uri.parse("smsto:");
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
                    smsIntent.putExtra("sms_body", smsBody);
                    startActivity(smsIntent);
                } else {

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    boolean isRequested = false;
    LocationRequest mLocationRequest;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(15000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (receivedData != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(receivedData, 17);
            mMap.animateCamera(cameraUpdate);
            setMartOption(receivedData);
            googleApiClient.disconnect();
        }
    }

    private void setMartOption( LatLng receivedData) {
        if (null == markerOptions) {
            markerOptions = new MarkerOptions().position(receivedData).title("your friend position");
            mMap.addMarker(markerOptions);
        } else markerOptions.position(receivedData);
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (!isRequested) {
            isRequested = true;
            createLocationRequest();
        }
        startLocationUpdates();
    }

    MarkerOptions markerOptions;

    //
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, mLocationRequest, new com.google.android.gms.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        LatLng vietNam = new LatLng(location.getLatitude(), location.getLongitude());
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(vietNam, 17);
                        setMartOption(vietNam);

                        mMap.animateCamera(cameraUpdate);
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
