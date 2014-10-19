package sjosten.android.gasfinder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {
	private static final String TABLE_STATIONS = "stations";

	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_LONGITUDE = "longitude";
	private static final String COLUMN_LATITUDE = "latitude";

	private static final String DATABASE_NAME = "stations.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table " + TABLE_STATIONS 
		+ "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_NAME
		+ " text not null, " + COLUMN_LONGITUDE + " real, " + COLUMN_LATITUDE + " real);";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
