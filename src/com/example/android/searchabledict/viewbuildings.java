package com.example.android.searchabledict;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.util.Hashtable;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.view.View;
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
       // names[0] = "build1";
		
		//InputStream openRawResource1 = getContext.R.raw.idfile;
		try {
            System.out.println("IN TRY");
            //     names[1] = "build2";

			   // InputStream stream = null;
			   // stream = assetManager.open("ID_file.txt");
	           // System.err.println("AFTER Assed manager open");
            InputStream is = getResources().openRawResource(R.raw.idfile); 
	          
            //     AssetFileDescriptor descriptor = getAssets().openFd("idfile1.txt"); 
            //        FileReader reader = new FileReader(descriptor.getFileDescriptor()); 
            
	            BufferedReader f = new BufferedReader(new InputStreamReader(is)); 
	            String line = "";
		        //  names[2] = "build3";
                
	            while ((line = f.readLine())!=null) 
	            {
	            	String[] a = line.split(",");
	                String ID = a[a.length-1];
	                String BuildingName = "";
	                for (int i=0; i<a.length-1; i++)
	                {
	                	BuildingName = BuildingName + a[i];
	                	if (i<a.length-2)
	                	{
	                		BuildingName = BuildingName + ",";
	                	}
	                }
	                d.put(BuildingName, ID);
	               names[buildingsCounter] = BuildingName;
	                
		            //System.err.println(BuildingName);

	                buildingsCounter++;
	            }
	        } catch (Exception e) {
	            System.err.println("Unable to read from ID_file.txt");
	        }
	        //names[3] = "build4";

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
		 //get the item taht was clicked
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