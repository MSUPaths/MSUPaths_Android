package com.example.android.searchabledict;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/** DBProvider
 * Queries database to provide search suggestions
 * Currently incomplete, being worked on by Anna
 */
public class DBProvider extends ContentProvider {
    public static String AUTHORITY = "database";
    private DBAdapter mDB;
    private MatrixCursor mSuggestionMat;

    // The columns we'll include in our search suggestions
    private static final String[] COLUMNS = {
            "_id", // must include this column
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA
    };

    @Override
    public boolean onCreate() {
        mDB = new DBAdapter(getContext());
        mDB.createDatabase();
        mDB.openDatabase();

        mSuggestionMat = new MatrixCursor(COLUMNS);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String query = uri.getLastPathSegment().toUpperCase();
        Cursor results =  mDB.getBuilding(query);
        for (int i = 0; i<results.getCount(); i++) {
            String [] row = {results.getString(0), results.getString(2), results.getString(4), results.getString(0)};
            mSuggestionMat.addRow(row);
            results.moveToNext();
        }
        return mSuggestionMat;
    }

    // Necessary for content provider but not implemented
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
