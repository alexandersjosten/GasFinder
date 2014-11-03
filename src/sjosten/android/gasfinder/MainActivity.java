package sjosten.android.gasfinder;

import java.io.IOException;
import java.util.List;

import sjosten.android.gasfinder.database.GasStationsDAO;
import sjosten.android.gasfinder.parser.GasStation;
import sjosten.android.gasfinder.parser.PanicException;
import sjosten.android.gasfinder.parser.Parser;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private GoogleMap map;
	private Marker currentLocation;
	private GasStationsDAO datasourceObject;
	private LocationRequest locationRequest;
	private LocationClient locationClient;
	private boolean updatesRequested;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		datasourceObject = new GasStationsDAO(this);
		datasourceObject.open();
		
		if(datasourceObject.isDatabaseEmpty()) {
			AssetManager am = getAssets();
			List<GasStation> stations = null;	
			try {
				stations = Parser.parseTXTFile("preem.txt", am.open("preem.txt"));
			} catch (IOException | PanicException e) {
				e.printStackTrace();
			}
			
			for(GasStation station : stations) {
				datasourceObject.insertGasStation(station);
			}
		}
		
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		if (map != null) {
			/*
			List<GasStation> dbStations = datasourceObject.getAllStations();
			for(GasStation station : dbStations) {
				map.addMarker(new MarkerOptions().position(
					new LatLng(
						station.getCoordinate().getLatitude(),
						station.getCoordinate().getLongitude()
					)).title("Preem"));
			}*/
			
			locationRequest = LocationRequest.create();
			locationRequest.setInterval(Constants.UPDATE_INTERVAL);
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			locationRequest.setFastestInterval(Constants.FAST_UPDATE_INTERVAL);
			
			updatesRequested = true;
			
			map.setMyLocationEnabled(true);
			
			locationClient = new LocationClient(this, this, this);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		updatesRequested = true;
		locationClient.connect();
	}
	
	@Override
	protected void onResume() {
		datasourceObject.open();
		updatesRequested = true;
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		datasourceObject.close();
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		if(locationClient.isConnected()) {
			locationClient.removeLocationUpdates(this);
		}
		
		locationClient.disconnect();
		map.setLocationSource(null);
		updatesRequested = false;
		
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_download:
			
			return true;
		case R.id.action_settings:
			List<String> stationNames = datasourceObject.getAllNames();
			Intent intent = new Intent(this, SettingsActivity.class);
			int counter = 0;
			for(String name : stationNames) {
				intent.putExtra("STATION_NAME_" + counter, name);
				counter++;
			}
			intent.putExtra("COUNTER", counter);
			startActivity(intent);
			return true;
		case R.id.action_about:
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(R.string.about_title);
			alertDialog.setMessage(getString(R.string.about_content));
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.about_btn_close), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();	
				}
			});
			
			alertDialog.show();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if(currentLocation == null) {
			currentLocation = map.addMarker(new MarkerOptions().position(
				new LatLng(
					location.getLatitude(),
					location.getLongitude()
				)).title("Your location"));
		}
		else {
			currentLocation.setPosition(
				new LatLng(location.getLatitude(), location.getLongitude())
			);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Toast.makeText(this, R.string.no_connection_error, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onConnected(Bundle bundle) {
		if(updatesRequested) {
			locationClient.requestLocationUpdates(locationRequest, this);
		}
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, R.string.disconnected_error, Toast.LENGTH_LONG).show();
	}
}
