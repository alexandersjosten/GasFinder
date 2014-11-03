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
}
