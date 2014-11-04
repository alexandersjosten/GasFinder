package sjosten.android.gasfinder;

import android.os.Environment;

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
	public static final String KML_SITE = "https://www.dropbox.com/sh/modmgg8ff4wws4h/AABXscOPQtPrFZWwGYOw8bzRa?dl=0";
	
	// Path to tmp kml-files
	public static final String KML_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/gasfinder/tmp_kml/";
}
