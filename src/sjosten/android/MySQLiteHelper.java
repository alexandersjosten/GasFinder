package sjosten.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {
    // Constants for the database
    private static final String TABLE_STATIONS = "stations";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ID = "_id";  // COLUMN_ID is the key
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_NATGAS = "nat_gas";  // True (1) if station is "naturgas", false (0) otherwise
    private static final String COLUMN_ELECTRICITY = "electricity";  // True (1) if station is "laddstation", false (0) otherwise
    
    private static final String DATABASE_NAME = "stations.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_DELETE = "DROP TABLE IF EXISTS " + TABLE_STATIONS;
    private static final String DATABASE_CREATE =
        "CREATE TABLE " + TABLE_STATIONS + "(" + COLUMN_ID + " integer primary key autoincrement, " +
        COLUMN_NAME + " text not null, " + COLUMN_LATITUDE + " real, " + COLUMN_LONGITUDE + " real, " +
        COLUMN_NATGAS + " integer, " + COLUMN_ELECTRICITY + " integer);";
    
    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(DATABASE_DELETE);
        onCreate(database);
    }
}
