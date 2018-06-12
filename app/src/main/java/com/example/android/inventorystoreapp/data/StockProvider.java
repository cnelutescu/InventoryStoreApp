package com.example.android.inventorystoreapp.data;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

public class StockProvider extends ContentProvider {


    /** Tag for the log messages */
    public static final String LOG_TAG = StockProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the stock table */
    private static final int STOCK = 100;

    /** URI matcher code for the content URI for a single stock product in the stock table */
    private static final int STOCK_ID = 101;

    // /** URI matcher codes for the content URI
    private static final int SUGGESTIONS_PRODUCT = 102;
    private static final int SEARCH_PRODUCT = 2;
    private static final int GET_PRODUCT = 3;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCK, STOCK);

        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY,StockContract.PATH_STOCK + "/#", STOCK_ID);


        // Suggestion items of Search Dialog is provided by this uri ("search_suggest_query")
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGESTIONS_PRODUCT);
    }

    /** Database helper that will provide us access to the database
     Make sure the variable is a global variable, so it can be referenced from other
     ContentProvider methods. */
    public StockDbHelper mDbHelper;

    private HashMap<String, String> mAliasMap;


    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new StockDbHelper(getContext());


        // This HashMap is used to map table fields to Custom Suggestion fields
        mAliasMap = new HashMap<String, String>();

        // Unique id for the each Suggestions ( Mandatory )
        mAliasMap.put("_ID", StockContract.StockEntry._ID + " as " + "_id" );
        // Text for Suggestions ( Mandatory )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, StockContract.StockEntry.COLUMN_STOCK_NAME + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        // Icon for Suggestions ( Optional )
        //  mAliasMap.put( SearchManager.SUGGEST_COLUMN_ICON_1, FIELD_FLAG + " as " + SearchManager.SUGGEST_COLUMN_ICON_1);
        // This value will be appended to the Intent data on selecting an item from Search result or Suggestions ( Optional )
        //  mAliasMap.put( SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, FIELD_ID + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID );


        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                // User provided a FILTER string?
                if(selectionArgs!=null){
                    // Filter the list with the FILTER string provided by User
                    cursor = getProducts(selectionArgs);
                    break;
                } else {
                    // Query the stock table directly for all records with null for
                    // projection, selection, selection arguments, and sort order. The cursor
                    // will contain multiple (all) rows of the stock table.
                    cursor = database.query(StockContract.StockEntry.TABLE_NAME, projection, selection, selectionArgs,
                            null, null, sortOrder);
                }
                break;
            case STOCK_ID:
                // For the STOCK_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventorystoreapp/stock/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = StockContract.StockEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the stock table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(StockContract.StockEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor, for any cursor returned by the query method
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

