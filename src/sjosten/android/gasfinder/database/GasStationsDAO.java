package sjosten.android.gasfinder.database;

import java.util.ArrayList;
import java.util.List;

import sjosten.android.gasfinder.Constants;
import sjosten.android.gasfinder.parser.Coordinate;
import sjosten.android.gasfinder.parser.GasStation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

// Data Access Object for the gas stations database
public class GasStationsDAO {
	private SQLiteDatabase database;
	private MySQLiteHelper databaseHelper;
	private String[] columns = {MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_NAME,
			MySQLiteHelper.COLUMN_LONGITUDE, MySQLiteHelper.COLUMN_LATITUDE};

	public GasStationsDAO(Context context) {
		databaseHelper = new MySQLiteHelper(context);
	}

	public void open() {
		try {
			database = databaseHelper.getWritableDatabase();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		databaseHelper.close();
	}

	public void insertGasStation(GasStation station) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_NAME, station.getName());
		values.put(MySQLiteHelper.COLUMN_LONGITUDE, station.getCoordinate().getLongitude());
		values.put(MySQLiteHelper.COLUMN_LATITUDE, station.getCoordinate().getLatitude());
		
		database.insert(MySQLiteHelper.TABLE_STATIONS, null, values);
	}
	
	public List<GasStation> getAllStations() {
		List<GasStation> stations = new ArrayList<>();
		Cursor cursor = database.query(
			MySQLiteHelper.TABLE_STATIONS,
			columns,
			null,
			null,
			null,
			null,
			null
		);
		
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			stations.add(cursorToGasStation(cursor));
			cursor.moveToNext();
		}
		
		cursor.close();
		return stations;
	}
	
	public List<GasStation> getAllSpecificStations(String stationName) {
		List<GasStation> stations = new ArrayList<>();
		Cursor cursor = database.query(
			MySQLiteHelper.TABLE_STATIONS,
			columns,
			MySQLiteHelper.COLUMN_NAME + " = ?",
			new String[] {stationName},
			null,
			null,
			null
		);
		
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			stations.add(cursorToGasStation(cursor));
			cursor.moveToNext();
		}
		
		return stations;
	}
	
	public List<GasStation> getAllStationsWithinDistance(String stationName, Location currentLocation, int distance) {
		List<GasStation> stations = new ArrayList<>();
		Cursor cursor = database.query(
			MySQLiteHelper.TABLE_STATIONS,
			columns,
			MySQLiteHelper.COLUMN_NAME + " = ?",
			new String[] {stationName},
			null,
			null,
			null
		);
		
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			GasStation currentStation = cursorToGasStation(cursor);
			
			double latDistance = Math.toRadians(currentLocation.getLatitude() - currentStation.getCoordinate().getLatitude());
			double lngDistance = Math.toRadians(currentLocation.getLongitude() - currentStation.getCoordinate().getLongitude());
			double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
                    (Math.cos(Math.toRadians(currentLocation.getLatitude()))) *
                    (Math.cos(Math.toRadians(currentStation.getCoordinate().getLatitude()))) *
                    (Math.sin(lngDistance / 2)) *
                    (Math.sin(lngDistance / 2));
			
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			double dist = Constants.EARTH_RADIUS * c;
			
			if(dist < distance) {
				stations.add(currentStation);
			}
			cursor.moveToNext();
		}
		
		return stations;
	}
	
	public List<String> getAllNames() {
		List<String> names = new ArrayList<>();
		String[] cols = {MySQLiteHelper.COLUMN_NAME};
		Cursor cursor = database.query(true, MySQLiteHelper.TABLE_STATIONS, cols, null, null, null, null, null, null);
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast()) {
			names.add(cursor.getString(0));
			cursor.moveToNext();
		}
		return names;
	}
	
	// returns true iff database is empty, false otherwise (i.e. negate result of cursor.moveToFirst())
	public boolean isDatabaseEmpty() {
		return !database.query(
			MySQLiteHelper.TABLE_STATIONS,
			columns,
			null,
			null,
			null,
			null,
			null
		).moveToFirst();
	}
	
	private GasStation cursorToGasStation(Cursor cursor) {
		long id = cursor.getLong(0);
		String name = cursor.getString(1);
		Coordinate coords = new Coordinate(cursor.getDouble(2), cursor.getDouble(3));
		GasStation station = new GasStation(coords, name);
		station.setId(id);
		return station;
	}
}
