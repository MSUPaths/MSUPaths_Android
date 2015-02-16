package com.example.android.searchabledict;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/** MainDisplay
 * Opening activity with buttons to select Search, BuildingList, or AboutApp
 */
public class MainDisplay extends Activity
{
    private TextView mTextView;
    private ImageView mImage;
    private Button searchButton;
    private Button listButton;
    private Button aboutButton;
	private DBAdapter mDb = new DBAdapter(this);
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {	
    	super.onCreate(savedInstanceState);
    
    	setContentView(R.layout.main);
    	
        mDb.createDatabase();
	
    	mTextView = (TextView) findViewById(R.id.textField);
    	mImage = (ImageView) findViewById(R.id.welcome_photo);
    	searchButton = (Button) findViewById(R.id.search_button);
    	listButton = (Button) findViewById(R.id.list_button);
    	aboutButton = (Button)findViewById(R.id.about_button);	

    	//setting the image
    	mImage.setImageResource(R.drawable.sparty_image);
    	
    	// set what happens when you click the "About" button
    	aboutButton.setOnClickListener(new OnClickListener()
    	{
    		@Override
    		public void onClick(View v)
    		{
    			Intent aboutIntent = new Intent(getApplicationContext(), AboutApp.class);
    			startActivity(aboutIntent);
    		}
    	});
    
    	// set what happens when you click the "Building List" button
    	listButton.setOnClickListener(new OnClickListener()
    	{
    		@Override
    		public void onClick(View v)
    		{
    			Intent listIntent = new Intent(getApplicationContext(), BuildingList.class);
    			startActivity(listIntent);
    		}
    	});
    	
    	// set what happens when you click the "Search" button
    	searchButton.setOnClickListener(new OnClickListener()
    	{
    		@Override
    		public void onClick(View v)
    		{
    			Intent searchIntent = new Intent(getApplicationContext(), BuildingSearch.class);
    			startActivity(searchIntent);

    		}
    	
    	});  	
    }

    // Put information from database cursor into intent for use with description
    public void openDescription(Cursor cursor) 
    {
        Intent next = new Intent();
        next.setClass(this, BuildingDescription.class);
        next.putExtra("building", cursor.getString(2));
        next.putExtra("definition", cursor.getString(3));
        next.putExtra("abbr", cursor.getString(1));
        next.putExtra("common", cursor.getString(2));
        next.putExtra("description", cursor.getString(4));
        next.putExtra("imagename", cursor.getString(5));
        startActivity(next);
    }
    public DBAdapter getDatabase()
    {
    	return mDb;
    }
}
