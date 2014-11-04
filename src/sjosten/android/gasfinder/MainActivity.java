package sjosten.android.gasfinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sjosten.android.gasfinder.database.GasStationsDAO;
import sjosten.android.gasfinder.parser.GasStation;
import sjosten.android.gasfinder.parser.PanicException;
import sjosten.android.gasfinder.parser.Parser;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends Activity implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, OnMarkerClickListener {

	private GoogleMap map;
	//private Marker currentLocation;
	private Location currentLocation;
	protected static GasStationsDAO datasourceObject;
	private LocationRequest locationRequest;
	private LocationClient locationClient;
	private boolean updatesRequested;
	private List<Polyline> activePolylines;
	private List<Marker> activeMarkers;
	private String jsonResult;
	
	// This is for the inner class, but need to call dismiss in onPause in order to not
	// get an exception
	private ProgressDialog progress;
	private boolean zoomed;
	
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
		map.setOnMarkerClickListener(this);

		locationRequest = LocationRequest.create();
		locationRequest.setInterval(Constants.UPDATE_INTERVAL);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setFastestInterval(Constants.FAST_UPDATE_INTERVAL);
		
		updatesRequested = true;
		
		map.setMyLocationEnabled(true);
		activePolylines = new ArrayList<>();
		activeMarkers = new ArrayList<>();
		
		zoomed = false;
		locationClient = new LocationClient(this, this, this);
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
		
		if(jsonResult != null) {
			drawPath(jsonResult);			
		}
		
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		if(progress != null) {
			progress.dismiss();
		}
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
			startActivity(new Intent(this, DownloadKMLActivity.class));
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
			return true;
		case R.id.action_distance:
			if(activeMarkers.isEmpty()) {
				Toast.makeText(this, R.string.no_markers_found, Toast.LENGTH_LONG).show();
			}
			else {
				getClosestMarker(false);
			}
			return true;
		case R.id.action_time:
			if(activeMarkers.isEmpty()) {
				Toast.makeText(this, R.string.no_markers_found, Toast.LENGTH_LONG).show();
			}
			else {
				getClosestMarker(true);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if(currentLocation == null || 
				!(currentLocation.getLatitude() == location.getLatitude() && 
				  currentLocation.getLongitude() == location.getLongitude())) {
			currentLocation = location;
			drawStations();
			if(!zoomed) { // Simply to zoom once..
				map.animateCamera(
					CameraUpdateFactory.newLatLngZoom(
						new LatLng(
							currentLocation.getLatitude(),
							currentLocation.getLongitude()
						),
						Constants.MAP_ZOOM
					)
				);
				
				zoomed = true;
			}
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

	@Override
	public boolean onMarkerClick(Marker marker) {
		String url = makeURL(
			new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
			marker.getPosition()
		);
		JsonAsyncTask jsonAsync = new JsonAsyncTask(url, true);
		jsonAsync.execute();
		return false;
	}
	
	private String makeURL(LatLng from, LatLng to) {
		return Constants.URL_DIRECTION + "origin=" + from.latitude + "," + 
				from.longitude + "&destination=" + to.latitude + "," + to.longitude;
	}
	
	private Marker getClosestMarker(boolean time) {
		Map<String, Marker> map = new HashMap<>();
		for(Marker marker : activeMarkers) {
			String url = makeURL(
				new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 
				marker.getPosition()
			);
			map.put(url, marker);
		}
		ShortestTravelTask stt = new ShortestTravelTask(map, time);
		stt.execute();
		return null;
	}
	
	private class JsonAsyncTask extends AsyncTask<Void, Void, String> {
		private String url;
		private boolean draw;
		public JsonAsyncTask(String url, boolean draw) {
			this.url = url;
			this.draw = draw;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = new ProgressDialog(MainActivity.this);
			progress.setMessage("Fetching route for you, please wait...");
			progress.setIndeterminate(true);
			progress.show();
		}
		
		@Override
		protected String doInBackground(Void... params) {
			return JSONParser.getJSONFromURL(url);
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			progress.dismiss();
			if(result != null) {
				jsonResult = result;
				if(draw) {
					drawPath(result);					
				}
			}
		}
	}
	
	private class ShortestTravelTask extends AsyncTask<Void, Void, Marker> {
		private Map<String, Marker> map;
		private boolean time;
		public ShortestTravelTask(Map<String, Marker> map, boolean time) {
			this.map = map;
			this.time = time;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = new ProgressDialog(MainActivity.this);
			if(time) {
				progress.setMessage("Fetching shortest traveling time for you, please wait...");
			}
			else {
				progress.setMessage("Fetching shortest traveling distance for you, please wait...");
			}
			progress.setIndeterminate(true);
			progress.show();
		}
		
		@Override
		protected Marker doInBackground(Void... params) {
			double currentLowest = Double.POSITIVE_INFINITY;
			Marker currentLowestMarker = null;
			for(String key : map.keySet()) {
				String json = JSONParser.getJSONFromURL(key);
				if(time) {
					double currentTime = JSONParser.getTravelingTime(json);
					if(currentTime < currentLowest) {
						currentLowest = currentTime;
						currentLowestMarker = map.get(key);
					}
				}
				else {
					double currentDistance = JSONParser.getTravelingDistance(json);
					if(currentDistance < currentLowest) {
						currentLowest = currentDistance;
						currentLowestMarker = map.get(key);
					}
				}
				
			}
			return currentLowestMarker;
		}
		
		@Override
		protected void onPostExecute(Marker result) {
			super.onPostExecute(result);
			progress.dismiss();
			if(result != null) {
				onMarkerClick(result);
			}			
		}
	}


	private void drawPath(String result) {
		// Start by removing the old path..
		for(Polyline p : activePolylines) {
			p.remove();
		}
		activePolylines.clear();
		
		String polylines = JSONParser.getEncodedString(result);
		List<LatLng> latLngs = decodePolylines(polylines);
		for(int i = 0; i < latLngs.size() - 1; i++) {
			LatLng origin = latLngs.get(i);
			LatLng destination = latLngs.get(i + 1);
			activePolylines.add(
				map.addPolyline(
					new PolylineOptions().add(origin, destination)
						.width(Constants.POLYLINE_WIDTH)
						.color(Color.BLUE)
						.geodesic(true)
				)
			);
		}
	}
	
	// This is very inspired by the example at http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
	private List<LatLng> decodePolylines(String polylines) {
		List<LatLng> resultList = new ArrayList<>();
		int index = 0;
		int polylinesLength = polylines.length();
		int latitude = 0;
		int longitude = 0;
		
		while(index < polylinesLength) {
			int b = -1;
			int shift = 0;
			int result = 0;
			do {
				b = polylines.charAt(index) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
				index++;
			} while(b >= 0x20);
			
			int dLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			latitude += dLat;
			
			shift = 0;
			result = 0;
			do {
				b = polylines.charAt(index) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
				index++;
			} while(b >= 0x20);
			
			int dLng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			longitude += dLng;
			
			resultList.add(
				new LatLng(
					((double)latitude / 1E5),
					((double)longitude / 1E5)
				)
			);
		}
		
		return resultList;
	}
	
	private void drawStations() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		List<String> stationNames = datasourceObject.getAllNames();
		List<GasStation> dbStations = new ArrayList<>();
		String distanceString = preferences.getString(getString(R.string.radius_key), "0");
		
		for(Marker marker : activeMarkers) {
			marker.remove();
		}
		
		activeMarkers.clear();
				
		if(distanceString.equals("All")) {
			for(int i = 0; i < stationNames.size(); i++) {
				String name = stationNames.get(i);
				switch(name.toLowerCase(Locale.getDefault())) {
				case "preem":
					if(preferences.getBoolean(getString(R.string.preem_key), false)) {
						dbStations.addAll(datasourceObject.getAllSpecificStations(name));
					}
					break;
				case "st1 sverige":
					if(preferences.getBoolean(getString(R.string.st1_key), false)) {
						dbStations.addAll(datasourceObject.getAllSpecificStations(name));
					}
					break;
				case "shell":
					if(preferences.getBoolean(getString(R.string.shell_key), false)) {
						dbStations.addAll(datasourceObject.getAllSpecificStations(name));
					}
					break;
				case "gasmackar":
					if(preferences.getBoolean(getString(R.string.natural_gas_key), false)) {
						dbStations.addAll(datasourceObject.getAllSpecificStations(name));
					}
					break;
				}
			}
		}
		else {
			int distance = Integer.parseInt(distanceString);
			for(int i = 0; i < stationNames.size(); i++) {
				String name = stationNames.get(i);
				switch(name.toLowerCase(Locale.getDefault())) {
				case "preem":
					if(preferences.getBoolean(getString(R.string.preem_key), false)) {
						dbStations.addAll(datasourceObject.getAllStationsWithinDistance(name, currentLocation, distance));
					}
					break;
				case "st1 sverige":
					if(preferences.getBoolean(getString(R.string.st1_key), false)) {
						dbStations.addAll(datasourceObject.getAllStationsWithinDistance(name, currentLocation, distance));
					}
					break;
				case "shell":
					if(preferences.getBoolean(getString(R.string.shell_key), false)) {
						dbStations.addAll(datasourceObject.getAllStationsWithinDistance(name, currentLocation, distance));
					}
					break;
				case "gasmackar":
					if(preferences.getBoolean(getString(R.string.natural_gas_key), false)) {
						dbStations.addAll(datasourceObject.getAllStationsWithinDistance(name, currentLocation, distance));
					}
					break;
				}
			}
		}
		
		
		for(GasStation station : dbStations) {
			activeMarkers.add(map.addMarker(new MarkerOptions().position(
				new LatLng(
					station.getCoordinate().getLatitude(),
					station.getCoordinate().getLongitude()
				)).title(station.getName())));
		}
	}
}
