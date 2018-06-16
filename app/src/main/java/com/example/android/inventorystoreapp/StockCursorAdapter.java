package com.example.android.inventorystoreapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorystoreapp.data.StockContract;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


/**
 * {@link StockCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of stock data as its data source. This adapter knows
 * how to create list items for each row of products data in the {@link Cursor}.
 */
public class StockCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link StockCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public StockCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the stock data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current stock product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        ImageView imageViewProduct = view.findViewById(R.id.product_image);
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView orderedTextView = view.findViewById(R.id.ordered);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        ImageView saleButton = view.findViewById(R.id.sale);

        // Find the columns of stock attributes that we're interested in
        int imageColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_IMAGE);
        int rotationColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_ROTATION_NUMBER);
        int nameColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_PRICE);
        int orderedColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_ORDERED);
        int quantityColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_QUANTITY);

        // Read the stock attributes from the Cursor for the current product
        int id = cursor.getInt(cursor.getColumnIndex(StockContract.StockEntry._ID));
        Uri productImageUri = Uri.parse(cursor.getString(imageColumnIndex));
        int rotationNumber = cursor.getInt(rotationColumnIndex);

        String stockName = cursor.getString(nameColumnIndex);

        double dblPrice = cursor.getDouble(priceColumnIndex);
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setMaximumFractionDigits(340);        // 340 = DecimalFormat.DOUBLE_FRACTION_DIGITS
        String strPrice = df.format(dblPrice);

        String stockQuantity = cursor.getString(quantityColumnIndex);
        String stockOrdered = cursor.getString(orderedColumnIndex);

        final int mQuantity = cursor.getInt(quantityColumnIndex);

        // If the product fields are empty string or null, then use some default text
        // that says "N/A", so the TextView isn't blank.
        if (TextUtils.isEmpty(stockName)) {
            stockName = context.getString(R.string.not_available);
        }
        if (TextUtils.isEmpty(stockQuantity)) {
            stockQuantity = "0";
        }
        if (TextUtils.isEmpty(stockOrdered)) {
            stockOrdered = "0";
        }

        final Uri currentProductUri = ContentUris.withAppendedId(StockContract.StockEntry.CONTENT_URI, id);

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(stockName);
        priceTextView.setText(strPrice);
        quantityTextView.setText(stockQuantity);
        orderedTextView.setText(stockOrdered);

        // Picasso allows for hassle-free image loading in your application — in one line of code!
        Picasso.get().load(productImageUri)
                .placeholder(R.drawable.image_placeholder)
                .fit()
                .into(imageViewProduct);

        int angle = rotationNumber * EditActivity.IMAGE_ANGLE;
        imageViewProduct.setRotation(angle);

        // Listener for sale button
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                // Decrease the quantity in stock, no negative quantities are displayed
                if (mQuantity > 0) {
                    int quantity = mQuantity;
                    values.put(StockContract.StockEntry.COLUMN_STOCK_QUANTITY, --quantity);
                    resolver.update(
                            currentProductUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(currentProductUri, null);
                    Toast.makeText(context, R.string.stock_product_reduced_by_one, Toast.LENGTH_SHORT).show();
                } else {
                    // Product not in stock!
                    Toast.makeText(context, R.string.product_not_in_stock, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}
