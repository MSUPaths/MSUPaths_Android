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

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MenuInflater;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.util.List;

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
            // from click on search results
            
        	// removing the photo (8 = GONE, meaning it should not take up space in the layout)
        	mImage.setVisibility(8);
        	
        	// making the list visible again (0 = VISIBLE)
        	mList.setVisibility(0);
        	
        	Dictionary.getInstance().ensureLoaded(getResources());
            String word = intent.getDataString();
            Dictionary.Word theWord = Dictionary.getInstance().getMatches(word).get(0);
            launchWord(theWord);
            finish();
        } 
        else if (Intent.ACTION_SEARCH.equals(intent.getAction())) 
        {
            String query = intent.getStringExtra(SearchManager.QUERY);
            mTextView.setText(getString(R.string.search_results, query));
            WordAdapter wordAdapter = new WordAdapter(Dictionary.getInstance().getMatches(query.toUpperCase()));
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

    private void launchWord(Dictionary.Word theWord) 
    {
        Intent next = new Intent();
        next.setClass(this, WordActivity.class);
        next.putExtra("word", theWord.word);
        next.putExtra("definition", theWord.definition);
        next.putExtra("abbr", theWord.abbr);
        next.putExtra("common", theWord.common);
        next.putExtra("description", theWord.description);
        next.putExtra("imagename", theWord.imagename);
        startActivity(next);
    }

    class WordAdapter extends BaseAdapter implements AdapterView.OnItemClickListener 
    {

        private final List<Dictionary.Word> mWords;
        private final LayoutInflater mInflater;

        public WordAdapter(List<Dictionary.Word> words) 
        {
            mWords = words;
            mInflater = (LayoutInflater) SearchableDictionary.this.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
		public int getCount() 
        {
            return mWords.size();
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
            bindView(view, mWords.get(position));
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

        private void bindView(TwoLineListItem view, Dictionary.Word word) 
        {
            view.getText1().setText(word.word);
            view.getText2().setText(word.abbr);
        }

        @Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        {
            launchWord(mWords.get(position));
        }
    }
}