//--------------- Filter DB info -------------------
    /** Construct the query and apply it to return the Countries corresponding to selectionArgs  */
    public Cursor getProducts(String[] selectionArgs){

        String selection = StockContract.StockEntry.COLUMN_STOCK_NAME + " like ? ";	// selection = "name like ? "

        if(selectionArgs!=null){
            selectionArgs[0] = "%"+selectionArgs[0] + "%";   	//	selectionArgs[0] = "%...selection string...%"
        }

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setProjectionMap(mAliasMap);

        queryBuilder.setTables(StockContract.StockEntry.TABLE_NAME);

        // Apply the query and get the cursor with the countries selected
        Cursor cursor = queryBuilder.query(mDbHelper.getReadableDatabase(),
                new String[] { "_ID",
                        SearchManager.SUGGEST_COLUMN_TEXT_1 ,
                     //   SearchManager.SUGGEST_COLUMN_ICON_1 ,
                     //   SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
                             } ,
                selection,
                selectionArgs,
                null,
                null,
                StockContract.StockEntry.COLUMN_STOCK_NAME + " asc ",null // "name asc "
        );
        return cursor;

    }

    //----------------------------------
    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return insertStock(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a stock product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertStock(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(StockContract.StockEntry.COLUMN_STOCK_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        // If the price is provided, check that it's greater than or equal to 0
        double price = values.getAsDouble(StockContract.StockEntry.COLUMN_STOCK_PRICE);
        // long price = values.getAsInteger(StockContract.StockEntry.COLUMN_STOCK_PRICE);
        // if (price != null && price < 0) {
        if (price < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(StockContract.StockEntry.COLUMN_STOCK_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }

        // If the ordered quantity is provided, check that it's greater than or equal to 0
        Integer ordered = values.getAsInteger(StockContract.StockEntry.COLUMN_STOCK_ORDERED);
        if (ordered != null && ordered < 0) {
            throw new IllegalArgumentException("Product requires valid ordered quantity");
        }

        // Check that the supplier name is not null
        String supplierName = values.getAsString(StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Product requires a supplier name");
        }

        // Check that the supplier phone is not null
        String supplierPhone = values.getAsString(StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_PHONE);
        if (supplierPhone == null) {
            throw new IllegalArgumentException("Product requires a supplier phone");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new stock product with the given values
        long id = database.insert(StockContract.StockEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the stock content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }
//--------------------

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                // update more rows in stock table
                return updateStock(uri, contentValues, selection, selectionArgs);
            case STOCK_ID:
                // For the STOCK_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = StockContract.StockEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateStock(uri, contentValues, selection, selectionArgs);
            default:
                // illegal argument
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update stock products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateStock(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link StockEntry#COLUMN_STOCK_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(StockContract.StockEntry.COLUMN_STOCK_NAME)) {
            String name = values.getAsString(StockContract.StockEntry.COLUMN_STOCK_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // If the price is provided, check that it's greater than or equal to 0
        if (values.containsKey(StockContract.StockEntry.COLUMN_STOCK_PRICE)) {
            double price = values.getAsDouble(StockContract.StockEntry.COLUMN_STOCK_PRICE);
            // long price = values.getAsInteger(StockContract.StockEntry.COLUMN_STOCK_PRICE);
            //String price = values.getAsString(StockContract.StockEntry.COLUMN_STOCK_PRICE);
            // if (price != null && price < 0) {
            //if (price == null) {
            if (price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        // If the quantity is provided, check that it's greater than or equal to 0
        if (values.containsKey(StockContract.StockEntry.COLUMN_STOCK_QUANTITY)) {
            Integer quantity = values.getAsInteger(StockContract.StockEntry.COLUMN_STOCK_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
        }

        // If the ordered quantity is provided, check that it's greater than or equal to 0
        if (values.containsKey(StockContract.StockEntry.COLUMN_STOCK_ORDERED)) {
            Integer ordered = values.getAsInteger(StockContract.StockEntry.COLUMN_STOCK_ORDERED);
            if (ordered != null && ordered < 0) {
                throw new IllegalArgumentException("Product requires valid ordered quantity");
            }
        }

        // Check that the supplier name is not null
        if (values.containsKey(StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Product requires a supplier name");
            }
        }

        // Check that the supplier phone is not null
        if (values.containsKey(StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_PHONE)) {
            String supplierPhone = values.getAsString(StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_PHONE);
            if (supplierPhone == null) {
                throw new IllegalArgumentException("Product requires a supplier phone");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(StockContract.StockEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed and they have to update the UI
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(StockContract.StockEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STOCK_ID:
                // Delete a single row given by the ID in the URI
                selection = StockContract.StockEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(StockContract.StockEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed and they have to update the UI
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     * return a String that describes the type of the data stored at the input Uri
     * This String is known as the MIME type, which can also be referred to as content type
     * The returned MIME type should start with
     * “vnd.android.cursor.item” for a single record, or
     * “vnd.android.cursor.dir/” for multiple items.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return StockContract.StockEntry.CONTENT_LIST_TYPE;
            case STOCK_ID:
                return StockContract.StockEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


}
