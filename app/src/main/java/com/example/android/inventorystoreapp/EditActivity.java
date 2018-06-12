package com.example.android.inventorystoreapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorystoreapp.data.StockContract;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;


/**
 * Allows user to create a product record or edit an existing one.
 */
public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the stock data loader */
    private static final int EXISTING_STOCK_LOADER = 0;

    // Request permission code for read external storage
    public static final int READ_STORAGE_REQUEST_CODE = 1;

    /** Request code for product image */
    public static final int IMAGE_REQUEST_CODE = 2;

    // Default Uri for product image
    private String selectedPictureUri = "NO IMAGE";

    /** Content URI for the existing product (null if it's a new product) */
    private Uri mCurrentStockUri;

    /** EditText field to enter the product's name */
    private EditText mNameEditText;

    /** EditText field to enter the product's price */
    private EditText mPriceEditText;

    /** EditText field to enter the product's quantity */
    private EditText mQuantityEditText;

    /** EditText field to enter the product's ordered quantity */
    private EditText mOrderedQuantityEditText;

    /** EditText field to enter the product's supplier name */
    private EditText mSupplierNameEditText;

    /** EditText field to enter the product's supplier phone */
    private EditText mSupplierPhoneEditText;

    /** ImageView field to display product picture */
    private ImageView imageViewProduct;

    /** EditText field to enter the product's supplier email */
    private EditText mSupplierEmailEditText;

    private ImageButton imageButtonDecreaseQuantity;
    private ImageButton imageButtonIncreaseQuantity;
    private ImageButton imageButtonDeliveredQuantity;
    private ImageButton imageButtonPhoneOrder;

    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentStockUri = intent.getData();

        // If the intent DOES NOT contain a stock content URI, then we know that we are
        // creating a new product in stock.
        if (mCurrentStockUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.edit_activity_title_new_product));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.edit_activity_title_edit_product));

            // Initialize a loader to read the Product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_STOCK_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.product_name);
        mPriceEditText = (EditText) findViewById(R.id.product_price);
        mQuantityEditText = (EditText) findViewById(R.id.product_quantity);
        mOrderedQuantityEditText = (EditText) findViewById(R.id.product_ordered);
        mSupplierNameEditText = (EditText) findViewById(R.id.supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.supplier_phone);
        mSupplierEmailEditText = (EditText) findViewById(R.id.supplier_email);
        imageViewProduct = (ImageView) findViewById(R.id.product_image);

        imageButtonDecreaseQuantity = (ImageButton) findViewById(R.id.decrease_quantity);
        imageButtonIncreaseQuantity = (ImageButton) findViewById(R.id.increase_quantity);
        imageButtonDeliveredQuantity = (ImageButton) findViewById(R.id.delevered_quantity);
        imageButtonPhoneOrder = (ImageButton) findViewById(R.id.phone_order);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mOrderedQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        imageButtonDeliveredQuantity.setOnTouchListener(mTouchListener);

        /**
         * OnTouchListener that listens for user touches on ImageView, implying that they are modifying
         * the product image, and we change the mProductHasChanged boolean to true.
         */
        imageViewProduct.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mProductHasChanged = true;
                return false;
            }
        });

        // Lister to Update product image when user clicks it
        imageViewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProductImage(view);
            }
        });

        // Listener to Update product quantity when user clicks decrease_quantity button
        imageButtonDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProductQuantity("-1", 0);
            }
        });

        // Listener to Update product quantity when user clicks increase_quantity button
        imageButtonIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProductQuantity("1", 0);
            }
        });

        // Listener to Update product quantity when user clicks delivered button
        imageButtonDeliveredQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String orderedQuantityString = mOrderedQuantityEditText.getText().toString().trim();
                updateProductQuantity(orderedQuantityString, 1);
            }
        });

        // Listener to Start phone dialer when user clicks order by phone button
        imageButtonPhoneOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // intent to start phone dialer
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mSupplierPhoneEditText.getText().toString().trim()));
                startActivity(intent);
            }
        });

    }

    // Update product quantity when user clicks delivered button
    public void updateProductQuantity(String orderedQuantityString, int i) {

        String quantityString = mQuantityEditText.getText().toString().trim();
        if (TextUtils.isEmpty(quantityString)) {
            mQuantityEditText.setText("0");
            quantityString = "0";
        }
        int quantity = Integer.parseInt(quantityString);
        int newQuantity = quantity + Integer.parseInt(orderedQuantityString);
        if (newQuantity <= 0) {
            newQuantity = 0;
            // Product not in stock!
            Toast.makeText(this, "Stock is empty. Need to order!", Toast.LENGTH_LONG).show();
        } else if (newQuantity > 9999 && i == 0) {
            newQuantity = 9999;
            Toast.makeText(this, "Maximum stock quantity for one product is 9999!", Toast.LENGTH_LONG).show();
        } else if (newQuantity > 9999 && i == 1) {
            newQuantity = quantity;
            Toast.makeText(this, "Ordered quantity too big. Maximum stock quantity exceed 9999 after deliver!", Toast.LENGTH_LONG).show();
        } else if (newQuantity <= 9999 && i == 1) {
            mOrderedQuantityEditText.setText("0");
        }
        mQuantityEditText.setText(Integer.toString(newQuantity));
    }

    // Update product image when user clicks product image
    public void updateProductImage(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android build version is Marshmallow(M) or above so we have to ask for runtime permissions
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                // We already have permission
                selectProductImage();
            } else {
                // We have to ask for permission
                String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissionRequest, READ_STORAGE_REQUEST_CODE);
            }
        } else {
            //Android build version is less than Marshmallow(M)so we don't have to ask for runtime permissions
            selectProductImage();
        }
    }

    // Verify the result for after request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_STORAGE_REQUEST_CODE && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
            // User granted read permission
            selectProductImage();
        } else {
            // Inform user app needs grant read permission
            Toast.makeText(this, "App needs permission to read images on your device.", Toast.LENGTH_LONG).show();
        }
    }

    // Allow a user to select an image with any of the installed apps which is registered for implicit intent ACTION_PICK
    private void selectProductImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        // get Pictures directory on device
        File dirPictures = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES);
        String dirPicturesPath = dirPictures.getPath();
        // get uri for picture directory path
        Uri uri = Uri.parse(dirPicturesPath);
        // Configure data type is any image type
        intent.setDataAndType(uri, "image/*");
        // start any app user select to get the product image from device
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    // Verify the result after user selected a product image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
            }
            // get Uri for the selected product picture
            Uri mProductPictureUri = data.getData();
            selectedPictureUri = mProductPictureUri.toString();
            // Picasso allows for hassle-free image loading in your application — in one line of code!
            Picasso.get().load(mProductPictureUri)
                    .placeholder(R.drawable.image_placeholder)
                    .fit()
                    .into(imageViewProduct);
        }
    }

    // onPrepareOptions method is being called after onCreateOptionsMenu method
    // when we call invalidateOptionsMenu(), the menu display again. So onPrepareOptionMenu() will be invoked
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentStockUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Get user input from editor and save product stock into database.
     */
    private boolean saveProduct() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();

        String priceString = mPriceEditText.getText().toString().trim();


        String quantityString = mQuantityEditText.getText().toString().trim();
        String orderedQuantityString = mOrderedQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();

         // Check if this is supposed to be a new product in stock
        // and check if all the fields in the editor are blank
        if ( mCurrentStockUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(orderedQuantityString) &&
                TextUtils.isEmpty(supplierNameString) && TextUtils.isEmpty(supplierPhoneString) &&
                TextUtils.isEmpty(supplierEmailString) ) {
            // Since no fields were modified, we can return early without creating a new product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return true;
        }

        // If the product fields are empty string or null, then add a Toast that prompts the user
        // to input the correct information before they can continue
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, "Product name field is empty. Please input a value.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "Product price field is empty. Please input a value.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "Product stock quantity field is empty. Please input a value.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, "Supplier name field is empty. Please input a value.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(supplierPhoneString)) {
            Toast.makeText(this, "Supplier phone field is empty. Please input a value.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(supplierEmailString)) {
            Toast.makeText(this, "Supplier email field is empty. Please input a value.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        int mQuantity = Integer.parseInt(quantityString) + Integer.parseInt(orderedQuantityString);
        if (TextUtils.isEmpty(orderedQuantityString)) {
            orderedQuantityString = "0";
            mOrderedQuantityEditText.setText(orderedQuantityString);
        } else if (mQuantity > 9999) {
            Toast.makeText(this, "Ordered quantity too big. Maximum stock quantity can exceed 9999 after deliver!", Toast.LENGTH_LONG).show();
            return false;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(StockContract.StockEntry.COLUMN_STOCK_NAME, nameString);

//---------------------------------------------------------------------------------------------
//        int i = priceString.indexOf('.');
//        if (i>=0) {
//        }

        //String newPriceString = priceString.replace(".", "");
        //long intPrice = Integer.parseInt(newPriceString);
        //intPrice = intPrice * 100;
        //values.put(StockContract.StockEntry.COLUMN_STOCK_PRICE, newPriceString);

        //long lngPrice = Integer.parseInt(priceString);

        double dblPrice = Double.parseDouble(priceString);
        // dblPrice = dblPrice*100;
        //long lngPrice = (long)dblPrice;
        // long lngPrice = Long.parseLong(priceString);

        //BigDecimal bd = new BigDecimal(priceString);
        //int packedInt = bd.scaleByPowerOfTen(2).intValue();


        //values.put(StockContract.StockEntry.COLUMN_STOCK_PRICE, priceString);
        // values.put(StockContract.StockEntry.COLUMN_STOCK_PRICE, lngPrice);
        values.put(StockContract.StockEntry.COLUMN_STOCK_PRICE, dblPrice);
//-------------------------------------------------------------------------------------------------
        values.put(StockContract.StockEntry.COLUMN_STOCK_QUANTITY, quantityString);
        values.put(StockContract.StockEntry.COLUMN_STOCK_ORDERED, orderedQuantityString);
        values.put(StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_NAME, supplierNameString);
        values.put(StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_PHONE, supplierPhoneString);
        values.put(StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_EMAIL, supplierEmailString);
        values.put(StockContract.StockEntry.COLUMN_STOCK_IMAGE, selectedPictureUri);

        // Determine if this is a new or existing stock product by checking if mCurrentStockUri is null or not
        if (mCurrentStockUri == null) {
            // This is a NEW stock product, so insert a new stock product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(StockContract.StockEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.edit_insert_product_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.edit_insert_product_successful),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentStockUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentStockUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentStockUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.edit_update_product_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.edit_update_product_successful),
                        Toast.LENGTH_LONG).show();
            }
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_edit.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar  menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                if (saveProduct()) {
                    // Exit activity
                    finish();
                    return true;
                }
                break;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Hook up the up button
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (MainActivity)
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     * Hook up the back button
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    //------- delete

    // will create a dialog that calls deleteProduct when the delete button is pressed.
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
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
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentStockUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentStockUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentStockUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.edit_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.edit_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

//------------------
    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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


    //-------------------
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the stock table
        String[] projection = {
                StockContract.StockEntry._ID,
                StockContract.StockEntry.COLUMN_STOCK_NAME,
                StockContract.StockEntry.COLUMN_STOCK_PRICE,
                StockContract.StockEntry.COLUMN_STOCK_QUANTITY,
                StockContract.StockEntry.COLUMN_STOCK_ORDERED,
                StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_NAME,
                StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_PHONE,
                StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_EMAIL,
                StockContract.StockEntry.COLUMN_STOCK_IMAGE};
        // make a new CursorLoader, passing in the uri and the projection and return it to caller
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentStockUri,                // Query the content URI for the current product
                projection,                    // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                // Default sort order
    }

    // When the data from the product is loaded into a cursor, onLoadFinished() is called.
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_NAME);
            int priceColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_QUANTITY);
            int orderedColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_ORDERED);
            int supplierColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_PHONE);
            int emailColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_SUPPLIER_EMAIL);
            int imageColumnIndex = cursor.getColumnIndex(StockContract.StockEntry.COLUMN_STOCK_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);

            int quantity = cursor.getInt(quantityColumnIndex);
            int ordered = cursor.getInt(orderedColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            selectedPictureUri = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
//------------------------------------------------------------------------------------------
            // double price = cursor.getDouble(priceColumnIndex);
            // int price = cursor.getInt(priceColumnIndex);

            //String strPrice = cursor.getString(priceColumnIndex);
            //String strPrice = Integer.toString(price);
            //long lngPrice = cursor.getInt(priceColumnIndex);
            //long lngPrice = Long.parseLong(strPrice);
            //strPrice = new StringBuilder(strPrice).insert(strPrice.length()-2, ".").toString();
            double dblPrice = cursor.getDouble(priceColumnIndex);
            String strPrice = String.valueOf(dblPrice);


            //BigDecimal bd = new BigDecimal(strPrice);
            //int packedInt = bd.scaleByPowerOfTen(2).intValue();


            //strPrice = new StringBuilder(strPrice).insert(strPrice.length()-2, ".").toString();

/*
            mPriceEditText.setText(Double.toString(price));
            Double db = (Double.valueOf(price));

            //NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
            //String stockPrice  = numberFormat.format(price);
            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            //String stockPrice  = String.valueOf(price);
            String stockPrice  = numberFormat.format(price);
            mPriceEditText.setText(stockPrice);
*/
            //mPriceEditText.setText(Integer.toString(price));
            mPriceEditText.setText(strPrice);
            //mPriceEditText.setText(Long.toString(lngPrice));
//---------------------------------------------------------------------------------------------

            mQuantityEditText.setText(Integer.toString(quantity));
            mOrderedQuantityEditText.setText(Integer.toString(ordered));
            mSupplierNameEditText.setText(supplier);
            mSupplierPhoneEditText.setText(phone);
            mSupplierEmailEditText.setText(email);

            // Picasso allows for hassle-free image loading in your application — in one line of code!
            Picasso.get().load(selectedPictureUri)
                    .placeholder(R.drawable.image_placeholder)
                    .fit()
                    .into(imageViewProduct);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mOrderedQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
        mSupplierEmailEditText.setText("");
    }


}
