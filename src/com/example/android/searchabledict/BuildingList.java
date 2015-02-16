package com.example.android.searchabledict;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/** BuildingList
 * Populates list view from database asset, called by Building List button in MainDisplay
 */
public class BuildingList extends MainDisplay
{
    DBAdapter mDb = getDatabase();
	static String[] column = new String[] {"name"};
	static int[] to = new int [] {R.id.name};
	public void onCreate(Bundle savedInstanceState)
	{
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.list);

		 final ListView listView = (ListView) findViewById(R.id.listview);        
		 
         mDb.openDatabase();
         
         Cursor dbCursor = mDb.getAllBuildings();
         
		 SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.list, dbCursor, column, to, 0);
		 
		 listView.setAdapter(cursorAdapter);
			
		 listView.setOnItemClickListener(new OnItemClickListener() {
		         public void onItemClick(AdapterView<?> parent, View view,
		                 int position, long id) {

		             Cursor dbCursor = mDb.searchByID(Long.toString(id));
		             openDescription(dbCursor);
		             mDb.close();
		             finish();

		     }
		});


	 }
	
}
