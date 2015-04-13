package com.example.android.searchabledict;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteParameters;
import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;

import java.util.List;

//Will eventually display the map and draw the lines
public class MapDisplay extends Activity {

    public static MapView mMapView = null;
    GraphicsLayer mGraphicsLayer;
    Route mRoute;

    GraphicsLayer routeLayer;



    // Network tasks must be handled by background thread, handles query and displays route
    private class RouteQuery extends AsyncTask<Double, Void, RouteResult> {
        Exception mException;

        @Override
        protected RouteResult doInBackground(Double... coords) {
            mException = null;
            RouteResult results = null;
            try {
                RouteTask routeTask = RouteTask.createOnlineRouteTask(getResources().getString(R.string.geocode_url), null);
                RouteParameters routeParams = routeTask.retrieveDefaultRouteTaskParameters();

                // create routing features class
                NAFeaturesAsFeature naFeatures = new NAFeaturesAsFeature();

                // Create the stop points from point geometry
                StopGraphic startPnt = new StopGraphic(new Point(coords[0], coords[1]));
                StopGraphic endPnt = new StopGraphic(new Point(coords[2], coords[3]));

                // set features on routing feature class
                naFeatures.setFeatures(new Graphic[]{startPnt, endPnt});

                // set stops on routing feature class
                routeParams.setStops(naFeatures);
                results = routeTask.solve(routeParams);
            } catch (Exception e) {
                System.out.println("***ERROR: " + e + "***");
            }
            return results;
        }

        protected void onPostExecute(RouteResult results) {
            if (mException == null) {
                List<Route> routes = results.getRoutes();
                System.out.println(routes.size());
                for (int i = 0; i < routes.size(); i++) {
                    mRoute = routes.get(i);
                    // Access the whole route geometry and add it as a graphic
                    Geometry routeGeom = mRoute.getRouteGraphic().getGeometry();
                    Graphic symbolGraphic = new Graphic(routeGeom, new SimpleLineSymbol(Color.BLUE, 3));
                    mGraphicsLayer.addGraphic(symbolGraphic);
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps);
        mMapView = (MapView) findViewById(R.id.map);
        mGraphicsLayer = new GraphicsLayer();
        mMapView.addLayer(mGraphicsLayer);

        Intent intent = getIntent();

        double startLong = -84.480924;  // should be changed to user's current location
        double startLat = 42.7250467;

        double endLat = Double.valueOf(intent.getStringExtra("latitude"));
        double endLong = Double.valueOf(intent.getStringExtra("longitude"));

        new RouteQuery().execute(startLong, startLat, endLong, endLat);
    }

}

