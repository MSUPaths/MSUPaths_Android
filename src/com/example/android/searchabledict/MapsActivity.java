/*
 * Google Maps Application
 * By Nicholas Speeter
 * Based from Tutorial by Wei-Meng Lee http://mobiforge.com/developing/story/using-google-maps-android
 */

package com.example.android.searchabledict;



//import android.R;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.SearchManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.maps.Overlay;

import android.util.Log;
import android.view.Menu;


import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;
import com.google.android.maps.MapView.LayoutParams;  

import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public class MapsActivity extends  MapActivity 
{

    private static final int MENU_SEARCH = 1;
	
	//class member variables
    MapView mapView; 
    MapController mc;
    GeoPoint p;
    GeoPoint p2;
    GeoPoint p3;
    GeoPoint p4;
    GeoPoint pPrevious;
    private static final String URL ="https://dev.gis.msu.edu/FlexData/wayfinding?QUERY={\"QUERYTYPE\":\"FINDPATH\",\"ARGUMENTS\":{\"FROM\":{\"TYPE\":\"LOCATION\",\"EASTING\":-84.483277,\"NORTHING\":42.734279},\"TO\":{\"TYPE\":\"IDENTIFIER\",\"OBJECT_TYPE\":\"BUILDING\",\"OBJECT_ID\":\"0081\"}}}";/** Called when the activity is first created. */ 
	 //String[] namesBuildings = new String[816];//holds building names (817 buildings)
	int buildingsCounter =0;
    Dictionary<String, String> buildingsDictionary = new Hashtable<String, String>();
	String currentBuildingID;
	String dataObjectFromWebservice = "";
	JSONArray coordArray;
    JSONObject menuObject = null;
    GeoPoint currentUserLocationGPSpoint;
    //double currentUserLat = 42.734279;
    //double currentUserLong= -84.483277;
    //String testLat= "42.734279";
    //String testLong = "-84.483277";

	//function to fill the dictionary
	public  void FillDictionary(Dictionary<String, String> d)
	{
		try {
			
            // System.out.println("IN TRY");
            // names[1] = "build2";

			// InputStream stream = null;
			// stream = assetManager.open("ID_file.txt");
	        // System.err.println("AFTER Asset manager open");
            
			InputStream is = getResources().openRawResource(R.raw.idfile); 
	          
            // AssetFileDescriptor descriptor = getAssets().openFd("idfile1.txt"); 
            // FileReader reader = new FileReader(descriptor.getFileDescriptor()); 
            
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
	            
	            buildingsDictionary.put(BuildingName, ID);
	            //namesB[buildingsCounter] = BuildingName;
	                
		        //System.err.println(BuildingName);

	            buildingsCounter++;
	        }
	    } catch (Exception e) 
	    {
	        System.err.println("Unable to read from ID_file.txt");
	    }
	}

	// draw map overlay
    class MapOverlay extends com.google.android.maps.Overlay 
    {
    	//webservice function getData() - sends the request and returns the data object
		public String getData() throws UnsupportedEncodingException 
    	{
    		StringBuilder builder = new StringBuilder();
    		HttpClient client = new DefaultHttpClient();	//HttpGet httpGet = new HttpGet( 
    	 
            //get precision for webservice
    		String latSub = Double.toString(globalStrings.currentUserLat);
    		String longSub = Double.toString(globalStrings.currentUserLong);

            //fill buildings dictionary
            FillDictionary(buildingsDictionary);
            
            //get the corresponding building name id
            currentBuildingID = buildingsDictionary.get(globalStrings.nameSelectedBuilding);
    		currentBuildingID.trim();
    		String currentBuildingIDParsed = currentBuildingID.replaceAll("\\W", "");
    		
            //webservice get request
    		HttpGet httpGet =	new HttpGet("https://dev.gis.msu.edu/FlexData/wayfinding?QUERY=" + 
    											java.net.URLEncoder.encode(
														"{\"QUERYTYPE\":\"FINDPATH\",\"ARGUMENTS\":{\"FROM\":" +
														//"{\"TYPE\":\"LOCATION\",\"EASTING\":-84.483277," + // make latitude and longitude the current gps coordinates of the user
														//"\"NORTHING\":42.734279},\"TO\":" +"{\"TYPE\":\"IDENTIFIER\",\"OBJECT_TYPE\":" +
														"{\"TYPE\":\"LOCATION\",\"EASTING\":" +
														longSub.substring(0,10) + // make longitude and latitude the current gps coordinates of the user (this line is current user longitude)						   
														"\"NORTHING\":" +
														latSub.substring(0, 9) +         //current user latitude
														"},\"TO\":" +"{\"TYPE\":\"IDENTIFIER\",\"OBJECT_TYPE\":" +
														"\"BUILDING\",\"OBJECT_ID\":\"" +
														currentBuildingIDParsed +
														"\"}}}","UTF-8")); // make object id the previously found building id from the dictionary
    	
    		//Toast toast = Toast.makeText(MapsActivity.this, currentBuildingIDParsed, Toast.LENGTH_LONG);
    		//toast.setGravity(Gravity.TOP,-30,50);
    		//toast.show();
    		//"\"BUILDING\",\"OBJECT_ID\":\"0165\"}}}","UTF-8")); // make object id the previously found building id from the dictionary
    		//HttpGet httpGet = new HttpGet( 
    		// "https://dev.gis.msu.edu/FlexData/wayfinding?QUERY={\"QUERYTYPE\":\"FINDPATH\",\"ARGUMENTS\":{\"FROM\":{\"TYPE\":\"LOCATION\",\"EASTING\":-84.483277,\"NORTHING\":42.734279},\"TO\":{\"TYPE\":\"IDENTIFIER\",\"OBJECT_TYPE\":\"BUILDING\",\"OBJECT_ID\":\"0081\"}}}"); 
    	 
    		try
    		{
    			HttpResponse response = client.execute(httpGet);
    			StatusLine statusLine = response.getStatusLine();
    			int statusCode = statusLine.getStatusCode();
    			if (statusCode == 200) 
    			{
    				HttpEntity entity = response.getEntity();
    				InputStream content = entity.getContent();
    				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
   

    				String line;
    				while ((line = reader.readLine()) != null)
    				{
    					builder.append(line);

    				}

    			}
    			else 
    			{
    				Log.e(MapsActivity.class.toString(), "Failed to download file");
    			}

    		}
    		catch (ClientProtocolException e) 
    		{
    			e.printStackTrace();
    		}
    		catch (IOException e)
    		{
    			e.printStackTrace();
    		}
        
    		return builder.toString();
    	}//end webservice function getData()
    
    	
		
        @Override
        public boolean draw(Canvas canvas, MapView mapView, 
        boolean shadow, long when)       //draw stairs
        {
        	super.draw(canvas, mapView, shadow);   
            
            //fill the dictionary with the building names and numbers
        	//FillDictionary(buildingsDictionary);
        	//System.out.println(d.toString());
 
            //---translate the GeoPoint to screen pixels---
            Point screenPts = new Point();
            mapView.getProjection().toPixels(p, screenPts);
 
            //---add the stair marker---
            //Bitmap bmp = BitmapFactory.decodeResource(
            //    getResources(),  R.drawable.stairs);    
            
            //drawing path state is on
            if(globalStrings.pathDrawingState ==1)
            {   // draw stairs
                // canvas.drawBitmap(bmp, screenPts.x, screenPts.y-50, null);   
                
            	//make webservice query only once
                	if(globalStrings.webserviceCallBool==1)
	                {
	                	try
	                	{
	                		dataObjectFromWebservice = getData();//gets the data from webservice
	                		globalStrings.webserviceCallBool =0;//set the webservice call to off for drawing (only need to get the data once)
	                		//Toast toast = Toast.makeText(MapsActivity.this, dataObjectFromWebservice, Toast.LENGTH_LONG);
	                		//toast.setGravity(Gravity.TOP,-30,50);
	                		//toast.show();
	                	}
	                	catch(Exception e)
	                	{
	                		e.printStackTrace();  	
	                	}
	                }
            
	                //set path drawing state back to zero
	                //globalStrings.pathDrawingState = 0; 
	        
	                //below is the code to create a JSON object and draw a path from it
	                JSONObject jObject = null;
	                // String jString = "{\"menu\":	{\"id\": \"file\", \"value\": \"File\", \"popup\": { \"menuitem\": 	[ {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"}, 	{\"value\": \"Open\", \"onclick\": \"OpenDoc()\"}, 	 	{\"value\": \"Close\", \"onclick\": \"CloseDoc()\"}]}}}";
	
	                // String jString = "{\"STATUS\" : \"SUCCESS\", \"DISTANCE\" : 1761.47, \"PATH\" : [ { \"SIDEWALK\" : [-84.48001625,42.72745537, -84.48005603,42.72739020, -84.48017542,42.72742940, -84.48023150,42.72745366,-84.48024563,42.72745873,-84.48026009,42.72746328,-84.48027484,42.72746729,-84.48040169,42.72753839,-84.48042311,42.72755071,-84.48044374,42.72756375,-84.48046354,42.72757747,-84.48048247,42.72759184, -84.48049704,42.72760474,-84.48051080,42.72761812,-84.48052373,42.72763195,-84.48053579,42.72764619,-84.48054697,42.72766082,-84.48055321,42.72766836,-84.48056058,42.72767533,-84.48056899,42.72768163,-84.48067718,42.72779784,-84.48069970,42.72782210,-84.48072316,42.72784586,-84.48074755,42.72786911,-84.48077286,42.72789182,-84.48079454,42.72790829,-84.48081524,42.72792544,-84.48083491,42.72794323,-84.48085353,42.72796163,-84.48087106,42.72798060,-84.48088746,42.72800011,-84.48093293,42.72806455,-84.48098691,42.72814601,-84.48099969,42.72816562,-84.48101331,42.72818492,-84.48102775,42.72820390,-84.48104298,42.72822253,-84.48105727,42.72823849,-84.48107265,42.72825388,-84.48108907,42.72826868,-84.48110575,42.72828227,-84.48112332,42.72829525,-84.48114172,42.72830757,-84.48116896,42.72833426,-84.48119668,42.72836068,-84.48137347,42.72842641,-84.48107864,42.72884640,-84.48096906,42.72901426,-84.48092027,42.72909513,-84.48086738,42.72921085,-84.48086346,42.72921933,-84.48086095,42.72922810,-84.48085989,42.72923703,-84.48086030,42.72924598,-84.48086217,42.72925484,-84.48086822,42.72925838,-84.48087351,42.72926253,-84.48087793,42.72926719,-84.48088139,42.72927228,-84.48088382,42.72927768,-84.48088517,42.72928329,-84.48089038,42.72932035,-84.48089316,42.72943669,-84.48090461,42.72961135,-84.48090448,42.72961929,-84.48090303,42.72962716,-84.48090028,42.72963485,-84.48089629,42.72964222,-84.48091619,42.72975385,-84.48097982,42.72984261,-84.48105039,42.72996723,-84.48114648,42.73013259,-84.48114648,42.73013259,-84.48116462,42.73016275,-84.48118139,42.73019332,-84.48119677,42.73022430,-84.48121076,42.73025563,-84.48122333,42.73028728,-84.48122656,42.73029584,-84.48123099,42.73030411,-84.48123656,42.73031199,-84.48124322,42.73031939,-84.48125089,42.73032624,-84.48125532,42.73042300,   -84.48126734,42.73044790,-84.48128042,42.73047250,-84.48129455,42.73049679,-84.48130970,42.73052073,-84.48132586,42.73054432,-84.48134302,42.73056752,-84.48135969,42.73058702,-84.48137754,42.73060595,-84.48139653,42.73062427,-84.48141662,42.73064193,-84.48143778,42.73065891,-84.48145995,42.73067517,-84.48148310,42.73069067,-84.48150717,42.73070538,-84.48153212,42.73071928,-84.48155790,42.73073233,-84.48156443,42.73073681,-84.48157004,42.73074193,-84.48157461,42.73074758,-84.48157806,42.73075365,-84.48158032,42.73076002,-84.48158133,42.73076656,-84.48160174,42.73077048,-84.48162179,42.73077532,-84.48164140,42.73078104,-84.48166050,42.73078765,-84.48167902,42.73079509,-84.48169688,42.73080336,-84.48171403,42.73081242,-84.48173040,42.73082224,-84.48173441,42.73083549,-84.48173708,42.73084892,-84.48173839,42.73086246,-84.48173833,42.73087604,-84.48173691,42.73088958, -84.48173412,42.73090300] },{ \"CROSSWALK\" : [-84.48173412,42.73090300,-84.48173264,42.73098443 ] },{ \"SIDEWALK\" : [-84.48173264,42.73098443,-84.48173074,42.73101162,] },{ \"UNMARKED\" : [-84.48168998,42.73132173,-84.48168960,42.73138836 ] },{ \"SIDEWALK\" : [-84.48168960,42.73138836,-84.48168838,42.73142496 ] }]}";
	
	                String attributeId = null ;	
           
	                try 
	                {
	                	// create the actuall json object from webservice string
	                	jObject = new JSONObject(dataObjectFromWebservice);
	                } 
	                catch (JSONException e) 
	                {
	                	e.printStackTrace();
	                } 
   		
	                try 
	                {
	                	menuObject = jObject.getJSONObject("CONTENT");
	                } 
	                catch (JSONException e) 
	                {
	                	e.printStackTrace();
	                }
          
	                //extract path gps point data
	                JSONObject popupObject = null;
	                popupObject = menuObject;
	                try 
	                {
	                	coordArray = popupObject.getJSONArray("GEOMETRY");
	                } 
	                catch (JSONException e) 
	                {
	                	e.printStackTrace();
	                }
	   		
	                //Toast toast = Toast.makeText(MapsActivity.this, coordArray.toString(), Toast.LENGTH_LONG);
	                //toast.setGravity(Gravity.TOP,-30,50);
	                //toast.show();
		    	
	                //draw geo points

			 
	            	//extract geo points and draw them
	                try 
	                {
	                	//get previous geo point for the loop
	                	String coordinatesPrev[] = {coordArray.getString(1), coordArray.getString(0)};
	                	double latPrev = Double.parseDouble(coordinatesPrev[0]);
	                	double lngPrev= Double.parseDouble(coordinatesPrev[1]);

                		pPrevious = new GeoPoint(
                				(int) (latPrev * 1E6), 
                				(int) (lngPrev * 1E6));
		     
                		int i =0;
			 
	                	while ( coordArray.getString(i+1) != null ) 
	                	{
						 
	                		// canvas.drawLine(50, 70, 100, 190, paint);
	                		String coordinates2[] = {coordArray.getString(i+1), coordArray.getString(i)};
	                		double lat2 = Double.parseDouble(coordinates2[0]);
	                		double lng2= Double.parseDouble(coordinates2[1]);
	 
	                		//create newest geo point
	                		p3 = new GeoPoint(
	                				(int) (lat2 * 1E6), 
	                				(int) (lng2 * 1E6));
					 
					     
					       p4 = pPrevious; // copy the geo point
					     	      	 
					     
					       Point screenPts2 = new Point();
					       mapView.getProjection().toPixels(p3, screenPts2);
					 
					       //   Bitmap bmp2 = BitmapFactory.decodeResource(
					       //           getResources(),  R.drawable.red);            
					       //   canvas.drawBitmap(bmp2, screenPts2.x, screenPts2.y-50, null); //draw bitmap dot for the path dots
				        
					       //draw a path line code below	        
					       Paint mPaint = new Paint();   
					       mPaint.setDither(true);
					       mPaint.setColor(Color.GREEN);
					       mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
					       mPaint.setStrokeJoin(Paint.Join.ROUND);
					       mPaint.setStrokeCap(Paint.Cap.ROUND);
					       mPaint.setStrokeWidth(4); 
					       //GeoPoint gP1 = new GeoPoint(19240000,-99120000);
					       //GeoPoint gP2 = new GeoPoint(37423157, -122085008);
					       Point p1 = new Point();
					       Point p2 = new Point();
					       Path path = new Path();  
					       Projection projection = mapView.getProjection();
					       projection.toPixels(p4, p1);
					       projection.toPixels(p3, p2);
					       path.moveTo(p2.x, p2.y);
					       path.lineTo(p1.x,p1.y); 
					       canvas.drawPath(path, mPaint); //draw path with lines
					       //draw a line code above
						   //set the previous
					       pPrevious = p3;
					       i = i+2;
                	} // end while
                	
                } 
	            catch (NumberFormatException e) 
	            {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	            catch (JSONException e) 
	            {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
                
            }//end parenthesis for draw bool on
        
            return true;
            
        }// end of draw function
        
	    @Override 
	    public boolean onTouchEvent(MotionEvent event, MapView mapView) //find touched location
	    {   
	        	
	    	/*
	    					
	        //---when user lifts his finger--- // get the lat and lon of touched point
	        if (event.getAction() == 1) {                
	            GeoPoint p = mapView.getProjection().fromPixels(
	                (int) event.getX(),
	                (int) event.getY());
	                Toast.makeText(getBaseContext(), 
	                    p.getLatitudeE6() / 1E6 + "," + 
	                    p.getLongitudeE6() /1E6 , 
	                    Toast.LENGTH_SHORT).show();
	                
	  
	        }                        
	        */
	    	
	    	//---when user lifts his finger--- // get the address code  
	        /*
	        if (event.getAction() == 1) {                
	            GeoPoint p = mapView.getProjection().fromPixels(
	                (int) event.getX(),
	                (int) event.getY());
	
	            Geocoder geoCoder = new Geocoder(
	                getBaseContext(), Locale.getDefault());
	            try {
	                List<Address> addresses = geoCoder.getFromLocation(
	                    p.getLatitudeE6()  / 1E6, 
	                    p.getLongitudeE6() / 1E6, 1);
	
	                String add = "";
	                if (addresses.size() > 0) 
	                {
	                    for (int i=0; i<addresses.get(0).getMaxAddressLineIndex(); 
	                         i++)
	                       add += addresses.get(0).getAddressLine(i) + "\n";
	                }
	
	                Toast.makeText(getBaseContext(), add, Toast.LENGTH_SHORT).show();
	            }
	            catch (IOException e) {                
	                e.printStackTrace();
	            }   
	            return true;
	        }
	        else                
	            return false; // get the address code */
	
	        return false; 	
	            
	    }// end of onTouchEvent        

    } // end of MapOverlay class 
    
    
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener ll = new mylocationlistener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);

        
        mapView = (MapView) findViewById(R.id.mapView);
        LinearLayout zoomLayout = (LinearLayout)findViewById(R.id.zoom);  
        View zoomView = mapView.getZoomControls(); 
        // View zoomView = mapView.getZoomControls();
        zoomLayout.addView(zoomView, 
        					new LinearLayout.LayoutParams(
        							android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 
        							android.view.ViewGroup.LayoutParams.WRAP_CONTENT)); 
        mapView.displayZoomControls(true);
        

        mc = mapView.getController();
        String coordinates[] = {"42.7269444", " -84.4838889"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);
 
        p = new GeoPoint(
            (int) (lat * 1E6), 
            (int) (lng * 1E6));
 
        mc.animateTo(p);
        mc.setZoom(17); 

        //---Add a location marker---
        MapOverlay mapOverlay = new MapOverlay();
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(mapOverlay);  
        
        mapView.invalidate();

    }
    
    //Main Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {   
        menu.add(0, MENU_SEARCH, 0, R.string.menu_search)
        			.setIcon(android.R.drawable.ic_search_category_default)
        			.setAlphabeticShortcut(SearchManager.MENU_KEY);
    	MenuInflater inflater = getMenuInflater(); 
    	inflater.inflate(R.layout.menu, menu);   
    	return true;
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {  
    	// Handle item selection    
    	switch (item.getItemId()) 
    	{   
    	case R.id.selectDestination: 
    		// show the buildings
    		Intent buildingIntent = new Intent(MapsActivity.this, viewbuildings.class);
    		startActivity(buildingIntent);
    		finish();
    		//newGame();
    	case R.id.clearPaths:
    		globalStrings.pathDrawingState = 0;//set paths to off
    		return true;  
 
		default:       
    		//return super.onOptionsItemSelected(item);  
    	}
		
    	return false;
	} // end onOptionsItemSelected
  
    
    
    private  class mylocationlistener implements LocationListener 
    {
    	
        @Override
        public void onLocationChanged(Location location) 
        {
            if (location != null) 
            {
            	//Log.d("LOCATION CHANGED", location.getLatitude() + "");
            	//Log.d("LOCATION CHANGED", location.getLongitude() + "");
            
            
            	//Toast.makeText(MapsActivity.this,                            // toast displays the lat and longitude when you change location
            	//location.getLatitude() + "" + location.getLongitude(),
            	//Toast.LENGTH_LONG).show();
            
            	//String coordinates2[] = {location.getLatitude(), location.getLongitude()};
            	double lat2 = location.getLatitude();    // the code below should zoom/move to the new location
            	double lng2 = location.getLongitude();
     
            	//set the member variables for current lat and long
            	globalStrings.currentUserLat = location.getLatitude();
            	globalStrings.currentUserLong = location.getLongitude();
            
            	currentUserLocationGPSpoint = new GeoPoint(
            									(int) (lat2* 1E6 ), 
        										(int) (lng2* 1E6 ));
            

            	Toast toast = Toast.makeText(MapsActivity.this, globalStrings.currentUserLat + "," +globalStrings.currentUserLong, Toast.LENGTH_LONG);
            	toast.setGravity(Gravity.TOP,-30,50);
            	toast.show();
            
            	//mc.animateTo(currentUserLocationGPSpoint); //move to the current location of the user
            	//mc.setZoom(17);   //set the zoom to 17
            
            	//double proxToEgrLat = location.getLatitude() - 42.8;
            
            	if ( lat2 > 42.723362 && lat2 <42.724939 ) // display toast when you are close to the engineering building
            	{
            		if ( lng2 > -84.48193 && lng2 < -84.478605) // check proximity
            		{
            		
            			//Toast.makeText(MapsActivity.this,               // toast displays the location egr
                        //"You are by the engineering building." ,
            			// Toast.LENGTH_LONG).show();
            		}
            	}
            
            	mapView.invalidate(); //redraw
               
            }// end if
        }// end onLocationChanged
        
        @Override
        public void onProviderDisabled(String provider) 
        {
        	
        }
        @Override
        public void onProviderEnabled(String provider) 
        {
        	
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) 
        {
        	
        }
    }// end of myLocationListener  
           
    
    
    @Override
    protected boolean isRouteDisplayed() 
    {
        return false;
    }

}