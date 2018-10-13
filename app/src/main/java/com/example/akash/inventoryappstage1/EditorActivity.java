package com.example.akash.inventoryappstage1;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.akash.inventoryappstage1.data.InventoryContract;
import com.example.akash.inventoryappstage1.data.InventoryContract.ProductEntry;
import com.example.akash.inventoryappstage1.data.InventoryDBHelper;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * EditText field to enter the products's name
     */
    @BindView(R.id.product_name_edit_text)
    EditText productName;

    /**
     * EditText field to enter the products's price
     */
    @BindView(R.id.product_price_edit_text)
    EditText productPrice;

    /**
     * EditText field to enter the products's quantity
     */
    @BindView(R.id.product_quantity_edit_text)
    EditText productQuantity;

    /**
     * EditText field to enter the products's supplier name
     */
    @BindView(R.id.product_supplier_name_edit_text)
    EditText supplierName;

    /**
     * EditText field to enter the products's supplier's phone number
     */
    @BindView(R.id.product_supplier_phone_number_edit_text)
    EditText supplierPhoneNumber;

    InventoryDBHelper dbHelper;

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

    /**
     * hold the boolean value for changes made in any EditText
     */
    private boolean productHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the productHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.add_new_product_text));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_existing_product));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //attach OnTouchListener for each EditText

        productName.setOnTouchListener(mTouchListener);
        productPrice.setOnTouchListener(mTouchListener);
        productQuantity.setOnTouchListener(mTouchListener);
        supplierName.setOnTouchListener(mTouchListener);
        supplierPhoneNumber.setOnTouchListener(mTouchListener);

        dbHelper = new InventoryDBHelper(this);
    }

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
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /***
     * take input from EditText and save into the product database, here pr stands for product
     */
    private void saveProduct() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String prName = productName.getText().toString().trim();
        String prSupplierName = supplierName.getText().toString().trim();
        String prSupplierPhoneNumber = supplierPhoneNumber.getText().toString().trim();
        String priceString = productPrice.getText().toString().trim();
        String quantityString = productQuantity.getText().toString().trim();

        //check whether all the values for each field is provided by user or not
        if(TextUtils.isEmpty(prName) || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(quantityString)
                || TextUtils.isEmpty(prSupplierName) || TextUtils.isEmpty(prSupplierPhoneNumber)){
            Toast.makeText(this,"Please fill all details",Toast.LENGTH_SHORT).show();
            return;
        }
        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (mCurrentProductUri == null && TextUtils.isEmpty(prName) && TextUtils.isEmpty(prSupplierName)
                && TextUtils.isEmpty(prSupplierPhoneNumber) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString)) {
            return;
        }
        int prPrice = 0;
        int prQuantity = 0;

        if (!TextUtils.isEmpty(priceString)) {
            prPrice = Integer.parseInt(productPrice.getText().toString().trim());
        }
        if (!TextUtils.isEmpty(quantityString)) {
            prQuantity = Integer.parseInt(productQuantity.getText().toString().trim());
        }

        // Gets the database in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME, prName);
        values.put(InventoryContract.ProductEntry.COLUMN_PRICE, prPrice);
        values.put(InventoryContract.ProductEntry.COLUMN_QUANTITY, prQuantity);
        values.put(InventoryContract.ProductEntry.COLUMN_SUPPLIER_NAME, prSupplierName);
        values.put(InventoryContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, prSupplierPhoneNumber);

        if (mCurrentProductUri == null) {
            Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (uri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_saved), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {

            int rowAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowAffected == 0) {
                Toast.makeText(this, R.string.editing_error_with_updating, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editing_update_successful, Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!productHasChanged) {
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                //save product to the database
                saveProduct();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
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
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String projection[] = {InventoryContract.ProductEntry._ID,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ProductEntry.COLUMN_PRICE,
                InventoryContract.ProductEntry.COLUMN_QUANTITY,
                InventoryContract.ProductEntry.COLUMN_SUPPLIER_NAME,
                InventoryContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER};
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int productNameIndex = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int productPriceIndex = data.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int productQuantityIndex = data.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int supplierNameIndex = data.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneNumberIndex = data.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String prName = data.getString(productNameIndex);
            int prPrice = data.getInt(productPriceIndex);
            int prQuantity = data.getInt(productQuantityIndex);
            String prSupplierName = data.getString(supplierNameIndex);
            String prSupplierPhoneNumber = data.getString(supplierPhoneNumberIndex);

            // Update the views on the screen with the values from the database
            productName.setText(prName);
            productPrice.setText(String.valueOf(prPrice));
            productQuantity.setText(String.valueOf(prQuantity));
            supplierName.setText(prSupplierName);
            supplierPhoneNumber.setText(prSupplierPhoneNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        productName.setText("");
        productPrice.setText(String.valueOf(0));
        productQuantity.setText(String.valueOf(0));
        supplierName.setText("");
        supplierPhoneNumber.setText("");
    }
}
