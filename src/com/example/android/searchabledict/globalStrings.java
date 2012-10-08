package com.example.android.searchabledict;
import android.app.Activity;
public class globalStrings extends Activity{

	//name of selected building
	public static String nameSelectedBuilding = "";
	
	//path drawing state
	public static int pathDrawingState = 0;

	//webservice calling boolean (so we only get the data once for each path)
    public static int webserviceCallBool=0;
    
    //current user location lat and long (initialized to the union,
    //these variables change on movement of the user)
    public static double currentUserLat = 42.734279;
    public static double currentUserLong= -84.483277;

}

