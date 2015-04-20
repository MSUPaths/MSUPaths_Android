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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.text.Layout;
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
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Segment;
import com.esri.core.geometry.SegmentIterator;
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

import java.security.Provider;
import java.util.ArrayList;
import java.util.HashMap;

//Will eventually display the map and draw the lines
public class MapDisplay extends Activity implements
        RoutingListFragment.onDrawerListSelectedListener
        {

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

    // Current route, route summary, and gps location
    Route curRoute = null;
    String routeSummary = null;
    public static Point mLocation = null ;
    //public static Point mLocation = new Point(-84.499708, 42.715706) ;
    public static boolean routingStarted=false;

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
            if(routingStarted==false) {
                Intent intent = getIntent();
                mDestinationName = intent.getStringExtra("building_name");
                Point destination = new Point(Double.valueOf(intent.getStringExtra("longitude")), Double.valueOf(intent.getStringExtra("latitude")));
                QueryDirections(mLocation, destination);
                routingStarted=true;
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

                    rfaf.setFeatures(new Graphic[] { point1, point2 });
                    rfaf.setCompressedRequest(true);
                    rp.setStops(rfaf);

                    // Solve the route and use the results to update UI when received
                    mResults = mRouteTask.solve(rp);
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

        if (mResults == null) {
            Toast.makeText(MapDisplay.this, mException.toString(), Toast.LENGTH_LONG).show();
            curDirections = null;
            return;
        }

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
        for (RouteDirection rd : curRoute.getRoutingDirections()) {
            HashMap<String, Object> attribs = new HashMap<String, Object>();
            attribs.put("text", rd.getText());
            attribs.put("time", Double.valueOf(rd.getMinutes()));
            attribs.put("length", Double.valueOf(rd.getLength()));
            attribs.put("count", Integer.toString(count));
            curDirections.add(String.format("%d. %s%n%.1f time (%.1f length)",
                    count, rd.getText(), rd.getMinutes(), rd.getLength()));
            Graphic routeGraphic = new Graphic(rd.getGeometry(), segmentHider,
                    attribs);
            Geometry g=rd.getGeometry();
            int t= g.getType().value();

            Log.i("MapDisplay::updateUI",""+g.isSegment(t)+" "+g.isMultiPath(t)+" "+g.isPoint(t));
            MultiPath p = (MultiPath)g;
            Log.i("MapDisplay::updateUI",""+p.getPathCount()+" "+p.getPointCount()+" "+p.getSegmentCount());
            int cnt=p.getPointCount();
            for(int i=0;i<cnt;i++)
            {
                Point tmpPt = p.getPoint(i);
                Log.i(TAG,""+tmpPt.getX()+" "+tmpPt.getY());
            }

            Log.i(TAG,""+curDirections.toArray()[curDirections.size()-1]);
            hiddenSegmentsLayer.addGraphic(routeGraphic);
            count++;
        }
        // Reset the selected segment
        selectedSegmentID = -1;

        // Add the full route graphics, start and destination graphic to the routeLayer
        Graphic routeGraphic = new Graphic(curRoute.getRouteGraphic().getGeometry(), routeSymbol);
        Graphic endGraphic = new Graphic(
                ((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic
                        .getGeometry()).getPointCount() - 1), destinationSymbol);

        routeLayer.addGraphics(new Graphic[] { routeGraphic, endGraphic });

        // Get the full route summary
        routeSummary = String.format("Path to %s%n%.1f minutes (%.1f miles)",
                mDestinationName, curRoute.getTotalMinutes(),
                curRoute.getTotalMiles());

        directionsLabel.setText(routeSummary);

        // Zoom to the extent of the entire route with a padding
        tmpPoly=mMap.getExtent();
        mMap.setExtent(curRoute.getEnvelope(), 250);

        // Replacing the first and last direction segments
        curDirections.remove(0);
        curDirections.add(0, "Start Location");

        curDirections.add(mDestinationName);
    }

    void clearSegments(){
        hiddenSegmentsLayer.removeAll();
        routeLayer.removeAll();
        curDirections.clear();
        directionsLabel.setText("");
        mMap.setExtent(tmpPoly);
        RoutingListAdapter adapter = (RoutingListAdapter)RoutingListFragment.mDrawerList.getAdapter();
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
        routingStarted=false;
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
        String count  = segment.split(". ")[0];

        // Look for the graphic that corresponds to this direction
        for (int index : hiddenSegmentsLayer.getGraphicIDs()) {
            Graphic g = hiddenSegmentsLayer.getGraphic(index);
            if (count.equals((String) g.getAttributeValue("count"))) {

                // When found, hide the currently selected, show the new selection
                hiddenSegmentsLayer.updateGraphic(selectedSegmentID,segmentHider);
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
}

