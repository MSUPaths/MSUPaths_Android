/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.searchabledict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

/**
 * The main activity for the dictionary.  Also displays search results triggered by the search
 * dialog.
 */
public class SearchableDictionary extends Activity 
{

    private static final int MENU_SEARCH = 1;

    private TextView mTextView;
    private ListView mList;
    private ImageView mImage;
    private Button searchButton;
    private Button listButton;
    private Button aboutButton;
    private DBAdapter mDB;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        setContentView(R.layout.main);
        mTextView = (TextView) findViewById(R.id.textField);
        mList = (ListView) findViewById(R.id.list);
        mImage = (ImageView) findViewById(R.id.welcome_photo);
        searchButton = (Button) findViewById(R.id.search_button);
        listButton = (Button) findViewById(R.id.list_button);
        aboutButton = (Button)findViewById(R.id.about_button);
        
        //making the list not take up any space in the layout
        mList.setVisibility(8);
        
        //setting the image
        mImage.setImageResource(R.drawable.sparty_image);
        
		//load the database      
		mDB = new DBAdapter(this);
		mDB.open();
		//FillMasterDatabase(mDB);	
        
        // set what happens when you click the "About" button
        aboutButton.setOnClickListener(new OnClickListener()
        {
        	@Override
			public void onClick(View v)
        	{
        		Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
        		startActivity(aboutIntent);
        		//finish();
        	}
        });
        
        // set what happens when you click the "Building List" button
        listButton.setOnClickListener(new OnClickListener()
        {
        	@Override
			public void onClick(View v)
        	{
        		Intent listIntent = new Intent(getApplicationContext(), viewbuildings.class);
            	startActivity(listIntent);
            	//finish();
        	}
        });
	
        // set what happens when you click the "Search" button
        searchButton.setOnClickListener(new OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	{
        		// call search requested function
        		onSearchRequested();
        	}
        	
        });

        if (Intent.ACTION_VIEW.equals(intent.getAction())) 
        {
        	System.out.println("IN ACTIONVIEW");        	
            // from click on search results
            
        	// removing the photo (8 = GONE, meaning it should not take up space in the layout)
        	mImage.setVisibility(8);
        	
        	// making the list visible again (0 = VISIBLE)
        	mList.setVisibility(0);
        	
            String id = intent.getDataString();
            launchWord( mDB.searchByID(mDB.searchByID2(id).getString(1)) );
            finish();
        } 
        else if (Intent.ACTION_SEARCH.equals(intent.getAction())) 
        {
        	System.out.println("IN ACTIONSEARCH");
            String query = intent.getStringExtra(SearchManager.QUERY);
           
            mTextView.setText(getString(R.string.search_results, query));
            Cursor dbCursor = mDB.searchBuilding(query);
            dbCursor.moveToFirst();
            WordAdapter2 wordAdapter = new WordAdapter2(dbCursor);
            mList.setAdapter(wordAdapter);
            mList.setOnItemClickListener(wordAdapter);
        }

        Log.d("dict", intent.toString());
        if (intent.getExtras() != null) {
            Log.d("dict", intent.getExtras().keySet().toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
       /* menu.add(0, MENU_SEARCH, 0, R.string.menu_search)
                .setIcon(android.R.drawable.ic_search_category_default)
                .setAlphabeticShortcut(SearchManager.MENU_KEY);
        return super.onCreateOptionsMenu(menu); */
        
        MenuInflater inflater = getMenuInflater(); 
    	inflater.inflate(R.menu.menu, menu); 
    	return true;
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
            case MENU_SEARCH:
                onSearchRequested();
                return true;
            case R.id.destinationSearch:
            	// show the search dialog
            	onSearchRequested();
            	return true;
            case R.id.selectDestination:
            	// show the building list
            	Intent buildingIntent = new Intent(this, viewbuildings.class);
            	startActivity(buildingIntent);
            	finish();
            case R.id.clearPaths:
            	//set paths to off
        		globalStrings.pathDrawingState = 0;
        		return true; 
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void launchWord(Cursor cursor) 
    {
        cursor.moveToFirst();
    	System.out.println("IN LAUNCHWORD: "+cursor.getString(0));
        Intent next = new Intent();
        next.setClass(this, WordActivity.class);
        next.putExtra("word", cursor.getString(2));
        next.putExtra("definition", cursor.getString(3));
        next.putExtra("abbr", cursor.getString(1));
        next.putExtra("common", cursor.getString(2));
        next.putExtra("description", cursor.getString(4));
        next.putExtra("imagename", cursor.getString(5));
        mDB.close();
        cursor.close();
        startActivity(next);
    }
    
    class WordAdapter2 extends BaseAdapter implements AdapterView.OnItemClickListener 
    {

        private final Cursor mCursor;
        private final LayoutInflater mInflater;

        public WordAdapter2(Cursor dbCursor) 
        {
            mCursor = dbCursor;
            mInflater = (LayoutInflater) SearchableDictionary.this.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
    	public int getCount() 
        {
            return mCursor.getCount();
        }

        @Override
    	public Object getItem(int position) 
        {
            return position;
        }

        @Override
    	public long getItemId(int position) 
        {
            return position;
        }

        @Override
    	public View getView(int position, View convertView, ViewGroup parent) 
        {
            TwoLineListItem view = (convertView != null) ? (TwoLineListItem) convertView :
                    createView(parent);
            bindView(view, mCursor);
            return view;
        }

        private TwoLineListItem createView(ViewGroup parent) 
        {
            TwoLineListItem item = (TwoLineListItem) mInflater.inflate(
                    android.R.layout.simple_list_item_2, parent, false);
            item.getText2().setSingleLine();
            item.getText2().setEllipsize(TextUtils.TruncateAt.END);
            return item;
        }

        private void bindView(TwoLineListItem view, Cursor cursor) 
        {
            view.getText1().setText(cursor.getString(2));
            view.getText2().setText(cursor.getString(1));
        }

        @Override
    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        {
        	Cursor buildCursor = mDB.searchByID(mCursor.getString(0));
        	mCursor.close();
        	launchWord(buildCursor);
        }
        
    }
	private void FillMasterDatabase(DBAdapter db)
	{
		InputStream is = getResources().openRawResource(R.raw.build_master); 
		BufferedReader f = new BufferedReader(new InputStreamReader(is)); 
		String line = "";
		int rowId = 0;
		int rowId2 = 0;
		try{
			while ((line = f.readLine())!=null) 
			{
					line.trim();
					String[] a = line.split(", ");  // split ID, latitude, longitude into array
					db.insertBuilding(Integer.toString(rowId), a[0], a[1], a[2], a[3], a[4], a[5], a[6] );
					db.virtualBuilding(Integer.toString(rowId), a[0], a[1], a[2], a[3], a[4], a[5], a[6] );
					db.insertAlias(Integer.toString(rowId2), Integer.toString(rowId), a[7].toUpperCase());	
					db.virtualAlias(Integer.toString(rowId2), Integer.toString(rowId), a[7].toUpperCase());
					rowId2++;
					if( a.length - 8 > 0 ){
						int i = a.length-1;
						while( i > 7 ){
							db.insertAlias( Integer.toString(rowId2), Integer.toString(rowId) , a[i].toUpperCase() );
							db.virtualAlias(Integer.toString(rowId2), Integer.toString(rowId) , a[i].toUpperCase() );
							i--;
							rowId2++;
						}
					}
					rowId++;
			}
		}
		catch( IOException e){
			e.printStackTrace();
		}
	}
}


