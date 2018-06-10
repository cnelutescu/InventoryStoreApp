package com.example.android.inventorystoreapp;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.android.inventorystoreapp.data.StockContract;



//public class SearchableActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {
public class SearchableActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

	ListView mLVProducts;					//  products ListView
	SimpleCursorAdapter mCursorAdapter;		// CursorAdapter for the ListView
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_searchable);	// ListView
		
		// Getting reference to products List
		mLVProducts = (ListView)findViewById(R.id.lv_products);

        // This is a new product, so change the app bar to say "Add a Product"
        setTitle(getString(R.string.search_activity_title_select_product));

        // Setting item click listener to products List
		mLVProducts.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
                //Intent countryIntent = new Intent(getApplicationContext(), CountryActivity.class);
				Intent intent = new Intent(SearchableActivity.this, EditActivity.class);

                // Creating a uri to fetch product details corresponding to selected listview item
                Uri data = Uri.withAppendedPath(StockContract.StockEntry.CONTENT_URI, String.valueOf(id));
                //Uri currentProductUri = ContentUris.withAppendedId(StockContract.StockEntry.CONTENT_URI, id);

                // Setting uri to the data on the intent for EditActivity
                intent.setData(data);
                
                // Open the EditActivity
                startActivity(intent);
            }
        });

		// Defining CursorAdapter for the Products ListView
		mCursorAdapter = new SimpleCursorAdapter(getBaseContext(),
				android.R.layout.simple_list_item_1,
	            null,
	            new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1},
	            new int[] { android.R.id.text1}, 0);
		
		// Setting the cursor adapter for the Products ListView
		mLVProducts.setAdapter(mCursorAdapter);
		
		// Getting the intent that invoked this activity
		Intent intent = getIntent();		
		
		// If this activity is invoked by selecting an item from Suggestion of Search dialog or 
		// from ListView of SearchActivity
		if(intent.getAction().equals(Intent.ACTION_VIEW)){

			// If this activity is invoked from ListView of SearchActivity:
			Intent editIntent = new Intent(this, EditActivity.class);
            editIntent.setData(intent.getData());
			// Open the  Edit Activity
            startActivity(editIntent);
            finish();

		}else if(intent.getAction().equals(Intent.ACTION_SEARCH)){

			// If this activity is invoked, when user presses "Go", "Search" in the Keyboard of Search Dialog:
			String query = intent.getStringExtra(SearchManager.QUERY); // get query string
			doSearch(query);		// do the search with the query string from intent extra data
		}		
	}	

	// Do the search for the product when user presses "Go", "Search"
	private void doSearch(String query){
		Bundle data = new Bundle();
		data.putString("query", query);
		
		// Invoking onCreateLoader() in non-ui thread
		getSupportLoaderManager().initLoader(1, data, this);	// call onCreateLoader
	}

	/** This method is invoked by initLoader() above*/
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle data) {
		Uri uri = StockContract.StockEntry.CONTENT_URI;
		// return rhe cursor loader for uri and query string
		return new CursorLoader(getBaseContext(), uri, null, null , new String[]{data.getString("query")}, null);	
	}

	/** This method is executed in ui thread, after onCreateLoader() above*/
	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {	
		mCursorAdapter.swapCursor(c);		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub		
	}



}