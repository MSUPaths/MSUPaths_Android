package com.example.android.searchabledict;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

/** BuildingSearch
 * Opens search dialog and handles results, called by Search button in MainDisplay
 */
public class BuildingSearch extends MainDisplay {
    
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        
        onSearchRequested();
        
        //From click on search results
        if (Intent.ACTION_VIEW.equals(intent.getAction())) 
        {       	
            String id = intent.getDataString();
            
            DBAdapter db = getDatabase();
            db.openDatabase();

            Cursor dbCursor = db.searchByID(id);
            openDescription(dbCursor);
            db.close();
            finish();
            
        }

        //Called when search query, currently prints query for debugging
        else if (Intent.ACTION_SEARCH.equals(intent.getAction())) 
        {
            String query = intent.getStringExtra(SearchManager.QUERY);
            System.out.println(query);
           
        }
    }
    
}
