/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the use restrictions
 * http://help.arcgis.com/en/sdk/10.0/usageRestrictions.htm.
 */

package com.example.android.searchabledict;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteDirection;
import com.esri.core.tasks.na.RouteParameters;
import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

//Will eventually display the map and draw the lines
public class MapDisplay extends Activity implements
        RoutingListFragment.onDrawerListSelectedListener {

    public static MapView mMap = null;

    Route mRoute;
    GraphicsLayer routeLayer, hiddenSegmentsLayer;

    // Symbol used to make route segments "invisible"
    SimpleLineSymbol segmentHider = new SimpleLineSymbol(Color.WHITE, 5);

    // Symbol used to highlight route segments
    SimpleLineSymbol segmentShower = new SimpleLineSymbol(Color.RED, 5);

    // Label showing the current direction, time, and length
    TextView directionsLabel;
    Polygon tmpPoly;

    // List of the directions for the current route (used for the ListActivity)
    public static ArrayList<String> curDirections = null;
    public static ArrayList<Point> curGPSPoints = null;

    public static ArrayList<Point> mIntersectionList = null;
    public static int nextDirection;
    // Current route, route summary, and gps location
    Route curRoute = null;
    String routeSummary = null;
    public static Point mLocation = null;
    //FOR TESTING
    //public static Point mLocation = new Point(-84.499708, 42.715706) ;
    public static boolean routingStarted = false;

    // Global results variable for calculating route on separate thread
    RouteTask mRouteTask = null;
    RouteResult mResults = null;
    String mDestinationName;

    // Variable to hold server exception to show to user
    Exception mException = null;

    ImageView img_currLocation;

    public static DrawerLayout mDrawerLayout;
    LocationDisplayManager ldm;

    // Handler for processing the results
    final Handler mHandler = new Handler();
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    // Progress dialog to show when route is being calculated
    ProgressDialog dialog;

    // Index of the currently selected route segment (-1 = no selection)
    int selectedSegmentID = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps);

        // Retrieve the map and initial extent from XML layout
        mMap = (MapView) findViewById(R.id.map);

        // Add the route graphic layer (shows the full route)
        routeLayer = new GraphicsLayer();
        mMap.addLayer(routeLayer);

        // Initialize the RouteTask
        try {
            mRouteTask = RouteTask
                    .createOnlineRouteTask(getResources().getString(R.string.geocode_url), null);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        // Add the hidden segments layer (for highlighting route segments)
        hiddenSegmentsLayer = new GraphicsLayer();
        mMap.addLayer(hiddenSegmentsLayer);

        curDirections = new ArrayList<String>();
        curGPSPoints = new ArrayList<Point>();
        mIntersectionList = new ArrayList<Point>();
        nextDirection = 1;

        // Make the segmentHider symbol "invisible"
        segmentHider.setAlpha(1);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        img_currLocation = (ImageView) findViewById(R.id.iv_myLocation);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // Set up the directions label
        directionsLabel = (TextView) findViewById(R.id.directionsLabel);


        /**
         * On single clicking the directions label, start a ListActivity to show
         * the list of all directions for this route. Selecting one of those
         * items will return to the map and highlight that segment.
         *
         */
        directionsLabel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (curDirections == null)
                    return;

                mDrawerLayout.openDrawer(Gravity.END);

                String segment = directionsLabel.getText().toString();

                ListView lv = RoutingListFragment.mDrawerList;
                for (int i = 0; i < lv.getCount() - 1; i++) {
                    String lv_segment = lv.getItemAtPosition(i).toString();
                    if (segment.equals(lv_segment)) {
                        lv.setSelection(i);
                    }
                }
            }
        });


        LocationDisplayManager locationDisplayManager = mMap.getLocationDisplayManager();
        locationDisplayManager.setLocationListener(new MyLocationListener());
        locationDisplayManager.start();
        locationDisplayManager.setAutoPanMode(LocationDisplayManager.AutoPanMode.OFF);


    }

    private class MyLocationListener implements LocationListener {

        public MyLocationListener() {
            super();
        }

        /**
         * If location changes, update our current location. If being found for
         * the first time, zoom to our current position with a resolution of 20
         */
        public void onLocationChanged(Location loc) {
            if (loc == null)
                return;
            boolean zoomToMe = (mLocation == null) ? true : false;
            mLocation = new Point(loc.getLongitude(), loc.getLatitude());
            /*if (zoomToMe) {
                final SpatialReference wm = SpatialReference.create(102100);
                final SpatialReference egs = SpatialReference.create(4326);
                Point p = (Point) GeometryEngine.project(mLocation, egs,wm);
                mMap.zoomToResolution(p, 20.0);
            }*/
            // Get destination name, latitude, and longitude from intent
            if (routingStarted == false) {
                Intent intent = getIntent();
                mDestinationName = intent.getStringExtra("building_name");
                Point destination = new Point(Double.valueOf(intent.getStringExtra("longitude")), Double.valueOf(intent.getStringExtra("latitude")));
                QueryDirections(mLocation, destination);
                routingStarted = true;
            } else {
                if (curDirections.size() > nextDirection && curGPSPoints.size() > nextDirection) {

                    double curLat = mLocation.getY();
                    double curLong = mLocation.getX();
                    double nextLat = curGPSPoints.get(nextDirection).getY();
                    double nextLong = curGPSPoints.get(nextDirection).getX();

                    double dist = getDistFromCoords(curLat, curLong, nextLat, nextLong);

                    Log.i("onLocationChanged", "" + Double.toString(dist));


                    if (dist < 1) {
                        directionsLabel.setText(curDirections.get(nextDirection));
                        nextDirection++;
                    }
                }
            }

        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS Disabled",
                    Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS Enabled",
                    Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    private double getDistFromCoords(double lat1, double long1, double lat2, double long2) {
        if ((Math.abs(long2) < 180 && Math.abs(lat2) < 90) ||
                (Math.abs(long2) > 20037508.3427892) || (Math.abs(lat2) > 20037508.3427892)) {
            return haversine(lat1, long1, lat2, long2);
        }

        double x = long2;
        double y = lat2;
        double num3 = x / 6378137.0;
        double num4 = num3 * 57.295779513082323;
        double num5 = Math.floor((double) ((num4 + 180.0) / 360.0));
        double num6 = num4 - (num5 * 360.0);
        double num7 = 1.5707963267948966 - (2.0 * Math.atan(Math.exp((-1.0 * y) / 6378137.0)));
        long2 = num6;
        lat2 = num7 * 57.295779513082323;
        Log.i("getDistFromCoords", "" + Double.toString(lat1) + " " + Double.toString(lat2));
        return haversine(lat1, long1, lat2, long2);
    }

    /**
     * Calculates the distance in km between two lat/long points
     * using the haversine formula
     */
    public static double haversine(
            double lat1, double lng1, double lat2, double lng2) {
        int r = 6371; // average radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = r * c;
        return d * 1000; //convert to meters
    }

    /**
     * Set query parameters and run route query in new thread
     */
    private void QueryDirections(final Point mLocation, final Point p) {

        // Show that the route is calculating
        dialog = ProgressDialog.show(MapDisplay.this, "Walk MSU", "Calculating route...", true);
        // Spawn the request off in a new thread to keep UI responsive
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    // Start building up routing parameters
                    RouteParameters rp = mRouteTask.retrieveDefaultRouteTaskParameters();
                    NAFeaturesAsFeature rfaf = new NAFeaturesAsFeature();

                    // Create the stop points (start at our location, go to pressed location)
                    StopGraphic point1 = new StopGraphic(mLocation);
                    StopGraphic point2 = new StopGraphic(p);

                    rfaf.setFeatures(new Graphic[]{point1, point2});
                    rfaf.setCompressedRequest(true);
                    rp.setStops(rfaf);

                    // Solve the route and use the results to update UI when received
                    mResults = mRouteTask.solve(rp);

                    /*QueryTask queryTask = new QueryTask(getResources().getString(R.string.intersection_url));
                    QueryParameters query = new QueryParameters();
                    //query.setInSpatialReference(SpatialReference.create(102100));
                    //query.setOutFields(new String[]{"STATE_NAME", "POP2000"});
                    query.setWhere("1=1"); //Get all results
                    query.setReturnGeometry(true);
                    queryTask.execute(query);*/

                    // create HttpClient
                    HttpClient httpclient = new DefaultHttpClient();

              //      String string = "";
               //     for (int i = 0; i  < 1000; i++){ string += Integer.toString(i);}

                    // make GET request to the given URL
                    HttpResponse httpResponse = httpclient.execute(new HttpGet("http://prod.gis.msu.edu/arcgis/rest/services/routing/intersections/MapServer/0/query?where=1%3D1&text=&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=&returnGeometry=true&returnTrueCurves=false&maxAllowableOffset=&geometryPrecision=&outSR=&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&resultOffset=&resultRecordCount=&f=pjson"));

                    // receive response as inputStream
                    InputStream inputStream = httpResponse.getEntity().getContent();

                    //Parse JSON and create array list of intersections
                    JSONObject intersectionJSON = new JSONObject(convertInputStreamToString(inputStream));
                    JSONArray intersectionArray = intersectionJSON.getJSONArray("features");
                    Log.i("QUERY RESULTS", Integer.toString(intersectionArray.length()));
                    for (int i = 0; i < intersectionArray.length(); i++) {
                        JSONObject geometry = intersectionArray.getJSONObject(i).getJSONObject("geometry");
                        double x = geometry.getDouble("x");
                        double y = geometry.getDouble("y");
                        mIntersectionList.add(new Point(x, y));
                    }

                    mHandler.post(mUpdateResults);
                } catch (Exception e) {
                    mException = e;
                    mHandler.post(mUpdateResults);
                }
            }
        };
        // Start the operation
        t.start();
    }

    /**
     * Updates the UI after a successful rest response has been received.
     */
    void updateUI() {
        dialog.dismiss();
        String TAG = "MapDisplay::updateUI";

        if (mResults == null){// || mIntersectionList == null) {
            Toast.makeText(MapDisplay.this, mException.toString(), Toast.LENGTH_LONG).show();
            curDirections = null;
            return;
        }

        Log.i("INTERSECTIONS", Integer.toString(mIntersectionList.size()));
        // Creating a fragment if it has not been created
        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentByTag("Nav Drawer") == null) {
            FragmentTransaction ft = fm.beginTransaction();
            RoutingListFragment frag = new RoutingListFragment();
            ft.add(frag, "Nav Drawer");
            ft.commit();
        } else {
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(fm.findFragmentByTag("Nav Drawer"));
            RoutingListFragment frag = new RoutingListFragment();
            ft.add(frag, "Nav Drawer");
            ft.commit();
        }

        // Unlock the Navigation Drawer
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        curRoute = mResults.getRoutes().get(0);

        // Symbols for the route and the destination (blue line, checker flag)
        SimpleLineSymbol routeSymbol = new SimpleLineSymbol(Color.BLUE, 3);
        PictureMarkerSymbol destinationSymbol = new PictureMarkerSymbol(
                mMap.getContext(), getResources().getDrawable(
                R.drawable.ic_action_place));

        // Add all the route segments with their relevant information to the hiddenSegmentsLayer
        // Add the direction information to the list of directions
        int count = 0;
        ArrayList<Point> routePoints = new ArrayList<Point>();
        for (RouteDirection rd : curRoute.getRoutingDirections()) {
            //REMOVE AFTER UURAF
            /*<String, Object> attribs = new HashMap<String, Object>();
            attribs.put("text", rd.getText());
            attribs.put("time", Double.valueOf(rd.getMinutes()));
            attribs.put("length", Double.valueOf(rd.getLength()));
            attribs.put("count", Integer.toString(count));
            curDirections.add(String.format("%d. %s%n%.1f time (%.1f length)",
                    count, rd.getText(), rd.getMinutes(), rd.getLength()));
            Graphic routeGraphic = new Graphic(rd.getGeometry(), segmentHider,
                    attribs);*/
            Geometry g = rd.getGeometry();
            int t = g.getType().value();

            Log.i(TAG, "" + g.isSegment(t) + " " + g.isMultiPath(t) + " " + g.isPoint(t));
            MultiPath p = (MultiPath) g;
            Log.i(TAG, "" + p.getPathCount() + " " + p.getPointCount() + " " + p.getSegmentCount());
            int cnt = p.getPointCount();
            if(cnt>0)curGPSPoints.add(p.getPoint(cnt-1));
            for (int i = 0; i < cnt - 1; i++) {
                Point tmpPt = p.getPoint(i);
                routePoints.add(tmpPt);
                Log.i(TAG, "" + tmpPt.getX() + " " + tmpPt.getY());
            }

            //hiddenSegmentsLayer.addGraphic(routeGraphic);
            count++;
        }
        Log.i(TAG, Integer.toString(routePoints.size()));
        int routePointX, routePointY, intersectionX, intersectionY;
        Polyline path = new Polyline();
        for (int i = 1; i < routePoints.size(); i++) {
            routePointX = (int) routePoints.get(i).getX();
            routePointY = (int) routePoints.get(i).getY();
            Line segment = new Line();
            segment.setStart(routePoints.get(i - 1));
            segment.setEnd(routePoints.get(i));
            path.addSegment(segment, false);  //false so that a new path will not be started
            for (int k = 0; k < mIntersectionList.size(); k++) {
                intersectionX = (int) mIntersectionList.get(k).getX();
                intersectionY = (int) mIntersectionList.get(k).getY();
                if ((routePointX / 10 - 5 < intersectionX / 10 && intersectionX / 10 < routePointX / 10 + 5 &&
                        routePointY / 10 - 5 < intersectionY / 10 && intersectionY / 10 < routePointY / 10 + 5)
                        || i == routePoints.size() - 1) //Add the last segment regardless of intersections
                {

                    Log.i(TAG, "intersection: " + Integer.toString(intersectionX) + " routePoint: " + Integer.toString(routePointX));
                    Log.i(TAG, "intersection: " + Integer.toString(intersectionY) + " routePoint: " + Integer.toString(routePointY));
                    //Add intersection to GPS points list to enable the directions to update while walking
                    curGPSPoints.add(routePoints.get(i));

                    //Set up attributes to associate with path segment
                    HashMap<String, Object> attribs = new HashMap<String, Object>();
                    attribs.put("text", "text");
                    attribs.put("time", "time");
                    attribs.put("length", "length");
                    attribs.put("count", Integer.toString(count));

                    //Add directions for segment to list of directions (NEEDS CALCULATIONS)
                    curDirections.add(String.format("%d. %s%n%.1f time (%.1f length)", count, "directions", 0.0, 0.0));

                    //Create graphic for segment and set as hidden
                    Graphic routeGraphic = new Graphic(path, segmentHider, attribs);

                    //Add graphic to hidden layer
                    hiddenSegmentsLayer.addGraphic(routeGraphic);

                    //Start new path from last segment of previous path
                    path.setEmpty();

                    //Update count; used for indexing hidden segments
                    count++;
                    break;
                }
            }
        }

        Log.i(TAG, "" + curDirections.toArray()[curDirections.size() - 1]);
        // Reset the selected segment
        selectedSegmentID = -1;

        // Add the full route graphics, start and destination graphic to the routeLayer
        Graphic routeGraphic = new Graphic(curRoute.getRouteGraphic().getGeometry(), routeSymbol);
        Graphic endGraphic = new Graphic(
                ((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic
                        .getGeometry()).getPointCount() - 1), destinationSymbol);

        routeLayer.addGraphics(new Graphic[]{routeGraphic, endGraphic});

        // Get the full route summary
        routeSummary = String.format("Path to %s%n%.1f minutes (%.1f miles)",
                mDestinationName, curRoute.getTotalMinutes(),
                curRoute.getTotalMiles());

        directionsLabel.setText(routeSummary);

        // Zoom to the extent of the entire route with a padding
        tmpPoly = mMap.getExtent();
        mMap.setExtent(curRoute.getEnvelope(), 250);
        if (curDirections != null) {
            // Replacing the first and last direction segments
            curDirections.remove(0);
            curDirections.add(0, "Start Location");

            curDirections.add(mDestinationName);
            directionsLabel.setText(curDirections.get(0));
        }
    }

    void clearSegments() {
        hiddenSegmentsLayer.removeAll();
        routeLayer.removeAll();
        curDirections.clear();
        directionsLabel.setText("");
        mMap.setExtent(tmpPoly);
        RoutingListAdapter adapter = (RoutingListAdapter) RoutingListFragment.mDrawerList.getAdapter();
        adapter.directions.clear();
        RoutingListFragment.mDrawerList.setAdapter(adapter);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.direction:
                if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(Gravity.END);
                }
                return true;

            case R.id.clearPaths:
                clearSegments();
                return true;

            case R.id.selectDestination:
                Intent listIntent = new Intent(getApplicationContext(), BuildingList.class);
                startActivity(listIntent);
                return true;

            case R.id.destinationSearch:
                Intent searchIntent = new Intent(getApplicationContext(), BuildingSearch.class);
                startActivity(searchIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.pause();
        routingStarted = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMap.unpause();

    }

    /*
    * When the user selects the segment from the listview, it gets highlighted on the map
    */
    @Override
    public void onSegmentSelected(String segment) {

        if (segment == null) return;

        // Segment begins with direction number
        String count = segment.split(". ")[0];

        // Look for the graphic that corresponds to this direction
        for (int index : hiddenSegmentsLayer.getGraphicIDs()) {
            Graphic g = hiddenSegmentsLayer.getGraphic(index);
            if (count.equals((String) g.getAttributeValue("count"))) {

                // When found, hide the currently selected, show the new selection
                hiddenSegmentsLayer.updateGraphic(selectedSegmentID, segmentHider);
                hiddenSegmentsLayer.updateGraphic(index, segmentShower);
                selectedSegmentID = index;

                // Update label with information for that direction
                directionsLabel.setText(segment);

                // Zoom to the extent of that segment
                mMap.setExtent(hiddenSegmentsLayer.getGraphic(selectedSegmentID).getGeometry(), 50);
                break;
            }
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}