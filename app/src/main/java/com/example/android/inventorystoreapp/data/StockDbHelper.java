package com.example.android.inventorystoreapp.data;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.example.android.inventorystoreapp.data.StockContract.StockEntry;

import java.util.HashMap;

/**
 * Database helper for Inventory Store app. Manages database creation and version management.
 */
public class StockDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = StockDbHelper.class.getSimpleName();
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;


    private HashMap<String, String> mAliasMap;


    /**
     * Constructs a new instance of {@link StockDbHelper}.
     *
     * @param context of the app
     */
    public StockDbHelper(Context context) {
        //------------!!!!!!!!!!!!!!!!!!! -- factory??????????
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // This HashMap is used to map table fields to Custom Suggestion fields
        mAliasMap = new HashMap<String, String>();

        // Unique id for the each Suggestions ( Mandatory )
        mAliasMap.put("_ID", StockEntry._ID + " as " + "_id" );
        // Text for Suggestions ( Mandatory )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, StockEntry.COLUMN_STOCK_NAME + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);

    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the stock table
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE "
                + StockEntry.TABLE_NAME
                + " ("
                + StockEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + StockEntry.COLUMN_STOCK_NAME + " TEXT NOT NULL, "

        //???????????
                // + StockEntry.COLUMN_STOCK_PRICE + " REAL NOT NULL, "
                // + StockEntry.COLUMN_STOCK_PRICE + " INTEGER NOT NULL DEFAULT '0', "
                // + StockEntry.COLUMN_STOCK_PRICE + " TEXT NOT NULL, "
                // + StockEntry.COLUMN_STOCK_PRICE + " DECIMAL(10,2) NOT NULL, "
                + StockEntry.COLUMN_STOCK_PRICE + " NUMERIC NOT NULL, "

                + StockEntry.COLUMN_STOCK_QUANTITY + " INTEGER NOT NULL DEFAULT '0', "
                + StockEntry.COLUMN_STOCK_ORDERED + " INTEGER NOT NULL DEFAULT '0', "
                + StockEntry.COLUMN_STOCK_SUPPLIER_NAME + " TEXT NOT NULL, "
                + StockEntry.COLUMN_STOCK_SUPPLIER_PHONE + " TEXT NOT NULL, "
                + StockEntry.COLUMN_STOCK_SUPPLIER_EMAIL + " TEXT NOT NULL, "
                + StockEntry.COLUMN_STOCK_IMAGE + " TEXT NOT NULL DEFAULT 'NO IMAGE'"
                + ");";
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here in stage 1
    }

}