package com.example.android.searchabledict;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;


/** DBAdapter
 * Access, query, and manage building database
 * Currently functioning but incomplete, being worked on by Anna
 */
public class DBAdapter extends SQLiteOpenHelper{
	private static String DATABASE_PATH = "/data/data/com.example.android.searchabledict/databases/";
	private static final String DATABASE_NAME = "building.db";

    private final Context context;
	private SQLiteDatabase mDb;
	
	public DBAdapter(Context ctx){
		super(ctx, DATABASE_NAME, null, 1);
		this.context = ctx;
	}
    
    public void createDatabase(){
        
    	boolean dbExist = checkDataBase();
     
    	if(!dbExist){
    		//Creates empty database in the default system path
    		this.getReadableDatabase();

            //overwrite empty database with our database.
    		try {     
    			copyDatabase();    
    		} 
    		catch (IOException e) {     
    			throw new Error("Error copying database");     
    		}
    	} 
    	mDb = this.getWritableDatabase();
    }
    /**
     * Check if the database already exists
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
    
   	SQLiteDatabase checkDB = null;
    
   	try{
   		String dbPath = DATABASE_PATH + DATABASE_NAME;
   		checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    
   	}
   	catch(SQLiteException e){
    
   		//database doesn't exist yet.
    
   	}
    
   	if(checkDB != null){
    
   		checkDB.close();
    
   	}
    
   	return checkDB != null ? true : false;
   }
    
    /**
    * Copies your database from your local assets-folder to the just created empty database in the
    * system folder, from where it can be accessed and handled.
    * This is done by transfering bytestream.
    * */
    private void copyDatabase() throws IOException{

       //Open your local db as the input stream
       InputStream myInput = context.getAssets().open(DATABASE_NAME);

       // Path to the just created empty db
       String outFileName = DATABASE_PATH + DATABASE_NAME;

       //Open the empty db as the output stream
       OutputStream myOutput = new FileOutputStream(outFileName);

       //transfer bytes from the inputfile to the outputfile
       byte[] buffer = new byte[1024];
       int length;
       while ((length = myInput.read(buffer))>0){
        myOutput.write(buffer, 0, length);
       }

       //Close the streams
       myOutput.flush();
       myOutput.close();
       myInput.close();

    }
    public void openDatabase() throws SQLException{

        //Open the database
        String dbPath = DATABASE_PATH + DATABASE_NAME;
        mDb = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if(mDb != null)
        mDb.close();

        super.close();

    }


    public Cursor getAllBuildings()
    {
        return mDb.query("main", new String[] {"_id", "abbreviation", "name", "id", "description", "image", "latitude", "longitude"}, null, null, null, null, null);
    }


    public Cursor getBuilding(String query) throws SQLException
    {
        Cursor cursor =  mDb.rawQuery("SELECT * FROM main WHERE name LIKE '" + query + "%'", null);

        if(cursor != null)
        {
            cursor.moveToFirst();
        }
        return cursor;
    }
    /*
    
    public Cursor jointQuery()
    {
    	return db.rawQuery("SELECT * FROM" + DATABASE_TABLE_1 + ", " + DATABASE_TABLE_2 +
    			" WHERE " + KEY_ROWID_1 + " = " + ASSOC_ID +
    			null, null);
    }   
*/
    public Cursor searchByID(String id) throws SQLException {
        String query = "SELECT * FROM main WHERE _id = '" + id +"';";
        Cursor mCursor = mDb.rawQuery(query,null);
 
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
     }
    
    public Cursor searchByAliasId(String id) throws SQLException {
        String query = "SELECT * FROM alias WHERE _id = '" + id +"';";
        Cursor mCursor = mDb.rawQuery(query,null);
 
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
     }
  

	@Override
	public void onCreate( SQLiteDatabase db){
	}
	
	@Override
	public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion){
	}
}
