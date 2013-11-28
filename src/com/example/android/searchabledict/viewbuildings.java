package com.example.android.searchabledict;

import java.util.Dictionary;
import java.util.Hashtable;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class viewbuildings extends ListActivity {
	Dictionary<String, String> d = new Hashtable<String, String>();
    static int buildingsCounter =0;
	//fill the buildings array with their names
    static int estimatedBuildingsCount = 817;
	static String[] names = new String[816];
	// AssetManager assetManager =   viewbuildings.this.getAssets();

	//function to fill the dictionary
	public  void FillDictionary(Dictionary<String, String> d)
	{
        System.out.println("IN FILL DICTIONARY");
        
                  DBAdapter db = new DBAdapter(this); 
                  db.open();
                  Cursor cursor = db.getAllBuildings();
                  cursor.moveToFirst();
                  int length = cursor.getCount();
          	      for (int i=0; i<length; i++) {
          	    	  d.put( cursor.getString(2), cursor.getString(3) );
          	    	  names[i] = cursor.getString(2);
          	    	  buildingsCounter++;
          	    	  cursor.moveToNext();
          	     }
          	      db.close();
	}
	
	
	 @Override
	public void onCreate(Bundle savedInstanceState)
	 {
		 super.onCreate(savedInstanceState);
		 //read in the buildings from the text file
         //fill the dictionary with the building names and numbers
     	FillDictionary(d);
     	//System.out.println(d.toString());
		 
		 
		 this.setListAdapter(new ArrayAdapter<String>(this,
				 android.R.layout.simple_list_item_1,names));
	 }

	 
	 
	 
	@Override
    protected void onListItemClick(ListView l,  View v, int position,long id)
	{
		 super.onListItemClick(l, v, position, id);
		 //get the item that was clicked
		 Object o = this.getListAdapter().getItem(position);
		 String keyword = o.toString();
		 
		 //set global singleton to the keyword
		 globalStrings.nameSelectedBuilding = keyword;
		 //set global path drawing state to one for on
		 globalStrings.pathDrawingState =1;
		 //set the webservice call boolean to on
		 globalStrings.webserviceCallBool=1;

		 
		 //start new intent
		 Intent myIntent = new Intent(this, MapsActivity.class);
		 startActivity(myIntent);
		 finish();
	}

}