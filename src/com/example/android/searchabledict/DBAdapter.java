package com.example.android.searchabledict;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter{
	public static final String DATABASE_NAME = "building.db";
	public static final int DATABASE_VERSION = 1;
	
    public static final String DATABASE_TABLE_1 = "main";
    public static final String KEY_ROWID_1 = "_id";
    public static final String ABBREVIATION = "abbreviation";
    public static final String BUILD_NAME = "name";
    public static final String BUILD_ID = "id";
    public static final String DESCRIPTION = "description";
    public static final String IMAGE = "image";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    
    public static final String DATABASE_TABLE_2 = "alias";
    public static final String KEY_ROWID_2 = "_id";
    public static final String ASSOC_ID = "assoc_id";
    public static final String ALIAS = "alias";
    
    public static final String DATABASE_TABLE_FTS = "virtualAlias";
    public static final String DATABASE_TABLE_FTS2 = "virtualBuidling";
	
	private static final String CREATE_TABLE_MAIN = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_1 + 
    		"(" + KEY_ROWID_1 + ", " + ABBREVIATION + ", " + BUILD_NAME + ", "+ BUILD_ID + ", " + 
    		DESCRIPTION + ", " + IMAGE + ", "+ LATITUDE + " ," + LONGITUDE + ");";
    
    private static final String CREATE_TABLE_ALIAS = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE_2 + 
    		"(" + KEY_ROWID_2 +", " + ASSOC_ID + ", " +  ALIAS + ");";
	
    
    private static final String CREATE_FTS_TABLE = "CREATE VIRTUAL TABLE " + DATABASE_TABLE_FTS + " USING fts3(" +
    		KEY_ROWID_2 +", " + ASSOC_ID + ", " +  ALIAS + ");";
    
    private static final String CREATE_FTS_TABLE2 ="CREATE VIRTUAL TABLE " + DATABASE_TABLE_FTS2 + " USING fts3(" +
    		KEY_ROWID_1 +", "  + ABBREVIATION + ", " + BUILD_NAME + ", "+ BUILD_ID + ", " + 
    		DESCRIPTION + ", " + IMAGE + ", "+ LATITUDE + " ," + LONGITUDE + ");";
    
    private final Context context;
	public DatabaseHelper DBHelper;
	public SQLiteDatabase db;
	
	public DBAdapter(Context ctx){
		this.context = ctx;
		this.DBHelper = new DatabaseHelper( this.context );
	}
    
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper( Context context ){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate( SQLiteDatabase db){
			db.execSQL( CREATE_TABLE_MAIN );
			db.execSQL( CREATE_TABLE_ALIAS );
			db.execSQL( CREATE_FTS_TABLE );
			db.execSQL( CREATE_FTS_TABLE2 );
		}
		
		@Override
		public void onUpgrade( SQLiteDatabase db, int oldVerison, int newVersion){
			android.util.Log.w(DATABASE_NAME,"Upgrading database, which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS" + DATABASE_TABLE_1);
    		db.execSQL("DROP TABLE IF EXISTS" + DATABASE_TABLE_2);
    		onCreate(db);
		}
	}
	
	public DBAdapter open() throws SQLException{
		this.db = this.DBHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		this.DBHelper.close();
	}
    public long insertBuilding(String _id, String abbreviation, String name, String ID, String description, String image, String latitude, String longitude)
    {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_ROWID_1, _id);
    	initialValues.put(ABBREVIATION, abbreviation);
    	initialValues.put(BUILD_NAME, name);
    	initialValues.put(BUILD_ID, ID);
    	initialValues.put(DESCRIPTION, description);
    	initialValues.put(IMAGE, image);
    	initialValues.put(LATITUDE, latitude);
    	initialValues.put(LONGITUDE, longitude);
    	return db.insert(DATABASE_TABLE_1, null, initialValues);
    }
    
    public Cursor getBuilding(long id) throws SQLException
	{
	    Cursor cursor = db.query(true, DATABASE_TABLE_1, new String[] {KEY_ROWID_1, ABBREVIATION, BUILD_NAME, BUILD_ID, DESCRIPTION, IMAGE, LATITUDE, LONGITUDE}, KEY_ROWID_1 + "=" + id, null, null, null, null, null);
	    if(cursor != null) 
	    {
	    	cursor.moveToFirst();
	    }
	    return cursor;
	}
    
    public Cursor getAllBuildings()
    {
    	return db.query(DATABASE_TABLE_1, new String[] {KEY_ROWID_1, ABBREVIATION, BUILD_NAME, BUILD_ID, DESCRIPTION, IMAGE, LATITUDE, LONGITUDE}, null, null, null, null, null);
    }
    
    public long insertAlias(String _id, String ID,  String alias)
    {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_ROWID_2, _id);
    	initialValues.put(ASSOC_ID, ID);
    	initialValues.put(ALIAS, alias);
    	return db.insert(DATABASE_TABLE_2, null, initialValues);
    }
    public Cursor getAlias(long id) throws SQLException
	{
	    Cursor cursor = db.query(true, DATABASE_TABLE_2, new String[] {KEY_ROWID_2, ASSOC_ID, ALIAS}, KEY_ROWID_2 + "=" + id, null, null, null, null, null);
	    if(cursor != null) 
	    {
	    	cursor.moveToFirst();
	    }
	    return cursor;
	}
    public Cursor getAllAliases()
    {
    	return db.query(DATABASE_TABLE_2, new String[] {KEY_ROWID_2, ASSOC_ID,  ALIAS}, null, null, null, null, null);
    }
    
    public Cursor jointQuery()
    {
    	return db.rawQuery("SELECT * FROM" + DATABASE_TABLE_1 + ", " + DATABASE_TABLE_2 +
    			" WHERE " + KEY_ROWID_1 + " = " + ASSOC_ID +
    			null, null);
    }   
    
    public long virtualAlias(String _id, String ID,  String alias) {
    	 
        ContentValues initialValues = new ContentValues();
        String searchValue = _id + " " + ID + " " + alias; 
        initialValues.put(KEY_ROWID_2, _id);
    	initialValues.put(ASSOC_ID, ID);
    	initialValues.put(ALIAS, alias);
 
        return db.insert(DATABASE_TABLE_FTS, null, initialValues);
    }
   
    public long virtualBuilding(String _id, String abbreviation, String name, String ID, String description, String image, String latitude, String longitude)    
    {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_ROWID_1, _id);
    	initialValues.put(ABBREVIATION, abbreviation);
    	initialValues.put(BUILD_NAME, name);
    	initialValues.put(BUILD_ID, ID);
    	initialValues.put(DESCRIPTION, description);
    	initialValues.put(IMAGE, image);
    	initialValues.put(LATITUDE, latitude);
    	initialValues.put(LONGITUDE, longitude);
    	return db.insert(DATABASE_TABLE_FTS2, null, initialValues);
    }
 
    public Cursor searchBuilding(String inputText) throws SQLException {
        String query = "SELECT * FROM "
        + DATABASE_TABLE_FTS +
         " WHERE " +  ALIAS + " MATCH '" + inputText + "*';";
        Cursor mCursor = db.rawQuery(query,null);
 
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
 
    }
    
    public Cursor searchByID(String IDnumber) throws SQLException {
        String query = "SELECT * FROM "
        + DATABASE_TABLE_FTS2 +
         " WHERE " +  KEY_ROWID_1 + " MATCH '" + IDnumber + "';";
        Cursor mCursor = db.rawQuery(query,null);
 
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
}
