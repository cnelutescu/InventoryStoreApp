package com.example.android.inventorystoreapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorystoreapp.data.StockContract.StockEntry;

/**
 * Displays list of inventory store products that were entered and stored in the app
 */
 public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
// public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the stock products data loader; arbitrarily chosen = 0*/
    private static final int STOCK_LOADER = 0;

    /** Adapter for our ListView */
    StockCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the ListView which will be populated with the stock products inventory data
        ListView stockListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        stockListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of stock product data in the Cursor.
        // There is no product data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new StockCursorAdapter(this, null);
        stockListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        stockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditActivity}
                Intent intent = new Intent(MainActivity.this, EditActivity.class);

                // Form the content URI that represents the specific product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link StockEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.inventorystoreapp/stock/2"
                // if the product with ID 2 was clicked on.
                Uri currentProductUri = ContentUris.withAppendedId(StockEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);
                    // Launch the {@link EditActivity} to display the data for the current product.
                startActivity(intent);
            }
        });
        // Initialize the loader (Kick off the loader)
        getLoaderManager().initLoader(STOCK_LOADER, null, this);
    }


    /**
     * Helper method to insert hardcoded demo product data into the database.
     * For test and demo purposes only.
     */
    private void insertDemoData() {
        // Create a ContentValues object where column names are the keys,
        // and demo product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_STOCK_NAME, "Demo product");
        values.put(StockEntry.COLUMN_STOCK_PRICE, 12345);
        values.put(StockEntry.COLUMN_STOCK_QUANTITY, 10);
        values.put(StockEntry.COLUMN_STOCK_ORDERED, 5);
        values.put(StockEntry.COLUMN_STOCK_SUPPLIER_NAME, "Supplier 1");
        values.put(StockEntry.COLUMN_STOCK_SUPPLIER_PHONE, "0740000000");
        values.put(StockEntry.COLUMN_STOCK_SUPPLIER_EMAIL, "supplier1@gmail.com");

        // Insert a new row with a demo product into the provider using the ContentResolver.
        // Use the {@link StockEntry#CONTENT_URI} to indicate that we want to insert
        // into the stock database table.
        // Receive the new content URI that will allow us to access that new data in the future.
        Uri newUri = getContentResolver().insert(StockEntry.CONTENT_URI, values);

        Log.v("Inventory-Main", "New row inserted: Uri=" + newUri);
        Toast.makeText(this, "New row inserted: Uri=" + newUri, Toast.LENGTH_SHORT).show();
    }


    /**
     * This adds menu items to the app bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main.xml file.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * This process menu items option clicks in the app bar overflow menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert demo data" menu option
            case R.id.action_insert_demo_data:
                insertDemoData();
                return true;
            // Respond to a click on the "Delete all data" menu option
            case R.id.action_delete_all_data:
                // Delete stock table (all data)
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Add product" menu option
            case R.id.action_insert_product_data:
                // Start Edit activity
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
                return true;
            // Respond to a click on the "Filter product data" menu option
            case R.id.action_filter_data:
                // Filter product data in Main activity
                Log.v("Inventory-Main", "Pressed Menu Item: Filter product.");
                Toast.makeText(this, "Pressed Menu Item: Filter product.", Toast.LENGTH_LONG).show();
                onSearchRequested();		// called on Filter btn click	and open Search dialog
        }
        return super.onOptionsItemSelected(item);
    }


    // will create a dialog that calls deleteAllData when the delete button is pressed.
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteAllData();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Method to delete stock table(and delete all data in it)
     */
    private void deleteAllData() {
        int rowsDeleted = getContentResolver().delete(StockEntry.CONTENT_URI, null, null);
        Log.v("Inventory-Main", "Stock table deleted - all data erased, " + rowsDeleted + " rows deleted from stock database");
        Toast.makeText(this, "Stock table deleted - all data erased, " + rowsDeleted + " rows deleted from stock database", Toast.LENGTH_LONG).show();
    }

//------------------------------
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
            StockEntry._ID,       // ID always needed for the cursor to pass to any cursor adapter
            StockEntry.COLUMN_STOCK_NAME,
            StockEntry.COLUMN_STOCK_PRICE,
            StockEntry.COLUMN_STOCK_QUANTITY,
            StockEntry.COLUMN_STOCK_ORDERED,
            StockEntry.COLUMN_STOCK_SUPPLIER_NAME,
            StockEntry.COLUMN_STOCK_SUPPLIER_PHONE,
            StockEntry.COLUMN_STOCK_SUPPLIER_EMAIL,
            StockEntry.COLUMN_STOCK_IMAGE };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
            StockEntry.CONTENT_URI,   // Provider content URI to query
            projection,             // Columns to include in the resulting Cursor
            null,          // No selection clause
            null,       // No selection arguments
            null);         // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link StockCursorAdapter} with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted with cursor=null
        mCursorAdapter.swapCursor(null);
    }



}
