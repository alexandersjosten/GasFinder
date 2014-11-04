package sjosten.android.gasfinder;


public class Constants {
	// 5000ms == 5s, so update the location every 5 seconds
	public static final long UPDATE_INTERVAL = 5000;
	
	// The fast update interval, 1s
	public static final long FAST_UPDATE_INTERVAL = 1000;
	
	// URL for direction request
	public static final String URL_DIRECTION = "https://maps.googleapis.com/maps/api/directions/json?";
	
	// Line width for the polyline
	public static final int POLYLINE_WIDTH = 10;
	
	// Key for the saving instance
	public static final String SAVE_KEY = "json";
	
	// Address to kml-files
	public static final String KML_SITE = "http://www.student.bth.se/~alsj14/kml_files/index.html";
	
	public static final String KML_FILE = "tmp.kml";
	
	// Constant for earth radius
	public static final int EARTH_RADIUS = 6371;
	
	// Zoom value
	public static final int MAP_ZOOM = 10;
}
