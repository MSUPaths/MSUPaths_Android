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

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains logic to load the word of words and definitions and find a list of matching words
 * given a query.  Everything is held in memory; this is not a robust way to serve lots of
 * words and is only for demo purposes.
 *
 * You may want to consider using an SQLite database. In practice, you'll want to make sure your
 * suggestion provider is as efficient as possible, as the system will be taxed while performing
 * searches across many sources for each keystroke the user enters into Quick Search Box.
 */
public class Dictionary 
{

    public static class Word 
    {
    	public final String abbr;
        public final String word;
        public final String definition;
        public final String common;
        public final String description;
        public final String imagename;
        

        public Word(String abbr, String word, String definition, String common, String description, String imagename) 
        {
        	this.abbr = abbr;
            this.word = word;
            this.definition = definition;
            this.common = common;
            this.description = description;
            this.imagename = imagename;
        }
    }

    private static final Dictionary sInstance = new Dictionary();

    public static Dictionary getInstance() 
    {
        return sInstance;
    }

    private final Map<String, List<Word>> mDict = new ConcurrentHashMap<String, List<Word>>();
    private final ArrayList<Word> wordList = new ArrayList<Word>();
    
    private Dictionary() 
    {
    }

    private boolean mLoaded = false;

    /**
     * Loads the words and definitions if they haven't been loaded already.
     *
     * @param resources Used to load the file containing the words and definitions.
     */
    public synchronized void ensureLoaded(final Resources resources) 
    {
        if (mLoaded) return;

        new Thread(new Runnable() {
            @Override
			public void run() {
                try {
                    loadWords(resources);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private synchronized void loadWords(Resources resources) throws IOException 
    {
        if (mLoaded) return;

        Log.d("dict", "loading words");
        InputStream inputStream = resources.openRawResource(R.raw.building_abbr);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String line;
            while((line = reader.readLine()) != null) 
            {
                String[] strings = TextUtils.split(line, ",");
                if (strings.length < 2) continue;
                addWord(strings[0].trim(), strings[1].trim(), strings[2].trim(), strings[3].trim(), strings[4].trim(), strings[5].trim());
            }
        } finally 
        {
            reader.close();
        }
        mLoaded = true;
    }


    public List<Word> getMatches(String query) 
    {
        List<Word> list = mDict.get(query.toUpperCase());
        if (list == null)
        {
        	list = new ArrayList<Word>();
        }

        Map<String, Integer> levMapping = new ConcurrentHashMap<String, Integer>();
        for (int i=0; i<wordList.size(); i++ )
        {
        	String word = wordList.get(i).word;
        	int distance = getLevenshteinDistance(word,query);
        	levMapping.put(word, distance);
        }
        for (int j=0; j<10; j++)
        {
        	for (int i=0; i<wordList.size(); i++)
        	{
        		Word currWord = wordList.get(i);
        		Integer f = levMapping.get(currWord.word);
        		if (!list.contains(currWord) && f!= null)
        		{
    				if (f == j)
    				{
    					list.add(currWord);
    				}
        			
        		}
        	}
        }
       
     //   return list == null ? Collections.EMPTY_LIST : list;
        return list;
    }

    private void addWord(String abbr, String word, String definition, String common, String description, String imagename) 
    {
        final Word theWord = new Word(abbr, word, definition, common, description, imagename);
        wordList.add(theWord);
        
        addMatch(abbr, theWord);
// This code adds all substrings of a word to "matches"
        final int len = word.length();
        for (int i = 0; i < len; i++) 
        {
            final String prefix = word.substring(0, len - i);
            addMatch(prefix, theWord);
        }
    }

    private void addMatch(String query, Word word) 
    {
        List<Word> matches = mDict.get(query);
        if (matches == null) 
        {
            matches = new ArrayList<Word>();
            mDict.put(query, matches);
        }
        matches.add(word);
    }
    
    /*
     * Source: http://www.merriampark.com/ldjava.htm
     */
    public static int getLevenshteinDistance (String s, String t) 
    {
    	  if (s == null || t == null) {
    	    throw new IllegalArgumentException("Strings must not be null");
    	  }
    			
    	  /*
    	    The difference between this impl. and the previous is that, rather 
    	     than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
    	     we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
    	     is the 'current working' distance array that maintains the newest distance cost
    	     counts as we iterate through the characters of String s.  Each time we increment
    	     the index of String t we are comparing, d is copied to p, the second int[].  Doing so
    	     allows us to retain the previous cost counts as required by the algorithm (taking 
    	     the minimum of the cost count to the left, up one, and diagonally up and to the left
    	     of the current cost count being calculated).  (Note that the arrays aren't really 
    	     copied anymore, just switched...this is clearly much better than cloning an array 
    	     or doing a System.arraycopy() each time  through the outer loop.)

    	     Effectively, the difference between the two implementations is this one does not 
    	     cause an out of memory condition when calculating the LD over two very large strings.  		
    	  */		
    			
    	  int n = s.length(); // length of s
    	  int m = t.length(); // length of t
    			
    	  if (n == 0) {
    	    return m;
    	  } else if (m == 0) {
    	    return n;
    	  }

    	  int p[] = new int[n+1]; //'previous' cost array, horizontally
    	  int d[] = new int[n+1]; // cost array, horizontally
    	  int _d[]; //placeholder to assist in swapping p and d

    	  // indexes into strings s and t
    	  int i; // iterates through s
    	  int j; // iterates through t

    	  char t_j; // jth character of t

    	  int cost; // cost

    	  for (i = 0; i<=n; i++) {
    	     p[i] = i;
    	  }
    			
    	  for (j = 1; j<=m; j++) {
    	     t_j = t.charAt(j-1);
    	     d[0] = j;
    			
    	     for (i=1; i<=n; i++) {
    	        cost = s.charAt(i-1)==t_j ? 0 : 1;
    	        // minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
    	        d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
    	     }

    	     // copy current distance counts to 'previous row' distance counts
    	     _d = p;
    	     p = d;
    	     d = _d;
    	  } 
    			
    	  // our last action in the above loop was to switch d and p, so p now 
    	  // actually has the most recent cost counts
    	  return p[n];
    	}
    
    
    
}
