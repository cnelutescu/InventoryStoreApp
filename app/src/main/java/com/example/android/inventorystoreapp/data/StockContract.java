package com.example.android.inventorystoreapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory Store app.
 */
public final class StockContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private StockContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventorystoreapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventorystoreapp/stock/ is a valid path for
     * looking at product data. content://com.example.inventorystoreapp/stock/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_STOCK = "stock";




    /**
     * Inner class that defines constant values for the stock database table.
     * Each entry in the table represents a single product.
     */
    public static final class StockEntry implements BaseColumns {

        /** The content URI to access the stock data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_STOCK);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of stock products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single stock product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        public final static String TABLE_NAME = "stock";    // Name of database table for products stock
        public final static String _ID = BaseColumns._ID;   // Unique ID number Type: INTEGER
        public final static String COLUMN_STOCK_NAME ="name";   // Name of the product Type: TEXT
        public final static String COLUMN_STOCK_PRICE ="price"; // Price of the product Type: DOUBLE
        public final static String COLUMN_STOCK_QUANTITY ="quantity"; // Quantity of the product Type: INTEGER
        public final static String COLUMN_STOCK_ORDERED ="ordered";   // Ordered Quantity Type: INTEGER
        public static final String COLUMN_STOCK_SUPPLIER_NAME = "supplier_name";   // Supplier name Type: TEXT
        public static final String COLUMN_STOCK_SUPPLIER_PHONE = "supplier_phone"; // Supplier phone Type: TEXT
        public static final String COLUMN_STOCK_SUPPLIER_EMAIL = "supplier_email"; // Supplier email Type: TEXT
        public static final String COLUMN_STOCK_IMAGE = "image";      //    Image URI Type: TEXT
    }

}
